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

import jade.content.ContentManager;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sajas.core.behaviours.Behaviour;
import sajas.domain.AMSService;
import sajas.domain.DFService;
import sajas.wrapper.ContainerController;

/**
 * This is the base class for implementing agents.
 * Agent scheduling is simulation-platform-specific: it is handled through a static <code>AgentScheduler</code>,
 * which is set by SAJaS when launching the simulation.
 * 
 * Note: this class does not extend <code>jade.core.Agent</code> due to non-public method declarations that needed to be re-implemented.
 * 
 * @see jade.core.Agent
 * @author hlc
 *
 */
public class Agent {
	
	private String myName = null;  
	private AID myAID = null;
	private String myHap = null;

	private Scheduler myScheduler = new Scheduler();

	private MessageQueue mailBox = new MessageQueue();

	private transient Object[] arguments = null;

	/**
	 * @see jade.core.Agent#setArguments(Object[])
	 */
	public final void setArguments(Object args[]) {
		arguments = args;
	}

	/**
	 * @see jade.core.Agent#getArguments()
	 */
	public Object[] getArguments() {
		return arguments;
	}
	
	/**
	 * @see jade.core.Agent#addBehaviour(jade.core.behaviours.Behaviour)
	 */
	public void addBehaviour(Behaviour b) {
		b.setAgent(this);
		myScheduler.add(b);
	}

	/**
	 * @see jade.core.Agent#removeBehaviour(jade.core.behaviours.Behaviour)
	 * @see jade.core.Scheduler#remove(jade.core.behaviours.Behaviour)
	 */
	public void removeBehaviour(Behaviour b) {
		myScheduler.remove(b);
	}
	
	/**
	 * @see jade.core.Agent#setup()
	 */
	protected void setup() {
	}

	/**
	 * @see jade.core.Agent#takeDown()
	 */
	protected void takeDown() {
	}

	/**
	 * @see jade.core.Agent#doDelete()
	 */
	public void doDelete() {
		alive = false;
	}

	/**
	 * @see jade.core.Agent#setAID(jade.core.AID)
	 */
	public void setAID(AID aid) {
		myName = aid.getLocalName();
		myHap = aid.getHap();
		this.myAID = aid;
	}
	
	/**
	 * @see jade.core.Agent#getAID()
	 */
	public final AID getAID() {
		return myAID;
	}
	
	/**
	 * @see jade.core.Agent#getLocalName()
	 */
	public final String getLocalName() {
		return myName;
	}
	
	/**
	 * @see jade.core.Agent#getName()
	 */
	public final String getName() {
		if (myHap != null) {
			return myName + '@' + myHap;
		}
		else {
			return myName;
		}
	}
	
	/**
	 * @see jade.core.Agent#getAMS()
	 */
	public final AID getAMS() {
		return AMSService.amsAID;  
	}

