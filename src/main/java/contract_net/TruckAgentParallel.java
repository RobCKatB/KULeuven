package contract_net;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class TruckAgentParallel extends TruckAgent {

	public TruckAgentParallel(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, Point startPosition, int capacity,
			RandomGenerator rng) {
		super(defaultpdpmodel, roadModel, startPosition, capacity, rng);
	}

	@Override
	protected void processMessages(List<CNPMessage> messages, TimeLapse time){
		for (CNPMessage m : messages) {
			
			switch (m.getType()) {

			case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				if(!isCharging && isIdle){
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
					// We already have a parcel, cancel this one.
					// TODO: sendCancelMessage();
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

	@Override
	protected void afterDelivery(TimeLapse time) {
		// In parallel mode, the job is done.
	}
}
