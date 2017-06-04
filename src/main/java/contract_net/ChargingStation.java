package contract_net;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class ChargingStation implements CommUser, RoadUser{
	
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
	  
	public ChargingStation(Point startPosition, RandomGenerator rng){
		this.rng = rng;
		
		this.roadModel = Optional.absent();
		this.startPosition = startPosition;
		this.dockedVehicle = Optional.absent();
		
		// settings for commDevice belonging to TruckAgent
	    range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
	    reliability = rng.nextDouble();
		commDevice = Optional.absent();
	}
	
	private boolean checkTruckPosition(TruckAgent truck){
		return (truck.getPosition().isPresent()
				&& truck.getPosition().get().equals(this.getPosition().get()));

	}
	
	public void dock(TruckAgent truck){
		if(checkTruckPosition(truck)){
			dockedVehicle = Optional.of(truck);
		}
	}
	
	public void unDock(){
		dockedVehicle = Optional.absent();
	}
	
	public void chargeBattery(TruckAgent truck){
		if(dockedVehicle.isPresent()){
			if(checkTruckPosition(truck)){
				dockedVehicle.get().charge(this.POWER);
			}else{
				System.out.println("Truck drove away without undocking!");
				unDock();
			}
		}
		// TODO: some logging here?
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

}
