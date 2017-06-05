package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPFailureMessage extends CNPMessage {
	
	private String reasonForFailure;
	private CommUser receiver;


	public CNPFailureMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String reasonForFailure) {
		super(auction, type, sender);
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
}
