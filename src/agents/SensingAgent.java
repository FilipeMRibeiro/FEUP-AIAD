package agents;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import entities.Water;
import jade.core.AID;
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
	private ArrayList<Float> pollutionSamples;
	private ArrayList<AID> neighboursAID;

	public SensingAgent(int x, int y, OurModel model) {
		this.x = x;
		this.y = y;
		this.state = State.ON;
		this.batteryLevel = 100;
		this.color = Color.GREEN;
		// TODO should this be random?
		this.stdDeviation = Random.uniform.nextDoubleFromTo(minStdDeviation, maxStdDeviation);
		this.maxAdherence = 0;
		this.pollutionSamples = new ArrayList<Float>();
		this.neighboursAID = new ArrayList<AID>();

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
					neighboursAID.add(sensor.getAID());
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
		pollutionSamples.add(((Water) model.getRiver().getObjectAt(x, y)).getPollution());

		ACLMessage msg = new ACLMessage(Performatives.INFORM);

		try {
			msg.setContentObject(new Sample(getLastPollutionSample()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (AID aid : neighboursAID)
			msg.addReceiver(aid);

		send(msg);
	}

	public float getLastPollutionSample() {
		return pollutionSamples.get(pollutionSamples.size() - 1);
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

								double adherence = calcAdherence(receivedSample);

								if (maxAdherence < adherence) {
									maxAdherence = adherence;

									try {
										ACLMessage reply = msg.createReply();

										reply.setContentObject(new Adherence(adherence));

										send(reply);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

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

	private double calcAdherence(double pollutionSample) {
		double thisSensorPollutionSamplesMean = Utilities.mean(pollutionSamples);
		double valsSimilarity = Utilities.probNormalDistribution(pollutionSample, thisSensorPollutionSamplesMean,
				stdDeviation)
				/ Utilities.probNormalDistribution(thisSensorPollutionSamplesMean, thisSensorPollutionSamplesMean,
						stdDeviation);

		double eHj = Math.pow(Math.E, Utilities.entropy(stdDeviation));
		double eHmin = Math.pow(Math.E, Utilities.entropy(minStdDeviation));
		double eHmax = Math.pow(Math.E, Utilities.entropy(maxStdDeviation));

		double varModelCertainty = 1 - ((eHj - eHmin) / (eHmax - eHmin));

		return valsSimilarity * varModelCertainty;
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

}
