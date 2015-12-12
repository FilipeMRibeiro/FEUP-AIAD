package agents;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import entities.Water;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import launcher.OurModel;
import messages.Adherence;
import messages.Leadership;
import messages.Performatives;
import messages.Sample;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.Random;
import utilities.Utilities;

public class SensingAgent extends Agent implements Drawable {

	private final static double minStdDeviation = 0.0005;
	private final static double maxStdDeviation = 6;

	private final OurModel model;

	private int x, y;
	private State state;
	private int sleepCountdown;
	private float batteryLevel;
	private Color color;
	private double stdDeviation, maxAdherence;
	private float lastSamplePollutionLevel;
	private ArrayList<SensingAgent> neighbours;

	public SensingAgent(int x, int y, OurModel model) {
		this.x = x;
		this.y = y;
		this.state = State.ON;
		this.batteryLevel = 100;
		this.color = Color.GREEN;
		this.stdDeviation = Random.uniform.nextDoubleFromTo(minStdDeviation, maxStdDeviation);
		this.maxAdherence = 0;
		this.lastSamplePollutionLevel = 0;
		this.neighbours = new ArrayList<SensingAgent>();

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
				update();
			}
		});

		initMessageListener();
	}

	private void update() {
		switch (state) {
		case ON:
			if (batteryLevel > 0)
				sampleEnvironment();

			batteryLevel -= 0.1;

			if (batteryLevel <= 0) {
				batteryLevel = 0;
				state = State.OFF;
			}

			break;

		case SLEEP:
			if (batteryLevel <= 0) {
				batteryLevel = 0;
				state = State.OFF;
			}

			sleepCountdown--;

			if (sleepCountdown <= 0) {
				sleepCountdown = 0;
				state = State.ON;
			}

			break;

		case OFF:
			break;

		default:
			break;
		}
	}

	public void sampleEnvironment() {
		lastSamplePollutionLevel = ((Water) model.getRiver().getObjectAt(x, y)).getPollution();

		ACLMessage msg = new ACLMessage(Performatives.INFORM);

		try {
			msg.setContentObject(new Sample(lastSamplePollutionLevel));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (SensingAgent sensor : neighbours)
			msg.addReceiver(sensor.getAID());

		send(msg);
	}

	private void initMessageListener() {
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = myAgent.receive();

				if (msg != null) {
					try {
						Serializable message = msg.getContentObject();

						switch (msg.getPerformative()) {
						case Performatives.INFORM: {
							if (message instanceof Sample) {
								double receivedSample = ((Sample) message).getValue();

								// calculate adherence
								// TODO this is not complete
								double adherence = Utilities.normalDistribution(lastSamplePollutionLevel, stdDeviation);

								if (maxAdherence < adherence)
									maxAdherence = adherence;

								System.out.println("Received sample: " + receivedSample);
							} else if (message instanceof Adherence) {

								System.out.println("Received adherence: ");
							} else if (message instanceof Leadership) {
								System.out.println("Received leadership: ");
							} else {
								System.out.println("ERROR");
							}

							break;
						}

						case Performatives.FIRM_ADHERENCE: {
							break;
						}

						case Performatives.ACK_ADHERENCE: {
							sleep();

							break;
						}

						case Performatives.BREAK: {
							break;
						}

						case Performatives.WITHDRAW: {
							break;
						}

						default:
							break;
						}
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}

					// TODO delete this debug msg
					System.out.println(getLocalName() + " received message from agent " + msg.getSender().getName());
				} else {
					block();
				}
			}
		});
	}

	private void sleep() {
		state = State.SLEEP;

		// TODO change this and make it a parameter for the gui
		sleepCountdown = 100;
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
