package contract_net;

import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;

public class Parcel {
	private ParcelState state;
	private Truck assignedTruck;
	private long deliveryTime;

	public Parcel(Parcel parcel, ParcelState state, Truck assignedTruck, long deliveryTime) {
		this.parcel = parcel;
		this.state = state;
		this.assignedTruck = assignedTruck;
		this.deliveryTime = deliveryTime;
	}

	public Parcel getParcel() {
		return parcel;
	}

	public ParcelState getState() {
		return state;
	}

	public boolean isPickingUp() {
		return getState().isPickedUp() || getState() == ParcelState.PICKING_UP;
	}

	public Truck getDeliveringTruck() {
		return assignedTruck;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}
}
