package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CNPMessage implements MessageContents {

	private Message message;
	private ContractNetMessageType type;
	private MessageContents messageContents;
	private Parcel parcel; // parcel to be picked up
	private Optional<CommDevice> commDevice;

	public CNPMessage(Message message, Parcel parcel, ContractNetMessageType type){
		commDevice = Optional.absent();
		this.message = message;
		this.parcel = parcel;
		this.type = type;
	}
	
	
	public void setContractNetMessageType(ContractNetMessageType type){
		this.setType(type);
	}
	
	
	  /**
	   * @return The {@link CommUser} that send this message.
	   */
	  public CommUser getSender(Message m) {
	    return message.getSender();
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


	public ContractNetMessageType getType(ContractNetMessageType type) {
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