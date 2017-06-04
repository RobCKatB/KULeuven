package contract_net;
//
import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Proposal {
	private TruckAgent proposer;
	private Auction auction;
	
	
	//// of TruckAgent vervangen door CNPMessage proposal, waarbij CNPMessage.getSender() de truckAgent oplevert
	public Proposal(Auction auction, TruckAgent proposer, long timeCostProposal){
		this.auction = auction;
		this.proposer = proposer;		
		timeCostProposal = setTimeCostBid();
	}
	
	// calculate travel time for this bidder for the current auction
	public long setTimeCostBid(){
		return proposer.calculateTravelTime(proposer.getPosition(), auction.getParcel());
	}
	
}
