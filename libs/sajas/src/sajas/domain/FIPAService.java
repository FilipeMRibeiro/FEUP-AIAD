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
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;
import jade.lang.acl.ACLMessage;

import sajas.core.Agent;

/**
 * FIPA services are re-implemented to avoid blocking approaches, given SAJaS single thread nature.
 * As a consequence, <code>doFipaRequestClient</code> is not supported and issues an error message.
 * 
 * @see jade.domain.FIPAService
 * @author hlc
 *
 */
public class FIPAService {
	private static int cnt = 0;

	/**
	 * @see jade.domain.FIPAService#createRequestMessage(jade.core.Agent, jade.core.AID)
	 */
	static ACLMessage createRequestMessage(Agent sender, AID receiver) {
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(sender.getAID());
		request.addReceiver(receiver);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		request.setOntology(FIPAManagementVocabulary.NAME);
		request.setReplyWith("rw-" + sender.getName() + System.currentTimeMillis() + '-' + (++cnt));
		request.setConversationId("conv-" + sender.getName() + System.currentTimeMillis() + '-' + cnt);
		return request;
	}

	/**
	 * @see jade.domain.FIPAService#doFipaRequestClient(jade.core.Agent, jade.lang.acl.ACLMessage)
	 */
	public static ACLMessage doFipaRequestClient(Agent a, ACLMessage request) throws FIPAException {
		return doFipaRequestClient(a, request, 0);
	}

	/**
	 * @see jade.domain.FIPAService#doFipaRequestClient(jade.core.Agent, jade.lang.acl.ACLMessage, long)
	 * @see sajas.core.Agent#blockingReceive()
	 */
	public static ACLMessage doFipaRequestClient(Agent a, ACLMessage request, long timeout) throws FIPAException {
		System.err.println("WARNING: SAJaS does not currently support blocking approaches -- returning null");
		return null;
	}

}
