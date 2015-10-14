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

package sajas.core.behaviours;

//#APIDOC_EXCLUDE_FILE

import sajas.core.Agent;

import jade.lang.acl.ACLMessage;

/**
 * @see jade.core.behaviours.SenderBehaviour
 * @author hlc
 *
 */
public final class SenderBehaviour extends OneShotBehaviour {


  // The ACL message to send
	/**
	@serial
	*/
  private ACLMessage message;

  /**
     Send a given ACL message. This constructor creates a
     <code>SenderBehaviour</code> which sends an ACL message.
     @param a The agent this behaviour belongs to, and that will
     <code>send()</code> the message.
     @param msg An ACL message to send.
  */
  public SenderBehaviour(Agent a, ACLMessage msg) {
    super(a);
    message = msg;
    if (msg != null)
	message.setSender(myAgent.getAID());
  }

  /**
     Actual behaviour implementation. This method sends an ACL
     message, using either the given <code>AgentGroup</code> or the
     <code>:receiver</code> message slot to get the message recipient
     names.
  */
  public void action() {
    myAgent.send(message);
  }

}
