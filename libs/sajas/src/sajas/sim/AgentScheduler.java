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

package sajas.sim;

import sajas.core.Agent;

/**
 * This interface represents the scheduler for agents in the simulation. It allows agents to be added to the simulation scheduler.
 * The specific scheduler to use is set statically in the <code>Agent</code> class.
 * 
 * @author hlc
 *
 */
public interface AgentScheduler {

	/**
	 * Simulation framework-specific method for scheduling an agent for execution.
	 */
	public void scheduleAgent(Agent agent);
	
	/**
	 * Simulation framework-specific method for unscheduling an agent from execution.
	 */
	public boolean unscheduleAgent(Agent agent);

	/**
	 * Simulation framework-specific method for stopping the simulation.
	 */
	public void stopSimulation();
	
}
