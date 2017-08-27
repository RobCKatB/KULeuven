package contract_net;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.measure.unit.SystemOfUnits;

import org.apache.commons.math3.random.RandomGenerator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public abstract class TruckAgent extends Vehicle implements CommUser, MovingRoadUser {
//	private static final Logger LOGGER = LoggerFactory
//			    .getLogger(RouteFollowingVehicle.class);
	private Queue<Point> path;
	private Optional<CommDevice> commDevice;
	protected Optional<Parcel> currParcel;
	private Optional<ChargingStation> closestChargingStation;
	protected double energy;
	private double travelledDistance;	
	private long travelledTime;
	private int numberOfRecharges;
	private int numberOfDirectMessages;
	private int nrOfCancelMessages;
	private int nrOfProposalMessages;
	private int nrOfRefusalMessages;
	private int nrOfFailureMessages;
	private int nrOfInformDoneMessages;
	private int nrOfInformResultMessages;
	private DefaultPDPModel defaultpdpmodel;
	private RoadModel roadModel;
    private List<Proposal> proposals = new ArrayList<Proposal>();
    private List<Proposal> acceptedProposals = new ArrayList<Proposal>();
	private static final double SPEED = 1000d;
	private static final double ENERGYCONSUMPTION = 1d; // Per unit mileage
	private static final double ENERGYCAPACITY = 10000000d;

	// for CommUser
	private final double range;
	private final double reliability = 1.0D;
	static final double MIN_RANGE = .2;
	static final double MAX_RANGE = 1.5;
	static final long LONELINESS_THRESHOLD = 10 * 1000;
	private final RandomGenerator rng;

	// state of TruckAgent
    protected boolean isCharging;
    protected boolean isIdle;
    protected boolean isCarrying;
    
    private Optional<ChargingStation> chargingStation = Optional.absent();
	protected long startTimeTruckMoveToParcel;
	private long pickupTime;
	protected long acceptMessageTime;
	protected Auction acceptedAuction;
	  
	public TruckAgent(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, Point startPosition, int capacity, RandomGenerator rng){
		super(VehicleDTO.builder()
			      .capacity(capacity)
			      .startPosition(startPosition)
			      .speed(SPEED)
			      .build());
		this.rng=rng;
		this.defaultpdpmodel = defaultpdpmodel;// defined in the main
		this.roadModel = roadModel;// defined in the main
		currParcel = Optional.absent();
		// settings for commDevice belonging to TruckAgent
		range = Double.MAX_VALUE;
	    //range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    //reliability = rng.nextDouble();
		commDevice = Optional.absent();
		closestChargingStation = Optional.absent();
		// when you create a new TruckAgent, he is full of charge
		isCharging = false;
		isIdle = true;
		isCarrying = false;
		energy = ENERGYCAPACITY;
		travelledDistance = 0;
		numberOfRecharges = 0;
		numberOfDirectMessages = 0;
		nrOfCancelMessages = 0;
		nrOfProposalMessages=0;
		nrOfRefusalMessages=0;
		nrOfFailureMessages=0;
		nrOfInformDoneMessages=0;
		nrOfInformResultMessages=0;
		travelledTime = 0L;
	}

	@Override
	protected void tickImpl(TimeLapse time) {

		// Process all unread messages
		List<CNPMessage> unreadMessages = getUnreadMessages();
		if (!unreadMessages.isEmpty()) {
			processMessages(unreadMessages, time);
		}
	
		// If carrying a parcel
		if(currParcel.isPresent()){
			// drive to destination
			drive(time);
		}
		
		if(isCharging){
			driveToChargingStation(time);
		}
	
	}

	protected abstract void processMessages(List<CNPMessage> messages, TimeLapse time);
	
	protected void handleParcel(CNPMessage acceptProposalMessage, TimeLapse time){
		currParcel = Optional.of(acceptProposalMessage.getAuction().getParcel());
		acceptMessageTime = acceptProposalMessage.getTimeSent();
		acceptedAuction = acceptProposalMessage.getAuction();
		startTimeTruckMoveToParcel = time.getTime();
		isIdle = false;
	}
	
	private void drive(TimeLapse time) {
		if(!isCarrying){
			// Drive to pickup location
			moveTo(currParcel.get(),time);
			//Pickup?
			if (roadModel.equalPosition(this, currParcel.get())) {
				defaultpdpmodel.pickup(this, currParcel.get(), time);
				pickupTime = time.getTime();
				isCarrying = true;
				//System.out.println("PICKUP of parcel " + currParcel + " by "+this+" [energy left = "+ energy+"] [travelled distance = "+travelledDistance+"]");
			}
		}else{
			// Drive to destination
			moveTo(currParcel.get().getDeliveryLocation(),time);
			// Dropoff?
			if (roadModel.getPosition(this).equals(currParcel.get().getDeliveryLocation())) {
				defaultpdpmodel.deliver(this, currParcel.get(), time);
				long deliveryTime = time.getTime();
				// send INFORM_DONE and INFORM_RESULT message to
				// DispatchAgent that task is finished
				sendInformDone(acceptedAuction, ContractNetMessageType.INFORM_DONE, time);
				sendInformResult(acceptedAuction, ContractNetMessageType.INFORM_RESULT, time, pickupTime,
						deliveryTime, startTimeTruckMoveToParcel, acceptMessageTime);

				//System.out.println("DELIVERY of parcel " + currParcel + " by " + this + " [energy left ="+ energy+"] [travelled distance = "+travelledDistance+"]");
				isCarrying = false;
				isIdle = true;
				currParcel = Optional.absent();
				afterDelivery(time);
			}
		}
	}
	

	private void driveToChargingStation(TimeLapse time) {
		// truck moves to closest charging station to get fuel
		moveTo(closestChargingStation.get().getPosition().get(), time);
		// truck docks itself to the charging station and in the tick of the charging station he gets charged
		if (roadModel.equalPosition(this, closestChargingStation.get())){
//			System.out.println("BEFORE CHARGING: [energy left = "+energy +"] [travelled distance = "+travelledDistance+"]");
			charge(ENERGYCAPACITY/5);
			numberOfRecharges++;
//			System.out.println("AFTER CHARGING: [energy left = "+energy +"], max energy is " +ENERGYCAPACITY+" [travelled distance = "+travelledDistance+"]");
		}
	}

	/**
	 * Two local facades of the moveTo method to do some accounting.
	 */
	private void moveTo(RoadUser destination, TimeLapse time) {
		MoveProgress moveProgress = roadModel.moveTo(this, destination, time);
		this.consumeEnergy(moveProgress.distance().getValue()*1000);
		travelledDistance += moveProgress.distance().getValue();
	}
	private void moveTo(Point destination, TimeLapse time) {
		MoveProgress moveProgress = roadModel.moveTo(this, destination, time);
		this.consumeEnergy(moveProgress.distance().getValue()*1000);
		travelledTime +=moveProgress.time().getValue();
		travelledDistance += moveProgress.distance().getValue();
	}
	
	/*private void moveTo(RoadUser destination, TimeLapse time){
		
	}*/

	protected abstract void afterDelivery(TimeLapse time);


	/* 
	 * charging station
	 */
	public void charge(double amount){
		this.energy += amount;
		if(this.energy >= ENERGYCAPACITY){
			this.energy = ENERGYCAPACITY;
			this.isCharging = false;
			this.isIdle = true;
		}
	}
	
	public double getEnergy(){
		return this.energy;
	}
	
	private void consumeEnergy(double consumed){
		if(this.energy >= consumed){
			this.energy -= consumed;
		}	
	}
	

	private double calculateEnergyConsumption(double distance){
		return distance * ENERGYCONSUMPTION;
	}
	
	/*
	 * method to calculate how much energy is needed for the PDP task
	 */
	protected double calculateEnergyConsumptionTask(Point currTruckPosition, Parcel parcel){
		double distance = calculatePDPDistance(currTruckPosition, parcel);
		return calculateEnergyConsumption(distance);
	}
	
	/*
	 * method to calculate how much energy is needed for the truck to move from its current position to the parcel that has to be picked up
	 */
	private double calculateEnergyConsumptionTruckToPickup(Point currTruckPosition, Parcel parcel){
		double distance = calculatePDPDistanceCurrentToPickup(currTruckPosition, parcel);
		return calculateEnergyConsumption(distance);
	}
	
	/*
	 * method to calculate how much energy is needed for the truck to move from the parcel pickup location to the parcel delivery location
	 */
	private double calculateEnergyConsumptionPickupToDeliver(Point currTruckPosition, Parcel parcel){
		double distance = calculatePDPDistanceCurrentToPickup(currTruckPosition, parcel);
		return calculateEnergyConsumption(distance);
	}
	
	/*
	 * calculate amount of energy needed for the truck to go from current position to charging station
	 */
	protected double calculateEnergyConsumptionToChargingStation(Point position, Optional<ChargingStation> chargingStation){
		double distance = calculatePointToPointDistance(position, chargingStation.get().getPosition().get());
		return calculateEnergyConsumption(distance);
	}
	
	/*
	 * method to check whether the truck has enough energy to do the PDP task and go to the closest charging station 
	 * after the task has finished (at least if fuel level is low after the PDP task has finished)
	 */
	protected boolean enoughEnergy(Point currTruckPosition, Parcel parcel, Optional<ChargingStation> chargingStation){
		double energyNeeded = calculateEnergyConsumptionTask(currTruckPosition, parcel) + calculateEnergyConsumptionToChargingStation(parcel.getDeliveryLocation(), chargingStation);
		//System.out.println("energy needed:" + energyNeeded+ "[energy left="+energy+"]");
		if (energyNeeded <=  energy){
			return true;
		}
		else {
			return false;
		}
	}

	/**
	*calculate the distance a Truck has to travel to pickup and deliver a parcel: from its current position 
	*to the parcel pickup position and from the parcel pickup  position to the parcel destination position
	*Therefore, edgelengths of segments in the graph (model for streets) are summed
   */		
	private double calculatePDPDistance(Point currentTruckPosition, Parcel parcel){
		return calculatePDPDistanceCurrentToPickup(currentTruckPosition, parcel) + calculatePDPDistancePickupToDelivery(currentTruckPosition, parcel);
	}
	
	/*
	 * This method will be overwritten in TruckAgentDriving.
	 */
	protected double calculatePDPDistanceCurrentToPickup(Point currentTruckPosition, Parcel parcel){
		double currentToPickup = calculatePointToPointDistance(currentTruckPosition, parcel.getPickupLocation());
		return currentToPickup;
	}
	
	private double calculatePDPDistancePickupToDelivery(Point currentTruckPosition, Parcel parcel){
		double pickupToDelivery = calculatePointToPointDistance(parcel.getPickupLocation(), parcel.getDeliveryLocation());
		return pickupToDelivery;
	}
	
	/*
	 * calculate edge distance between two points in a graph
	 */
	protected double calculatePointToPointDistance(Point start, Point end){
		List<Point> fromStartToEnd = this.getRoadModel().getShortestPathTo(start, end);
		// make the sum of the vertices in the graph, from the first till the last point in the path
		double fromStartToEndLength = 0.0;
		for(int i = 0; i < fromStartToEnd.size()-1; i++){
			Point p1 = fromStartToEnd.get(i);
			Point p2 = fromStartToEnd.get(i+1);
			double edgelength = Point.distance(p1, p2);
			fromStartToEndLength += edgelength;
		}
		return fromStartToEndLength;
	}
	
	/**
	 * travel time = distance/speed
	 */
	public long calculateTravelTimePDP(Point currentTruckPosition, Parcel parcel){
		double shortestDistance = calculatePDPDistance(currentTruckPosition, parcel);
		long time = (long) (shortestDistance/SPEED);
		// TODO?? somehow we need to change the value of serviceDuration(SERVICE_DURATION), or we have to remove this from the main
		return time;
	}
	
	private long calculateTravelTimePDPCurrentToPickup(Point currentTruckPosition, Parcel parcel){
		double shortestDistance = calculatePDPDistanceCurrentToPickup(currentTruckPosition, parcel);
		long time = (long) (shortestDistance/SPEED);
		// TODO?? somehow we need to change the value of serviceDuration(SERVICE_DURATION), or we have to remove this from the main
		return time;
	}
	
	private long calculateTravelTimePDPPickupToDelivery(Point currentTruckPosition, Parcel parcel){
		double shortestDistance = calculatePDPDistancePickupToDelivery(currentTruckPosition, parcel);
		long time = (long) (shortestDistance/SPEED);
		// TODO?? somehow we need to change the value of serviceDuration(SERVICE_DURATION), or we have to remove this from the main
		return time;
	}
		
	/*
	 * commDevice settings
	 * (non-Javadoc)
	 * @see com.github.rinde.rinsim.core.model.comm.CommUser#setCommDevice(com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder)
	 */
	public void setCommDevice(CommDeviceBuilder builder) {
	    if (range >= 0) {
	        builder.setMaxRange(range);
	      }
	      commDevice = Optional.of(builder
	        .setReliability(reliability)
	        .build());
	}
	

	/*
	 * TruckAgent reads the messages he received
	 */
    private List<CNPMessage> getUnreadMessages() {
        CommDevice device = this.commDevice.get();
        List<CNPMessage> contents = new ArrayList<CNPMessage>();
        if (device.getUnreadCount() != 0) {
            ImmutableList<Message> messages = device.getUnreadMessages();
            contents = getMessageContent(messages);
        }
        return contents;
    }

    
	public List<CNPMessage> getMessageContent(ImmutableList<Message> messages) {
		Iterator<Message> it = messages.iterator();
		List<CNPMessage> contents = new ArrayList<>();
		while (it.hasNext()) {
			Message message = it.next();
			CNPMessage content = (CNPMessage)message.getContents();
			contents.add(content);
		}
		return contents;
	}
		
	
	public void doProposal(Point currentTruckPosition, Auction auction, TruckAgent proposer, TimeLapse timelapse){
		long timeCostBid = calculateTravelTimePDP(currentTruckPosition, auction.getParcel());
		long currentToPickup = calculateTravelTimePDPCurrentToPickup(currentTruckPosition, auction.getParcel());
		long pickupToDelivery = calculateTravelTimePDPPickupToDelivery(currentTruckPosition, auction.getParcel());
		// TODO: in more advanced version of the program with a certain delivery time for a parcel:
		// if currentTime is larger than the desired delivery time for the parcel, send no proposal
		
//		System.out.println("energy level before doing a proposal for truckagent " + this + " is " + energy);
		// TODO: in more advanced form of program, we could let the truck send a proposal even if there is not enough energy
		// taking into account the time needed for energy loading. In that case, no refusal has to be sent like in our case.
		Proposal proposal = new Proposal(auction, proposer, currentToPickup, pickupToDelivery, timeCostBid);
		//System.out.println("Truckagent " + this + " needs " + timeCostBid + " time and " +calculateEnergyConsumptionTask(currentTruckPosition, auction.getParcel()) + " energy for auction " + auction.toString()+ "[energy left = "+energy+"]");
		proposals.add(proposal);
		// TruckAgent sends proposal message to initiator of the auction (dispatchAgent)
		CNPProposalMessage cnpProposalMessage = new CNPProposalMessage(auction, ContractNetMessageType.PROPOSE, proposal, proposal.getProposer(), proposal.getAuction().getDispatchAgent(), timelapse.getTime());
		sendDirectMessage(cnpProposalMessage, auction.getDispatchAgent());
		nrOfProposalMessages++;
		
	}
	
	protected Optional<ChargingStation> findClosestChargingStation(){
		closestChargingStation = Optional.fromNullable(RoadModels.findClosestObject(
		          roadModel.getPosition(this), roadModel, ChargingStation.class));
		return closestChargingStation;
	}

	protected void goCharging(){
		//TODO: change VehicleState to CHARGING, but this is not an option in the predefined enum VehicleState
		isIdle = false;
		isCharging = true;
	}

	/*
	 * send messages from TruckAgent to DispatchAgent
	 */
	
	private void sendDirectMessage(CNPMessage content, CommUser recipient) {
		if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for truckagent");}
		CommDevice device = this.commDevice.get();
		device.send(content, recipient);
		numberOfDirectMessages++;
	}

	public void sendRefusal(Auction auction, String refusalReason, TimeLapse timeLapse){
		CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, ContractNetMessageType.REFUSE, this, auction.getDispatchAgent(), refusalReason, timeLapse.getTime());
		sendDirectMessage(cnpRefusalMessage, auction.getDispatchAgent());	
		nrOfRefusalMessages++;
	}
	
	public void sendFailure(Auction auction, ContractNetMessageType type, TimeLapse timeLapse){
		CNPFailureMessage cnpFailureMessage = new CNPFailureMessage(auction, type, this, auction.getDispatchAgent(), type.toString(), timeLapse.getTime());
		sendDirectMessage(cnpFailureMessage, auction.getDispatchAgent());
		nrOfFailureMessages++;
	}

	public void sendInformDone(Auction auction, ContractNetMessageType type, TimeLapse timeLapse){
		CNPInformDoneMessage cnpInformDoneMessage = new CNPInformDoneMessage(auction, type, this, auction.getDispatchAgent(), timeLapse.getTime());
		sendDirectMessage(cnpInformDoneMessage, auction.getDispatchAgent());	
		nrOfInformDoneMessages++;
	}

	public void sendInformResult(Auction auction, ContractNetMessageType type, TimeLapse timeLapse, long pickupTime, long deliveryTime, long truckStartTime, long CFPTimeSent){
		long timePickupToDelivery = deliveryTime - pickupTime;
		long timeTruckToPickup = pickupTime - truckStartTime;
		long timeTruckToPickupToDelivery =deliveryTime - truckStartTime;
		long timeCFPToDelivery=deliveryTime - CFPTimeSent;
		CNPInformResultMessage cnpInformResultMessage = new CNPInformResultMessage(auction, type, this, auction.getDispatchAgent(), timeTruckToPickup, timePickupToDelivery, timeTruckToPickupToDelivery, timeCFPToDelivery, timeLapse.getTime());
		sendDirectMessage(cnpInformResultMessage, auction.getDispatchAgent());
		nrOfInformResultMessages++;
//		System.out.println("INFORM RESULT sent: " +cnpInformResultMessage.toString());
	}
	
	protected void sendCancelMessage(Auction auction, String cancelReason, TimeLapse timeLapse){
		CNPCancelMessage cnpCancelMessage = new CNPCancelMessage(auction, ContractNetMessageType.CANCEL, this, auction.getDispatchAgent(), cancelReason, timeLapse.getTime());
		sendDirectMessage(cnpCancelMessage, auction.getDispatchAgent());
		nrOfCancelMessages++;
	}
	
	@Override
	public Optional<Point> getPosition() {
	    if (roadModel.containsObject(this)) {
	        return Optional.of(roadModel.getPosition(this));
	    }
	    return Optional.absent();
	}

	public List<Proposal> getAcceptedProposals() {
		return acceptedProposals;
	}

	public void setAcceptedProposals(List<Proposal> acceptedProposals) {
		this.acceptedProposals = acceptedProposals;
	}

	public boolean isCharging() {
		return isCharging;
	}

	public void setCharging(boolean isCharging) {
		this.isCharging = isCharging;
	}

	public double getTravelledDistance() {
		return travelledDistance;
	}

	public void setTravelledDistance(long travelledDistance) {
		this.travelledDistance = travelledDistance;
	}
	
	
	public int getNumberOfRecharges() {
		return numberOfRecharges;
	}

	public void setNumberOfRecharges(int numberOfRecharges) {
		this.numberOfRecharges = numberOfRecharges;
	}

	public int getNumberOfDirectMessages() {
		return numberOfDirectMessages;
	}

	public void setNumberOfDirectMessages(int numberOfDirectMessages) {
		this.numberOfDirectMessages = numberOfDirectMessages;
	}
	
	public int getNrOfCancelMessages() {
		return nrOfCancelMessages;
	}

	public void setNrOfCancelMessages(int nrOfCancelMessages) {
		this.nrOfCancelMessages = nrOfCancelMessages;
	}

	public int getNrOfProposalMessages() {
		return nrOfProposalMessages;
	}

	public void setNrOfProposalMessages(int nrOfProposalMessages) {
		this.nrOfProposalMessages = nrOfProposalMessages;
	}

	public int getNrOfRefusalMessages() {
		return nrOfRefusalMessages;
	}

	public void setNrOfRefusalMessages(int nrOfRefusalMessages) {
		this.nrOfRefusalMessages = nrOfRefusalMessages;
	}

	public int getNrOfFailureMessages() {
		return nrOfFailureMessages;
	}

	public void setNrOfFailureMessages(int nrOfFailureMessages) {
		this.nrOfFailureMessages = nrOfFailureMessages;
	}

	public int getNrOfInformDoneMessages() {
		return nrOfInformDoneMessages;
	}

	public void setNrOfInformDoneMessages(int nrOfInformDoneMessages) {
		this.nrOfInformDoneMessages = nrOfInformDoneMessages;
	}

	public int getNrOfInformResultMessages() {
		return nrOfInformResultMessages;
	}

	public void setNrOfInformResultMessages(int nrOfInformResultMessages) {
		this.nrOfInformResultMessages = nrOfInformResultMessages;
	}

	public long getTravelledTime() {
		return travelledTime;
	}

	public void setTravelledTime(long travelledTime) {
		this.travelledTime = travelledTime;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Truck [");
		
		builder.append(System.identityHashCode(this));
				
		builder.append(this.getPosition()).append(",");
		
		
		/*if(this.isCharging){
			builder.append("charging,");
		}
		if(this.isDelivering){
			builder.append("delivering,");
		}
		if(this.isIdle){
			builder.append("idle,");
		}
		if(this.isMoving){
			builder.append("moving,");
		}
		if(this.isPickingUp){
			builder.append("moving,");
		}
		
		builder.append("energy: ").append(this.energy);*/
		
		builder.append("]");
			  
		return builder.toString();
	}
		
}
