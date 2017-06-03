package contract_net;

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
	private Optional<Double> range;
	private Optional<CommDevice> commDevice;
	private Optional<TruckAgent> dockedVehicle;
	private final double POWER = 10; // Energy per tick deliverable to trucks
	
	public ChargingStation(Point startPosition,Double range){
		this.roadModel = Optional.absent();
		this.startPosition = startPosition;
		this.range = Optional.of(range);
		this.commDevice = Optional.absent();
		this.dockedVehicle = Optional.absent();
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
		if(this.range.isPresent()){
			builder.setMaxRange(this.range.get());
		}
		commDevice = Optional.of(builder.build());
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
