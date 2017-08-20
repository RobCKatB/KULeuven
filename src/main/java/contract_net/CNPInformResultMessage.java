package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class CNPInformResultMessage extends CNPMessage {

	private CommUser receiver;
	private long timePickupToDelivery;
	private long timeTruckToPickup;
	private long timeCFPToDelivery;
	private long timeTruckToPickupToDelivery;

	public CNPInformResultMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, long timeTruckToPickup, long timePickupToDelivery, long timeTruckToPickupToDelivery, long timeCFPToDelivery, long timeSent) {
		super(auction, type, sender, timeSent);
		this.receiver=receiver;
		this.timeTruckToPickup = timeTruckToPickup;
		this.timePickupToDelivery=timePickupToDelivery;
		this.timeTruckToPickupToDelivery=timeTruckToPickupToDelivery;
		this.timeCFPToDelivery=timeCFPToDelivery;
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
	
	public long getTimeTruckToPickup() {
		return timeTruckToPickup;
	}

	public void setTimeTruckToPickup(long timeTruckToPickup) {
		this.timeTruckToPickup = timeTruckToPickup;
	}

	
	public long getTimeTruckToPickupToDelivery() {
		return timeTruckToPickupToDelivery;
	}

	public void setTimeTruckToPickupToDelivery(long timeTruckToPickupToDelivery) {
		this.timeTruckToPickupToDelivery = timeTruckToPickupToDelivery;
	}

	public String toString(){
		String cnpMessage = super.toString();
		StringBuffer sb = new StringBuffer();
		sb.append(cnpMessage);
		sb.append("; INFORM RESULT message received by ");
		sb.append(receiver);
		sb.append(" has picked up and delivered parcel ");
		sb.append(super.getAuction().getParcel());
		sb.append(" with a time between pickup and delivery of ");
		sb.append(timePickupToDelivery);
		sb.append(", a time between call for proposal and delivery of ");
		sb.append(timeTruckToPickup);
		sb.append(", and a time between call for proposal and delivery of ");
		sb.append(timeCFPToDelivery);
		return sb.toString();
	}

}
