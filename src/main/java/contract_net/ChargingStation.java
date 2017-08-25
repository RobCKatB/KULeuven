package contract_net;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class ChargingStation implements CommUser, RoadUser, TickListener{
	
	private Optional<RoadModel> roadModel;
	private Point startPosition;
	private Optional<CommDevice> commDevice;
	private Optional<TruckAgent> dockedVehicle;
	private final double POWER = 10; // Energy per tick deliverable to trucks
	// for CommUser
	private final double range;
	private final double reliability;
	static final double MIN_RANGE = .2;
	static final double MAX_RANGE = 1.5;
	static final long LONELINESS_THRESHOLD = 10 * 1000;
	private final RandomGenerator rng;
	  
	public ChargingStation(Point startPosition, RoadModel roadModel, RandomGenerator rng){
		this.rng = rng;
		
		this.roadModel = Optional.absent();
		this.startPosition = startPosition;
		this.dockedVehicle = Optional.absent();
		
		// settings for commDevice belonging to TruckAgent
	    //range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
		range = Double.MAX_VALUE;
	    reliability = rng.nextDouble();
		commDevice = Optional.absent();
	}
	
	private boolean checkTruckPosition(TruckAgent truck){
		return (truck.getPosition().isPresent()
				&& truck.getPosition().get().equals(this.getPosition().get()));

	}
	
	/**
	 * Dock a truck in this charging station.
	 * 
	 * @param truck
	 * 			The truck to be docked.
	 * @return
	 * 			True if success, false otherwise.
	 */
	public boolean tryDock(TruckAgent truck){
		if(!this.isBusy() && checkTruckPosition(truck)){
			dockedVehicle = Optional.of(truck);
			return true;
		}else{
			return false;
		}
	}
	
	public void unDock(){
		dockedVehicle = Optional.absent();
	}

	@Override
	public Optional<Point> getPosition() {
		return Optional.of(roadModel.get().getPosition(this));
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		commDevice = Optional.of(builder.setMaxRange(this.range).build());
	}

	public boolean isBusy() {
		return !dockedVehicle.isPresent();
	}

	@Override
	public void initRoadUser(RoadModel model) {
		this.roadModel = Optional.of(model);
		this.roadModel.get().addObjectAt(this, startPosition);
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if(dockedVehicle.isPresent()){
			if(checkTruckPosition(dockedVehicle.get())){
				dockedVehicle.get().charge(this.POWER*timeLapse.getTickLength());
			}else{
				System.out.println("Truck drove away without undocking!");
				unDock();
			}
		}
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {}
	
	@Override
	public String toString() {
		 return new StringBuilder("CharigingStation [")
		.append(this.getPosition().get())
		.append(",")
		.append(dockedVehicle.get())
		.append("]")
		    .toString();
	}

}
