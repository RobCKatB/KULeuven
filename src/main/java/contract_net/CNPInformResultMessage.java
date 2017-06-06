package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class CNPInformResultMessage extends CNPMessage {

	private Auction auction;
	private ContractNetMessageType type;
	private CommUser sender;
	private CommUser receiver;
	private long timePickupToDelivery;
	private long timeCFPToDelivery;
	private long timeSent;
	
	public CNPInformResultMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, long timePickupToDelivery, long timeCFPToDelivery, long timeSent) {
		super(auction, type, sender, timeSent);
		this.receiver=receiver;
		this.timePickupToDelivery=timePickupToDelivery;
		this.timeCFPToDelivery=timeCFPToDelivery;
		this.timeSent=timeSent;
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

	public long getTimePickupToDelivery() {
		return timePickupToDelivery;
	}

	public void setTimePickupToDelivery(long timePickupToDelivery) {
		this.timePickupToDelivery = timePickupToDelivery;
	}

	public long getTimeCFPToDelivery() {
		return timeCFPToDelivery;
	}

	public void setTimeCFPToDelivery(long timeCFPToDelivery) {
		this.timeCFPToDelivery = timeCFPToDelivery;
	}

	public long getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(long timeSent) {
		this.timeSent = timeSent;
	}
	

}
