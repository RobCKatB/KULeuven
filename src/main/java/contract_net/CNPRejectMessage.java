package contract_net;

import java.util.List;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPRejectMessage extends CNPMessage {

	private String rejectionReason;
	private CommUser receiver;
	
	public CNPRejectMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String rejectionReason, long timeSent) {
		super(auction, type, sender, timeSent);
		this.rejectionReason = rejectionReason;
		this.receiver = receiver;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
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
