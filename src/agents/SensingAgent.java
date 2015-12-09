package agents;

import java.awt.Color;

import entities.Water;
import jade.lang.acl.ACLMessage;
import launcher.OurModel;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class SensingAgent extends Agent implements Drawable {

	private final OurModel model;

	private int x, y;
	private float batteryLevel;
	private Color color;
	private float lastSamplePollutionLevel;

	public SensingAgent(int x, int y, OurModel model) {
		this.x = x;
		this.y = y;
		this.batteryLevel = 100;
		this.color = Color.GREEN;
		this.lastSamplePollutionLevel = 0;

		this.model = model;
	}

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		if (getLocalName().equals("S-30")) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(model.getSensorsList().get(0).getAID());
			msg.setLanguage("English");
			msg.setOntology("Weather-forecast-ontology");
			msg.setContent("Today it’s raining");
			send(msg);
		}

		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				if (batteryLevel > 0)
					sampleEnvironment();

				updateBatteryLevel();
			}
		});

		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = myAgent.receive();

				if (msg != null) {
					System.out.println("Received message from agent " + msg.getSender().getName());

					batteryLevel = 0;
				} else {
					block();
				}
			}
		});
	}

	public void sampleEnvironment() {
		lastSamplePollutionLevel = ((Water) model.getRiver().getObjectAt(x, y)).getPollution();
	}

	private void updateBatteryLevel() {
		batteryLevel -= 0.1; // 0.01;

		if (batteryLevel < 20)
			color = Color.RED;
		else if (batteryLevel < 60)
			color = Color.YELLOW;
		else
			color = Color.GREEN;

		if (batteryLevel < 0)
			batteryLevel = 0;
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastRect(color);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public float getBatteryLevel() {
		return batteryLevel;
	}

	public float getLastSamplePollutionLevel() {
		return lastSamplePollutionLevel;
	}

}
