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

//#MIDP_EXCLUDE_FILE

import sajas.core.*;
import sajas.core.behaviours.*;
import jade.lang.acl.*;
import sajas.proto.states.MsgReceiver;
import sajas.proto.states.ReplySender;

import java.util.Date;

import sajas.proto.states.*;

/**
 * Note: this class has been re-implemented to redirect the use of the agent, behaviour and protocol classes to SAJaS versions.
 * 
 * @see jade.proto.Responder
 * @author hlc
 *
 */
abstract class Responder extends FSMBehaviour {


	// Data store keys
	/**
	   Key to retrieve from the DataStore of the behaviour the last received
	   ACLMessage
	 */
	public final String RECEIVED_KEY = "__Received_key" + hashCode();
	
	/**
	   Key to set into the DataStore of the behaviour the new ACLMessage 
	   to be sent back to the initiator as a reply.
	 */
	public final String REPLY_KEY = "__Reply_key" + hashCode();



    // private inner classes for the FSM states


    private static class CfpReceiver extends MsgReceiver {

	public CfpReceiver(Agent myAgent, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
	    super(myAgent, mt, deadline, s, msgKey);
	}

	// For persistence service
	private CfpReceiver() {
	}

	public int onEnd() {
	    Responder fsm = (Responder)getParent();
	    MsgReceiver nextRecv = (MsgReceiver)fsm.getState(RECEIVE_NEXT);

	    // Set the template to receive next messages
	    ACLMessage received = (ACLMessage)getDataStore().get(fsm.RECEIVED_KEY);
	    nextRecv.setTemplate(MessageTemplate.MatchConversationId(received.getConversationId()));
	    return super.onEnd();
	}

    } // End of CfpReceiver class

    private static class NextReceiver extends MsgReceiver {

	public NextReceiver(Agent myAgent, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
	    super(myAgent, mt, deadline, s, msgKey);
	}

	// For persistence service
	private NextReceiver() {
	}

	public void onStart() {
	    // Set the deadline for receiving the next message on the basis 
	    // of the last reply sent
	    Responder fsm = (Responder)getParent();
	    ACLMessage reply = (ACLMessage)getDataStore().get(fsm.REPLY_KEY);
	    if (reply != null) {
		Date d = reply.getReplyByDate();
		if (d != null && d.getTime() > System.currentTimeMillis()) {
		    setDeadline(d.getTime());
		}
	    }
	}

    } // End of NextReceiver class

    private static class CheckInSeq extends OneShotBehaviour {

	private int ret;
	private static final long     serialVersionUID = 4487495895818000L;

	public CheckInSeq(Agent a) {
	    super(a);
	}

	// For persistence service
	private CheckInSeq() {
	}

	public void action() {
	    Responder fsm = (Responder)getParent();
	    ACLMessage received = (ACLMessage)getDataStore().get(fsm.RECEIVED_KEY);
	    if (fsm.checkInSequence(received)) {
		ret = received.getPerformative();
	    }
	    else {
		ret = -1;
	    }
	}
	public int onEnd() {
	    return ret;
	}

    } // End of CheckInSeq class


    private static class HandleOutOfSeq extends OneShotBehaviour {

	private static final long     serialVersionUID = 4487495895818005L;

	public HandleOutOfSeq(Agent a) {
	    super(a);
	}

	// For persistence service
	private HandleOutOfSeq() {
	}

	public void action() {
	    Responder fsm = (Responder)getParent();
	    fsm.handleOutOfSequence((ACLMessage)getDataStore().get(fsm.RECEIVED_KEY));
	}

    } // End of HandleOutOfSeq class


    private static class SendReply extends ReplySender {

	public SendReply(Agent a, String replyKey, String msgKey) {
	    super(a, replyKey, msgKey);
	}

	// For persistence service
	private SendReply() {
	}

	public int onEnd() {
	    int ret = super.onEnd();
	    Responder fsm = (Responder)getParent();
	    fsm.replySent(ret);
	    return ret;
	}

    } // End of SendReply class


