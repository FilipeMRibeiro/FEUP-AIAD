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

package sajas.proto;

import sajas.core.Agent;
import sajas.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

/**
 * Note: this class has been re-implemented to redirect the use of the agent and behaviour classes to SAJaS versions.
 * 
 * @see jade.proto.SSIteratedContractNetResponder
 * @author hlc
 *
 */
public class SSIteratedContractNetResponder extends SSContractNetResponder {
	
	public SSIteratedContractNetResponder(Agent a, ACLMessage cfp) {
		this(a, cfp, new DataStore());
	}
	
	/**
	 Construct a SSIteratedContractNetResponder that is activated 
	 by the reception of a given initiation CFP message and uses 
	 a given DataStore.
	 */
	public SSIteratedContractNetResponder(Agent a, ACLMessage cfp, DataStore store) {
		super(a, cfp, store);
		
		registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP, new String[]{HANDLE_CFP, SEND_REPLY, RECEIVE_NEXT}); 
	}
	
	protected boolean checkInSequence(ACLMessage received) {
		if (received.getPerformative() == ACLMessage.CFP) {
			// New iteration --> Move the received message to the CFP_KEY and return true
			getDataStore().put(this.CFP_KEY, received);
			return true;
		}
		else {
			return super.checkInSequence(received);
		}
	}
	
	protected void beforeReply(ACLMessage reply) {
		ACLMessage lastReceivedMsg = (ACLMessage)getDataStore().get(RECEIVED_KEY);
		if (lastReceivedMsg != null && lastReceivedMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			// We are sending the reply to the ACCEPT_PROPOSAL --> Jump out and terminate just after sending this reply
			forceTransitionTo(DUMMY_FINAL);
		}
	}
	
	protected void afterReply(ACLMessage reply) {
	}
}
