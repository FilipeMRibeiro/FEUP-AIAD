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

import sajas.core.*;
import sajas.core.behaviours.*;
import jade.lang.acl.*;
import jade.core.CaseInsensitiveString;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPANames;
import sajas.proto.states.*;
import jade.util.leap.Iterator;

/**
 * Note: this class has been re-implemented to redirect the use of the agent, behaviour and protocol classes to SAJaS versions.
 * 
 * @see jade.proto.ProposeResponder
 * @author hlc
 *
 */
public class ProposeResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {
	
  /** 
   * key to retrieve from the DataStore of the behaviour the ACLMessage 
   *	object sent by the initiator.
   **/
  public final String PROPOSE_KEY = "__propose" + hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the ACLMessage 
   *	object sent as a response to the initiator.
   **/
  public final String RESPONSE_KEY = "__response" + hashCode();

  // FSM states names
  protected static final String RECEIVE_PROPOSE = "Receive-propose";
  protected static final String PREPARE_RESPONSE = "Prepare-response";
  protected static final String SEND_RESPONSE = "Send-response";

    // Private inner classes for the FSM states
    private static class PrepareResponse extends OneShotBehaviour {

	public PrepareResponse(Agent a) {
	    super(a);
	}

	// For persistence service
	private PrepareResponse() {
	}

        public void action() {
	    ProposeResponder fsm = (ProposeResponder)getParent();
	    DataStore ds = getDataStore();
	    ACLMessage propose = (ACLMessage) ds.get(fsm.PROPOSE_KEY);

	    ACLMessage response = null;
	    try {
		response = fsm.prepareResponse(propose); 
	    }
	    catch (NotUnderstoodException nue) {
		response = nue.getACLMessage();
	    }
	    catch (RefuseException re) {
		response = re.getACLMessage();
	    }
	    ds.put(fsm.RESPONSE_KEY, response);
        }

    } // End of PrepareResponse class


  // The MsgReceiver behaviour used to receive propose messages
  MsgReceiver rec = null;
	
  /**
   * This static method can be used 
   * to set the proper message template (based on the interaction protocol 
   * and the performative)
   * into the constructor of this behaviour.
   * @see FIPANames.InteractionProtocol
   **/
  public static MessageTemplate createMessageTemplate(String iprotocol){
	
    if(CaseInsensitiveString.equalsIgnoreCase(FIPA_PROPOSE,iprotocol))
	    return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPA_PROPOSE),MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
    else
      return MessageTemplate.MatchProtocol(iprotocol);
  }

  /**
   * Constructor of the behaviour that creates a new empty DataStore
   * @see #ProposeResponder(Agent a, MessageTemplate mt, DataStore store) 
   **/
  public ProposeResponder(Agent a, MessageTemplate mt){
    this(a,mt, new DataStore());
  }

  /**
   * Constructor.
   * @param a is the reference to the Agent object
   * @param mt is the MessageTemplate that must be used to match
   * the initiator message. Take care that 
   * if mt is null every message is consumed by this protocol.
   * @param store the DataStore for this protocol
   **/
  public ProposeResponder(Agent a, MessageTemplate mt, DataStore store) {
    super(a);
		
    setDataStore(store);
		
    // Register the FSM transitions
    registerDefaultTransition(RECEIVE_PROPOSE, PREPARE_RESPONSE);
    registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
    registerDefaultTransition(SEND_RESPONSE, RECEIVE_PROPOSE);
	
    // Create and register the states that make up the FSM
    Behaviour b = null;

    // RECEIVE_PROPOSE
    rec = new MsgReceiver(myAgent, mt, -1, getDataStore(), PROPOSE_KEY);
    registerFirstState(rec, RECEIVE_PROPOSE);

    // PREPARE_RESPONSE
    b = new PrepareResponse(myAgent);
    b.setDataStore(getDataStore());		
    registerState(b, PREPARE_RESPONSE);

    // SEND_RESPONSE
    b = new ReplySender(myAgent, RESPONSE_KEY, PROPOSE_KEY);
    b.setDataStore(getDataStore());		
    registerState(b, SEND_RESPONSE);
  }

  /**
   * Reset this behaviour.
   */
  public void reset() {
    super.reset();
    DataStore ds = getDataStore();
    ds.remove(PROPOSE_KEY);
    ds.remove(RESPONSE_KEY);
  }
    
  /**
   * This method allows to change the <code>MessageTemplate</code>
   * that defines what messages this ProposeResponder will react to
   * and reset the protocol.
   */
  public void reset(MessageTemplate mt) {
    this.reset();
    rec.reset(mt, -1, getDataStore(), PROPOSE_KEY);
  }
    

  /**   
   * This method is called when the initiator's
   * message is received that matches the message template
   * passed in the constructor. 
   * This default implementation return null which has
   * the effect of sending no reponse. Programmers should
   * override the method in case they need to react to this event.
   * @param propose the received message
   * @return the ACLMessage to be sent as a response (i.e. one of
   * <code>accept_proposal, reject_proposal, not-understood</code>. 
   * <b>Remind</b> to use the method createReply of the class ACLMessage 
   * in order to create a good reply message
   * @see jade.lang.acl.ACLMessage#createReply()
   **/
  protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
    return null;
  }
    
  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the PREPARE_RESPONSE state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * It is responsibility of the registered behaviour to put the
   * response to be sent into the datastore at the <code>RESPONSE_KEY</code>
   * key.
   * @param b the Behaviour that will handle this state
   */
  public void registerPrepareResponse(Behaviour b) {
    registerState(b, PREPARE_RESPONSE);
    b.setDataStore(getDataStore());
  }

    //#MIDP_EXCLUDE_BEGIN

    // For persistence service
    private ProposeResponder() {
    }
    //#MIDP_EXCLUDE_END

}
