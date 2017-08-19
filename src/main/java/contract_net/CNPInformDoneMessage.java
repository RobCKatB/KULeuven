package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class CNPInformDoneMessage extends CNPMessage {
	private CommUser receiver;

	public CNPInformDoneMessage(Auction auction, ContractNetMessageType type, CommUser sender, CommUser receiver, long timeSent) {
		super(auction, type, sender, timeSent);
		// TODO ?? carry information about ParcelState.DELIVERED
		this.receiver = receiver;
	}


	public String toString(){
		String cnpMessage = super.toString();
		StringBuffer sb = new StringBuffer();
		sb.append(cnpMessage);
		sb.append("receiver "+ receiver);
		sb.append("; INFORM DONE message: Truckagent ");
		sb.append(super.getSender());
		sb.append(" has picked up and delivered parcel ");
		sb.append(super.getAuction().getParcel());
		return sb.toString();
	}
}
