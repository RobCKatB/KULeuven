package contract_net;
//
import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Proposal {
	private TruckAgent proposer;
	private Auction auction;
	private long timeCostProposal;
	private boolean auctionOpen;
	
	
	//TODO ?? of TruckAgent vervangen door CNPMessage proposal, waarbij CNPMessage.getSender() de truckAgent oplevert
	public Proposal(Auction auction, TruckAgent proposer, long timeCostProposal){
		this.auction = auction;
		this.proposer = proposer;		
		this.timeCostProposal = timeCostProposal;
	}
	
	// calculate travel time for this bidder for the current auction
	public long setTimeCostBid(){
		return proposer.calculateTravelTimePDP(proposer.getPosition().get(), auction.getParcel());
	}

	public TruckAgent getProposer() {
		return proposer;
	}

	public void setProposer(TruckAgent proposer) {
		this.proposer = proposer;
	}

	public Auction getAuction() {
		return auction;
	}

	public void setAuction(Auction auction) {
		this.auction = auction;
	}

	public long getTimeCostProposal() {
		return timeCostProposal;
	}

	public void setTimeCostProposal(long timeCostProposal) {
		this.timeCostProposal = timeCostProposal;
	}	
	
	//TODO ?? include method isReplyTo to check that the proposal is linked to the right call for proposal message

}
