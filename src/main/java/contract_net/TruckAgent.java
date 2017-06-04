// finalize auction stop criterion, followed by award and then end of auction
//
package contract_net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;


public class TruckAgent extends Vehicle implements CommUser, MovingRoadUser {
	private static final Logger LOGGER = LoggerFactory
			    .getLogger(RouteFollowingVehicle.class);
	private Queue<Point> path;
	private Optional<CommDevice> commDevice;
	private Optional<RoadModel> roadModel;
	private Optional<Parcel> currParcel;
	private int capacity;
	private Point startPosition;
    private double energy;
    private List<Proposal> proposals = new ArrayList<Proposal>();
    private boolean isCharging;
    private List<CNPMessage> unreadMessages;
	private long lastReceiveTime;
	private DefaultPDPModel defaultpdpmodel;

	private static final double SPEED = 1000d;
	private static final double ENERGYCONSUMPTION = 1d; // Per unit mileage
	private static final double ENERGYCAPACITY = 1000d;
	// for CommUser
	  private final double range;
	  private final double reliability;
	  static final double MIN_RANGE = .2;
	  static final double MAX_RANGE = 1.5;
	  static final long LONELINESS_THRESHOLD = 10 * 1000;
	  private final RandomGenerator rng;

	
	public TruckAgent(DefaultPDPModel defaultpdpmodel, Point startPosition, int capacity, RandomGenerator rng){
		super(VehicleDTO.builder()
			      .capacity(capacity)
			      .startPosition(startPosition)
			      .speed(SPEED)
			      .build());
		this.rng=rng;
		this.defaultpdpmodel = defaultpdpmodel;// defined in the main
		currParcel = Optional.absent();
		roadModel = Optional.absent();
		// settings for commDevice belonging to TruckAgent
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
		commDevice = Optional.absent();
		// when you create a new TruckAgent, he is full of charge
		isCharging = false;

	}

	  /**
		*calculate the distance a Truck has to travel to pickup and deliver a parcel: from its current position 
		*to the parcel pickup position and from the parcel pickup  position to the parcel destination position
		*Therefore, edgelengths of segments in the graph (model for streets) are summed
	   */
		public double calculateDistance(Optional<Point> currTruckPosition, Parcel parcel){
			Point currentTruckPosition = currTruckPosition.get();
			List<Point> currentToPickup = this.getRoadModel().getShortestPathTo(currentTruckPosition, parcel.getPickupLocation());
			List<Point> pickupToDelivery = this.getRoadModel().getShortestPathTo(parcel.getPickupLocation(), parcel.getDeliveryLocation());
			// make the sum of the vertices in the graph, from the first till the last point in the path
			double currentToPickupLength = 0.0;
			for(int i = 0; i < currentToPickup.size()-1; i++){
				Point p1 = currentToPickup.get(i);
				Point p2 = currentToPickup.get(i+1);
				double edgelength1 = Point.distance(p1, p2);
				currentToPickupLength = currentToPickupLength + edgelength1;
			}
			double pickupToDeliveryLength = 0.0;
			for(int i = 0; i < pickupToDelivery.size()-1; i++){
				Point p1 = pickupToDelivery.get(i);
				Point p2 = pickupToDelivery.get(i+1);
				double edgelength2 = Point.distance(p1, p2);
				pickupToDeliveryLength = currentToPickupLength + edgelength2;
			}
			return currentToPickupLength+pickupToDeliveryLength;
		}
		
		  /**
			* travel time = distance/speed
		   */
		public long calculateTravelTime(Optional<Point> currentTruckPosition, Parcel parcel){
			double shortestDistance = calculateDistance(currentTruckPosition, parcel);
			long time = (long) (shortestDistance/SPEED);
			return time;
		}
			
		public void sendDirectMessage(CNPMessage content, CommUser recipient) {
			if (!this.commDevice.isPresent()) {throw new IllegalStateException("No commdevice activated for truckagent");}
			CommDevice device = this.commDevice.get();
			device.send(content, recipient);
		}
		
		public List<Proposal> getProposals() {
			return proposals;
		}

