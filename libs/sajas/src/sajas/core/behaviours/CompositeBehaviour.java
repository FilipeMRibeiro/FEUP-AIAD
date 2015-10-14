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

import jade.util.leap.*;

import sajas.core.Agent;

/**
 * @see jade.core.behaviours.CompositeBehaviour
 * @author hlc
 *
 */
public abstract class CompositeBehaviour extends Behaviour {

	/**
    This variable marks the state when no child-behaviour has been run yet.
	 */
	private boolean starting = true;
	/**
    This variable marks the state when all child-behaviours have been run.
	 */
	private boolean finished = false;

	private boolean currentDone;
	private int currentResult;

	//#APIDOC_EXCLUDE_BEGIN  
	protected boolean currentExecuted = false;
	//#APIDOC_EXCLUDE_END


	/**
     Default constructor, does not set the owner agent.
	 */
	protected CompositeBehaviour() {
		super();
	}

	/**
     This constructor sets the owner agent.
     @param a The agent this behaviour belongs to.
	 */
	protected CompositeBehaviour(Agent a) {
		super(a);
	} 

	/**
     Executes this <code>CompositeBehaviour</code>. This method 
     executes children according to the scheduling policy 
     defined by concrete subclasses that implements 
     the <code>scheduleFirst()</code> and <code>scheduleNext()</code>
     methods.
	 */
	public final void action() {
		if(starting) {
			scheduleFirst();
			starting = false;
		}
		else {
			if (currentExecuted) {
				scheduleNext(currentDone, currentResult);
			}
		}

		// Get the current child
		Behaviour current = getCurrent();
		currentExecuted = false;
		currentDone = false;
		currentResult = 0;

		if (current != null) {
			if (current.isRunnable()) {
				// Execute the current child
				current.actionWrapper();
				currentExecuted = true;

				// If it is done --> call its onEnd() method
				if (current.done()) {
					currentDone = true;
					currentResult = current.onEnd();
				}

				// Check if this CompositeBehaviour is finished
				finished = checkTermination(currentDone, currentResult);
			}
			else {
				// The currently scheduled child is not runnable --> This
				// Composite behaviour must block too and notify upwards
				myEvent.init(false, NOTIFY_UP);
				super.handle(myEvent);
			}
		}
		else {
			// There are no children to execute
			finished = true;
		}

	}

	/**
     Checks whether this behaviour has terminated.
     @return <code>true</code> if this <code>CompositeBehaviour</code>
     has finished executing, <code>false</code>otherwise.
	 */
	public final boolean done() {
		return (finished);
	}

	/**
	 * This method schedules the first child to be executed
	 */
	protected abstract void scheduleFirst();

	/**
	 * This method schedules the next child to be executed
	 * @param currentDone a flag indicating whether the just executed
	 * child has completed or not.
	 * @param currentResult the termination value (as returned by
	 * <code>onEnd()</code>) of the just executed child in the case this
	 * child has completed (otherwise this parameter is meaningless)
	 */
	protected abstract void scheduleNext(boolean currentDone, int currentResult);

	/**
	 * This methods is called after the execution of each child
	 * in order to check whether the <code>CompositeBehaviour</code>
	 * should terminate.
	 * @param currentDone a flag indicating whether the just executed
	 * child has completed or not.
	 * @param currentResult the termination value (as returned by
	 * <code>onEnd()</code>) of the just executed child in the case this
	 * child has completed (otherwise this parameter is meaningless)
	 * @return true if the <code>CompositeBehaviour</code>
	 * should terminate. false otherwise.
	 */
	protected abstract boolean checkTermination(boolean currentDone, int currentResult);

	/**
	 * This method returns the child behaviour currently 
	 * scheduled for execution
	 */
	protected abstract Behaviour getCurrent();

	/**
	 * This method returns a Collection view of the children of 
	 * this <code>CompositeBehaviour</code> 
	 */
	public abstract Collection getChildren();

	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	protected void handleBlockEvent() {
		// Notify upwards
		super.handleBlockEvent();

		// Then notify downwards
		myEvent.init(false, NOTIFY_DOWN);
		handle(myEvent);
	}

	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	public void handleRestartEvent() {
		// Notify downwards
		myEvent.init(true, NOTIFY_DOWN);
		handle(myEvent);

		// Then notify upwards
		super.handleRestartEvent();
	}
	//#APIDOC_EXCLUDE_END

	/**
     Puts a <code>CompositeBehaviour</code> back in initial state. The
     internal state is cleaned up and <code>reset()</code> is
     recursively called for each child behaviour. 
	 */
	public void reset() {
		resetChildren();

		starting = true;
		finished = false;
		super.reset();
	}

	protected void resetChildren() {
		Collection c = getChildren();
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				b.reset();
			}
		}
	}

	/**
     Associates this behaviour with the agent it belongs to.
     Overrides the method in the base class to propagate the
     setting to all children.
     @param a The agent this behaviour belongs to.
     @see jade.core.behaviours.Behaviour#setAgent(Agent a)
	 */
	public void setAgent(Agent a) {
		Collection c = getChildren();
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				b.setAgent(a);
			}
		}

		super.setAgent(a);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected void registerAsChild(Behaviour b) {
		b.setParent(this);
	}
	//#APIDOC_EXCLUDE_END

}
