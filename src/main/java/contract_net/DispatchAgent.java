package contract_net;

// code inspired by com.github.rinde.rinsim.examples.comm/RandomBroadcastAgent.class

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.LoggerFactory;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.Optional;

import communication.TypedMessage;



public class DispatchAgent implements CommUser, TickListener {
	

	//agent is represented as a finite state machine
	private int state = 0;
	
	// stillToBeAssignedParcels uit simulator halen want parcels zijn geregistreerd in de simulator
	private List<Parcel> stillToBeAssignedParcels = new ArrayList<Parcel>();
	private List<CNPMessage> messages = new ArrayList<CNPMessage>();
	private List<Message> unreadMessages = new ArrayList<Message>();
	//list of potential VehicleAgent contractors
	private List<VehicleAgent> potentialContractors = new ArrayList<VehicleAgent>();
	private List<VehicleAgent> lostContractors = new ArrayList<VehicleAgent>();
	private VehicleAgent winningContractor = null;
	//used to record the number of received messages
	//in this version, we impose the manager to wait till receiving answers from all the contractors
	private int numberOfreceivedMessages = 0;
	//record the best proposal
	private int bestProposal = Integer.MAX_VALUE;
	//record the agent responsible for the best proposal
	private Optional<CommDevice> commDevice;
	  long lastReceiveTime;
	  private final double range;
	  private final double reliability;
	  static final double MIN_RANGE = .2;
	  static final double MAX_RANGE = 1.5;
	  static final long LONELINESS_THRESHOLD = 10 * 1000;
	  private final RandomGenerator rng;
	  private CNPMessage cnpmessage;
	
	
	public DispatchAgent(RandomGenerator r, List<VehicleAgent> potentialContractors, List<CNPMessage> messages, List<Parcel> stillToBeAssignedParcels, Optional<CommDevice> commDevice) {
		this.potentialContractors = potentialContractors;
		this.messages = messages;
		this.stillToBeAssignedParcels = stillToBeAssignedParcels;
		this.commDevice = Optional.absent();
		
		rng = r;
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
	}
	
	//// methodes zijn een samenraapsel van anderen, moet nog herschreven worden////


	public void addParcel(Parcel p){
		stillToBeAssignedParcels.add(p);
	}

	// if the dispatch agent wants to communicate with all other commUsers, i.e. all trucks	
    public void sendBroadcastMessage(CommUser from, ArrayLis<CommUser> to, CNPMessage content) {   	
        if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for the phone app");}
        CommDevice device = this.commDevice.get();
        commDevice.broadcast(content, recipient);
    }
	
    
    // if the dispatch agent wants to communicate to only one other CommUser, i.e. one specific truck
    public void sendDirectMessage(CNPMessage content, CommUser from, CommUser recipient) {
        if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for the phone app");}
        CommDevice device = this.commDevice.get();
        device.send(content, recipient);
    }

	   
  
	// CommUser methods implemented
	@Override
	// not needed for us since the dispatch agent is a phone app, so it has not one position at a certain physical depot
	public Optional<Point> getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		//    if (range >= 0) {
	    if (range >= 0) {
	        builder.setMaxRange(range);
	      }
	      commDevice = Optional.of(builder
	        .setReliability(reliability)
	        .build());
	    }
	
	
	// thicklistener methods implemented
	@Override
	  public void tick(TimeLapse timeLapse) {
		
		/* this part left away since our dispatch agent is a mobile app, so without fixed location
	    if (!destination.isPresent()) {
	      destination = Optional.of(roadModel.get().getRandomPosition(rng));
	    }
	    roadModel.get().moveTo(this, destination.get(), timeLapse);
	    if (roadModel.get().getPosition(this).equals(destination.get())) {
	      destination = Optional.absent();
	    }
	    */
		
	    if (commDevice.get().getUnreadCount() > 0) {
	      lastReceiveTime = timeLapse.getStartTime();
	      unreadMessages = commDevice.get().getUnreadMessages();
	      
	      
			for (Message m : unreadMessages) {
				
				// here I want the enum from ContractNetMessageType
				lostContractors.clear();
				
				
					switch (m.getType()) {

						case CALL_FOR_PROPOSAL:
							sendBroadcastMessage(m, potentialContractors)
							break;
						case ACCEPT_PROPOSAL:
							// maak nog een auction klasse of methode
							winningContractor = m.getWinner;
							if(winningContractor != null)
								sendDirectMessage(m, winningContractor);
							break;
						case REJECT_PROPOSAL:
							lostContractors = potentialContractors.remove(winningContractor);
							sendBroadcastMessage(message.LOST, lostContractors);
							break;
						default:
							break;
					}
				}

			}
	      commDevice.get().broadcast(Messages.NICE_TO_MEET_YOU);
	    } else if (commDevice.get().getReceivedCount() == 0) {
	    	//do nothign;
	    } else if (timeLapse.getStartTime()
	      - lastReceiveTime > LONELINESS_THRESHOLD) {
	      device.get().rebroadcast(Message);
	    }
	  }


	@Override
	public void afterTick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
		
	}
	    device = Optional.of(builder
	      .setReliability(reliability)
	      .build());
		
	}


}
