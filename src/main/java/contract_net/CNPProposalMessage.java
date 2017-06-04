package contract_net;
//
public class CNPProposalMessage extends CNPMessage {
	
	private Auction auction;
	private ContractNetMessageType type;
	private Proposal proposal;

	public CNPProposalMessage(Auction auction, ContractNetMessageType type, Proposal proposal) {
		super(auction, type);
		this.proposal = proposal;
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


	

}
