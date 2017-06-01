package contract_net;

import java.math.RoundingMode;
import java.util.Queue;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.google.common.base.Optional;
import com.google.common.math.DoubleMath;

public class VehicleAgent implements CommUser {
	private static final Logger LOGGER = LoggerFactory
			    .getLogger(RouteFollowingVehicle.class);
	private Queue<Point> path;
	private Optional<Point> initialPosition;
	

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


}
