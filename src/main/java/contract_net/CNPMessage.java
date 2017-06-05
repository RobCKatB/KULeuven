package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CNPMessage implements MessageContents {

	private ContractNetMessageType type;
	private Auction auction; // parcel to be picked up by a Truck, auction initiated by DispatchAgent
	private Optional<CommDevice> commDevice;
	private CommUser sender;

	// or Auction instead of Parcel as parameter for constructor?
	public CNPMessage(Auction auction, ContractNetMessageType type, CommUser sender){
		commDevice = Optional.absent(); // commDevice contains unreadMessages and outbox
		this.auction = auction;
		this.type = type;
		this.sender = sender;
	}
	
	
	public void setType(ContractNetMessageType type) {
		this.type = type;
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
	  //public boolean isBroadcast() {
	  //  return !to().isPresent();
	  //}
	 


	  @Override
	  public String toString() {
	    return MoreObjects.toStringHelper("Message")
	      .add("contents", getContents())
	      .toString();
	  }

	
	CommUser from() {
		return sender;
	}
	
	/*
	Optional<CommUser> to(){
		System.out.println("this is a broadcast message");
	}
	*/

	MessageContents contents() {
		return null;
	}

	Predicate<CommUser> predicate() {
		return null;
	}

}