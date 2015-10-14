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

package sajas.wrapper;

import java.util.ArrayList;
import java.util.List;

import sajas.core.Agent;

import jade.wrapper.ControllerException;

/**
 * The controller for the SAJaS platform.
 * It provides a means to stop the platform (and simulation), similar to JADE's API.
 * 
 * @see jade.wrapper.PlatformController
 * @author hlc
 *
 */
public class PlatformController {

	// the scheduler for agents
	private static sajas.sim.AgentScheduler agentScheduler = new sajas.sim.AgentScheduler() {
		public void scheduleAgent(Agent agent) {}
		public boolean unscheduleAgent(Agent agent) { return false; }
		public void stopSimulation() {}
	};
	
	/**
	 * Sets the agent scheduler.
	 * This method is used by simulation launchers to set the simulation engine-specific scheduler.
	 * Programmers should have no need to use this method.
	 * @exclude
	 */
	public static void setAgentScheduler(sajas.sim.AgentScheduler as) {
		PlatformController.agentScheduler = as;
	}
	
	// list of containers in the platform
	private List<ContainerController> containerControllers = new ArrayList<ContainerController>();

	/**
	 * Used by sajas.core.Runtime to add ContainerControllers as containers are created.
	 * Programmers should have no need to use this method.
	 * @param containerController
	 * @exclude
	 */
	public void addContainerController(ContainerController containerController) {
		containerControllers.add(containerController);
	}
	
	/**
	 * @see jade.wrapper.PlatformController#kill()
	 */
	public void kill() throws ControllerException {
		for(ContainerController containerController : containerControllers) {
			containerController.kill();
		}
		
		// terminate simulation
		agentScheduler.stopSimulation();
	}
    
}
