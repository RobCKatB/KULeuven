package contract_net;
//
import java.util.ArrayList;
import java.util.List;

public class Auctions {
	
	private Auction auction;
	private List<Auction> auctions;
	
	public Auctions(List<Auction> auctions){
		auctions = new ArrayList<Auction>();
	}
	
	public void addAuction(Auction auction){
		auctions.add(auction);
	}
	
	public void getActiveAuctions(List<Auction> auctions){
		List<Auction> activeAuctions = new ArrayList<Auction>();
		List<Auction> inactiveAuctions = new ArrayList<Auction>();
		for(Auction a: auctions){
			if (a.isActiveAuction()){
				activeAuctions.add(a);
			}
			else {
				inactiveAuctions.add(a);
			}
		}
	}

}
