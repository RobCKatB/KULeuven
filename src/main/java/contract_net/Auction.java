package contract_net;

import com.github.rinde.rinsim.core.model.comm.Message;

public class Auction {
	
	DispatchAgent dispatchAgent;
	Parcel parcel;
	
	
	public Auction(DispatchAgent dispatchAgent, Parcel parcel){
		this.dispatchAgent = dispatchAgent;
		this.parcel = parcel;	
	}
	
	public void startAuction(DispatchAgent dispatchAgent){
		Message m = new Message();
		CNPMessage cnpm = new CNPMessage();
		dispatchAgent.sendBroadcastMessage(content);
	}

}
