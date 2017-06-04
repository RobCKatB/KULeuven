package contract_net;
//KB
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.DateTime;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Auction {
    private static AtomicInteger uniqueId=new AtomicInteger();
    private int auctionId;
	private DispatchAgent dispatchAgent;
	private Parcel parcel;
	private long deadline;
	private boolean activeAuction;

	
	
	public boolean isActiveAuction() {
		return activeAuction;
	}


	public void setActiveAuction(boolean activeAuction) {
		this.activeAuction = activeAuction;
	}


	public Auction(DispatchAgent dispatchAgent, Parcel parcel, long deadline, boolean activeAuction){
		this.auctionId = generateAuctionId();
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
		this.deadline = deadline;
		this.activeAuction = activeAuction;
	}
	
	
	public DispatchAgent getDispatchAgent() {
		return dispatchAgent;
	}


	public void setDispatchAgent(DispatchAgent dispatchAgent) {
		this.dispatchAgent = dispatchAgent;
	}


	public Parcel getParcel() {
		return parcel;
	}


	public void setParcel(Parcel parcel) {
		this.parcel = parcel;
	}


	public long getDeadline() {
		return deadline;
	}


	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}


	public int getAuctionId() {
		return auctionId;
	}


	public int generateAuctionId() {
		return uniqueId.getAndIncrement();
	}


	public DispatchAgent getSenderAuction(){
		return dispatchAgent;
	}
	
}
