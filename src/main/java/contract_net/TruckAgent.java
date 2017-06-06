package contract_net;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;


public class TruckAgent extends Vehicle implements CommUser, MovingRoadUser {
	private static final Logger LOGGER = LoggerFactory
			    .getLogger(RouteFollowingVehicle.class);
	private Queue<Point> path;
	private Optional<CommDevice> commDevice;
	private Optional<RoadModel> roadModel;
	private Optional<Parcel> currParcel;
	private int capacity;
	private Point startPosition;
    private double energy;
    private List<Proposal> proposals = new ArrayList<Proposal>();
    private boolean isCharging;
    private List<CNPMessage> unreadMessages;
	private long lastReceiveTime;
	private DefaultPDPModel defaultpdpmodel;
    private List<Proposal> acceptedProposals = new ArrayList<Proposal>();
	private static final double SPEED = 1000d;
	private static final double ENERGYCONSUMPTION = 1d; // Per unit mileage
	private static final double ENERGYCAPACITY = 1000d;

	// for CommUser
	  private final double range;
	  private final double reliability;
	  static final double MIN_RANGE = .2;
	  static final double MAX_RANGE = 1.5;
	  static final long LONELINESS_THRESHOLD = 10 * 1000;
	  private final RandomGenerator rng;

	
	public TruckAgent(DefaultPDPModel defaultpdpmodel, Point startPosition, int capacity, RandomGenerator rng){
		super(VehicleDTO.builder()
			      .capacity(capacity)
			      .startPosition(startPosition)
			      .speed(SPEED)
			      .build());
		this.rng=rng;
		this.defaultpdpmodel = defaultpdpmodel;// defined in the main
		currParcel = Optional.absent();
		roadModel = Optional.absent();
		// settings for commDevice belonging to TruckAgent
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
		commDevice = Optional.absent();
		// when you create a new TruckAgent, he is full of charge
		isCharging = false;

	}

		public void sendDirectMessage(CNPMessage content, CommUser recipient) {
			if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for truckagent");}
			CommDevice device = this.commDevice.get();
			device.send(content, recipient);
		}
		
		public List<Proposal> getProposals() {
			return proposals;
		}

		public void setProposals(List<Proposal> proposals) {
			this.proposals = proposals;
		}
/*
		public void bidCFP(CNPMessage m, Bid bid){};
		public void declineCFP(CNPMessage m, CNPMessage reaction){};
		public void load(Parcel p){
			if (ParcelState.AVAILABLE)
				ParcelState.PICKING_UP;
				pdpModel.pickup(this, p, time);
		};
		public void unload(Parcel p){
			pdpModel.drop(vehicle, p, time);
			ParcelState.DELIVERED;
		};
		public void move(Parcel p, Location l){
			pdpModel.service(vehicle, p, time);
			ParcelState.DELIVERING
		}
		
		// parcel pickup and delivery
		protected void tickImpl(TimeLapse time) {
		    final RoadModel rm = getRoadModel();
		    final PDPModel pm = getPDPModel();

		    if (!time.hasTimeLeft()) {
		      return;
		    }
		    if (!curr.isPresent()) {
		      curr = Optional.fromNullable(RoadModels.findClosestObject(
		        rm.getPosition(this), rm, Parcel.class));
		    }

		    if (curr.isPresent()) {
		      final boolean inCargo = pm.containerContains(this, curr.get());
		      // sanity check: if it is not in our cargo AND it is also not on the
		      // RoadModel, we cannot go to curr anymore.
		      if (!inCargo && !rm.containsObject(curr.get())) {
		        curr = Optional.absent();
		      } else if (inCargo) {
		        // if it is in cargo, go to its destination
		        rm.moveTo(this, curr.get().getDeliveryLocation(), time);
		        if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
		          // deliver when we arrive
		          pm.deliver(this, curr.get(), time);
		        }
		      } else {
		        // it is still available, go there as fast as possible
		        rm.moveTo(this, curr.get(), time);
		        if (rm.equalPosition(this, curr.get())) {
		          // pickup parcel
		          pm.pickup(this, curr.get(), time);
		        }
		      }
		    }
		}
		

		@Override
		public void tick(long currentTime, long timeStep) {

			handleIncomingMessages(mailbox.getMessages());

			// Drive when possible
			if (targetedPackage != null) {
				if (!path.isEmpty()) {
					truck.drive(path, timeStep);
				} else {
					if (targetedPackage.needsPickUp())
						pickUpAndGo();
					else
						deliver();
				}
			}
		}
		
		
		*/
		
