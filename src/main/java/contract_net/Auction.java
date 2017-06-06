package contract_net;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.DateTime;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Auction {
    private static AtomicInteger uniqueId=new AtomicInteger();
    private int auctionId;
	private DispatchAgent dispatchAgent;
	private Parcel parcel;
	private long auctionDuration;
	private boolean activeAuction;
	private long startTime;

	public Auction(DispatchAgent dispatchAgent, Parcel parcel, long startTime, long deadline, boolean activeAuction){
		this.auctionId = generateAuctionId();
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
		this.startTime = startTime;
		this.auctionDuration = deadline;
		this.activeAuction = activeAuction;
	}
	
	public int getId(){
		return auctionId;
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


	public long getAuctionDuration() {
		return auctionDuration;
	}


	public void setAuctionDuration(long deadline) {
		this.auctionDuration = deadline;
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
	
	public boolean isActiveAuction() {
		return activeAuction;
	}


	public void setActiveAuction(boolean activeAuction) {
		this.activeAuction = activeAuction;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}
	
	@Override
	public String toString() {
		 StringBuilder builder = new StringBuilder("Auction|")
				 	.append(auctionId)
				 	.append("[")
					.append(getDispatchAgent())
					.append(",")
					.append(getParcel())
					.append(",")
					.append("starttime: ").append(getStartTime())
					.append(",")
					.append("duration: ").append(getAuctionDuration())
					.append(",");
		 
		if(isActiveAuction()){
			builder.append("active");
		}else{
			builder.append("inachtive");
		}

		builder.append("]");
			  
		return builder.toString();
	}
}
