package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.google.common.base.Optional;

public class CNPRefusalMessage extends CNPMessage {

	private Auction auction;
	private ContractNetMessageType type;
	private String refusalReason;
	private CommUser sender;
	private CommUser receiver;
	
	public CNPRefusalMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, String refusalReason) {
		super(auction, type, sender);
		this.refusalReason = refusalReason;
		this.sender = sender;
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
	Optional<CommUser> to(){
		return Optional.of(receiver);
	}
}
