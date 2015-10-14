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

import sajas.core.*;
import sajas.core.behaviours.*;
import sajas.lang.acl.ConversationList;
import jade.lang.acl.*;

/**
 * Note: this class has been re-implemented to redirect the use of the agent, behaviour and conversation list classes to SAJaS versions.
 * 
 * @see jade.proto.SSResponderDispatcher
 * @author hlc
 *
 */
public abstract class SSResponderDispatcher extends CyclicBehaviour {
	private ConversationList activeConversations;
	private MessageTemplate template;
	
	public SSResponderDispatcher(Agent a, MessageTemplate tpl) {
		super(a);
		activeConversations = new ConversationList(a);
		template = MessageTemplate.and(
				tpl,
				activeConversations.getMessageTemplate());
	}
	
	public final void action() {
		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {
			// Be sure a conversation-id is set. If not create a suitable one
			if (msg.getConversationId() == null) {
				msg.setConversationId(createConversationId(myAgent.getLocalName()));
			}
			final String convId = msg.getConversationId();				
			Behaviour ssResponder = createResponder(msg);
			if (ssResponder != null) {
				activeConversations.registerConversation(convId);
				SequentialBehaviour sb = new SequentialBehaviour() {
					private static final long serialVersionUID = 12345678L;
					
					public int onEnd() {
						activeConversations.deregisterConversation(convId);
						return super.onEnd();
					}
				};
				sb.setBehaviourName(convId+"-Responder");
				sb.addSubBehaviour(ssResponder);
				addBehaviour(sb);
			}
		}
		else {
			block();
		}
	}
	
	/**
	 * This method is responsible for creating a suitable <code>Behaviour</code> acting as responder
	 * in the interaction protocol initiated by message <code>initiationMsg</code>.
	 * @param initiationMsg The message initiating the interaction protocol
	 * @return
	 */
	protected abstract Behaviour createResponder(ACLMessage initiationMsg); 
	
	protected void addBehaviour(Behaviour b) {
		myAgent.addBehaviour(b);
	}
	
	private static long cnt = 0;
	private synchronized static String createConversationId(String name) {
		return "C-"+name+'-'+System.currentTimeMillis()+'-'+(cnt++);
	}
}
