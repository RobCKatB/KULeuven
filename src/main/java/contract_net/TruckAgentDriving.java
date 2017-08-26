package contract_net;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class TruckAgentDriving extends TruckAgent {

	// A list of the auctions on which we have done a proposal that is
	// still valid.
	// When a proposal gets accepted, all others get invalid and will
	// be cancelled.
	private HashSet<Auction> validProposals = new HashSet<Auction>();
	private LinkedList<CNPMessage> wonAuctionsQueue = new LinkedList<CNPMessage>();
	// The energy used by all delivered parcels + the energy that is needed for all
	// parcels in the queue.
	double energyUsedByQueue = 0;
	
	public TruckAgentDriving(DefaultPDPModel defaultpdpmodel, RoadModel roadModel, Point startPosition, int capacity,
			RandomGenerator rng) {
		super(defaultpdpmodel, roadModel, startPosition, capacity, rng);
	}

	@Override
	protected void processMessages(List<CNPMessage> messages, TimeLapse time){
		for (CNPMessage m : messages) {
			
			switch (m.getType()) {

			case CALL_FOR_PROPOSAL: // TruckAgent receives call for proposal from a Dispatch Agent
				if(isCharging){
					// Not right state
					sendRefusal(m.getAuction(), "Charging or busy", time);
//					System.out.println(this+" > refusal sent for "+m.getAuction()+" because busy or charging [energy level = "+this.getEnergy()+"]");
				}else if(!enoughEnergy(this.getPosition().get(), m.getAuction().getParcel(), findClosestChargingStation())){
					// Not enough energy
					goCharging();
					energyUsedByQueue = 0;
					sendRefusal(m.getAuction(), "truck is charging", time);
					System.out.println(this+" > REFUSAL sent because not suffient energy [energy left = "+ getEnergy() + "; energy needed = "+calculateEnergyConsumptionTask(this.getPosition().get(), m.getAuction().getParcel())+"] for auction " + m.getAuction());
				}else{
					// Everything fine; do proposal
					doProposal(this.getPosition().get(), m.getAuction(), this, time);
					validProposals.add(m.getAuction());
//					System.out.println(this+" > proposal sent for "+m.getAuction());
				}
				break;
				
			case ACCEPT_PROPOSAL:
				if(validProposals.contains(m.getAuction())){
					if(!currParcel.isPresent()){
						handleParcel(m, time);
					}else{
						// Add the parcel to the running list of parcels we will handle.
						// Concrete: store the accept_proposal message to be handled when
						// it's their turn.
						wonAuctionsQueue.add(m);
					}
					energyUsedByQueue += calculateEnergyConsumptionTask(getLastQueuePosition(), m.getAuction().getParcel());
					// As our state our queue has been altered, the calculations of proposals
					// we already sent are no longer valid.
					validProposals.clear();
				}else{
					sendCancelMessage(m.getAuction(), "Proposal no longer valid.", time);
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
	protected double calculatePDPDistanceCurrentToPickup(Point currentTruckPosition, Parcel parcel){
		
		// Construct the path we will have tot ake
		ArrayList<Point> path = new ArrayList<Point>();
		
		path.add(currentTruckPosition);
		
		// Current parcel
		if(currParcel.isPresent()){
			if(!isCarrying){
				// We have to drive to the pickup location
				path.add(currParcel.get().getPickupLocation());
			}
			path.add(currParcel.get().getDeliveryLocation());
		}
		
		// All parcels in queue
		for(CNPMessage message: wonAuctionsQueue){
			path.add(message.getAuction().getParcel().getPickupLocation());
			path.add(message.getAuction().getParcel().getDeliveryLocation());
		}
		
		// Parcel of auction
		path.add(parcel.getPickupLocation()); // Only to pickup, see name of method
		
		return calculatePathDistance(path);
	}

	private double calculatePathDistance(ArrayList<Point> path){
		double distance = 0;
		for (int i = 0; i < path.size()-1; i++) {
			distance += calculatePointToPointDistance(path.get(i), path.get(i+1));
		}
		return distance;
	}
	
	@Override
	protected boolean enoughEnergy(Point currTruckPosition, Parcel parcel, Optional<ChargingStation> chargingStation){
		double energyNeeded = calculateEnergyConsumptionTask(getLastQueuePosition(), parcel) + calculateEnergyConsumptionToChargingStation(parcel.getDeliveryLocation(), chargingStation);
		energyNeeded += energyUsedByQueue;
//		System.out.println("energy needed:" + energyNeeded+ "[energy left="+energy+"]");
		if (energyNeeded <=  energy){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Return the position this truck will have when all his current
	 * tasks are finished.
	 * @return
	 */
	private Point getLastQueuePosition(){
		if(!wonAuctionsQueue.isEmpty()){
			return wonAuctionsQueue.getLast().getAuction().getParcel().getDeliveryLocation();
		}else if(currParcel.isPresent()){
			return currParcel.get().getDeliveryLocation();
		}else{
			return this.getPosition().get();
		}
	}
	
	protected void afterDelivery(TimeLapse time){
		// Get right on with the next parcel in the queue.
		if(!wonAuctionsQueue.isEmpty()){
			handleParcel(wonAuctionsQueue.removeFirst(), time);
		}
	}

}
