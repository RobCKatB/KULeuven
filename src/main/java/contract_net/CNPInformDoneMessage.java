package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class CNPInformDoneMessage extends CNPMessage {
	private long timeSent;


	public CNPInformDoneMessage(Auction auction, ContractNetMessageType type, CommUser sender, long timeSent) {
		super(auction, type, sender, timeSent);
		// TODO ?? carry information about ParcelState.DELIVERED
	}


	public long getTimeSent() {
		return timeSent;
	}


	public void setTimeSent(long timeSent) {
		this.timeSent = timeSent;
	}
	
}
