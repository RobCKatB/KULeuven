package contract_net;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.random.RandomGenerator;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;



public class DispatchAgent extends Depot implements CommUser, TickListener {



	private DefaultPDPModel defaultpdpmodel;
	private RoadModel roadModel;
	private List<CNPMessage> unreadMessages = new ArrayList<CNPMessage>();
	private AuctionResult auctionResult;
	private List<AuctionResult> auctionResults;
	private long currentTime;

	// Collections for the four stages a parcel goes through.
	private HashSet<Parcel> parcelsInitial = new HashSet<Parcel>();			// Parcels just added to this DispatchAgent.
	private HashSet<Parcel> parcelsAuctionRunning = new HashSet<Parcel>();	// A running auction.
	private HashSet<Parcel> parcelsHandled = new HashSet<Parcel>();			// Parcel is being handled by a TruckAgent.
	private HashSet<Parcel> parcelsDelivered = new HashSet<Parcel>();		// Done.
	// A map for the auctions.
	private HashMap<Parcel,Auction> auctions = new HashMap<Parcel,Auction>();
	
	//record the agent responsible for the best proposal
	private Optional<CommDevice> commDevice;
	private final double range;
	private final double reliability = 1.0D;
	static final double MIN_RANGE = .2;
	static final double MAX_RANGE = 1.5;
	static final long LONELINESS_THRESHOLD = 10 * 1000;
	private static final long AUCTION_DURATION = 10000;
	
	public DispatchAgent(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, RandomGenerator rng, Point position, List<AuctionResult> auctionResults) {
		super(position);
		this.defaultpdpmodel = defaultpdpmodel;// defined in the main
		this.roadModel = roadModel; // defined in the main
		new ArrayList<Parcel>();
		this.auctionResults = auctionResults;
		commDevice = Optional.absent();
		//range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
		//range = 9000000.0D;
		range = Double.MAX_VALUE;
	}

	// thicklistener methods implemented
	@Override
	public void tick(TimeLapse timeLapse) {
		currentTime = timeLapse.getTime();
		dispatchParcels(currentTime, AUCTION_DURATION);
		if (this.commDevice.get().getUnreadCount() > 0) {
			unreadMessages = readMessages();
			
			for (CNPMessage m : unreadMessages) {

				switch (m.getType()) {

				case REFUSE:
					// do nothing
					CNPRefusalMessage cnpRefusalMessage = (CNPRefusalMessage)m;
					break;
				case PROPOSE:
					CNPProposalMessage cnpProposalMessage = (CNPProposalMessage)m;
					Auction auction = cnpProposalMessage.getAuction();
//					System.out.println("proposal from truckagent "+ cnpProposalMessage.getSender()+ "is " +cnpProposalMessage.getProposal().toString());

					auction.addProposal(cnpProposalMessage.getProposal(), cnpProposalMessage.getTimeSent());
					
					break;
				case FAILURE:
					CNPFailureMessage cnpFailureMessage = (CNPFailureMessage)m;
					System.out.println("Truckagent "+ cnpFailureMessage.from() + " has sent failed to do a proposal for auction "+ cnpFailureMessage.getAuction().toString());
					// TODO do nothing or in more advanced form of the program: rebroadcast call for proposal
					break;
				case INFORM_DONE:
					CNPInformDoneMessage cnpInformDoneMessage = (CNPInformDoneMessage)m;
					System.out.println("Truckagent "+ cnpInformDoneMessage.from() + " has sent an INFORM DONE message "+ cnpInformDoneMessage.toString());
					// truck tells that parcel is delivered
					// TODO: store in AuctionResult that parcel is delivered
					break;
				case INFORM_RESULT:
					// an INFORM_RESULT message comes from the truckagent that won the action, so the truckagent that had the best proposal for the PDP task
					CNPInformResultMessage cnpInformResultMessage = (CNPInformResultMessage)m;
					System.out.println("INFORM RESULT message received by dispatchagent from truckagent "+  cnpInformResultMessage.toString());
					List<Proposal> validProposalsForThisParcel= cnpInformResultMessage.getAuction().getProposals();
					List<Proposal> tooLateProposalsForThisParcel= cnpInformResultMessage.getAuction().getTooLateProposals();
					Proposal bestProposalForThisParcel = selectBestProposal(validProposalsForThisParcel);
					List<Proposal> rejectedProposalsForThisParcel= getRejectedProposalsForThisParcel(cnpInformResultMessage.getAuction().getParcel(), bestProposalForThisParcel, validProposalsForThisParcel, tooLateProposalsForThisParcel);
					
					auctionResult = new AuctionResult(cnpInformResultMessage.getAuction(), bestProposalForThisParcel, bestProposalForThisParcel.getProposer(), AUCTION_DURATION, cnpInformResultMessage.getTimeTruckToPickup(), cnpInformResultMessage.getTimePickupToDelivery(), cnpInformResultMessage.getTimeTruckToPickupToDelivery(), cnpInformResultMessage.getTimeCFPToDelivery(), rejectedProposalsForThisParcel);
					System.out.println("AUCTION RESULT: " +auctionResult.toString());
					auctionResults.add(auctionResult);

					// TODO: set status of Package on IS_DELIVERED if this was not yet the case
					
					break;
				default:
					break;
				}
			}
		}
		
		// Send accept and reject proposal messages for each running auction that is done.
		HashSet<Parcel> toBeClosedAuctionParcels = (HashSet<Parcel>) parcelsAuctionRunning.clone();
		for (Parcel parcel: toBeClosedAuctionParcels){
			Auction auction = auctions.get(parcel);
			
			if(auction.isActive() && auction.isExpired(timeLapse.getEndTime())){
			
				List<Proposal> validProposalsForThisParcel = auction.getProposals();
				System.out.println("Valid proposals for "+ auction);
				for(Proposal p: validProposalsForThisParcel){
					System.out.println(p.toString());
				}
				List<Proposal> tooLateProposalsForThisParcel = auction.getTooLateProposals();
				if(!validProposalsForThisParcel.isEmpty() || !tooLateProposalsForThisParcel.isEmpty()){
					sendAcceptRejectProposalMessages(timeLapse, parcel, validProposalsForThisParcel, tooLateProposalsForThisParcel);
					auction.setActive(false);
				}else{
					// Remove the auction
					auctions.remove(auction);
					// Place the parcel back in the right set
					parcelsAuctionRunning.remove(parcel);
					parcelsInitial.add(parcel);
				}
	
				
			}
		}
	}

