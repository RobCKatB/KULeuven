package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPAcceptMessage extends CNPMessage {
	private Proposal proposal;
	private CommUser receiver;
	private long timeSent;

	public CNPAcceptMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, Proposal proposal, long timeSent) {
		super(auction, type, sender, timeSent);
		this.proposal = proposal;
		this.receiver = receiver;
	}

	public Proposal getProposal() {
		return proposal;
	}

	public void setProposal(Proposal proposal) {
		this.proposal = proposal;
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
}
