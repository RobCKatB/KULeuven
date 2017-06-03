package contract_net;

import java.util.HashMap;

import com.github.rinde.rinsim.core.model.comm.CommDevice;

public class AuctionWinnerOverview {
	private AuctionResult auctionResult;
	private TruckAgent winner;
	private HashMap<AuctionResult, TruckAgent> auctionWinnerMap;
	
	public AuctionWinnerOverview(){
		auctionWinnerMap = new HashMap<AuctionResult, TruckAgent>();
	}
	
	public void addAuctionWinnerPair(AuctionResult auctionResult, TruckAgent winner){
		auctionWinnerMap.put(auctionResult, winner);
	}

}
