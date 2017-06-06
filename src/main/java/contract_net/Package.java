package contract_net;


import javax.annotation.Nullable;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

public class Package extends Parcel{
	
	/**
	 * Create a new package.
	 * @param parcelDto The {@link ParcelDTO} detailing all immutable information
	 *          of a parcel.
	 */
	
	public Package(ParcelDTO parcelDto) {
		this(parcelDto, null);
	}

	public Package(ParcelDTO parcelDto, @Nullable  String toString) {
		super(parcelDto, toString);
	}

}
