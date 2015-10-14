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

import sajas.core.Agent;

/**
 * The controller for an agent.
 * It provides a means to start the agent, similar to JADE's API.
 * 
 * @see jade.wrapper.AgentController
 * @author hlc
 *
 */
public class AgentController {

	private Agent agent;

	/**
	 * This constructor is used internally when launching an agent in the platform.
	 * Programmers should have no need to use this method.
	 * @param agent
	 * @exclude
	 */
	protected AgentController(Agent agent) {
		this.agent = agent;
	}
	
//	/**
//	 * 
//	 * @return
//	 * @exclude
//	 */
//	public Agent getAgent() {
//		return agent;
//	}
	
	/**
	 * @see jade.wrapper.AgentController#start()
	 * @see jade.wrapper.AgentControllerImpl#start()
	 */
	public void start() {
		agent.start();
	}
	
}
