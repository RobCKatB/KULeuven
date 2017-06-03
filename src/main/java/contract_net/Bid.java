package contract_net;

import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Bid {
	private Parcel parcel;
	private TruckAgent bidder;
	private Auction auction;
	
	public Bid(Auction auction, Parcel parcel, TruckAgent bidder, long timeCostBid){
		this.auction = auction;
		this.parcel = parcel;
		this.bidder = bidder;		
		timeCostBid = setTimeCostBid();
	}
	
	// calculate travel time for this bidder for the current auction
	public long setTimeCostBid(){
		return bidder.calculateTravelTime(bidder.getPosition(), parcel);
	}
	
}
