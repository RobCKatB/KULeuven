package contract_net;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.DateTime;
import com.github.rinde.rinsim.core.model.comm.Message;

public class Auction {
    private static AtomicInteger uniqueId=new AtomicInteger();
    private int auctionId;
	private DispatchAgent dispatchAgent;
	private Package parcel;
	private long durationAuction;

	
	
	public Auction(int auctionID, DispatchAgent dispatchAgent, Package parcel, long durationAuction){
		auctionID=uniqueId.getAndIncrement();
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
		this.durationAuction = durationAuction;

	}
	
	
	public DispatchAgent getSenderAuction(){
		return dispatchAgent;
	}


}