		// uit oude rinsim
		/*
		private void handleMessages(long currentTime) {
			Queue<Message> messages = mailbox.getMessages();
			for (Message message : messages) {
				if (message instanceof CNetMessage) {
					handleCNet((CNetMessage) message, currentTime);
				}
			}
		}

		private void handleCNet(CNetMessage message, long currentTime) {
			if (message instanceof CallForProposals) {
				eDisp.dispatchEvent(new Event(EventType.CFPReceived, this));
				CallForProposals cfp = (CallForProposals) message;
				Proposal p = null;
				if (proposal == null) {
					p = createBestProposal(cfp, currentTime);
				} else if (currentTime > proposal.getDeliveryTime()) {
					proposal = null;
				}
				if (p != null) {
					proposal = p;
					eDisp.dispatchEvent(new Event(EventType.ProposalSent, this));
					communicationAPI.send(cfp.getSender(), p);
				} else {
					Refusal ref = new Refusal(this, cfp);
					eDisp.dispatchEvent(new Event(EventType.RefusalSent, this));
					communicationAPI.send(cfp.getSender(), ref);
					
				}
			}
			if (message instanceof AcceptProposal) {
				eDisp.dispatchEvent(new Event(EventType.AcceptProposalReceived, this));
				AcceptProposal ap = (AcceptProposal) message;
				reorderPlannedTasks(ap, currentTime);
			}
			if (message.isResponseTo(proposal)) {
				proposal = null;
			}
		}
		
		*/
		
		

		  

		@Override
		protected void tickImpl(TimeLapse time) {
		    final RoadModel roadModel = getRoadModel();
		    final PDPModel pdpModel = getPDPModel();
		    

		/*
	    if (!destination.isPresent()) {
	      destination = Optional.of(roadModel.get().getRandomPosition(rng));
	    }
	    roadModel.get().moveTo(this, destination.get(), timeLapse);
	    if (roadModel.get().getPosition(this).equals(destination.get())) {
	      destination = Optional.absent();
	    }
	    
	    */

		// if we somehow need the time of a certain action
		//long currentTime = time.getTime();

		if (commDevice.get().getUnreadCount() > 0) {
			unreadMessages = readMessages();

			for (CNPMessage m : unreadMessages) {
				
					switch (m.getType()) {

					case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
					    // TruckAgents can participate in auctions when they are IDLE (not occupied by anything else) and
					    // if they are not charging
						if(!isCharging && VehicleState.IDLE.equals(getPDPModel().getVehicleState(this))){
							/// check that truck has enough capacity for the PDP task
							doProposal(this.getPosition().get(), m.getAuction(), this, time);
						} else {
							sendFailure(m.getAuction(), ContractNetMessageType.FAILURE, time);
						}
						break;
					case ACCEPT_PROPOSAL:
						doPDP(m, time);

						/*
						// TODO: add accepted proposal to a List of all accepted proposals for this TruckAgent
						 * CNPAcceptMessage cnpAccept = (CNPAcceptMessage)m;
						 * acceptedProposals.add(cnpAccept.getProposal());
						 * not right since this does not store all acceptedProposals for one TruckAgent, it just stores the acceptedProposal for this auction
						 */

						break;
					case REJECT_PROPOSAL:
						// do nothing. The TruckAgent did not win the Auction for a certain package, so currently no tasks for the truck
						break;
					default:
						break;
					}
				} 
			}
		}

		/* 
		 * charging station
		 */
		public void charge(double amount){
			this.energy = Math.max(this.energy+amount, ENERGYCAPACITY);
		}
		
		// TODO: Robbert: methode schrijven om te checken hoeveel energie de Truck nog over heeft
		public double checkEnergyLevel(){
			return 10.0;
		}
		
