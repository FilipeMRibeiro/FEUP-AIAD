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

package sajas.sim.repasts;

import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;

import sajas.core.AID;
import sajas.core.Agent;
import sajas.domain.AMSService;
import sajas.domain.DFService;
import sajas.sim.AgentScheduler;
import sajas.wrapper.PlatformController;

/**
 * A SAJaS launcher for the Repast Simphony simulation framework.
 * 
 * @author hlc
 *
 */
public abstract class RepastSLauncher implements ContextBuilder<Object>, AgentScheduler {
	
	private Context<Object> mainContext;
	private AgentAction agentAction;
	
	/**
	 * Gets the name of the simulation.
	 * @return
	 */
	public abstract String getName();

	@Override
	public Context build(Context<Object> context) {
		// store context for adding agents at runtime
		mainContext = context;
		
		// initialize infrastructural components
		initializeJADEPlatform();
		
		// create action for behaviour scheduling
		ScheduleParameters params = ScheduleParameters.createRepeating(1, 1);
		agentAction = new AgentAction();
		RunEnvironment.getInstance().getCurrentSchedule().schedule(params, agentAction);
		context.add(agentAction);
		
		// set behaviour scheduler for agents
		Agent.setAgentScheduler(this);
		PlatformController.setAgentScheduler(this);
		
		// launch JADE and the MAS
		launchJADE();
		
		return context;
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
		mainContext.add(agent);
		agentAction.addAgent(agent);
	}
	
	@Override
	public boolean unscheduleAgent(Agent agent) {
		mainContext.remove(agent);
		return agentAction.removeAgent(agent);
	}
	
	@Override
	public void stopSimulation() {
		RunEnvironment.getInstance().endRun();		
	}

}
