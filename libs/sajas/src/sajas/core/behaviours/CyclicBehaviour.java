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

import sajas.core.Agent;

/**
 * @see jade.core.behaviours.CyclicBehaviour
 * @author hlc
 *
 */
public abstract class CyclicBehaviour extends Behaviour {

	/**
	 * @see jade.core.behaviours.CyclicBehaviour#CyclicBehaviour()
	 */
	public CyclicBehaviour() {
		super();
	}

	/**
	 * @see jade.core.behaviours.CyclicBehaviour#CyclicBehaviour(jade.core.Agent)
	 */
	public CyclicBehaviour(Agent a) {
		super(a);
	}

	/**
	 * @see jade.core.behaviours.CyclicBehaviour#done()
	 */
	public final boolean done() {
		return false;
	}

}
