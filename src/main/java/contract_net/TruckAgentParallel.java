package contract_net;

import java.util.HashSet;
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
		
		HashSet<CNPAcceptMessage> acceptProposalMessages = new HashSet<CNPAcceptMessage>();
		
		for (CNPMessage m : messages) {
			
			switch (m.getType()) {

			case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				if(isCharging){
					// Not right state
					sendRefusal(m.getAuction(), "charging", time);
//					System.out.println(this+" > refusal sent for "+m.getAuction()+" because charging [energy level = "+this.getEnergy()+"]");
				}else if(!isIdle){
					// Not right state
					sendRefusal(m.getAuction(), "busy", time);
//					System.out.println(this+" > refusal sent for "+m.getAuction()+" because busy [energy level = "+this.getEnergy()+"]");
				}else if(!enoughEnergy(this.getPosition().get(), m.getAuction().getParcel(), findClosestChargingStation())){
					// Not enough energy
					goCharging();
					sendRefusal(m.getAuction(), "truck is charging", time);
//					System.out.println(this+" > REFUSAL sent because not suffient energy [energy left = "+ getEnergy() + "; energy needed = "+calculateEnergyConsumptionTask(this.getPosition().get(), m.getAuction().getParcel())+"] for auction " + m.getAuction());
				}else{
					// Everything fine; do proposal
					doProposal(this.getPosition().get(), m.getAuction(), this, time);
//					System.out.println(this+" > proposal sent for "+m.getAuction());
				}
				break;
				
			case ACCEPT_PROPOSAL:
				if(!currParcel.isPresent()){
					CNPAcceptMessage accMessage = (CNPAcceptMessage)m;
					acceptProposalMessages.add(accMessage);
				}else{
					// We already have a parcel, cancel this one.
					sendCancelMessage(m.getAuction(), "Already handeling a parcel.", time);
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
		
		// If we had multiple accept_proposals, look which one of those was
		// for the best proposal. Cancel the others.
		if(!acceptProposalMessages.isEmpty()){
			
			long bestTimeCost = Long.MAX_VALUE;
			CNPAcceptMessage bestProposalMessage = null;
			
			for(CNPAcceptMessage m: acceptProposalMessages){
				if(m.getProposal().getTimeCostProposal() < bestTimeCost){
					bestTimeCost = m.getProposal().getTimeCostProposal();
					bestProposalMessage = m;
				}
			}
			
			handleParcel(bestProposalMessage, time);
			
			// Cancel the others
			for(CNPAcceptMessage m: acceptProposalMessages){
				if(m!=bestProposalMessage){
					sendCancelMessage(m.getAuction(), "Other proposal was better", time);
				}
			}
		}
		
	}
	
	@Override
	protected void afterDelivery(TimeLapse time) {
		// In parallel mode, the job is done.
	}
}
