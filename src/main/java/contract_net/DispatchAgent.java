package contract_net;

// we are calling methods from com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.LoggerFactory;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Container;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;


public class DispatchAgent implements CommUser, TickListener {



	private DefaultPDPModel defaultpdpmodel;
	// stillToBeAssignedParcelss uit simulator halen want Parcels zijn geregistreerd in de simulator
	private Collection<Parcel> toBeDispatchedParcels;
	private List<CNPMessage> CNPmessages = new ArrayList<CNPMessage>();
	private List<CNPMessage> unreadMessages = new ArrayList<CNPMessage>();
	private List<Proposal> proposals = new ArrayList<Proposal>();
	//list of potential VehicleAgent contractors
	private List<TruckAgent> potentialContractors = new ArrayList<TruckAgent>();
	// deze twee moeten in veiling
	private List<TruckAgent> lostContractors = new ArrayList<TruckAgent>();
	private TruckAgent winningContractor = null;
	private ArrayList<CommUser> commUsers = new ArrayList<CommUser>(); // TruckAgent commUsers coupled to DispatchAgent
	private Proposal bestProposal;
	//used to record the number of received messages
	//in this version, we impose the manager to wait till receiving answers from all the contractors
	private int numberOfreceivedMessages = 0;
	private AuctionResult auctionResult;
	private List<AuctionResult> auctionResults;

	//record the agent responsible for the best proposal
	private Optional<CommDevice> commDevice;
	// settings of commDevice
	private long lastReceiveTime;
	private final double range;
	private final double reliability;
	static final double MIN_RANGE = .2;
	static final double MAX_RANGE = 1.5;
	static final long LONELINESS_THRESHOLD = 10 * 1000;
	private static final long AUCTION_DURATION = 1000;
	private final RandomGenerator rng;
	private CNPMessage cnpmessage;

	public DispatchAgent(DefaultPDPModel defaultpdpmodel, RandomGenerator rng, List<AuctionResult> auctionResults) {
		this.defaultpdpmodel = defaultpdpmodel;// defined in the main
		toBeDispatchedParcels = new ArrayList<Parcel>();
		this.auctionResults = auctionResults;
		commDevice = Optional.absent();
		// settings for commDevice belonging to DispatchAgent
		this.rng = rng;
		range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
		reliability = rng.nextDouble();
	}

	/*
	public DispatchAgent(List<VehicleAgent> potentialContractors, List<CNPMessage> messages, List<Parcel> toBeDispatchedParcel) {
		this.potentialContractors = potentialContractors;
		this.messages = messages;
		this.toBeDispatchedParcels = toBeDispatchedParcels;
		commDevice = Optional.absent();
	}
	 */

	// which Parcels have to be dispatched to the different truckAgents?
	public Collection<Parcel> getANNOUNCEDParcels(){
		return defaultpdpmodel.getParcels(ParcelState.ANNOUNCED);
	}

	public Collection<Parcel> getAVAILABLEParcels(){
		return defaultpdpmodel.getParcels(ParcelState.AVAILABLE);
	}

	public Collection<Parcel> getToBeDispatchedParcels(){
		toBeDispatchedParcels = getANNOUNCEDParcels();
		toBeDispatchedParcels.addAll(getAVAILABLEParcels());
		return toBeDispatchedParcels;
	}

	// which truckAgents are available to perform a task?
	public Set<Vehicle> getTruckAgents(){
		return defaultpdpmodel.getVehicles();
	}

	public VehicleState getTruckAgentState(TruckAgent truckAgent){
		return defaultpdpmodel.getVehicleState(truckAgent);
	}

	// get the commUser/commDevice pairs that are coupled; commModel is defined in main
	public ImmutableBiMap<CommUser, CommDevice> getCommUserDevice(CommModel commModel){
		ImmutableBiMap<CommUser, CommDevice> commUsersDevices= commModel.getUsersAndDevices();
		return commUsersDevices;
	}

