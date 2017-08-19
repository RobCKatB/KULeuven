package contract_net;

import java.util.ArrayList;
import java.util.List;

public class AuctionResults {
	
	private List<AuctionResult> auctionResults;
	
	public AuctionResults(){
		auctionResults = new ArrayList<AuctionResult>();
	}

	public List<AuctionResult> getAuctionResults() {
		return auctionResults;
	}

	public void setAuctionResults(List<AuctionResult> auctionResults) {
		this.auctionResults = auctionResults;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(AuctionResult ar : auctionResults){
			sb.append(ar);
			sb.append("\n"); 
		}
		return sb.toString();
	}
}
