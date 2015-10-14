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

import jade.core.Profile;
import jade.wrapper.StaleProxyException;

import sajas.domain.DFAgent;
import sajas.wrapper.ContainerController;
import sajas.wrapper.PlatformController;

/**
 * SAJaS runtime, replicating JADE's API.
 * A current limitation is that an AMS agent is not launched in the main container.
 * However, the AMSService is available for (de)registering agents.
 * From my experience with JADE, however, interaction with the AMS or the AMS agent is seldom needed.
 * The DF agent and DFService are much more likely to be used, and are both available.
 * 
 * @see jade.core.Runtime
 * @author hlc
 *
 */
public class Runtime {

	private static Runtime theInstance = new Runtime();
	
	private PlatformController platformController = new PlatformController();
	
	private Runtime() {
	}
	
	/**
	 * @see jade.core.Runtime#instance()
	 */
	public static Runtime instance() {
		return theInstance;
	}

	/**
	 * @see jade.core.Runtime#createMainContainer(jade.core.Profile)
	 */
	public ContainerController createMainContainer(Profile p) {
		ContainerController main = new ContainerController();
		main.setPlaformController(platformController);
		platformController.addContainerController(main);
		
		try {
			// TODO launch AMS agent
			
			// launch DF agent
			main.acceptNewAgent("df", new DFAgent()).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		return main;
	}

	/**
	 * @see jade.core.Runtime#createAgentContainer(jade.core.Profile)
	 */
	public ContainerController createAgentContainer(Profile p) {
		ContainerController containerController = new ContainerController();
		containerController.setPlaformController(platformController);
		platformController.addContainerController(containerController);
		
		return containerController;
	}
	
}
