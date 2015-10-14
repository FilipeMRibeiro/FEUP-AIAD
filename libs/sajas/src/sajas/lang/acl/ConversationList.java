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

package sajas.lang.acl;

import sajas.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Serializable;

import java.util.Vector;

/**
 * Note: this class has been re-implemented to redirect the use of the agent class to SAJaS versions.
 * 
 * @see sajas.lang.acl.ConversationList
 */
public class ConversationList implements Serializable{
	private Vector conversations = new Vector();
	protected Agent myAgent = null;
	protected int cnt = 0;
	
	private MessageTemplate myTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
		public boolean match(ACLMessage msg) {
			String convId = msg.getConversationId();
			return (convId == null || (!conversations.contains(convId)));
		}
	} );
	
	/**
	   Construct a ConversationList to be used inside a given agent.
	 */
	public ConversationList(Agent a) {
		myAgent = a;
	}
	
	/**
	   Register a conversation creating a new unique ID.
	 */
	public String registerConversation() {
		String id = createConversationId();
		conversations.addElement(id);
		return id;
	}
	
	/**
	   Register a conversation with a given ID.
	 */
	public void registerConversation(String convId) {
		if (convId != null) {
			conversations.addElement(convId);
		}
	}
	
	/**
	   Deregister a conversation with a given ID.
	 */
	public void deregisterConversation(String convId) {
		if (convId != null) {
			conversations.removeElement(convId);
		}
	}

	/**
	   Deregister all conversations.
	 */
	public void clear() {
		conversations.removeAllElements();
	}
	
	/**
	   Return a template that matches only messages that do not belong to 
	   any of the conversations in this list.
	 */
	public MessageTemplate getMessageTemplate() {
		return myTemplate;
	}

	public String toString() {
		return "CL"+conversations;
	}
	
	protected String createConversationId() {
		return myAgent.getName()+(cnt++);
	}
}
		