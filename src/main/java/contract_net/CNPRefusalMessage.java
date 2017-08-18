package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPRefusalMessage extends CNPMessage {

	private String refusalReason;
	private CommUser receiver;
	
	public CNPRefusalMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String refusalReason, long timeSent) {
		super(auction, type, sender, timeSent);
		this.refusalReason = refusalReason;
	}
	
	public String getRefusalReason() {
		return refusalReason;
	}

	public void setRefusalReason(String refusalReason) {
		this.refusalReason = refusalReason;
	}
	Optional<CommUser> to(){
		return Optional.of(receiver);
	}


	public CommUser getReceiver() {
		return receiver;
	}

	public void setReceiver(CommUser receiver) {
		this.receiver = receiver;
	}
}
