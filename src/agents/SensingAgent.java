package agents;

import java.awt.Color;
import java.util.ArrayList;

import entities.Water;
import jade.lang.acl.ACLMessage;
import launcher.OurModel;
import messages.Languages;
import messages.Ontologies;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

public class SensingAgent extends Agent implements Drawable {

	private final OurModel model;

	private int x, y;
	private State state;
	private float batteryLevel;
	private Color color;
	private float lastSamplePollutionLevel;
	private ArrayList<SensingAgent> neighbours;

	public SensingAgent(int x, int y, OurModel model) {
		this.x = x;
		this.y = y;
		state = State.ON;
		this.batteryLevel = 100;
		this.color = Color.GREEN;
		this.lastSamplePollutionLevel = 0;
		neighbours = new ArrayList<SensingAgent>();

		this.model = model;
	}

	public void buildNeighboursList() {
		for (SensingAgent sensor : model.getSensorsList()) {
			if (!sensor.getLocalName().equals(getLocalName())) {
				int deltaX = x - sensor.getX();
				int deltaY = y - sensor.getY();

				double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

				// TODO change this and make it a parameter for the gui
				if (distance <= 10)
					neighbours.add(sensor);
			}
		}
	}

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				if (batteryLevel > 0)
					sampleEnvironment();

				updateBatteryLevel();
			}
		});

		initMessageListener();
	}

	public void sampleEnvironment() {
		lastSamplePollutionLevel = ((Water) model.getRiver().getObjectAt(x, y)).getPollution();

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

		msg.setLanguage(Languages.INFORM);
		msg.setOntology(Ontologies.SAMPLE);
		msg.setContent(Float.toString(lastSamplePollutionLevel));

		for (SensingAgent sensor : neighbours) {
			msg.addReceiver(sensor.getAID());
			send(msg);
		}
	}

	private void updateBatteryLevel() {
		batteryLevel -= 0.1;

		if (batteryLevel < 0)
			batteryLevel = 0;
	}

	private void initMessageListener() {
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = myAgent.receive();

				if (msg != null) {
					switch (msg.getLanguage()) {
					case Languages.INFORM: {
						switch (msg.getOntology()) {
						case Ontologies.SAMPLE: {
							float receivedSample = Float.parseFloat(msg.getContent());

							System.out.println("Received sample: " + receivedSample);

							if (receivedSample > lastSamplePollutionLevel)
								sleep();

							break;
						}

						case Ontologies.ADHERENCE: {
							break;
						}

						case Ontologies.LEADERSHIP: {
							break;
						}

						default:
							break;
						}

						break;
					}

					case Languages.FIRM_ADHERENCE: {
						break;
					}

					case Languages.ACK_ADHERENCE: {
						break;
					}

					case Languages.BREAK: {
						break;
					}

					case Languages.WITHDRAW: {
						break;
					}

					default:
						break;
					}

					System.out.println(getLocalName() + " received message from agent " + msg.getSender().getName());
				} else {
					block();
				}
			}
		});
	}

	private void sleep() {
		state = State.SLEEP;

		// TODO do something here
	}

	@Override
	public void draw(SimGraphics g) {
		switch (state) {
		case OFF:
			color = Color.BLACK;
			break;

		case ON:
			if (batteryLevel < 20)
				color = Color.RED;
			else if (batteryLevel < 60)
				color = Color.YELLOW;
			else
				color = Color.GREEN;
			break;

		case SLEEP:
			color = Color.GRAY;
			break;

		default:
			break;
		}

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
