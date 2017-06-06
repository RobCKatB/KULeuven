package contract_net;


import javax.annotation.Nullable;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

public class Package extends Parcel{
	
	/**
	 * Create a new package.
	 * @param parcelDto The {@link ParcelDTO} detailing all immutable information
	 *          of a parcel.
	 */
	
	private boolean isAnnounced;
	private boolean isAvailable;
	private boolean isDelivered;
	private boolean isDelivering;
	private boolean in_cargo;
	private boolean picking_up;
	private boolean picked_up;
	private ParcelStateExtended state;
	private DefaultPDPModel defaultpdpmodel;
	
	public Package(ParcelDTO parcelDto, ParcelStateExtended state, DefaultPDPModel defaultpdpmodel) {
		this(parcelDto, null);
		state = defaultpdpmodel.getParcelState(parcelDto);
	}

	public Package(ParcelDTO parcelDto, @Nullable  String toString) {
		super(parcelDto, toString);
	}

}