	/**
	 * @see jade.core.Agent#getDefaultDF()
	 */
	public AID getDefaultDF() {
		return DFService.getDFAID();
	}

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
		Agent.agentScheduler = as;
	}

	/**
	 * Starts this agent by adding it to the agent scheduler.
	 */
	public void start() {
		agentScheduler.scheduleAgent(this);
		
		// for agent immediate availability (e.g. DF registration, ...)
		step();
	}


	private boolean firstStep = true;
	private boolean alive = true;
	
	/**
	 * Note that this implementation does not take into account agent life-cycle management.
	 * @see jade.core.Agent#run()
	 */
	public final void step() {
		if(firstStep) {
			setup();
			firstStep = false;
			return;
		}
		
		if(alive) {
			Behaviour currentBehaviour;
			try {
				currentBehaviour = myScheduler.schedule();
				// might get a null behaviour if no behaviour is ready
				if(currentBehaviour != null) {
					long oldRestartCounter = currentBehaviour.getRestartCounter();

					currentBehaviour.actionWrapper();
					if(currentBehaviour.done()) {
						currentBehaviour.onEnd();   // ignoring return value
						myScheduler.remove(currentBehaviour);
					} else {
						if(oldRestartCounter != currentBehaviour.getRestartCounter()) {
							currentBehaviour.handleRestartEvent();
						}

						if(!currentBehaviour.isRunnable()) {
							myScheduler.block(currentBehaviour);
						}
					}
				} else {
					// no ready behaviours: unschedule agent
					agentScheduler.unscheduleAgent(this);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			takeDown();
			agentScheduler.unscheduleAgent(this);
			myContainer.removeAgent(this);
		}
	}
	
	/**
	 * @see jade.core.Agent#send(jade.lang.acl.ACLMessage)
	 */
	public final void send(ACLMessage msg) {
		if (msg != null) {
			msg.setSender(getAID());
			AMSService.send(msg, true, false);
		}
	}
	
	/**
	 * @see jade.core.Agent#receive()
	 */
	public final ACLMessage receive() {
		return receive(null);
	}

	/**
	 * @see jade.core.Agent#receive(jade.lang.acl.MessageTemplate)
	 */
	public final ACLMessage receive(MessageTemplate template) {
		return mailBox.receive(template);
	}

	/**
	 * @see jade.core.Agent#blockingReceive()
	 */
	public final ACLMessage blockingReceive() {
		return blockingReceive(null, 0);
	}

	/**
	 * @see jade.core.Agent#blockingReceive(long)
	 */
	public final ACLMessage blockingReceive(long millis) {
		return blockingReceive(null, millis);
	}

	/**
	 * @see jade.core.Agent#blockingReceive(jade.lang.acl.MessageTemplate)
	 */
	public final ACLMessage blockingReceive(MessageTemplate pattern) {
		return blockingReceive(pattern, 0);
	}

	/**
	 * @see jade.core.Agent#blockingReceive(jade.lang.acl.MessageTemplate, long)
	 */
	public final ACLMessage blockingReceive(MessageTemplate pattern, long millis) {
		System.err.println("WARNING: SAJaS does not currently support blocking approaches -- returning null");
		return null;
	}

	/**
	 * @see jade.core.Agent#postMessage(jade.lang.acl.ACLMessage)
	 */
	public final void postMessage(final ACLMessage msg) {
		mailBox.add(msg);
		myScheduler.restartAll();

		// re-schedule agent
		agentScheduler.scheduleAgent(this);
		// FIXME: should probably do this in notifyRestarted(...), since there may be causes other than receiving messages for restarting behaviours.
		// But a problem is that notifyRestarted(...) will be called many times for a single restartAll() invocation.
	}


	// the container controller
	private /*transient*/ ContainerController myContainer = null;
	
	/**
	 * Used by sajas.wrapper.ContainetController to appropriately set the ContainerController of this agent.
	 * Programmers should have no need to use this method.
	 * @param containerController
	 * @exclude
	 */
	public void setContainerController(ContainerController containerController) {
		this.myContainer = containerController;
	}

	/**
	 * @see jade.core.Agent#getContainerController()
	 */
	public ContainerController getContainerController() {
		return myContainer;
	}
	
	
	// the content manager
	private ContentManager theContentManager = null;

	/**
	 * @see jade.core.Agent#getContentManager()
	 */
	public ContentManager getContentManager() {
		if (theContentManager == null) {
			theContentManager = new ContentManager();
		}
		return theContentManager;
	}

	/**
	 * @see jade.core.Agent#restartLater(jade.core.behaviours.Behaviour, long)
	 */
	public void restartLater(Behaviour b, long millis) {
		// XXX
	}
	
	/**
	 * @see jade.core.Agent#notifyRestarted(jade.core.behaviours.Behaviour)
	 */
	public void notifyRestarted(Behaviour b) {
		Behaviour root = b.root();
		if(root.isRunnable()) {
			myScheduler.restart(root);
		}
	}
	
	/**
	 * @see jade.core.Agent#notifyChangeBehaviourState(jade.core.behaviours.Behaviour, String, String)
	 */
	public void notifyChangeBehaviourState(Behaviour b, String from, String to) {
		b.setExecutionState(to);
		// XXX
	}

	/**
	 * @see jade.core.Agent#removeTimer(jade.core.behaviours.Behaviour)
	 */
	public void removeTimer(Behaviour b) {
		// XXX
	}
	
	
	/**
	 * This class stores all mail addressed to this agent.
	 * @author joaolopes, hlc
	 *
	 */
	private class MessageQueue {
		
		private List<ACLMessage> messages = new ArrayList<ACLMessage>();
		
		public MessageQueue() {
		}

		/**
		 * Adds a new message to the mail box. 
		 * @param message
		 */
		public void add(ACLMessage message) {
			messages.add(message);
		}

		/**
		 * Uses a template to retrieve the first matching message from the queue.
		 * This message will be then removed from the box.
		 * @param template
		 * @see jade.core.InternalMessageQueue#receive(jade.lang.acl.MessageTemplate)
		 * @return
		 */
		public ACLMessage receive(MessageTemplate pattern) {
			ACLMessage msg;
			for(Iterator<ACLMessage> it = messages.iterator(); it.hasNext();) {
				msg = (ACLMessage) it.next();
				if(pattern == null || pattern.match(msg)) {
					it.remove();
					return msg;
				}
			}
			
			return null;
		}
		
	}

}
