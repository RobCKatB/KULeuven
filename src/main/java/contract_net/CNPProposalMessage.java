package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPProposalMessage extends CNPMessage {
	
	private Proposal proposal;
	private CommUser receiver;

	public CNPProposalMessage(Auction auction, ContractNetMessageType type, Proposal proposal, CommUser sender, CommUser receiver, long timeSent) {
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


	public CommUser getReceiver() {
		return receiver;
	}

	public void setReceiver(CommUser receiver) {
		this.receiver = receiver;
	}
	
	Optional<CommUser> to(){
		return Optional.of(receiver);
	}

	public String toString() {
		String cnpMessage = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(cnpMessage);
		sb.append("Receiver of message: " );
		sb.append(receiver);
		sb.append("Proposal as reaction on CALL_FOR_PROPOSAL: " );
		sb.append(proposal);
		return sb.toString();
	}
}