		/*
		 * method to calculate how much energy is needed for the PDP task
		 */
		public double calculateEnergyConsumptionTask(Point currTruckPosition, Parcel parcel){
			double distance = calculatePDPDistance(currTruckPosition, parcel);
			double consumption = distance * ENERGYCONSUMPTION;
			return consumption;
		}
		
		/*
		 * when the truck has delivered a parcel, it still needs at least enough energy to go to the closest charging station
		 */
		public double calculateEnergyConsumtionToChargingStation(Parcel parcel, ChargingStation chargingStation){
			double distance = calculatePointToPointDistance(parcel.getDeliveryLocation(), chargingStation.getPosition().get());
			double consumption = distance * ENERGYCONSUMPTION;
			return consumption;
		}

		/*
		 * method to check whether the truck has enough energy to do the PDP task and go to the closest charging station 
		 * after the task has finished (at least if fuel level is low after the PDP task has finished)
		 */
		public boolean enoughEnergy(Point currTruckPosition, Parcel parcel, ChargingStation chargingStation){
			if (calculateEnergyConsumptionTask(currTruckPosition, parcel) + calculateEnergyConsumtionToChargingStation(parcel, chargingStation) >  energy){
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
		public double calculatePDPDistance(Point currentTruckPosition, Parcel parcel){
			double currentToPickup = calculatePointToPointDistance(currentTruckPosition, parcel.getPickupLocation());
			double pickupToDelivery = calculatePointToPointDistance(parcel.getPickupLocation(), parcel.getDeliveryLocation());
			return currentToPickup + pickupToDelivery;
		}
		
		/*
		 * calculate edge distance between two points in a graph
		 */
		public double calculatePointToPointDistance(Point start, Point end){
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
	    public List<CNPMessage> readMessages() {
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
			// TODO: check whether the truck has enough capacity to load the parcel
			Proposal proposal = new Proposal(auction, proposer, timeCostBid);
			// TODO: methode closestChargingStation() schrijven om dichtste chargingStation te vinden, die een object van het type ChargingStation teruggeeft
			// inspiratie zoeken in taxi example waar ze closest object zoeken via roadmodel
			// TODO: in more advanced version of the program with a certain delivery time for a parcel:
			// if currentTime is larger than the desired delivery time for the parcel, send no proposal
			
			// check whether there is enough energy to travel this distance, and only then do Proposal, otherwise send REFUSE
			if (enoughEnergy(currentTruckPosition, auction.getParcel(), closestChargingStation)){
				// TODO: in more advanced form of program, we could let the truck send a proposal even if there is not enough energy
				// taking into account the time needed for energy loading. In that case, no refusal has to be sent like in our case.
				proposals.add(proposal);
				// TruckAgent sends proposal message to initiator of the auction (dispatchAgent)
				CNPProposalMessage cnpProposalMessage = new CNPProposalMessage(auction, ContractNetMessageType.PROPOSE, proposal, proposal.getProposer(), proposal.getAuction().getDispatchAgent(), timelapse.getTime());
				sendDirectMessage(cnpProposalMessage, auction.getSenderAuction());
				// VehicleState stays IDLE as long as the proposal is not accepted by the DispatchAgent, what means that
				// the truck can participate in other auctions in the meantime
			} else {
				//TODO: change VehicleState to CHARGING, but this is not an option in the predefined enum VehicleState
				s = VehicleState.CHARGING;
				sendRefusal(auction, s);
				//TODO: go to charging station
			}

		}
		
		public void doPDP(CNPMessage m, TimeLapse time){
			defaultpdpmodel.service(this, m.getAuction().getParcel(), time); // calls deliver and pickup methods
			//TODO make this work
			roadModel.get().followPath(this, path, time);
			//TODO add method to move from current position to parcel and decrease fuel level
			defaultpdpmodel.pickup(this, m.getAuction().getParcel(), time); // status of parcel will be changed to PICKING_UP
			// wachten tot pickup klaar is
			//TODO add method to move from parcel to delivery location and decrease fuel level
			defaultpdpmodel.deliver(this, m.getAuction().getParcel(), time); // status of parcel will be changed to DELIVERING
			// we suppose that the truck stays at the last delivery place until a new PDP task is accepted
			// wachten tot deliver klaar is
			///sendDirectMessage(cnpFailureMessage, auction.getSenderAuction());
			defaultpdpmodel.continuePreviousActions(this, time); //sets status of Parcel on DELIVERED
			//TODO change vehicle state back to IDLE when parcel got status DELIVERED
			//TODO send INFORM_DONE and INFORM_RESULT message to DispatchAgent that task is finished
			sendInformDone(m.getAuction(), ContractNetMessageType.INFORM_DONE, time);
			// TODO: sendInformResult (first make method) and make class CNPInformResultMessage class extending CNPMessage, with extra parameter in constructor storing real travel time (and residual fuel level)
			// TODO change parcel state to DELIVERED if that was not yet done
			ParcelState.DELIVERED;
			
			//TODO when PDP is finished, check whether there is still more energy than the energy needed to go to the charging station
			// it there is only sufficient energy to go to charging station, go to charging station an change status of TruckAgent to TO_CHARGING
		}
		
		/*
		///// uit oude Rinsim
		private LinkedList<Point> getPath() {
			LinkedList<Point> path = null;
			RoadModel rm = truck.getRoadModel();
			if (currentTask != null) {
				Point pos = truck.getPosition();
				if (pos.equals(currentTask.getPickupLocation()) && !truck.hasLoad()) {
					truck.tryPickup();
					path = new LinkedList<Point>(rm.getShortestPathTo(truck,
									currentTask.getDeliveryLocation()));
				} else if (pos.equals(currentTask.getDeliveryLocation())) {
					truck.tryDelivery();
					currentTask = null;
				}
			}
			if (currentTask == null) {
				Point destination = null;
				if (!tasks.isEmpty()) {
					currentTask = tasks.remove(0);
					destination = currentTask.getPickupLocation();
				} else if (proposal == null) {
					List<Point> p = null;
					while (p == null || p.size() < 2) {
						destination = rm.getGraph()
							.getRandomNode(simulator.getRandomGenerator());
						p = rm.getShortestPathTo(truck, destination);
					}
					if (truck.getPosition().equals(p.get(0))) {
						destination = p.get(1);
					} else {
						destination = p.get(0);
					}
				}
				if (destination != null) {
					path = new LinkedList<Point>(rm.getShortestPathTo(truck, destination));
				}
			}
			return path;
		}
		
		*/
		/*
		 * send messages from TruckAgent to DispatchAgent
		 */
		public void sendRefusal(Auction auction, VehicleState s, TimeLapse timeLapse){
			CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, ContractNetMessageType.REFUSE, this, auction.getSenderAuction(), s.toString(), timeLapse.getTime());
			sendDirectMessage(cnpRefusalMessage, auction.getSenderAuction());	
		}
		
		public void sendFailure(Auction auction, ContractNetMessageType t, TimeLapse timeLapse){
			CNPFailureMessage cnpFailureMessage = new CNPFailureMessage(auction, t, this, auction.getSenderAuction(), t.toString(), timeLapse.getTime());
			sendDirectMessage(cnpFailureMessage, auction.getSenderAuction());
		}


		
		public void sendInformDone(Auction auction, ContractNetMessageType type, TimeLapse timeLapse){
			CNPInformDoneMessage cnpInformDoneMessage = new CNPInformDoneMessage(auction, type, this, auction.getSenderAuction(), timeLapse.getTime());
			sendDirectMessage(cnpInformDoneMessage, auction.getSenderAuction());	
		}

		@Override
		public Optional<Point> getPosition() {
		    if (roadModel.get().containsObject(this)) {
		        return Optional.of(roadModel.get().getPosition(this));
		      }
		      return Optional.absent();
		}

		public List<Proposal> getAcceptedProposals() {
			return acceptedProposals;
		}

		public void setAcceptedProposals(List<Proposal> acceptedProposals) {
			this.acceptedProposals = acceptedProposals;
		}
 
}
