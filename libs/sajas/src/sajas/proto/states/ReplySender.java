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

package sajas.proto.states;

//#CUSTOM_EXCLUDE_FILE

import sajas.core.*;
import sajas.core.behaviours.*;
import jade.lang.acl.*;

import jade.util.leap.Iterator;

/**
 * Note: this class has been re-implemented to redirect the use of the agent and behaviour classes to SAJaS versions.
 * 
 * @see jade.proto.states.ReplySender
 * @author hlc
 *
 */
public class ReplySender extends OneShotBehaviour {
	
	public static final int NO_REPLY_SENT = -1;
	private int ret;
	private String replyKey, msgKey;
	
	/**
	 * Constructor.
	 * @param a The Agent executing this behaviour
	 * @param replyKey DataStore's key where to read the reply message
	 * @param msgKey DataStore's key where to read the message to reply to.
	 * @param ds the dataStore for this bheaviour
	 **/ 
	public ReplySender(Agent a, String replyKey, String msgKey, DataStore ds) {
		this(a, replyKey, msgKey);
		setDataStore(ds);
	}
	
	/**
	 * Constructor.
	 * @param a The Agent executing this behaviour
	 * @param replyKey DataStore's key where to read the reply message
	 * @param msgKey DataStore's key where to read the message to reply to.
	 **/ 
	public ReplySender(Agent a, String replyKey, String msgKey) {
		super(a);
		this.replyKey = replyKey;
		this.msgKey = msgKey;	
	}
	
	public void action(){
		ret=NO_REPLY_SENT;
		DataStore ds = getDataStore();
		ACLMessage reply = (ACLMessage) ds.get(replyKey);
		if (reply != null) {
			ACLMessage msg = (ACLMessage) ds.get(msgKey);
			if (msg != null) {
				adjustReply(myAgent, reply, msg);
				myAgent.send(reply);
				ret = reply.getPerformative();
			}		
		}
	}
	
	public int onEnd() {
		return ret;
	}
	
	public void setMsgKey(String msgKey) {
		this.msgKey = msgKey;
	}
	
	public void setReplyKey(String replyKey) {
		this.replyKey = replyKey;
	}
	
	/**
	 Adjust all protocol fields and receivers in a reply to a given
	 message.
	 */
	public static void adjustReply(Agent myAgent, ACLMessage reply, ACLMessage msg) {
		// Set the conversationId
		reply.setConversationId(msg.getConversationId());
		// Set the inReplyTo
		reply.setInReplyTo(msg.getReplyWith());
		// Set the Protocol.
		reply.setProtocol(msg.getProtocol());
		// Set ReplyWith if not yet set
		if (reply.getReplyWith() == null)
			reply.setReplyWith(myAgent.getName() + java.lang.System.currentTimeMillis()); 
		
		// Set the receivers if not yet set
		if (!reply.getAllReceiver().hasNext()) {
			boolean no_reply_to = true;
			Iterator it = msg.getAllReplyTo();
			while(it.hasNext()){
				no_reply_to=false;
				reply.addReceiver((AID)it.next());
			}
			if(no_reply_to) {
				reply.addReceiver(msg.getSender());
			}	
		}
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	// For persistence service
	protected ReplySender() {
	}
	//#APIDOC_EXCLUDE_END
}
