package contract_net;


import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPCancelMessage extends CNPMessage {

	private String cancelReason;
	private CommUser receiver;
	
	public CNPCancelMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String cancelReason, long timeSent) {
		super(auction, type, sender, timeSent);
		this.cancelReason = cancelReason;
	}
	
	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancalReason(String refusalReason) {
		this.cancelReason = refusalReason;
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
	
	public String toString() {
		String cnpMessage = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(cnpMessage);
		sb.append(" Reason for cancelling this auction : " );
		sb.append(cancelReason);
		return sb.toString();
	}
}
