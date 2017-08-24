package contract_net;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class TruckAgentDriving extends TruckAgent {

	private LinkedList<CNPMessage> wonAuctionsQueue = new LinkedList<CNPMessage>();
	
	public TruckAgentDriving(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, Point startPosition, int capacity,
			RandomGenerator rng) {
		super(defaultpdpmodel, roadModel, startPosition, capacity, rng);
	}

	@Override
	protected void processMessages(List<CNPMessage> messages, TimeLapse time){
		for (CNPMessage m : messages) {
			
			switch (m.getType()) {

			case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				if(!isCharging){ // TODO: how to handle charging?
					doProposal(this.getPosition().get(), m.getAuction(), this, time);
				} else {
					sendRefusal(m.getAuction(), "Charging or busy", time);
					System.out.println(this+" > refusal sent for "+m.getAuction());
				}
				break;
				
			case ACCEPT_PROPOSAL:
				if(!currParcel.isPresent()){
					handleParcel(m, time);
				}else{
					// Add the parcel to the running list of parcels we will handle.
					// Concrete: store the accept_proposal message to be handled when
					// it's their turn.
					wonAuctionsQueue.add(m);
				}
				break;
				
			case REJECT_PROPOSAL:
					// Dispatch agent has rejected the proposal of truckagent.
					// Do nothing. The TruckAgent did not win the Auction for a certain package.
				break;
				
			default:
				break;
			}
		}
	}
	
	protected void afterDelivery(TimeLapse time){
		// Get right on with the next parcel in the queue.
		if(!wonAuctionsQueue.isEmpty()){
			handleParcel(wonAuctionsQueue.removeFirst(), time);
		}
	}

}