		public void setProposals(List<Proposal> proposals) {
			this.proposals = proposals;
		}
/*
		public void bidCFP(CNPMessage m, Bid bid){};
		public void declineCFP(CNPMessage m, CNPMessage reaction){};
		public void load(Parcel p){
			if (ParcelState.AVAILABLE)
				ParcelState.PICKING_UP;
				pdpModel.pickup(this, p, time);
		};
		public void unload(Parcel p){
			pdpModel.drop(vehicle, p, time);
			ParcelState.DELIVERED;
		};
		public void move(Parcel p, Location l){
			pdpModel.service(vehicle, p, time);
			ParcelState.DELIVERING
		}
		
		// parcel pickup and delivery
		protected void tickImpl(TimeLapse time) {
		    final RoadModel rm = getRoadModel();
		    final PDPModel pm = getPDPModel();

		    if (!time.hasTimeLeft()) {
		      return;
		    }
		    if (!curr.isPresent()) {
		      curr = Optional.fromNullable(RoadModels.findClosestObject(
		        rm.getPosition(this), rm, Parcel.class));
		    }

		    if (curr.isPresent()) {
		      final boolean inCargo = pm.containerContains(this, curr.get());
		      // sanity check: if it is not in our cargo AND it is also not on the
		      // RoadModel, we cannot go to curr anymore.
		      if (!inCargo && !rm.containsObject(curr.get())) {
		        curr = Optional.absent();
		      } else if (inCargo) {
		        // if it is in cargo, go to its destination
		        rm.moveTo(this, curr.get().getDeliveryLocation(), time);
		        if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
		          // deliver when we arrive
		          pm.deliver(this, curr.get(), time);
		        }
		      } else {
		        // it is still available, go there as fast as possible
		        rm.moveTo(this, curr.get(), time);
		        if (rm.equalPosition(this, curr.get())) {
		          // pickup parcel
		          pm.pickup(this, curr.get(), time);
		        }
		      }
		    }
		}
		

		@Override
		public void tick(long currentTime, long timeStep) {

			handleIncomingMessages(mailbox.getMessages());

			// Drive when possible
			if (targetedPackage != null) {
				if (!path.isEmpty()) {
					truck.drive(path, timeStep);
				} else {
					if (targetedPackage.needsPickUp())
						pickUpAndGo();
					else
						deliver();
				}
			}
		}
		
		
		/// from taxi example
		 *   @Override
  protected void tickImpl(TimeLapse time) {
    final RoadModel rm = getRoadModel();
    final PDPModel pm = getPDPModel();

    if (!time.hasTimeLeft()) {
      return;
    }
    if (!curr.isPresent()) {
      curr = Optional.fromNullable(RoadModels.findClosestObject(
        rm.getPosition(this), rm, Parcel.class));
    }

    if (curr.isPresent()) {
      final boolean inCargo = pm.containerContains(this, curr.get());
      // sanity check: if it is not in our cargo AND it is also not on the
      // RoadModel, we cannot go to curr anymore.
      if (!inCargo && !rm.containsObject(curr.get())) {
        curr = Optional.absent();
      } else if (inCargo) {
        // if it is in cargo, go to its destination
        rm.moveTo(this, curr.get().getDeliveryLocation(), time);
        if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
          // deliver when we arrive
          pm.deliver(this, curr.get(), time);
        }
      } else {
        // it is still available, go there as fast as possible
        rm.moveTo(this, curr.get(), time);
        if (rm.equalPosition(this, curr.get())) {
          // pickup customer
          pm.pickup(this, curr.get(), time);
        }
      }
    }
  }
		*/
		
		// uit oude rinsim
		/*
		private void handleMessages(long currentTime) {
			Queue<Message> messages = mailbox.getMessages();
			for (Message message : messages) {
				if (message instanceof CNetMessage) {
					handleCNet((CNetMessage) message, currentTime);
				}
			}
		}

		private void handleCNet(CNetMessage message, long currentTime) {
			if (message instanceof CallForProposals) {
				eDisp.dispatchEvent(new Event(EventType.CFPReceived, this));
				CallForProposals cfp = (CallForProposals) message;
				Proposal p = null;
				if (proposal == null) {
					p = createBestProposal(cfp, currentTime);
				} else if (currentTime > proposal.getDeliveryTime()) {
					proposal = null;
				}
				if (p != null) {
					proposal = p;
					eDisp.dispatchEvent(new Event(EventType.ProposalSent, this));
					communicationAPI.send(cfp.getSender(), p);
				} else {
					Refusal ref = new Refusal(this, cfp);
					eDisp.dispatchEvent(new Event(EventType.RefusalSent, this));
					communicationAPI.send(cfp.getSender(), ref);
					
				}
			}
			if (message instanceof AcceptProposal) {
				eDisp.dispatchEvent(new Event(EventType.AcceptProposalReceived, this));
				AcceptProposal ap = (AcceptProposal) message;
				reorderPlannedTasks(ap, currentTime);
			}
			if (message.isResponseTo(proposal)) {
				proposal = null;
			}
		}
		
		*/
		

		/* 
		 * charging station
		 */
		public void charge(double amount){
			this.energy = Math.max(this.energy+amount, ENERGYCAPACITY);
		}
		
