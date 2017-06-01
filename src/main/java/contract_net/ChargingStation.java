package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class ChargingStation implements CommUser{
	
	private final RoadModel roadModel;
	private Optional<Point> position;
	private Optional<CommDevice> commDevice;
	private Optional<Truck> dockedVehicle;
	private final double POWER = 10; // Energy per tick
	
	public ChargingStation(RoadModel roadModel, Point position){
		this.roadModel = roadModel;
		
	}
	
	public void dock(Truck truck){
		if(truck.getPosition().isPresent()
				&& truck.getPosition().get().equals(this.getPosition().get())){
			dockedVehicle = Optional.of(truck);
		}
	}
	
	public void unDock(){
		dockedVehicle = Optional.absent();
	}
	
	public void chargeBattery(Truck truck){
		if(dockedVehicle.isPresent()){
			dockedVehicle.get().charge(this.POWER);
		}
	}

	@Override
	public Optional<Point> getPosition() {
		return position;
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		commDevice = Optional.of(builder.build());
	}

	public boolean isBusy() {
		return !dockedVehicle.isPresent();
	}

}
