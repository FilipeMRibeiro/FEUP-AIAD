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
public class SequentialBehaviour extends SerialBehaviour {

	private List subBehaviours = new ArrayList();
	int current = 0;

	/**
     Default constructor. It does not set the owner agent for this
     behaviour.
	 */
	public SequentialBehaviour() {
	}

	/**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
	 */
	public SequentialBehaviour(Agent a) {
		super(a);
	}

	/**
     Prepare the first child for execution
     @see jade.core.behaviours.CompositeBehaviour#scheduleFirst
	 */
	protected void scheduleFirst() {
		// Schedule the first child
		current = 0;
	}

	/**
     Sequential policy for children scheduling. This method schedules
     children behaviours one at a time, in a FIFO fashion.
     @see jade.core.behaviours.CompositeBehaviour#scheduleNext(boolean, int)
	 */
	protected void scheduleNext(boolean currentDone, int currentResult) {
		if (currentDone) {
			// Schedule the next child only if the current one is terminated
			current++;
		}
	}

	/**
     Check whether this <code>SequentialBehaviour</code> must terminate.
     @return true when the last child has terminated. false otherwise
     @see jade.core.behaviours.CompositeBehaviour#checkTermination
	 */
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		return (currentDone && current >= (subBehaviours.size()-1));
	}

	/** 
     Get the current child
     @see jade.core.behaviours.CompositeBehaviour#getCurrent
	 */
	protected Behaviour getCurrent() {
		Behaviour b = null;
		if (subBehaviours.size() > current) {
			b = (Behaviour) subBehaviours.get(current);
		}
		return b;
	}

	/**
     Return a Collection view of the children of 
     this <code>SequentialBehaviour</code> 
     @see jade.core.behaviours.CompositeBehaviour#getChildren
	 */
	public Collection getChildren() {
		return subBehaviours;
	}

	/** 
     Add a sub behaviour to this <code>SequentialBehaviour</code>
	 */
	public void addSubBehaviour(Behaviour b) {
		subBehaviours.add(b);
		b.setParent(this);
		b.setAgent(myAgent);
	}

	/** 
     Remove a sub behaviour from this <code>SequentialBehaviour</code>
	 */
	public void removeSubBehaviour(Behaviour b) {
		boolean rc = subBehaviours.remove(b);
		if(rc) {
			b.setParent(null);
		}
		else {
			// The specified behaviour was not found. Do nothing
		}
	}

	public void reset() {
		super.reset();
		current = 0;
	}

	//#APIDOC_EXCLUDE_BEGIN

	public void skipNext() {
		current = subBehaviours.size();
	}

	//#APIDOC_EXCLUDE_END


	//#MIDP_EXCLUDE_BEGIN

	// For persistence service
	private Behaviour[] getSubBehaviours() {
		Object[] objs = subBehaviours.toArray();
		Behaviour[] result = new Behaviour[objs.length];
		for(int i = 0; i < objs.length; i++) {
			result[i] = (Behaviour)objs[i];
		}

		return result;
	}

	// For persistence service
	private void setSubBehaviours(Behaviour[] behaviours) {
		subBehaviours.clear();
		for(int i = 0; i < behaviours.length; i++) {
			subBehaviours.add(behaviours[i]);
		}
	}

	// For persistence service
	private int getCurrentIndex() {
		return current;
	}

	// For persistence service
	private void setCurrentIndex(int idx) {
		current = idx;
	}


	//#MIDP_EXCLUDE_END


}



