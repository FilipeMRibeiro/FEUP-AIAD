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

package sajas.sim.repast3;

import sajas.core.AID;
import sajas.core.Agent;
import sajas.domain.AMSService;
import sajas.domain.DFService;
import sajas.sim.AgentScheduler;
import sajas.wrapper.PlatformController;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;

/**
 * A SAJaS launcher for the Repast 3 simulation framework.
 * 
 * @author hlc
 *
 */
public abstract class Repast3Launcher extends SimModelImpl implements AgentScheduler {

	private Schedule schedule;
	private AgentAction agentAction;

	@Override
	public Schedule getSchedule() {
		return schedule;
	}
	
	@Override
	public void setup() {
		schedule = new Schedule();
	}
	
	@Override
	public void begin() {
		// initialize infrastructural components
		initializeJADEPlatform();
		
		// create action for agent scheduling
		agentAction = new AgentAction();
		schedule.scheduleActionBeginning(0, agentAction);
		
		// set scheduler for agents
		Agent.setAgentScheduler(this);
		PlatformController.setAgentScheduler(this);

		// launch JADE and the MAS
		launchJADE();
	}

	/**
	 * Sets up infrastructural components (AMS and DF services).
	 */
	private void initializeJADEPlatform() {
		AMSService.initialize();
		DFService.initialize();
		
		// set platform ID to the name of the simulation
		AID.setPlatformID(this.getName());
	}

	/**
	 * Launch JADE and the multi-agent system related with this simulation.
	 * This method is invoked after the simulation has been setup.
	 * Subclasses should include in this method every JADE-related startup code (JADE runtime, agents, ...).
	 */
	protected abstract void launchJADE();

	@Override
	public void scheduleAgent(Agent agent) {
		agentAction.addAgent(agent);
	}
	
	@Override
	public boolean unscheduleAgent(Agent agent) {
		return agentAction.removeAgent(agent);
	}
	
	@Override
	public void stopSimulation() {
		stop();
	}
	
}
