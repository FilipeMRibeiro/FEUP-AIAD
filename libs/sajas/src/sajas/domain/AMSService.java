/*****************************************************************
 SAJaS - Simple API for JADE-based Simulations is a framework to 
 facilitate running multi-agent simulations using the JADE framework.
 Copyright (C) 2015 Henrique Lopes Cardoso
 Universidade do Porto

 GNU Lesser General Public License

 This file is part of SAJaS.

 SAJaS is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SAJaS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SAJaS.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************/

package sajas.domain;

import jade.core.AID;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sajas.core.Agent;

/**
 * The AMSService is re-implemented to avoid blocking approaches, given SAJaS single thread nature.
 * Public static methods to register and deregister agents are still available, but implemented as database operations,
 * instead of requiring a FIPA communication with the AMS agent.
 * 
 * @see jade.domain.AMSService
 * @author hlc
 *
 */
public class AMSService extends FIPAService {

	public static AID amsAID = new sajas.core.AID("ams", AID.ISLOCALNAME);
	private static Map<AID, Agent> agents = new HashMap<AID, Agent>();
	
	/**
	 * Initializes the AMS service.
	 * Programmers should have no need to use this method.
	 * @exclude
	 */
	public static void initialize() {
		agents = new HashMap<AID, Agent>();
	}
	
	/**
	 * Simpler register method, without the need to provide an AMSAgentDescription.
	 * Programmers should have no need to use this method.
	 * @param agent
	 * @exclude
	 */
	public static void register(Agent agent) {
		Agent previous = agents.put(agent.getAID(), agent);
		if(previous != null) {
			System.err.println("AMSService.register(Agent): agent " + agent.getLocalName() + " replaced because of name-clash");
		}
	}

	/**
	 * @see jade.domain.AMSService#register(jade.core.Agent, jade.domain.FIPAAgentManagement.AMSAgentDescription)
	 */
	public static void register(Agent agent, AMSAgentDescription amsd) throws FIPAException {
		register(agent);
	}
	
	/**
	 * @see jade.domain.AMSService#deregister(jade.core.Agent)
	 */
	public static void deregister(Agent agent) {
		agents.remove(agent.getAID());
	}

	/**
	 * Gets the Agent object given its AID.
	 * Programmers should have no need to use this method.
	 * @param aid
	 * @return
	 * @exclude
	 */
	public static Agent getAgent(AID aid) {
		return agents.get(aid);
	}
	
	
	/**
	 * Posts a message in each of the receivers' mailbox.
	 * Programmers should have no need to use this method.
	 * @param msg			The message to deliver
	 * @param cloneOriginal	A flag indicating whether the message should be cloned, to prevent modifications of the original message
	 * @param needClone		If set, a separate (cloned) message will be sent to each receiver
	 * @exclude
	 */
	public static void send(ACLMessage msg, boolean cloneOriginal, boolean needClone) {
		
		ACLMessage newmsg;
		if(cloneOriginal) {
			// the original message is cloned to prevent modifications
			newmsg = (ACLMessage) msg.clone();
		} else {
			newmsg = msg;
		}
		
		Iterator it = msg.getAllIntendedReceiver();
		while (it.hasNext()) {
			AID receiver = (AID) it.next();
			ACLMessage toBeSent;
			if (needClone) {
				// the message is cloned so that each agent gets a separate message
				toBeSent = (ACLMessage) newmsg.clone();
			} else {
				toBeSent = newmsg;
			}
			
			// post message
			Agent theReceiver = getAgent(receiver);
			if(theReceiver != null) {
				getAgent(receiver).postMessage(toBeSent);
			} else {
				ACLMessage failure = msg.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				failure.setSender(amsAID);
				failure.setContent("Agent not found: " + receiver.getLocalName());
				getAgent(msg.getSender()).postMessage(failure);
				System.out.println(failure);
				return;   // FIXME: canceling further messages if a receiver is not found
			}
		}

	}

}
