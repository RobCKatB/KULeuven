package contract_net;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

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



public class DispatchAgent extends Depot implements CommUser, TickListener {



	private DefaultPDPModel defaultpdpmodel;
	private RoadModel roadModel;
	//Optional<RoadModel> roadModel;
	// stillToBeAssignedParcelss uit simulator halen want Parcels zijn geregistreerd in de simulator
	private Collection<Parcel> toBeDispatchedParcels;
	private List<CNPMessage> unreadMessages = new ArrayList<CNPMessage>();
	private List<Proposal> proposals = new ArrayList<Proposal>();
	private List<Proposal> tooLateProposals = new ArrayList<Proposal>();
	//used to record the number of received messages
	//in this version, we impose the manager to wait till receiving answers from all the contractors
	private int numberOfreceivedMessages = 0;
	private AuctionResult auctionResult;
	private List<AuctionResult> auctionResults;
	private long currentTime;

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
		toBeDispatchedParcels = new ArrayList<Parcel>();
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
				//TODO opletten, door de unread messages in te lezen, wordt inbox geleegd. Maar er zitten in de inbox ook bv CFP messages voor truckagent, die echter bij het loopen door dispatchagent niet herkend worden en verloren gaan.

				for (CNPMessage m : unreadMessages) {

					switch (m.getType()) {

					case REFUSE:
						// do nothing
						CNPRefusalMessage cnpRefusalMessage = (CNPRefusalMessage)m;
						System.out.println("REFUSAL MESSAGE sent by truckagent "+ cnpRefusalMessage.from() + " for auction "+ cnpRefusalMessage.getAuction().toString());
						break;
					case PROPOSE:
						CNPProposalMessage cnpProposalMessage = (CNPProposalMessage)m;
						// check that only proposals that arrive before the auction deadline are added
						if(cnpProposalMessage.getTimeSent() - cnpProposalMessage.getAuction().getStartTime()  < cnpProposalMessage.getAuction().getAuctionDuration())
						{
							proposals.add(cnpProposalMessage.getProposal());
							System.out.println("proposal from truckagent "+ cnpProposalMessage.getSender()+ "is " +cnpProposalMessage.getProposal().toString());
						} else {
							cnpProposalMessage.getAuction().setActiveAuction(false);
							tooLateProposals.add(cnpProposalMessage.getProposal());
						}
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
						List<Proposal> validProposalsForThisParcel= proposalsForThisParcel(cnpInformResultMessage.getAuction().getParcel(), proposals);
						List<Proposal> tooLateProposalsForThisParcel= proposalsForThisParcel(cnpInformResultMessage.getAuction().getParcel(), tooLateProposals);
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
			//send accept and reject proposal messages for each auction/parcel (1 auction per parcel case)
			for (Parcel parcel: toBeDispatchedParcels){
				List<Proposal> validProposalsForThisParcel= proposalsForThisParcel(parcel, proposals);
				List<Proposal> tooLateProposalsForThisParcel= proposalsForThisParcel(parcel, tooLateProposals);
				if(!validProposalsForThisParcel.isEmpty() || !tooLateProposalsForThisParcel.isEmpty()){
					sendAcceptRejectProposalMessages(timeLapse, parcel, validProposalsForThisParcel, tooLateProposalsForThisParcel);
				}

			}
		}

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

	public void dispatchParcels(long currentTime, long AUCTION_DURATION){
		toBeDispatchedParcels = getAVAILABLEParcels();
		System.out.println("to be dispatched parcels: "+  toBeDispatchedParcels);
		if(!toBeDispatchedParcels.isEmpty()){
			for(Parcel p: toBeDispatchedParcels){
				Auction auction = new Auction(this, p, currentTime, AUCTION_DURATION, true);
				sendCallForProposals(auction, p, currentTime, AUCTION_DURATION);
				System.out.println("Call for proposals sent by dispatchagent "+this+ " for parcel "+p+". Auction "+ auction.toString()+ " started at "+currentTime+", auction duration "+AUCTION_DURATION);
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
			System.out.println("ACCEPT proposal sent by dispatchagent " + this+ " to truckagent " +bestProp.getProposer());
		} else {
			// send REJECT_PROPOSAL message to all TruckAgents who sent a proposal to this auction, but did not win
			for(Proposal p: validProposalsForThisParcel){
				if(!p.equals(bestProp)){
					rejectedProposalsForThisParcel.add(p);
					sendRejectProposal(p.getAuction(), ContractNetMessageType.REJECT_PROPOSAL, p.getProposer(), "lost auction", timeLapse);
					System.out.println("REJECT proposal sent by dispatchagent " + this+ " to truckagent " +p.getProposer());
				}
			}
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
		CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, type, this, auction.getSenderAuction(), type.toString(), time.getTime());
		sendDirectMessage(cnpRefusalMessage, auction.getSenderAuction());	
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

	/*
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("DispatchAgent [");
				
		builder
		.append("parcels: ")
		.append(toBeDispatchedParcels.size())
		.append(",")
		.append("proposals: ")
		.append(proposals.size())
		.append(",")
		.append("tooLateProposals: ")
		.append(tooLateProposals.size())
		.append(",")
		.append("rejectedProposals: ")
		.append(rejectedProposals.size());
		
		
		
			  
		return builder.toString();
	}
	*/
}
