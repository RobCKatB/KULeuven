package contract_net;

import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Proposal {
	private TruckAgent proposer;
	private Auction auction;
	private long timeCostProposal;
	private long pickupToDeliveryTime;
	private long currentToPickupTime;
	private boolean auctionOpen;
	
	
	//TODO ?? of TruckAgent vervangen door CNPMessage proposal, waarbij CNPMessage.getSender() de truckAgent oplevert
	public Proposal(Auction auction, TruckAgent proposer, long currentToPickupTime, long pickupToDeliveryTime, long timeCostProposal){
		this.auction = auction;
		this.proposer = proposer;		
		this.timeCostProposal = timeCostProposal;
		this.pickupToDeliveryTime = pickupToDeliveryTime;
		this.currentToPickupTime = currentToPickupTime;
		}
	
	
	public long getPickupToDeliveryDistance(long pickupToDeliveryTime){
		return pickupToDeliveryTime * (long)this.getProposer().getSpeed();
	}
	
	public long getCurrentToPickupDistance(long currentToPickupTime){
		return currentToPickupTime * (long)this.getProposer().getSpeed();
	}
	
	public long getDistanceCostProposal(long timeCostProposal){
		return timeCostProposal * (long)this.getProposer().getSpeed();
	}

	public long getPickupToDeliveryTime() {
		return pickupToDeliveryTime;
	}

	public void setPickupToDeliveryTime(long pickupToDeliveryTime) {
		this.pickupToDeliveryTime = pickupToDeliveryTime;
	}

	public long getCurrentToPickupTime() {
		return currentToPickupTime;
	}

	public void setCurrentToPickupTime(long currentToPickupTime) {
		this.currentToPickupTime = currentToPickupTime;
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
	
	@Override
	public String toString() {
		 StringBuilder builder = new StringBuilder("Proposal [")
					.append("auction: ")
					.append(auction.getId())
					.append(",")
					.append("proposer: ")
		 			.append(proposer)
		 			.append("; calculated time needed for total PDP: ")
					.append(getTimeCostProposal())
					.append("; calculated time from current truck position to parcel pickup position: ")
		 			.append(currentToPickupTime)
		 			.append("; calculated time from parcel pickup position to parcel delivery position:  ")
		 			.append(pickupToDeliveryTime);

		builder.append("]");
			  
		return builder.toString();
	}
}
