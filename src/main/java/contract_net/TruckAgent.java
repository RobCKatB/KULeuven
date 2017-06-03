// finalize auction stop criterion, followed by award and then end of auction

package contract_net;

import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.google.common.base.Optional;
import com.google.common.math.DoubleMath;

import rinde.sim.contractnets.road.RouteProcessor.Buggy;
import rinde.sim.core.model.RoadModel.PathProgress;


public class TruckAgent implements CommUser, MovingRoadUser {
	private static final Logger LOGGER = LoggerFactory
			    .getLogger(RouteFollowingVehicle.class);
	private Queue<Point> path;
	private Optional<Point> initialPosition;
	private Optional<CommDevice> commDevice;
	private Optional<RoadModel> roadModel;
	private Truck truck;
	
	// for CommUser
	  private final double range;
	  private final double reliability;
	  static final double MIN_RANGE = .2;
	  static final double MAX_RANGE = 1.5;
	  static final long LONELINESS_THRESHOLD = 10 * 1000;
	  private final RandomGenerator rng;

	public TruckAgent(Point point, int capacity){
		new Truck(point, capacity);
		roadModel = Optional.absent();
		commDevice = Optional.absent();
		// settings for commDevice belonging to TruckAgent
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
	}
		
	public Truck getTruck() {
		return truck;
	}


	public void setTruck(Truck truck) {
		this.truck = truck;
	}
	  /**
	   * Computes the travel time for this vehicle to any point.
	   * @param p The point to calculate travel time to.
	   * @param timeUnit The time unit used in the simulation.
	   * @return The travel time in the used time unit.
	   */
	  protected long computeTravelTimeTo(Parcel parcel, Unit<Duration> timeUnit) {
		  Point deliveryLocation = parcel.getDeliveryLocation();
		  final Measure<Double, Length> distance = Measure.valueOf(Point.distance(
	      roadModel.getPosition(this), deliveryLocation), roadModel.getDistanceUnit());

	    return DoubleMath.roundToLong(
	      RoadModels.computeTravelTime(speed.get(), distance, timeUnit),
	      RoundingMode.CEILING);
	  }
	  
  
	 
		public double calculateDistance(Point currentTruckPosition, Parcel parcel){
			List<Point> currentToPickup = roadModel.get().getShortestPathTo(currentTruckPosition, parcel.getPickupLocation());
			List<Point> pickupToDelivery = roadModel.get().getShortestPathTo(parcel.getPickupLocation(), parcel.getDeliveryLocation());
			// make the sum of the vertices in the graph, from the first till the last point in the path
			double currentToPickupLength = 0.0;
			for(int i = 0; i < currentToPickup.size()-1; i++){
				Point p1 = currentToPickup.get(i);
				Point p2 = currentToPickup.get(i+1);
				double vertexlength1 = Point.distance(p1, p2);
				currentToPickupLength = currentToPickupLength + vertexlength1;
			}
			double pickupToDeliveryLength = 0.0;
			for(int i = 0; i < pickupToDelivery.size()-1; i++){
				Point p1 = pickupToDelivery.get(i);
				Point p2 = pickupToDelivery.get(i+1);
				double vertexlength2 = Point.distance(p1, p2);
				pickupToDeliveryLength = currentToPickupLength + vertexlength2;
			}
			return currentToPickupLength+pickupToDeliveryLength;

		}
		

	    
		public void calculateTravelTime(Point currentTruckPosition, Parcel parcel){
	
		}
			

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

		@Override
		public Optional<Point> getPosition() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCommDevice(CommDeviceBuilder builder) {
		    if (range >= 0) {
		        builder.setMaxRange(range);
		      }
		      commDevice = Optional.of(builder
		        .setReliability(reliability)
		        .build());
		}

		@Override
		public void initRoadUser(RoadModel model) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getSpeed() {
			return truck.getSpeed();
		}
		
		 
}
