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

//#CUSTOM_EXCLUDE_FILE

import jade.util.leap.*;

import sajas.core.Agent;

/**
 * @see jade.core.behaviours.SerialBehaviour
 * @author hlc
 *
 */
public abstract class SerialBehaviour extends CompositeBehaviour {

	/**
       Create a new <code>SerialBehaviour</code> object, without
       setting the owner agent.
	 */
	protected SerialBehaviour() {
		super();
	}

	/**
       Create a new <code>SerialBehaviour</code> object and set the
       owner agent.
       @param a The agent owning this behaviour.
	 */
	protected SerialBehaviour(Agent a) {
		super(a);
	}


	//#APIDOC_EXCLUDE_BEGIN

	/**
     Handle block/restart notifications. A
     <code>SerialBehaviour</code> is blocked <em>only</em> when
     its currently active child is blocked, and becomes ready again
     when its current child is ready. This method takes care of the
     various possibilities.

     @param rce The event to handle.
	 */
	protected void handle(RunnableChangedEvent rce) {
		if(rce.isUpwards()) {
			// Upwards notification
			if (rce.getSource() == this) {
				// If the event is from this behaviour, set the new 
				// runnable state and notify upwords.
				super.handle(rce);
			}
			else if (rce.getSource() == getCurrent()) {
				// If the event is from the currently executing child, 
				// create a new event, set the new runnable state and
				// notify upwords.
				myEvent.init(rce.isRunnable(), NOTIFY_UP);
				super.handle(myEvent);
			}
			else {
				// If the event is from another child, just ignore it
			}
		}
		else {
			// Downwards notifications 
			// Copy the state and pass it downwords only to the
			// current child
			setRunnable(rce.isRunnable());
			Behaviour b  = getCurrent();
			if (b != null) {
				b.handle(rce);
			}
		}  	
	}

	//#APIDOC_EXCLUDE_END

}



