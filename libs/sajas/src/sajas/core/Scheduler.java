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

package sajas.core;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import sajas.core.behaviours.Behaviour;

/**
 * An agent's internal behaviour scheduler.
 * This version has been simplified as compared to JADE's.
 * 
 * Note: this class has been re-implemented to redirect the use of the behaviour class to <code>sajas.core.behaviours.Behaviour</code>.
 * 
 * @see jade.core.Scheduler
 * @author hlc
 *
 */
class Scheduler {
	
	protected List<Behaviour> readyBehaviours = new LinkedList<Behaviour>();
	protected List<Behaviour> blockedBehaviours = new LinkedList<Behaviour>();
	private int currentIndex;
	
	public Scheduler() {
		currentIndex = 0;
	}
	
	public synchronized void add(Behaviour b) {
		readyBehaviours.add(b);
	}
	
	public synchronized void block(Behaviour b) {
		if (removeFromReady(b)) {
			blockedBehaviours.add(b);
		}
	}
	
	public synchronized void restart(Behaviour b) {
		if (removeFromBlocked(b)) {
			readyBehaviours.add(b);
		}
	}
	
	public synchronized void restartAll() {
		
		// ready behaviours
		// must (?) copy list of behaviours to avoid ConcurrentModification exceptions
		List<Behaviour> behaviours = new ArrayList<Behaviour>(readyBehaviours);
		for(Behaviour b : behaviours) {
			b.restart();
		}

		// blocked behaviours
		// must (?) copy list of behaviours to avoid ConcurrentModification exceptions
		behaviours = new ArrayList<Behaviour>(blockedBehaviours);
		for(Behaviour b : behaviours) {
			b.restart();
		}
	}
	
	public synchronized void remove(Behaviour b) {
		boolean found = removeFromBlocked(b);
		if(!found) {
			found = removeFromReady(b);
		}
	}
	
	public synchronized Behaviour schedule() throws InterruptedException {
		if(readyBehaviours.size() > 0) {
			Behaviour b = readyBehaviours.get(currentIndex);
			currentIndex = (currentIndex + 1) % readyBehaviours.size();
			return b;
		} else {
			return null;
		}
	}
	
	private boolean removeFromBlocked(Behaviour b) {
		return blockedBehaviours.remove(b);
	}
	
	private boolean removeFromReady(Behaviour b) {
		int index = readyBehaviours.indexOf(b);
		if(index != -1) {
			readyBehaviours.remove(b);
			if(index < currentIndex)
				--currentIndex;
			else if (index == currentIndex && currentIndex == readyBehaviours.size())
				currentIndex = 0;
		}
		return index != -1;
	}
	
}
