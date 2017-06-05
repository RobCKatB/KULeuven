package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public class CNPInformDoneMessage extends CNPMessage {

	public CNPInformDoneMessage(Auction auction, ContractNetMessageType type, CommUser sender) {
		super(auction, type, sender);
		// TODO ?? carry information about ParcelState.DELIVERED
	}
	
	

}
