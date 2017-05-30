package contract_net;

import com.github.rinde.rinsim.core.model.comm.MessageContents;

public enum ContractNetMessageType implements MessageContents {

		
		CALL_FOR_PROPOSAL,
		REFUSE,
		PROPOSE,
		REJECT_PROPOSAL,
		ACCEPT_PROPOSAL,
		FAILURE,
		INFORM_RESULT,
		INFORM_DONE;

}
