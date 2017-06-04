package contract_net;

import java.util.HashMap;


public class AuctionWinnerOverview {

	private HashMap<AuctionResult, TruckAgent> auctionWinnerMap;
	
	public AuctionWinnerOverview(){
		auctionWinnerMap = new HashMap<AuctionResult, TruckAgent>();
	}
	
	public void addAuctionWinnerPair(AuctionResult auctionResult, TruckAgent winner){
		auctionWinnerMap.put(auctionResult, winner);
	}

}
