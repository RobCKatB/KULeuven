package contract_net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.DateTime;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Auction {
    private static AtomicInteger uniqueId=new AtomicInteger();
    private int auctionId;
	private DispatchAgent dispatchAgent;
	private Parcel parcel;
	private long duration;
	private boolean active;
	private long startTime;
	private ArrayList<Proposal> proposals = new ArrayList<Proposal>();
	private ArrayList<Proposal> tooLateProposals = new ArrayList<Proposal>();

	public Auction(DispatchAgent dispatchAgent, Parcel parcel, long startTime, long deadline, boolean activeAuction){
		this.auctionId = generateAuctionId();
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
		this.startTime = startTime;
		this.duration = deadline;
		this.active = activeAuction;
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


	public long getDuration() {
		return duration;
	}


	public void setAuctionDuration(long deadline) {
		this.duration = deadline;
	}


	public int getAuctionId() {
		return auctionId;
	}


	public int generateAuctionId() {
		return uniqueId.getAndIncrement();
	}
	
	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
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
	
	public void addProposal(Proposal proposal, long time) {
		if(!this.isExpired(time)){
			this.proposals.add(proposal);
		}else{
			this.tooLateProposals.add(proposal);
		}
	}
	
	public ArrayList<Proposal> getProposals() {
		return proposals;
	}

	public ArrayList<Proposal> getTooLateProposals() {
		return tooLateProposals;
	}
	
	public boolean isExpired(long time) {
		return time - this.startTime > this.duration;
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
					.append("duration: ").append(getDuration())
					.append(",");
		 
		if(isActive()){
			builder.append("active");
		}else{
			builder.append("inachtive");
		}

		builder.append("]");
			  
		return builder.toString();
	}
}
