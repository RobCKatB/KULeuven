package contract_net;

import java.util.List;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPRejectMessage extends CNPMessage {

	private String rejectionReason;
	private List<CommUser> receivers;
	
	public CNPRejectMessage(Auction auction, ContractNetMessageType type, CommUser sender, List<CommUser> receivers, String rejectionReason) {
		super(auction, type, sender);
		this.rejectionReason = rejectionReason;
		this.receivers = receivers;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
	
	Optional<List<CommUser>> to(){
		return Optional.of(receivers);
	}
}
