package contract_net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.google.common.base.Optional;



public class DispatchAgent implements CommUser {
	

	//agent is represented as a finite state machine
	private int state = 0;
	private List<Parcel> stillToBeAssignedParcels = new ArrayList<Parcel>();
	private List<CNPMessage> messages = new ArrayList<CNPMessage>();
	//list of potential VehicleAgent contractors
	private List<VehicleAgent> potentialContractors = new ArrayList<VehicleAgent>();
	//used to record the number of received messages
	//in this version, we impose the manager to wait till receiving answers from all the contractors
	private int numberOfreceivedMessages = 0;
	//record the best proposal
	private int bestProposal = Integer.MAX_VALUE;
	//record the agent responsible for the best proposal
	private VehicleAgent bestProposalAgent = null;
	private Optional<CommDevice> commDevice;
	
	
	public DispatchAgent(List<VehicleAgent> potentialContractors, List<CNPMessage> messages, List<Parcel> stillToBeAssignedParcels, Optional<CommDevice> commDevice) {
		this.potentialContractors = potentialContractors;
		this.messages = messages;
		this.stillToBeAssignedParcels = stillToBeAssignedParcels;
		this.commDevice = commDevice;
	}
	
	//// methodes zijn een samenraapsel van anderen, moet nog herschreven worden////
	public void getDestination(Parcel p){
		for(int i = 0; i < stillToBeAssignedParcels.size()-1; i++){
			parcel = stillToBeAssignedParcels[i];
			p.getDeliveryLocation();
		}
	)

	public void addParcel(Parcel p){
		stillToBeAssignedParcels.add(p);
	}
	
	
	
	  public void tick(TimeLapse timeLapse) {
		    if (!destination.isPresent()) {
		      destination = Optional.of(roadModel.get().getRandomPosition(rng));
		    }
		    roadModel.get().moveTo(this, destination.get(), timeLapse);
		    if (roadModel.get().getPosition(this).equals(destination.get())) {
		      destination = Optional.absent();
		    }

		    if (device.get().getUnreadCount() > 0) {
		      lastReceiveTime = timeLapse.getStartTime();
		      device.get().getUnreadMessages();
		      device.get().broadcast(Messages.NICE_TO_MEET_YOU);
		    } else if (device.get().getReceivedCount() == 0) {
		      device.get().broadcast(Messages.HELLO_WORLD);
		    } else if (timeLapse.getStartTime()
		      - lastReceiveTime > LONELINESS_THRESHOLD) {
		      device.get().broadcast(Messages.WHERE_IS_EVERYBODY);
		    }
		  }
		   
	   public String getType() {
	      return null;
	   }
	   //invoked on a call to createAgent
	   public void initialise(String data) {
	      //transform the String agent identifier into something usable on the platform
		   potentialContractors.add(FIPAHelper.fromFIPASL(FIPAContent.newInstance(data)));
	   }
	   
		private void processMessages(messages) {

			bestProposer = null;
			lostProposers.clear();
			
			for (CNPMessage m : messages) {
				

				switch (m.getType()) {

					case CALL_FOR_PROPOSAL:
						calledForProposal(m.getSender());
						break;
					case ACCEPT_PROPOSAL:
						acceptedProposal(m.getSender());
						break;
					case REJECT_PROPOSAL:
						// Do Nothing;
						break;
				}

			}
			
			if (bestProposer != null && targetedPackage == null)
				sendProposal();
		}

		private void calledForProposal(CommunicationUser sender) {

			if (targetedPackage != null) {
				lostProposers.add(sender);
			} else if (bestProposer == null) {
				bestProposer = sender;
				bestDistance = Graphs.pathLength(truck.getRoadModel().getShortestPathTo(truck, sender.getPosition()));
			} else {
				double distance = Graphs.pathLength(truck.getRoadModel().getShortestPathTo(truck, sender.getPosition()));
				if (distance < bestDistance) {
					lostProposers.add(bestProposer);
					bestProposer = sender;
					bestDistance = distance;
				} else {
					lostProposers.add(sender);
				}
			}
		}
		
		
		private void sendProposal() {
			
			for (CommunicationUser proposer: lostProposers) {
				ContractNetMessage reply = new ContractNetMessage(this);
				reply.setType(ContractNetMessageType.REFUSE);
				communicationAPI.send(proposer, reply);
				LoggerFactory.getLogger("CONTRACTNET").info(proposer.hashCode() + " <- REFUSE <- " + this.hashCode());
			}

			ContractNetMessage reply = new ContractNetMessage(this);
			Point dest = bestProposer.getPosition();
			double distance = Graphs.pathLength(truck.getRoadModel().getShortestPathTo(truck, dest));
			reply.setType(ContractNetMessageType.PROPOSE);
			reply.setProposalValue(1 / distance);
			communicationAPI.send(bestProposer, reply);
			LoggerFactory.getLogger("CONTRACTNET").info(bestProposer.hashCode() + " <- PROPOSE <- " + this.hashCode());
		}

		private void acceptedProposal(CommunicationUser sender) {

			if (targetedPackage == null) {
				Point dest = sender.getPosition();

				path = new LinkedList<Point>(truck.getRoadModel().getShortestPathTo(truck, dest));
				targetedPackage = (PackageAgent) sender;

				LoggerFactory.getLogger("CONTRACTNET").info(this.hashCode() + " : starting path");
			} else {
				ContractNetMessage message = new ContractNetMessage(this);
				message.setType(ContractNetMessageType.FAILURE);
				communicationAPI.send(sender, message);
				LoggerFactory.getLogger("CONTRACTNET").info(sender.hashCode() + " <- FAILURE <- " + this.hashCode());
			}

		}
		
	   //invoked every time this is the turn for this agent to be executed
	   public void execute() {
	      retrieveNewMessages();
	      switch(state) {
	      // send call for proposals
	         case 0 : 
	                 for (AgentID target : targets) {
	                    StringMessage message = StringMessage.newInstance();
	                    message.setPerformative(IMessage.CALL_FOR_PROPOSAL);
	                    message.setContent("task0");
	                    message.getReceivers().add(target);
	                    message.setSender(this.getAgentID());
	                    send(message);
	                 }
	                 state = 1;
	                 break;
	         // dispatch agent is waiting for proposals
	         case 1 :
	                 for (IMessage message: getInbox()) {
	                    if (message.getPerformative().equals(IMessage.PROPOSE)) {
	                       receivedMessages++;
	                       if (new Integer(message.getContent()).intValue() < bestProposal) {
	                          bestProposal = new Integer(message.getContent()).intValue();
	                          bestProposalAgent = message.getSender();
	                       }
		            	if (receivedMessages == targets.size())
	                          state = 2;
		             }
		          }
	                 break;
	         // dispatch agent choses the best proposal
	         case 2 :
	                 StringMessage response = StringMessage.newInstance();
	                 response.setPerformative(IMessage.ACCEPT_PROPOSAL);
	                 response.setContent(new Integer(bestProposal).toString());
	                 response.getReceivers().add(bestProposalAgent);
	                 response.setSender(this.getAgentID());
	                 send(response);				
	                 state = 3;
	                 break;
	         // dispatch center is waiting for an inform-message from the vehicle agent to announce that the task is completed
	         case 3 : 
	                 for (IMessage message: getInbox()) {
	                    if (message.getPerformative().equals(IMessage.INFORM)) {
	                       state = 4;
	                    }
	                 }
	                 break;
	         // end
	         case 4 : 
	                 break;
	      }
	   }
	   public void update(Observable arg0, Object arg1) {
	   }
	}


}