		public void setCommDevice(CommDeviceBuilder builder) {
		    if (range >= 0) {
		        builder.setMaxRange(range);
		      }
		      commDevice = Optional.of(builder
		        .setReliability(reliability)
		        .build());
		}
		
		
		@Override
		protected void tickImpl(TimeLapse time) {
		/*
	    if (!destination.isPresent()) {
	      destination = Optional.of(roadModel.get().getRandomPosition(rng));
	    }
	    roadModel.get().moveTo(this, destination.get(), timeLapse);
	    if (roadModel.get().getPosition(this).equals(destination.get())) {
	      destination = Optional.absent();
	    }
	    
	    */

		// if we somehow need the time of a certain action
		//long currentTime = time.getTime();

		if (commDevice.get().getUnreadCount() > 0) {
			lastReceiveTime = time.getStartTime();
			unreadMessages = readMessages();

			for (CNPMessage m : unreadMessages) {
				//lostContractors.clear();

				switch (m.getType()) {

				case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				    // TruckAgents can participate in auctions when they are IDLE (not occupied by anything else) and
				    // if they are not charging
					if(!isCharging && VehicleState.IDLE.equals(this.getPDPModel().getVehicleState(this))){
						doProposal(this.getPosition(), m.getAuction(), this);
					}
					break;
				case ACCEPT_PROPOSAL:
					doPDP(m, time);
					break;
				case REJECT_PROPOSAL:
					// do nothing. The TruckAgent did not win the Auction for a certain package, so currently no tasks for the truck
					break;
				default:
					break;
				}
			}

		}
		}
		  
/*
		@Override
		protected void tickImpl(TimeLapse time) {
		    final RoadModel roadModel = getRoadModel();
		    final PDPModel pdpModel = getPDPModel();
		    
		    

		    // TruckAgent reads messages from DispatchAgent
		    List<CNPMessage> messagesFromDispatch = readMessages();
		    if (!isCharging){
			    VehicleState s = pdpModel.getVehicleState(this);
			    switch (s){
			    case PICKING_UP:	
			    	// send refuseProposalMesasge
			    	break;
			    case DELIVERING:
			    	// sendRefuseProposal
			    	break;
			    case IDLE:
			    	sendProposal();
					break;
				default:
					break;
			    }
		    } else {
				// send refuseProposalMessage
			}
		}
		*/    
		
	    public List<CNPMessage> readMessages() {
	        CommDevice device = this.commDevice.get();
	        List<CNPMessage> contents = new ArrayList<CNPMessage>();
	        if (device.getUnreadCount() != 0) {
	            ImmutableList<Message> messages = device.getUnreadMessages();
	            contents = getMessageContent(messages);
	        }
	        return contents;
	    }

	    
		public List<CNPMessage> getMessageContent(ImmutableList<Message> messages) {
			Iterator<Message> it = messages.iterator();
			List<CNPMessage> contents = new ArrayList<>();
			while (it.hasNext()) {
				Message message = it.next();
				CNPMessage content = (CNPMessage)message.getContents();
				contents.add(content);
			}
			return contents;
		}
			
		
		public void doProposal(Optional<Point> currentTruckPosition, Auction auction, TruckAgent proposer){
			long timeCostBid = calculateTravelTime(currentTruckPosition, auction.getParcel());
			Proposal proposal = new Proposal(auction, proposer, timeCostBid);
			proposals.add(proposal);
			// TruckAgent sends proposal message to initiator of the auction (dispatchAgent)
			CNPProposalMessage cnpProposalMessage = new CNPProposalMessage(auction, ContractNetMessageType.PROPOSE, proposal);
			sendDirectMessage(cnpProposalMessage, auction.getSenderAuction());
			// reply-to messageID
		}
		
		public void doPDP(CNPMessage m, TimeLapse time){
			//// add method to move from current position to parcel
			defaultpdpmodel.pickup(this, m.getAuction().getParcel(), time); // status of parcel will be changed to PICKING_UP
			//// add method to move from parcel to delivery location
			defaultpdpmodel.deliver(this, m.getAuction().getParcel(), time); // status of parcel will be changed to DELIVERED
			// we suppose that the truck stays at the last delivery place until a new PDP task is accepted
			
			//// think whether it still has to be added to an arrayList
		}
		
		public void sendRefusal(Auction auction, VehicleState s){
			CNPRefusalMessage cnpRefusalMessage = new CNPRefusalMessage(auction, ContractNetMessageType.REFUSE, s.toString());
			sendDirectMessage(cnpRefusalMessage, auction.getDispatchAgent());
		}

		    


		@Override
		public Optional<Point> getPosition() {
		    if (roadModel.get().containsObject(this)) {
		        return Optional.of(roadModel.get().getPosition(this));
		      }
		      return Optional.absent();
		}
		

	 
}
