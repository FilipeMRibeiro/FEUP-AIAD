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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.engine.schedule.IAction;

import sajas.core.Agent;

/**
 * Action for Repast Simphony agent execution in the simulation.
 * 
 * @author hlc
 *
 */
public class AgentAction implements IAction {

	private List<Agent> agents;

	public AgentAction() {
		this.agents = new ArrayList<Agent>();
	}

	public void addAgent(Agent a) {
		if(!agents.contains(a)) {
			agents.add(a);
		}
	}

	public boolean removeAgent(Agent a) {
		return agents.remove(a);
	}

	@Override
	public void execute() {
		// cannot iterate directly because agents might be added/removed at runtime (throwing ConcurrentModicationException)
		List<Agent> scheduledAgents = new ArrayList<Agent>(agents);

		// shuffle agents
		Collections.shuffle(scheduledAgents);

		// execute agents
		for(Agent agent : scheduledAgents) {
			agent.step();
		}
	}

}
