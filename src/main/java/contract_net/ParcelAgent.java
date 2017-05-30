package contract_net;

import org.eclipse.swt.widgets.DateTime;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

public class ParcelAgent implements CommUser {
	private Parcel parcel;
	private Point destination;
	private Point start;
	private DateTime timeParcelCreated;
	private long deliveryTime;
	private Truck PDPTruck; // the truck that picks up and delivers the parcel
	private RoadModel roadModel;
	
	public ParcelAgent(Parcel parcel){
		this.parcel = parcel;
	}
	
	
	// probably wrong place to make a parcel, should probably be done in a tick?
	private void makeParcel(RoadModel roadModel, Point start, Point destination){
		roadModel.addObjectAt(parcel, destination);
	}

}
