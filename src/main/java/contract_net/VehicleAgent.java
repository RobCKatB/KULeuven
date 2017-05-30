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
	/**
	 * calculate path length
	 * @param path
	 * @return
	 */
	public static double pathLength(Queue<Point> path)
	{
		double len = 0d;
		Point last = null;
		
		for (Point p : path)
		{
			if (last == null)
			{
				last = p;
				continue;
			}
			else
			{
				len += Math.sqrt(Math.pow(last.x-p.x, 2d) + Math.pow(last.y-p.y, 2d));
				last = p;
			}
		}
		
		return len;
	}

}
