package contract_net;

import java.util.List;

import com.github.rinde.rinsim.core.model.pdp.Parcel;

/**
 * this class contains all the data relevant for one specific auction
 * @author Katrien.Bernaerts
 *
 */
public class AuctionResult {
	
	private Auction auction;
	long auctionEndTime;
	private Parcel parcel;
	private DispatchAgent dispatchAgent;
	private TruckAgent truckAgent;
	private Proposal bestProposal;
	private long calculatedTimeBid; // the travel time that the TruckAgent communicated to the Dispatch Agent for the PDP task
	private long actualTotalTime; // actual time the Truck needed from the position of the truck to the parcel pickup and from the parcel pickup to the parcel delivery
	private long timeAvailableDelivered; // time between package becoming available and package being delivered
	///// waarschijnlijk gaat dit objecttype nog wijzigen, en ga ik aparte klassen maken voor refusals, failures, ...
	List<CNPMessage> refusals;
	List<CNPMessage> validProposals; // proposals done within the time limits of the auction
	List<CNPMessage> invalidProposals; // proposals that were sent to the dispatchAgent when the auction was already finished
	List<CNPMessage> failures;
	List<CNPMessage> inform_done; // message from TruckAgent to DispatchAgent that the PDP task is completed
	List<CNPMessage> inform_result; // message from TruckAgent to DispatchAgent that the PDP task is completed with some results (e.g. PDP time needed, ...)
	TruckAgent winner;
	
	public AuctionResult(Auction auction, Proposal bestProposal, TruckAgent winner, long auctionDuration){
		this.auction = auction;
		this.winner = winner;
		this.auctionEndTime = auctionDuration;
	}

	public Auction getAuction() {
		return auction;
	}

	public void setAuction(Auction auction) {
		this.auction = auction;
	}

	public long getAuctionDuration() {
		return auctionEndTime;
	}

	public void setAuctionDuration(long auctionDuration) {
		this.auctionEndTime = auctionDuration;
	}

	public TruckAgent getWinner() {
		return winner;
	}

	public void setWinner(TruckAgent winner) {
		this.winner = winner;
	}

	public Proposal getBestProposal() {
		return bestProposal;
	}

	public void setBestProposal(Proposal bestProposal) {
		this.bestProposal = bestProposal;
	}
}
