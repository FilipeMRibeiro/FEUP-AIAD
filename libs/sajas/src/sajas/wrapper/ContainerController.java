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

import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import sajas.core.Agent;
import sajas.core.AID;
import sajas.domain.AMSService;

/**
 * The controller for a container.
 * It provides means to create, add and remove agents, and to kill the container, similar to JADE's API.
 * 
 * @see jade.wrapper.ContainerController
 * @author hlc
 *
 */
public class ContainerController {

	// list of agents residing in this container
	private List<Agent> agents = new ArrayList<Agent>();
	
	/**
	 * @see jade.wrapper.ContainerController#createNewAgent(String, String, Object[])
	 */
	public AgentController createNewAgent(String nickname, String className, Object[] args) throws StaleProxyException {
		
		Agent agent = null;
		try {
			agent = (Agent) Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		agent.setArguments(args);

		AID aid = new AID(nickname, AID.ISLOCALNAME);
		if(AMSService.getAgent(aid) != null) {
			throw new StaleProxyException("Name-clash Agent " + nickname + " already present in the platform");
		}
		agent.setAID(aid);
		
		AMSService.register(agent);
		
		agent.setContainerController(this);
		agents.add(agent);

		return new AgentController(agent);
	}

	
	/**
	 * @see jade.wrapper.ContainerController#acceptNewAgent(String, jade.core.Agent)
	 */
	public AgentController acceptNewAgent(String nickname, Agent agent) throws StaleProxyException {

		AID aid = new AID(nickname, AID.ISLOCALNAME);
		if(AMSService.getAgent(aid) != null) {
			throw new StaleProxyException("Name-clash Agent " + nickname + " already present in the platform");
		}
		agent.setAID(aid);

		AMSService.register(agent);
//		AMSAgentDescription amsd = new AMSAgentDescription();
//		amsd.setName(aid);
//		amsd.setState(AMSAgentDescription.ACTIVE);	
//		try {
//			AMSService.register(agent, amsd);
//		} catch (FIPAException e) {
//			e.printStackTrace();
//		}
		
		agent.setContainerController(this);
		agents.add(agent);
		
		return new AgentController(agent);
	}
	
	/**
	 * Uased by sajas.core.Agent after terminating execution.
	 * Programmers should have no need to use this method.
	 * @param agent
	 * @exclude
	 */
	public void removeAgent(Agent agent) {
		agents.remove(agent);
	}
	
	/**
	 * @see jade.wrapper.ContainerController#kill()
	 */
	public void kill() throws StaleProxyException {
		List<Agent> currentAgents = new ArrayList<Agent>(agents);
		for(Agent agent : currentAgents) {
			agent.doDelete();
		}
	}
	
	// the platform controller
	private PlatformController platformController = null;
	
	/**
	 * Used by sajas.core.Runtime to appropriately set the PlatformController of this container controller.
	 * Programmers should have no need to use this method.
	 * @param platformController
	 * @exclude
	 */
	public void setPlaformController(PlatformController platformController) {
		this.platformController = platformController;
	}
	
	/**
	 * @see jade.wrapper.ContainerController#getPlatformController()
	 */
	public PlatformController getPlatformController() throws ControllerException {
		return platformController;
	}

}
