package agents;

import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import sajas.core.behaviours.WakerBehaviour;

public class TimeAgent extends Agent {

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		// Add the TickerBehaviour (period 1 sec)
		addBehaviour(new TickerBehaviour(this, 1000) {
			private static final long serialVersionUID = 1L;

			protected void onTick() {
				System.out.println("Agent " + myAgent.getLocalName() + ": tick=" + getTickCount());
			}
		});

		// Add the WakerBehaviour (wakeup-time 10 secs)
		addBehaviour(new WakerBehaviour(this, 10000) {
			private static final long serialVersionUID = 1L;

			protected void handleElapsedTimeout() {
				System.out.println("Agent " + myAgent.getLocalName() + ": It's wakeup-time. Bye...");
				myAgent.doDelete();
			}
		});
	}

}