package contract_net;

import org.eclipse.swt.widgets.DateTime;

import com.github.rinde.logistics.pdptw.mas.comm.AuctionStopCondition;
import com.github.rinde.rinsim.core.model.comm.Message;

public class Auction {
	
	private DispatchAgent dispatchAgent;
	private Package parcel;
	private DateTime durationAuction;
	////aanpassen
	final AuctionStopCondition<T> stopCondition;
	
	
	public Auction(DispatchAgent dispatchAgent, Package parcel, DateTime durationAuction){
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
		this.durationAuction = durationAuction;
	}
	
	public void startAuction(DispatchAgent dispatchAgent, Package parcel){
		CNPMessage cnpm = new CNPMessage();
		dispatchAgent.sendBroadcastMessage(content);
	}
	
	public DispatchAgent getSenderAuction(){
		return dispatchAgent;
	}


}
