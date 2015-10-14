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

import sajas.core.*;

import java.util.Date;

/**
 * @see jade.core.behaviours.WakerBehaviour
 * @author hlc
 *
 */
public abstract class WakerBehaviour extends SimpleBehaviour {
	
	//private static final long MINIMUM_TIMEOUT = 10000; // 1 second
	
	/**
	 @serial
	 */
	private long wakeupTime, blockTime, timeout;
	/**
	 @serial
	 */
	private int state;
	/**
	 @serial
	 */
	private boolean finished;
	
	/**
	 * This method constructs the behaviour.
	 * @param a is the pointer to the agent
	 * @param wakeupDate is the date when the task must be executed
	 */
	public WakerBehaviour(Agent a, Date wakeupDate) {
		super(a);
		timeout = 0;
		wakeupTime = wakeupDate.getTime();
		state = 0;
		finished = false;
	}
	
	/**
	 * This method constructs the behaviour.
	 * @param a is the pointer to the agent
	 * @param timeout indicates the number of milliseconds after which the
	 * task must be executed
	 */
	public WakerBehaviour(Agent a, long timeout) {
		super(a);
		wakeupTime = -1;
		this.timeout = timeout;
		state = 0;
		finished = false;
	}
	
	public final void action() {
		if (!finished) {
			switch (state) {
			case 0: {
				// Adjust wakeupTime in case the user set a relative time
				if (wakeupTime == -1) {
					wakeupTime = System.currentTimeMillis()+timeout;
				}
				// in this state the behaviour blocks itself
				blockTime = wakeupTime - System.currentTimeMillis();
				if (blockTime > 0) // MINIMUM_TIMEOUT)
					//blockTime = MINIMUM_TIMEOUT;
					block(blockTime);
				state++;
				break;
			}
			case 1: {
				// in this state the behaviour can be restarted for two reasons
				// 1. the timeout is elapsed (then the handler method is called 
				//                            and the behaviour is definitively finished) 
				// 2. a message has arrived for this agent (then it blocks again and
				//                            the FSM remains in this state)
				blockTime = wakeupTime - System.currentTimeMillis();
				if (blockTime <= 0) {
					// timeout is expired
					finished = true;
					onWake();
				} else 
					block(blockTime);
				break;
			}
			default : {
				state=0;
				break;
			}
			} // end of switch
		}
	} //end of action
	
	/**
	 This method is invoked when the deadline defined in the
	 constructor is reached (or when the timeout specified in the 
	 constructor expires).
	 Subclasses are expected to define this method specifying the action
	 that must be performed at that time.
	 */
	protected void onWake() {
		handleElapsedTimeout();
	}
	
	/**
	 * @deprecated Use onWake() instead
	 */
	protected void handleElapsedTimeout() {
	}
	
	/**
	 * This method must be called to reset the behaviour and starts again
	 * @param wakeupDate is the new time when the task must be executed again
	 */
	public void reset(Date wakeupDate) {
		reset();
		wakeupTime = wakeupDate.getTime();
	}
	
	/**
	 * This method must be called to reset the behaviour and starts again
	 * @param timeout indicates in how many milliseconds from now the behaviour
	 * must be waken up again. 
	 */
	public void reset(long timeout) {
		reset();
		this.timeout = timeout;
	}
	
	/**
	 * This method must be called to reset the behaviour and starts again
	 */
	public void reset() {
		super.reset();
		wakeupTime = -1;
		state = 0;
		finished = false;
	}
	
	/**
	 * Make this WakerBehaviour terminate without calling the onWake() method.
	 * Calling stop() has the same effect as removing this WakerBehaviour, but is Thread safe
	 */
	public void stop() {
		finished = true;
		restart();
	}
	
	public final boolean done() {
		return finished;
	}
	
	public final long getWakeupTime() {
		return wakeupTime;
	}
}
