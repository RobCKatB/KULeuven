// finalize auction stop criterion, followed by award and then end of auction

package contract_net;

import java.math.RoundingMode;
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
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.google.common.base.Optional;
import com.google.common.math.DoubleMath;

public class TruckAgent implements CommUser {
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

	public TruckAgent(Point startPosition, int capacity){
		truck = new Truck(startPosition, capacity);
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
	  protected long computeTravelTimeTo(Point p, Unit<Duration> timeUnit) {
	    final Measure<Double, Length> distance = Measure.valueOf(Point.distance(
	      getRoadModel().getPosition(this), p), getRoadModel()
	        .getDistanceUnit());

	    return DoubleMath.roundToLong(
	      RoadModels.computeTravelTime(speed.get(), distance, timeUnit),
	      RoundingMode.CEILING);
	  }
	  
		public void calculateDistance(CNPMessage type.CALL_FOR_PROPOSALS){
			roadModel.getShortestPathTo(currentPosition, parcelPosition);
	
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


}
