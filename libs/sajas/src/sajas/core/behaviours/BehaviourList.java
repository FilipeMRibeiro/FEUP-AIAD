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
import jade.util.leap.Serializable;

/**
 * @see jade.core.behaviours.BehaviourList
 * @author hlc
 *
 */
class BehaviourList extends LinkedList implements Serializable {
  	private int current = 0;
  	
  	/** 
  	 * Add a Behaviour to the tail of the list. This does not require
  	 * adjusting the current index
  	 */
  	public synchronized void addElement(Behaviour b) {
  		add(b);
  	}
  	
    /** 
     * Remove b from the list. If b was in the list, return true
     * otherwise return false.
     * This requires adjusting the current index in the following cases:
     * - the index of the removed Behaviour is < than the current index
     * - the removed Behaviour is the current one and it is also 
     * the last element of the list. In this case the current index
     * must be set to 0
     */
  	public synchronized boolean removeElement(Behaviour b) {
  		int index = indexOf(b);
    	if(index != -1) {
      		remove(b);
      		if (index < current) {
				--current;
      		}
      		else if (index == current && current == size()) {
      			current = 0;
      		}
    	}
    	return index != -1;
  	}
  	
  	/**
  	   Get the current behaviour
  	*/
  	public Behaviour getCurrent() {
  		Behaviour b = null;
  		try {
  			b = (Behaviour) get(current);
  		}
  		catch (IndexOutOfBoundsException ioobe) {
  			// Just do nothing. Null will be returned
  		}
  		return b;
  	}

  	/**
  	   Set the current index to the beginning of the list
  	*/
  	public synchronized void begin() {
  		current = 0;
  	}

  	/**
  	   Check whether the current behaviour is the last in the list
  	*/
  	private boolean currentIsLast() {
  		return (current == (size() - 1));
  	}
  	
  	/** 
  	 * Advance the current index (taking into account wrap around)
  	 * and return the new current Behaviour
  	 */ 
  	public synchronized Behaviour next() {
  		if (currentIsLast() || isEmpty()) {
  			current = 0;
  		}
  		else {
  			current++;
  		}
  		return getCurrent();
  	}
  	
}
  
 
