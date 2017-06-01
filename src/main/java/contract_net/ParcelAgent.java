package contract_net;

import org.eclipse.swt.widgets.DateTime;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
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
	private PDPModel pdpModel;
	private Simulator sim;
	
	public ParcelAgent(Parcel parcel){
		this.parcel = parcel;
	}
	
	
		
	}
	
	
	

}
