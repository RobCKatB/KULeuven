package contract_net;

// we are calling methods from com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.LoggerFactory;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Container;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;



public class DispatchAgent implements CommUser, TickListener {
	

	//agent is represented as a finite state machine
	private int state = 0;
	
	private DefaultPDPModel defaultpdpmodel;
	// stillToBeAssignedParcels uit simulator halen want parcels zijn geregistreerd in de simulator
	private Collection<Parcel> toBeDispatchedParcels;
	private List<CNPMessage> messages = new ArrayList<CNPMessage>();
	private List<Message> unreadMessages = new ArrayList<Message>();
	//list of potential VehicleAgent contractors
	private List<TruckAgent> potentialContractors = new ArrayList<TruckAgent>();
	
	// deze twee moeten in veiling
	private List<TruckAgent> lostContractors = new ArrayList<TruckAgent>();
	private TruckAgent winningContractor = null;
	private ArrayList<CommUser> commUsers = new ArrayList<CommUser>(); // TruckAgent commUsers coupled to DispatchAgent
	
	//used to record the number of received messages
	//in this version, we impose the manager to wait till receiving answers from all the contractors
	private int numberOfreceivedMessages = 0;
	//record the best proposal
	private int bestProposal = Integer.MAX_VALUE;
	//record the agent responsible for the best proposal
	private Optional<CommDevice> commDevice;
	// settings of commDevice
	  long lastReceiveTime;
	  private final double range;
	  private final double reliability;
	  static final double MIN_RANGE = .2;
	  static final double MAX_RANGE = 1.5;
	  static final long LONELINESS_THRESHOLD = 10 * 1000;
	  private final RandomGenerator rng;
	  private CNPMessage cnpmessage;
	
		public DispatchAgent(DefaultPDPModel defaultpdpmodel) {
			this.defaultpdpmodel = defaultpdpmodel;// defined in the main
			toBeDispatchedParcels = new ArrayList<Parcel>();
			commDevice = Optional.absent();
			// settings for commDevice belonging to DispatchAgent
		    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
		    reliability = rng.nextDouble();
		}
	
	/*
	public DispatchAgent(List<VehicleAgent> potentialContractors, List<CNPMessage> messages, List<Parcel> toBeDispatchedParcels) {
		this.potentialContractors = potentialContractors;
		this.messages = messages;
		this.toBeDispatchedParcels = toBeDispatchedParcels;
		commDevice = Optional.absent();
	}
	*/
	
	// which parcels have to be dispatched to the different truckAgents?
	public Collection<Parcel> getANNOUNCEDParcels(){
		return defaultpdpmodel.getParcels(ParcelState.ANNOUNCED);
	}
		
	public Collection<Parcel> getAVAILABLEParcels(){
		return defaultpdpmodel.getParcels(ParcelState.AVAILABLE);
	}
	
	public Collection<Parcel> getToBeDispatchedParcels(){
		toBeDispatchedParcels = getANNOUNCEDParcels();
		toBeDispatchedParcels.addAll(getAVAILABLEParcels());
		return toBeDispatchedParcels;
	}
	
	// which truckAgents are available to perform a task?
	public Set<Vehicle> getTruckAgents(){
		return defaultpdpmodel.getVehicles();
	}
	
	public VehicleState getTruckAgentState(TruckAgent truckAgent){
		Truck t = truckAgent.getTruck();
		return defaultpdpmodel.getVehicleState(t);
	}
	
	// get the commUser/commDevice pairs that are coupled; commModel is defined in main
	public ImmutableBiMap<CommUser, CommDevice> getCommUserDevice(CommModel commModel){
		ImmutableBiMap<CommUser, CommDevice> commUsersDevices= commModel.getUsersAndDevices();
		return commUsersDevices;
	}
	
	// the dispatchAgent has to communicate with commUsers from the class TruckAgent
	// this method retrieves all the TruckAgent registered to the CommModel of our Simulator
	public ArrayList<CommUser> getTruckAgentCommUsers(CommModel commModel){
		ImmutableBiMap<CommUser, CommDevice> commUsersDevices=getCommUserDevice(commModel);
		// loop through BiMap and collect all CommUsers belonging to the DispatchAgent commDevice in one ArrayList
		CommUser commUser = null;
		for (ImmutableBiMap.Entry<CommUser, CommDevice> entryCommDevice: commUsersDevices.entrySet()) {
			commUser = commUsersDevices.inverse().get(entryCommDevice);
		}
		commUsers.add(commUser);
	}

	// if the dispatch agent wants to communicate with all other commUsers
	// CNPMessage contains info about the Message and the ContractNetMessageType
	public void sendBroadcastMessage(CNPMessage content){
    	if (!this.commDevice.isPresent()) {
        	throw new IllegalStateException("No commdevice activated for this dispatch agent");
        	}
    	CommDevice device = this.commDevice.get();
		device.broadcast(content);
	}

    // if the dispatch agent wants to communicate to only one other CommUser, i.e. one specific truck
    public void sendDirectMessage(CNPMessage content, CommUser recipient) {
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
		range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
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
	
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {};


}