	// the dispatchAgent has to communicate with commUsers from the class TruckAgent
	// this method retrieves all the TruckAgent registered to this CommModel of our Simulator
	public ArrayList<CommUser> getTruckAgentCommUsers(CommModel commModel){
		ImmutableBiMap<CommUser, CommDevice> commUsersDevices=getCommUserDevice(commModel);
		// loop through BiMap and collect all CommUsers belonging to the DispatchAgent commDevice in one ArrayList
		CommUser commUser = null;
		for (ImmutableBiMap.Entry<CommUser, CommDevice> entryCommDevice: commUsersDevices.entrySet()) {
			commUser = commUsersDevices.inverse().get(entryCommDevice);
		}
		commUsers.add(commUser);
	}

	// if the dispatch agent wants to communicate with all other commUsers
	// CNPMessage contains info about the Message and the ContractNetMessageType
	public void sendBroadcastMessage(CNPMessage content){
		if (!this.commDevice.isPresent()) {
			throw new IllegalStateException("No commdevice activated for this dispatch agent");
		}
		CommDevice device = this.commDevice.get();
		device.broadcast(content);
	}

	// if the dispatch agent wants to communicate to only one other CommUser, i.e. one specific truck
	public void sendDirectMessage(CNPMessage content, CommUser recipient) {
		if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for dispatch agent");}
		CommDevice device = this.commDevice.get();
		device.send(content, recipient);
	}

	public void sendCallForProposals(Parcel parcel, long currentTime, long AUCTION_DURATION){
		ContractNetMessageType type = ContractNetMessageType.CALL_FOR_PROPOSAL;
		Auction auction = new Auction(this, parcel, currentTime + AUCTION_DURATION, currentTime, false);
		CNPMessage cnpMessage = new CNPMessage(auction, type, this);
		sendBroadcastMessage(cnpMessage);
	}

	public void dispatchParcels(long currentTime, long AUCTION_DURATION){
		toBeDispatchedParcels = getAVAILABLEParcels();
		if(!toBeDispatchedParcels.isEmpty()){
			for(Parcel p: toBeDispatchedParcels){
				Auction auction = new Auction(this, p, currentTime, AUCTION_DURATION, true);
				sendCallForProposals(p, currentTime, AUCTION_DURATION);
			}
		}
	}

	public Proposal selectBestProposal(List<Proposal> proposals){
		long maxProposal = proposals.get(0).getTimeCostProposal();
		Proposal bestProposal = proposals.get(0);
		for(Proposal p: proposals){
			if (p.getTimeCostProposal() > maxProposal){
				maxProposal = p.getTimeCostProposal();
				bestProposal = p;
			}
			
		}
		return bestProposal;
	}


	/// uit oude Rinsim
/*
	private AcceptProposal chooseBestProposal(Collection<Proposal> proposals, long currentTime) {
		AcceptProposal accepted = null;
		Iterator<Proposal> it = proposals.iterator();
		Proposal best = it.next();
		Proposal second = null;
		if (it.hasNext()) {
			second = it.next();
			if (second.getDeliveryTime() < best.getDeliveryTime()) {
				Proposal p = best;
				best = second;
				second = p;
			}
			while (it.hasNext()) {
				Proposal p = it.next();
				if (p.getDeliveryTime() < best.getDeliveryTime()) {
					second = best;
					best = p;
				} else if (p.getDeliveryTime() < second.getDeliveryTime()) {
					second = p;
				}
			}
		}
	
		
			// uit Goosens
		protected CNPAgent getWorkerWithBestProposal() {
		double bestProposal = Double.MAX_VALUE;
		CNPAgent bestAgent = null;
		for (CNPAgent agent: this.proposals.keySet()) {
			double proposal = this.proposals.get(agent);
			if (proposal < bestProposal) {
				bestProposal = proposal;
				bestAgent = agent;
			}
		}
		return bestAgent;
	}
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


	// thicklistener methods implemented
	@Override
	public void tick(TimeLapse timeLapse) {
		/*
	    if (!destination.isPresent()) {
	      destination = Optional.of(roadModel.get().getRandomPosition(rng));
	    }
	    roadModel.get().moveTo(this, destination.get(), timeLapse);
	    if (roadModel.get().getPosition(this).equals(destination.get())) {
	      destination = Optional.absent();
	    }
	    
	    */
		
