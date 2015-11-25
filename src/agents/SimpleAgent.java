package agents;

import jade.wrapper.ControllerException;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.OneShotBehaviour;

public class SimpleAgent extends Agent {

	public void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		// Add the CyclicBehaviour
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			public void action() {
				System.out.println("Cycling");
			}
		});

		// Add the generic behavior
		addBehaviour(new FourStepBehaviour());
	}

	public void afterMove() {
		System.out.println(this.getLocalName() + " just moved here.");
	}

	/**
	 * Inner class FourStepBehaviour
	 */
	private class FourStepBehaviour extends Behaviour {

		private static final long serialVersionUID = 1L;

		private int step = 1;

		public void action() {
			switch (step) {
			case 1:
				// Perform operation 1: print out a message
				System.out.println("Operation 1");
				break;
			case 2:
				// Perform operation 2: Add a OneShotBehaviour
				System.out.println("Operation 2. Adding one-shot behaviour");
				myAgent.addBehaviour(new OneShotBehaviour(myAgent) {
					private static final long serialVersionUID = 1L;

					public void action() {
						System.out.println("One-shot");
					}
				});
				break;
			case 3:
				// Perform operation 3: print out a message
				System.out.println("Operation 3");
				break;
			case 4:
				// Perform operation 3: print out a message
				System.out.println("Operation 4");
				break;
			}
			step++;
		}

		public boolean done() {
			return step == 5;
		}

		public int onEnd() {
			// shutdown
			try {
				myAgent.getContainerController().getPlatformController().kill();
			} catch (ControllerException e) {
				e.printStackTrace();
			}

			myAgent.doDelete();

			return super.onEnd();
		}

	}

}
