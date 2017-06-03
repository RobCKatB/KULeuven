// finalize auction stop criterion, followed by award and then end of auction

package contract_net;

import java.util.List;
import java.util.Queue;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
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

	
	public TruckAgent(Point startPosition, int capacity, RandomGenerator rng){
		super(VehicleDTO.builder()
			      .capacity(capacity)
			      .startPosition(startPosition)
			      .speed(SPEED)
			      .build());
		this.rng=rng;
		currParcel = Optional.absent();
		roadModel = Optional.absent();
		// settings for commDevice belonging to TruckAgent
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
		commDevice = Optional.absent();

	}

	  /**
		*calculate the distance a Truck has to travel to pickup and deliver a parcel: from its current position 
		*to the parcel pickup position and from the parcel pickup  position to the parcel destination position
		*Therefore, edgelengths of segments in the graph (model for streets) are summed
	   */
		public double calculateDistance(Point currentTruckPosition, Parcel parcel){
			List<Point> currentToPickup = roadModel.get().getShortestPathTo(currentTruckPosition, parcel.getPickupLocation());
			List<Point> pickupToDelivery = roadModel.get().getShortestPathTo(parcel.getPickupLocation(), parcel.getDeliveryLocation());
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
		public long calculateTravelTime(Point currentTruckPosition, Parcel parcel){
			double shortestDistance = calculateDistance(currentTruckPosition, parcel);
			long time = (long) (shortestDistance/SPEED);
			return time;
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public Optional<Point> getPosition() {
		    if (roadModel.get().containsObject(this)) {
		        return Optional.of(roadModel.get().getPosition(this));
		      }
		      return Optional.absent();
		}
	 
}
