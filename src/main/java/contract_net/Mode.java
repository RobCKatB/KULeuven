package contract_net;

public enum Mode {
	
	/**
	 * Trucks only participate in one auction at a time.
	 */
	BASIC,
	
	/**
	 * Trucks can participate in more auctions at a time.
	 * When an auction is won while the truck is already handling another parcel,
	 * they will respond with an cancel-message.
	 */
	PARALLEL_AUCTIONS,
	
	/**
	 * Trucks can participate in more auctinos at a time.
	 * Trucks can even participate in auctions while handeling other parcels,
	 * they will account for the time cost of finishing the current task in
	 * their bid.
	 * When an auction is won while the truck is already handling another parcel,
	 * they will queue the parcel.
	 */
	DRIVING_AUCTIONS
}
