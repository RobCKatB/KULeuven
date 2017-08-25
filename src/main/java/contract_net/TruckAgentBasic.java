package contract_net;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class TruckAgentBasic extends TruckAgent {
	
	private Optional<Auction> currAuction = Optional.absent();

	public TruckAgentBasic(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, Point startPosition, int capacity,
			RandomGenerator rng) {
		super(defaultpdpmodel, roadModel, startPosition, capacity, rng);
	}

	@Override
	protected void processMessages(List<CNPMessage> messages, TimeLapse time){
		for (CNPMessage m : messages) {
			
			switch (m.getType()) {

			case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				if(!isCharging && isIdle && !currAuction.isPresent()){
					doProposal(this.getPosition().get(), m.getAuction(), this, time);
					currAuction = Optional.of(m.getAuction());
					System.out.println(this+" > proposal sent for "+m.getAuction());
				} else {
					sendRefusal(m.getAuction(), "Charging or busy", time);
					System.out.println(this+" > refusal sent for "+m.getAuction());
				}
				break;
			case ACCEPT_PROPOSAL:
				if(!currParcel.isPresent()){
					handleParcel(m, time);
					currAuction = Optional.absent();
				}else{
					// This is impossible in basic mode.
					assert false; // Throw an error.
				}

				/*
				// TODO: add accepted proposal to a List of all accepted proposals for this TruckAgent
				 * CNPAcceptMessage cnpAccept = (CNPAcceptMessage)m;
				 * acceptedProposals.add(cnpAccept.getProposal());
				 * not right since this does not store all acceptedProposals for one TruckAgent, it just stores the acceptedProposal for this auction
				 */

				break;
			case REJECT_PROPOSAL:
				// Dispatch agent has rejected the proposal of truckagent.
				// Make sure we can participate in a new auction
				currAuction = Optional.absent();
				this.isIdle = true;

				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void afterDelivery(TimeLapse time) {
		// In basic mode, the job is done.
	}
}
