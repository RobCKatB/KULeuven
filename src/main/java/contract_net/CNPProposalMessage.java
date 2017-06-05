package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

//
public class CNPProposalMessage extends CNPMessage {
	
	private Auction auction;
	private ContractNetMessageType type;
	private Proposal proposal;
	private CommUser sender;
	private CommUser receiver;

	public CNPProposalMessage(Auction auction, ContractNetMessageType type, Proposal proposal, CommUser sender, CommUser receiver) {
		super(auction, type, sender);
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
}
