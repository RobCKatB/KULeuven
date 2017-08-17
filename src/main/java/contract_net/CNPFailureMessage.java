package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPFailureMessage extends CNPMessage {
	
	private String reasonForFailure;
	private CommUser receiver;
	private long timeSent;
	private Auction auction;
	private CommUser sender;


	public CNPFailureMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String reasonForFailure, long timeSent) {
		super(auction, type, sender, timeSent);
		this.reasonForFailure = reasonForFailure;
		this.receiver = receiver;
	}

	public String getReasonForFailure() {
		return reasonForFailure;
	}

	public void setReasonForFailure(String reasonForFailure) {
		this.reasonForFailure = reasonForFailure;
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

	public long getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(long timeSent) {
		this.timeSent = timeSent;
	}
	
	public String toString(){
		String cnpMessage = super.toString();
		StringBuffer sb = new StringBuffer();
		sb.append(cnpMessage);
		sb.append("; FAILURE message received by ");
		sb.append(receiver);
		sb.append(": Truckagent ");
		sb.append(sender);
		sb.append(" has picked up and delivered parcel ");
		sb.append(auction.getParcel());
		return sb.toString();
	}
}
