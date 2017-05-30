package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CNPMessage implements MessageContents {


	private ContractNetMessageType type;
	
	public CNPMessage(ContractNetMessageType type){
		this.type = type;
	}
	
	  /**
	   * @return The {@link CommUser} that send this message.
	   */
	  public CommUser getSender() {
	    return from();
	  }

	  /**
	   * @return The {@link MessageContents} that this message contains.
	   */
	  public MessageContents getContents() {
	    return contents();
	  }

	  /**
	   * @return <code>true</code> if this message is broadcast, or
	   *         <code>false</code> if this message is a direct message.
	   */
	  public boolean isBroadcast() {
	    return !to().isPresent();
	  }


	  @Override
	  public String toString() {
	    return MoreObjects.toStringHelper("Message")
	      .add("sender", getSender())
	      .add("contents", getContents())
	      .toString();
	  }


	public ContractNetMessageType getType() {
		return type;
	}



	public void setType(ContractNetMessageType type) {
		this.type = type;
	}
	
	CommUser from() {
		return null;
	}

	Optional<CommUser> to() {
		return null;
	}

	MessageContents contents() {
		return null;
	}

	Predicate<CommUser> predicate() {
		return null;
	}

}
