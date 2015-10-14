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

//#CUSTOM_EXCLUDE_FILE

import sajas.proto.*;

import sajas.core.Agent;
import sajas.core.behaviours.*;
import jade.lang.acl.*;

import jade.core.CaseInsensitiveString;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import sajas.proto.states.*;

import jade.domain.FIPANames;

import java.util.Date;

/**
 * Note: this class has been re-implemented to redirect the use of the agent, behaviour and protocol classes to SAJaS versions.
 * 
 * @see jade.proto.ContractNetResponder
 * @author hlc
 *
 */
public class ContractNetResponder extends SSContractNetResponder {
	/**
	 @deprecated Use <code>REPLY_KEY</code>
	 */
	public final String RESPONSE_KEY = REPLY_KEY;
	/** 
	 @deprecated Use either <code>ACCEPT_PROPOSAL_KEY</code> or 
	 <code>REJECT_PROPOSAL_KEY</code> according to the message 
	 that has been received
	 */             
	public final String PROPOSE_ACCEPTANCE_KEY = RECEIVED_KEY;
	/** 
	 @deprecated Use <code>REPLY_KEY</code>
	 */
	public final String RESULT_NOTIFICATION_KEY = REPLY_KEY;
	
	public static final String RECEIVE_CFP = "Receive-Cfp";
	
	/**
	 * Constructor of the behaviour that creates a new empty DataStore
	 * @see #ContractNetResponder(Agent a, MessageTemplate mt, DataStore store) 
	 **/
	public ContractNetResponder(Agent a,MessageTemplate mt) {
		this(a,mt, new DataStore());
	}
	
	/**
	 * Constructor of the behaviour.
	 * @param a is the reference to the Agent object
	 * @param mt is the MessageTemplate that must be used to match
	 * the initiator message. Take care that 
	 * if mt is null every message is consumed by this protocol.
	 * The best practice is to have a MessageTemplate that matches
	 * the protocol slot; the static method <code>createMessageTemplate</code>
	 * might be usefull. 
	 * @param store the DataStore for this protocol behaviour
	 **/
	public ContractNetResponder(Agent a,MessageTemplate mt,DataStore store) {
		super(a, null, store);
		
		Behaviour b = null;
		
		// RECEIVE_CFP
		b = new MsgReceiver(myAgent, mt, -1, getDataStore(), CFP_KEY);
		registerFirstState(b, RECEIVE_CFP);
		
		// The DUMMY_FINAL state must no longer be final
		b = deregisterState(DUMMY_FINAL);
		registerDSState(b, DUMMY_FINAL);
		
		registerDefaultTransition(RECEIVE_CFP, HANDLE_CFP);
		registerDefaultTransition(DUMMY_FINAL, RECEIVE_CFP);
	}
	
	/**
	 @deprecated Use <code>handleCfp()</code> instead
	 */
	protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		return null;
	}
	
	/**
	 @deprecated Use <code>handleAcceptProposal()</code> instead.
	 */
	protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept ) throws FailureException {
		return null;
	}	
	
	/**
	 @deprecated Use <code>registerHandleCfp()</code> instead.
	 */
	public void registerPrepareResponse(Behaviour b) {
		registerHandleCfp(b);
	}
	
	/**
	 @deprecated Use <code>registerHandleAcceptProposal()</code> instead.
	 */
	public void registerPrepareResultNotification(Behaviour b) {
		registerHandleAcceptProposal(b);
	}
	
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Redefine this method to call prepareResponse()
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
		return prepareResponse(cfp);
	}
	
	/**
	 Redefine this method to call prepareResultNotification()
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		return prepareResultNotification(cfp, propose, accept);
	}
	
	/**
	 Redefine this method so that the HANDLE_CFP state is not registered
	 as first state
	 */
	public void registerHandleCfp(Behaviour b) {
		registerDSState(b, HANDLE_CFP);
	}
	
	protected void sessionTerminated() {
		// Once the current session is terminated reinit the 
		// internal state to handle the next one
		reinit();
		
		// Be sure all children can be correctly re-executed
		resetChildren();
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 This static method can be used 
	 to set the proper message Template (based on the interaction protocol 
	 and the performative) to be passed to the constructor of this behaviour.
	 @see jade.domain.FIPANames.InteractionProtocol
	 */
	public static MessageTemplate createMessageTemplate(String iprotocol){
		if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET,iprotocol)) {
			return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
		}
		else if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,iprotocol)) {
			return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
		}
		else {
			return MessageTemplate.MatchProtocol(iprotocol);
		}
	}  
}
