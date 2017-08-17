package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPProposalMessage extends CNPMessage {
	
	private Auction auction;
	private ContractNetMessageType type;
	private Proposal proposal;
	private CommUser sender;
	private CommUser receiver;
	private long timeSent;

	public CNPProposalMessage(Auction auction, ContractNetMessageType type, Proposal proposal, CommUser sender, CommUser receiver, long timeSent) {
		super(auction, type, sender, timeSent);
		this.proposal = proposal;
		this.receiver = receiver;
	}
	
	public Auction getAuction() {
		return auction;
	}

	public void setAuction(Auction auction) {
		this.auction = auction;
	}

	public ContractNetMessageType getType() {
		return type;
	}

	public void setType(ContractNetMessageType type) {
		this.type = type;
	}

	public Proposal getProposal() {
		return proposal;
	}

	public void setProposal(Proposal proposal) {
		this.proposal = proposal;
	}

	public CommUser getSender() {
		return sender;
	}

	public void setSender(CommUser sender) {
		this.sender = sender;
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

	public long getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(long timeSent) {
		this.timeSent = timeSent;
	}
	

	public String toString() {
		String cnpMessage = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("CNPMessage: " );
		sb.append(cnpMessage);
		sb.append("Receiver of message: " );
		sb.append(receiver);
		sb.append("Proposal as reaction on CALL_FOR_PROPOSAL: " );
		sb.append(proposal);
		return sb.toString();
	}
}
