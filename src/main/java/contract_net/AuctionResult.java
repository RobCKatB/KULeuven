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
	long auctionDuration;
	private Proposal bestProposal;
	private List<Proposal> rejectedProposals;
	private TruckAgent winner;
	long realTimePickupToDelivery;
	long realTimeTruckToPickup;
	long realTimeTruckToPickupToDelivery;
	long realTimeCFPDelivery;
	
	public AuctionResult(Auction auction, Proposal bestProposal, TruckAgent winner, long auctionDuration, long realTimeTruckToPickup, long realTimePickupToDelivery, long realTimeTruckToPickupToDelivery, long realTimeCFPDelivery, List<Proposal> rejectedProposals){
		this.auction = auction;
		this.bestProposal = bestProposal;
		this.winner = winner;
		this.auctionDuration = auctionDuration;
		this.rejectedProposals = rejectedProposals;
		this.realTimeTruckToPickup = realTimeTruckToPickup;
		this.realTimePickupToDelivery = realTimePickupToDelivery;
		this.realTimeTruckToPickupToDelivery = realTimeTruckToPickupToDelivery;
		this.realTimeCFPDelivery = realTimeCFPDelivery;
	}

	public Auction getAuction() {
		return auction;
	}

	public void setAuction(Auction auction) {
		this.auction = auction;
	}

	public long getAuctionDuration() {
		return auctionDuration;
	}

	public void setAuctionDuration(long auctionDuration) {
		this.auctionDuration = auctionDuration;
	}

	public Proposal getBestProposal() {
		return bestProposal;
	}

	public void setBestProposal(Proposal bestProposal) {
		this.bestProposal = bestProposal;
	}

	public List<Proposal> getRejectedProposals() {
		return rejectedProposals;
	}

	public void setRejectedProposals(List<Proposal> rejectedProposals) {
		this.rejectedProposals = rejectedProposals;
	}

	public TruckAgent getWinner() {
		return winner;
	}

	public void setWinner(TruckAgent winner) {
		this.winner = winner;
	}

	public long getRealTimePickupToDelivery() {
		return realTimePickupToDelivery;
	}

	public void setRealTimePickupToDelivery(long realTimePickupToDelivery) {
		this.realTimePickupToDelivery = realTimePickupToDelivery;
	}

	public long getRealTimeTruckToPickup() {
		return realTimeTruckToPickup;
	}

	public void setRealTimeTruckToPickup(long realTimeTruckToPickup) {
		this.realTimeTruckToPickup = realTimeTruckToPickup;
	}

	public long getRealTimeTruckToPickupToDelivery() {
		return realTimeTruckToPickupToDelivery;
	}

	public void setRealTimeTruckToPickupToDelivery(long realTimeTruckToPickupToDelivery) {
		this.realTimeTruckToPickupToDelivery = realTimeTruckToPickupToDelivery;
	}

	public long getRealTimeCFPDelivery() {
		return realTimeCFPDelivery;
	}

	public void setRealTimeCFPDelivery(long realTimeCFPDelivery) {
		this.realTimeCFPDelivery = realTimeCFPDelivery;
	}

	public long getPickupTardiness(Proposal bestProposal, long realTimeTruckToPickup){
		long calculatedTimeTruckToPickup = bestProposal.getCurrentToPickup();
		return realTimeTruckToPickup - calculatedTimeTruckToPickup;
	}
	
	// negative results mean that the truck was faster than calculated, positive results mean that the truck was slower than calculated
	public long getDeliveryTardiness(Proposal bestProposal, long realTimePickupToDelivery){
		long calculatedTimePickupToDelivery = bestProposal.getPickupToDelivery();
		return realTimePickupToDelivery - calculatedTimePickupToDelivery;
	}
	
	public long getPickupDeliveryTardiness(Proposal bestProposal, long realTimeTruckToPickupToDelivery){
		long calculatedTimeTruckToPickupToDelivery = bestProposal.getTimeCostProposal();
		return realTimeTruckToPickupToDelivery - calculatedTimeTruckToPickupToDelivery;
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(auction);
		sb.append("\n"); 
		sb.append(" auction winner is truckagent ");
		sb.append(winner);
		sb.append("\n"); 
		sb.append(" auction duration: ");
		sb.append(auctionDuration);
		sb.append("\n"); 
		sb.append("real time between needed for truck to move to parcel for pickup: ");
		sb.append(realTimeTruckToPickup);
		sb.append("\n"); 
		sb.append("truck tardiness to move to parcel for pickup: ");
		sb.append(getPickupTardiness(bestProposal, realTimeTruckToPickup));
		sb.append("\n"); 
		sb.append("real time between pickup and delivery: ");
		sb.append(realTimePickupToDelivery);
		sb.append("\n"); 
		sb.append("truck tardiness between pickup and delivery: ");
		sb.append(realTimePickupToDelivery);
		sb.append("\n"); 
		sb.append("real time needed by truck for full pickup and delivery task: ");
		sb.append(getDeliveryTardiness(bestProposal, realTimePickupToDelivery));
		sb.append("\n"); 
		sb.append("total overtime for full pickup and delivery task: ");
		sb.append(getPickupDeliveryTardiness(bestProposal, realTimeTruckToPickupToDelivery));
		sb.append("\n"); 
		sb.append("real time between call for proposals and delivery: ");
		sb.append(realTimeCFPDelivery);
		sb.append("\n"); 
		sb.append("list of rejected proposals by dispatch agent: ");
		sb.append(rejectedProposals);

		return sb.toString();
		//TODO: other things to add to the stats output: -	Accepted parcels, Total pickups, Total deliveries, Total distance, Simulation time, Cost function, Total messages
	}
	
}