	/**
	 * Assign a parcel to this DispatchAgent.
	 * @param parcel
	 */
	public void assignParcel(Parcel parcel){
		parcelsInitial.add(parcel);
	}

	// which truckAgents are available to perform a task?
	public Set<Vehicle> getTruckAgents(){
		return defaultpdpmodel.getVehicles();
	}

	public VehicleState getTruckAgentState(TruckAgent truckAgent){
		return defaultpdpmodel.getVehicleState(truckAgent);
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

	public void sendCallForProposals(Auction auction, Parcel parcel, long currentTime, long AUCTION_DURATION){
		ContractNetMessageType type = ContractNetMessageType.CALL_FOR_PROPOSAL;
		CNPMessage cnpMessage = new CNPMessage(auction, type, this, currentTime);
		sendBroadcastMessage(cnpMessage);
	}

	public void dispatchParcels(long currentTime, long auctionDuration){
		HashSet<Parcel> toBeDispatched = (HashSet<Parcel>) parcelsInitial.clone();
		for(Parcel parcel: toBeDispatched){
			// Make an auction and notify the TruckAgents
			Auction auction = new Auction(this, parcel, currentTime, auctionDuration, true);
			sendCallForProposals(auction, parcel, currentTime, auctionDuration);
			// Save the auction
			auctions.put(parcel, auction);
			// Place the parcel in the right set
			parcelsInitial.remove(parcel);
			parcelsAuctionRunning.add(parcel);
			
			System.out.println(this+" > Send call propposal for "+parcel+". "+ auction.toString());
		}
	}

	public Proposal selectBestProposal(List<Proposal> proposals){
		long minProposal = Long.MAX_VALUE;
		Proposal bestProposal = null;
		for(Proposal p: proposals){
			if (p.getTimeCostProposal() < minProposal){
				minProposal = p.getTimeCostProposal();
				bestProposal = p;
			}
			
		}
		return bestProposal;
	}

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


	public List<Proposal> proposalsForThisParcel(Parcel parcel, List<Proposal> proposals){
		List<Proposal> proposalsForThisParcel = new ArrayList<Proposal>();

		for(Proposal prop: proposals){
			if(prop.getAuction().getParcel().equals(parcel)){
				proposalsForThisParcel.add(prop);
			}
		}
		return proposalsForThisParcel ;
	}
	
	public List<Proposal> getRejectedProposalsForThisParcel(Parcel parcel, Proposal bestProposal, List<Proposal> validProposalsForThisParcel, List<Proposal> tooLateProposalsForThisParcel){
		List<Proposal> rejectedProposalsForThisParcel = new ArrayList<Proposal>();
		for(Proposal valid: validProposalsForThisParcel){
			if(!valid.equals(bestProposal)){
				rejectedProposalsForThisParcel.add(valid);}
		}
		for(Proposal tooLate: tooLateProposalsForThisParcel){
			rejectedProposalsForThisParcel.add(tooLate);
		}
		return rejectedProposalsForThisParcel;
	}
	
	public void sendAcceptRejectProposalMessages(TimeLapse timeLapse, Parcel parcel, List<Proposal> validProposalsForThisParcel, List<Proposal> tooLateProposalsForThisParcel){
		Proposal bestProp = selectBestProposal(validProposalsForThisParcel);
		System.out.println("Best proposal :" +bestProp);
		List<Proposal> rejectedProposalsForThisParcel = new ArrayList<Proposal>();
		if(bestProp != null){
			sendAcceptProposal(bestProp.getAuction(), bestProp, ContractNetMessageType.ACCEPT_PROPOSAL, timeLapse);
			System.out.println(this+" > ACCEPT proposal sent to truck "+bestProp.getProposer());
			// send REJECT_PROPOSAL message to all TruckAgents who sent a proposal to this auction, but did not win
			for(Proposal p: validProposalsForThisParcel){
				if(!p.equals(bestProp)){
					rejectedProposalsForThisParcel.add(p);
					sendRejectProposal(p.getAuction(), ContractNetMessageType.REJECT_PROPOSAL, p.getProposer(), "lost auction", timeLapse);
					System.out.println(this+" > REJECT proposal sent to truck "+p.getProposer());
				}
			}
		}
		else{
			System.out.println("ERROR: no best proposal, best proposal is empty");
		}
		// send REJCECT_PROPOSAL message to all TruckAgent who sent their proposal for this parcel after the auction deadline had passed, i.e. the non valid proposals
		for(Proposal p: tooLateProposalsForThisParcel){
			rejectedProposalsForThisParcel.add(p);
			sendRejectProposal(p.getAuction(), ContractNetMessageType.REJECT_PROPOSAL, p.getProposer(), "too late", timeLapse);
			System.out.println("REJECT proposal due to too late submisseion sent by dispatchagent " + this+ " to truckagent " +p.getProposer());
		}
		/*moved to INFORM_RESULT case since there all needed information is gathered
		// TODO: PDPtime and CFPtoDelivery time not yet known, make those parameters nullable in class AuctionResult instead of filling in here 0
		auctionResult = new AuctionResult(bestProposal.getAuction(), bestProposal, bestProposal.getProposer(), AUCTION_DURATION, 0, 0, rejectedProposalsForThisParcel);
		System.out.println("auction result" +auctionResult.toString());
		auctionResults.add(auctionResult);
		*/
		
	}
	
	public void sendAcceptProposal(Auction auction, Proposal bestProp, ContractNetMessageType type, TimeLapse time){
		CNPAcceptMessage cnpAcceptMessage = new CNPAcceptMessage(auction, type, this, bestProp.getProposer(), bestProp, time.getTime());
		sendDirectMessage(cnpAcceptMessage, bestProp.getProposer());
	}
	
	public void sendRefusal(Auction auction, ContractNetMessageType type,TimeLapse time){
		CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, type, this, auction.getDispatchAgent(), type.toString(), time.getTime());
		sendDirectMessage(cnpRefusalMessage, auction.getDispatchAgent());	
	}
	
	 
	public void sendRejectProposal(Auction auction, ContractNetMessageType s, CommUser loser, String rejectionReasen, TimeLapse time){
		CNPRejectMessage cnpRejectMessage = new CNPRejectMessage(auction, s, this, loser, rejectionReasen, time.getTime());
		sendDirectMessage(cnpRejectMessage, loser);
	}


	public AuctionResult getAuctionResult() {
		return auctionResult;
	}

	public void setAuctionResult(AuctionResult auctionResult) {
		this.auctionResult = auctionResult;
	}

	// CommUser methods implemented
	@Override
	public Optional<Point> getPosition() {
	   if (roadModel.containsObject(this)) {
	        return Optional.of(roadModel.getPosition(this));
	      }
	      return Optional.<Point>absent();
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
	
	}
	
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO waarmee moet deze methode overschreven worden?
		//TODO we moeten nog ergens zorgen dat we een CollisionGraphRoadModel hebben, zie opdracht
		
		//roadModel = Optional.of(pRoadModel);
		//pdpModel = Optional.of(pPdpModel);
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}
	
	public List<CNPMessage> getUnreadMessages() {
		return unreadMessages;
	}

	public void setUnreadMessages(List<CNPMessage> unreadMessages) {
		this.unreadMessages = unreadMessages;
	}

}