  //#APIDOC_EXCLUDE_BEGIN
	// FSM states names
	protected static final String RECEIVE_INITIATION = "Receive-Initiation";
	protected static final String RECEIVE_NEXT = "Receive-Next";
	protected static final String HANDLE_OUT_OF_SEQUENCE = "Handle-Out-of-seq";
	protected static final String CHECK_IN_SEQ = "Check-In-seq";
	protected static final String SEND_REPLY = "Send-Reply";

	/**
	* Constructor of the behaviour that creates a new empty DataStore
	* @see #Responder(Agent a, MessageTemplate mt, DataStore store)
	**/
	public Responder(Agent a, MessageTemplate mt) {
	     this(a, mt, new DataStore());
	}

	/**
	 * Constructor of the behaviour.
	 * @param a is the reference to the Agent object
	 * @param mt is the MessageTemplate that must be used to match
	 * the initiation message. Take care that if mt is null every message is
	 * consumed by this protocol.
	 * @param store the DataStore for this protocol behaviour
	 **/
	public Responder(Agent a, MessageTemplate mt, DataStore store) {
		super(a);
		setDataStore(store);
		
		registerDefaultTransition(RECEIVE_INITIATION, CHECK_IN_SEQ);
		registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);
		
		registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE);
		registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[] {HANDLE_OUT_OF_SEQUENCE});
		
		
		Behaviour b;
		
		// RECEIVE_INITIATION 
		b = new CfpReceiver(myAgent, mt, -1, getDataStore(), RECEIVED_KEY);
		registerFirstState(b, RECEIVE_INITIATION);
		
		// RECEIVE_NEXT 
		b = new NextReceiver(myAgent, null, -1, getDataStore(), RECEIVED_KEY);
		registerState(b, RECEIVE_NEXT);

		// CHECK_IN_SEQ
		b = new CheckInSeq(myAgent);
		registerDSState(b, CHECK_IN_SEQ);
        
		// HANDLE_OUT_OF_SEQUENCE
		b = new HandleOutOfSeq(myAgent);
		registerDSState(b, HANDLE_OUT_OF_SEQUENCE);
		
		// SEND_REPLY
		b = new SendReply(myAgent, REPLY_KEY, RECEIVED_KEY);
		registerDSState(b, SEND_REPLY);
  }

    // For persistence service
    private Responder() {
    }

  //#APIDOC_EXCLUDE_END

  /**
     This method is called whenever a message is received that does
     not comply to the protocol rules.
     This default implementation does nothing.
     Programmers may override it in case they need to react to this event.
     @param  msg the received out-of-sequence message.
   */
  protected void handleOutOfSequence(ACLMessage msg) {
  }

  /**
     This method allows to register a user defined <code>Behaviour</code>
     in the HANDLE_OUT_OF_SEQ state.
     This behaviour would override the homonymous method.
     This method also sets the 
     data store of the registered <code>Behaviour</code> to the
     DataStore of this current behaviour.
     The registered behaviour can retrieve
     the <code>out of sequence</code> ACLMessage object received
     from the datastore at the <code>RECEIVED_KEY</code>
     key.
     @param b the Behaviour that will handle this state
   */
  public void registerHandleOutOfSequence(Behaviour b) {
      registerDSState(b, HANDLE_OUT_OF_SEQUENCE);
  }

  /**
     Reset this behaviour.
   */
  public void reset() {
		super.reset();
		DataStore ds = getDataStore();
		ds.remove(RECEIVED_KEY);
		ds.remove(REPLY_KEY);
  }
  
  //#APIDOC_EXCLUDE_BEGIN
  /**
     Check whether a received message complies with the protocol rules.
   */
  protected abstract boolean checkInSequence(ACLMessage received);
  
  /**
     This method can be redefined by protocol specific implementations
     to update the status of the protocol after a reply has been sent.
     This default implementation does nothing.
   */
  protected void replySent(int exitValue) {
  }
  
  /**
     Utility method to register a behaviour in a state of the 
     protocol and set the DataStore appropriately
   */
  protected void registerDSState(Behaviour b, String name) {
    b.setDataStore(getDataStore());
    registerState(b,name);
  }
  //#APIDOC_EXCLUDE_END
}