		long currentTime = timeLapse.getTime();
	    dispatchParcels(currentTime, AUCTION_DURATION);
	


		if (commDevice.get().getUnreadCount() > 0) {
			lastReceiveTime = timeLapse.getStartTime();
			unreadMessages = readMessages();

			for (CNPMessage m : unreadMessages) {
				lostContractors.clear();

				switch (m.getType()) {

				case REFUSE:
					/// send reject message to TruckAgents who lost the auction
					///sendReject(m.getAuction(), ContractNetMessageType.REJECT_PROPOSAL);
					break;
				case PROPOSE:
					// TODO: if you work with an auction deadline, check that only proposals that arrive before the auction deadline are added
					CNPProposalMessage mess = (CNPProposalMessage)m;
					proposals.add(mess.getProposal());
					// send ACCEPT_PROPOSAL message to TruckAgent who won this auction					
					sendAcceptProposal(mess.getAuction(), ContractNetMessageType.ACCEPT_PROPOSAL);
					// send REJECT_PROPOSAL message to all TruckAgents who sent a proposal to this auction, but did not win
					sendRejectProposal(null, null, commUsers);
					break;
				case FAILURE:
					// do nothing or in more advanced form of the program: rebroadcast call for proposal
					break;
				case INFORM_DONE:
					// truck tells that parcel is delivered
					// TODO: store in AuctionResult that parcel is delivered
					break;
				case INFORM_RESULT:
					// receive message from truck telling that parcel is delivered and giving information about the actual travel time, travel distance, fuel level,...
					// TODO: store this information in AuctionResult
					// TODO: set status of Package on IS_DELIVERED if this was not yet the case
					break;
				default:
					break;
				}
			}
			generateAuctionResults(timeLapse);

		}
	}

	public void generateAuctionResults(TimeLapse timeLapse){
		bestProposal = selectBestProposal(proposals);
		if(bestProposal != null){
			sendAcceptProposal(bestProposal.getAuction(), ContractNetMessageType.ACCEPT_PROPOSAL);
			// stop this auction. In a more advanced model of the algorithm, you can decide to stop the auction only when 
			// the truck has actually delivered the parcel and the INFORM_DONE message is sent
			bestProposal.getAuction().setActiveAuction(false);
			auctionResult = new AuctionResult(bestProposal.getAuction(), bestProposal, bestProposal.getProposer(), timeLapse.getTime());
			auctionResults.add(auctionResult);
			/// change ParcelState
		}
	}
	
	public void sendAcceptProposal(Auction auction, ContractNetMessageType type){
		CNPAcceptMessage cnpAcceptMessage = new CNPAcceptMessage(auction, type, this, bestProposal.getProposer(), bestProposal);
		sendDirectMessage(cnpAcceptMessage, bestProposal.getProposer());
	}
	
	public void sendRefusal(Auction auction, ContractNetMessageType type){
		CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, type, this, auction.getSenderAuction(), type.toString());
		sendDirectMessage(cnpRefusalMessage, auction.getSenderAuction());	
	}
	
	 
		public void sendRejectProposal(Auction auction, ContractNetMessageType s, List<CommUser> auctionLosers){
			CNPRejectMessage cnpRejectMessage = new CNPRejectMessage(auction, s, this, auctionLosers, s.toString());
			//TODO loop through all elements from auctionLosers, and send each a direct message
			sendDirectMessage(cnpRejectMessage, auction.getSenderAuction());
		}


	public AuctionResult getAuctionResult() {
		return auctionResult;
	}

	public void setAuctionResult(AuctionResult auctionResult) {
		this.auctionResult = auctionResult;
	}

	// CommUser methods implemented
	@Override
	// not needed for us since the dispatch agent is a phone app, so it has not one position at a certain physical depot
	public Optional<Point> getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		//    if (range >= 0) {
		if (range >= 0) {
			builder.setMaxRange(range);
		}
		commDevice = Optional.of(builder
				.setReliability(reliability)
				.build());
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
	
	}
	commDevice = Optional.of(builder
			.setReliability(reliability)
			.build());
	
	}
	
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		//// wat moet hier???
	}
}
