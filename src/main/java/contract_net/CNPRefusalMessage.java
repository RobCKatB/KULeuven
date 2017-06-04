package contract_net;
//
public class CNPRefusalMessage extends CNPMessage {

	private Auction auction;
	private ContractNetMessageType type;
	private String refusalReason;
	
	
	public CNPRefusalMessage(Auction auction, ContractNetMessageType type, String refusalReason) {
		super(auction, type);
		this.refusalReason = refusalReason;
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

	public String getRefusalReason() {
		return refusalReason;
	}

	public void setRefusalReason(String refusalReason) {
		this.refusalReason = refusalReason;
	}

}
