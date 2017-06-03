package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CNPMessage implements MessageContents {

	private ContractNetMessageType type;
	private Parcel parcel; // parcel to be picked up by a Truck, auction initiated by DispatchAgent
	private Optional<CommDevice> commDevice;

	// or Auction instead of Parcel as parameter for constructor?
	public CNPMessage(Parcel parcel, ContractNetMessageType type){
		commDevice = Optional.absent(); // commDevice contains unreadMessages and outbox
		this.parcel = parcel;
		this.type = type;
	}
	
	
	public void setType(ContractNetMessageType type) {
		this.type = type;
	}


	public Parcel getParcel() {
		return parcel;
	}


	public void setParcel(Parcel parcel) {
		this.parcel = parcel;
	}


	public ContractNetMessageType getType() {
		return type;
	}


	public void setContractNetMessageType(ContractNetMessageType type){
		this.setType(type);
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
	      .add("contents", getContents())
	      .toString();
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