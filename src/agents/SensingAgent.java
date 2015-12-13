package agents;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import entities.Water;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import launcher.OurModel;
import messages.Adherence;
import messages.Inform;
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

	private final static double MIN_STD_DEVIATION = 0.0005;
	private final static double MAX_STD_DEVIATION = 6;

	private final static float SECURITY_ENERGY_LEVEL = 1.5f;
	private final static float MAXIMUM_ENERGY_LEVEL = 100f;

	private final OurModel model;

	private int x, y;
	private State state;
	private int sleepCountdown;
	private float energyLevel;
	private Color color;
	private double stdDeviation, maxAdherence;
	private ArrayList<Double> pollutionSamples;

	private boolean leader;
	private HashMap<AID, Double> neighboursLastSampleMap, neighboursAdherenceMap;
	private Set<AID> dependantNeighbours;
	private AID leaderNodeOfMe;

	public SensingAgent(int x, int y, OurModel model) {
		this.x = x;
		this.y = y;
		this.state = State.ON;
		this.energyLevel = MAXIMUM_ENERGY_LEVEL;
		this.color = Color.GREEN;
		// TODO should this be random?
		this.stdDeviation = Random.uniform.nextDoubleFromTo(MIN_STD_DEVIATION, MAX_STD_DEVIATION);
		this.maxAdherence = 0;
		this.pollutionSamples = new ArrayList<Double>();

		this.leader = false;
		this.neighboursLastSampleMap = new HashMap<AID, Double>();
		this.neighboursAdherenceMap = new HashMap<AID, Double>();
		this.dependantNeighbours = new TreeSet<AID>();
		this.leaderNodeOfMe = null;

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
					neighboursAdherenceMap.put(sensor.getAID(), 0.0);
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
			if (energyLevel > 0)
				sampleEnvironment();

			energyLevel -= 0.1;

			if (energyLevel <= 0) {
				energyLevel = 0;
				state = State.OFF;
			}

			break;

		case SLEEP:
			if (energyLevel <= 0) {
				energyLevel = 0;
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

		for (AID aid : neighboursAdherenceMap.keySet())
			msg.addReceiver(aid);

		send(msg);
	}

	public double getLastPollutionSample() {
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
						switch (msg.getPerformative()) {
						case Performatives.INFORM: {
							Inform message = (Inform) msg.getContentObject();

							if (message instanceof Sample) {
								double receivedSample = message.getValue();

								neighboursLastSampleMap.put(msg.getSender(), receivedSample);

								// System.out.println("Received sample: " +
								// receivedSample);

								double adherence = calcAdherence(receivedSample);

								if (maxAdherence < adherence) {
									maxAdherence = adherence;

									neighboursAdherenceMap.put(msg.getSender(), adherence);

									try {
										ACLMessage reply = msg.createReply();

										reply.setContentObject(new Adherence(adherence));

										send(reply);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} else if (message instanceof Adherence) {
								System.out.println("Received adherence.");
								double receivedAdherence = message.getValue();

								double leadership = calcLeadership(receivedAdherence);

								System.out.println("leadership: " + leadership);

								try {
									ACLMessage reply = msg.createReply();

									reply.setContentObject(new Leadership(leadership));

									send(reply);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else if (message instanceof Leadership) {
								System.out.println("Received leadership.");

								ACLMessage reply = msg.createReply();
								reply.setPerformative(Performatives.FIRM_ADHERENCE);
								send(reply);
							} else {
								System.out.println("--- ERROR ---");
							}

							break;
						}

						case Performatives.FIRM_ADHERENCE: {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(Performatives.ACK_ADHERENCE);
							send(reply);

							leader = true;
							dependantNeighbours.add(msg.getSender());

							break;
						}

						case Performatives.ACK_ADHERENCE: {
							if (!leader && msg.getSender() != leaderNodeOfMe) {
								ACLMessage withdrawMsg = msg.createReply();

								withdrawMsg.setPerformative(Performatives.WITHDRAW);

								send(withdrawMsg);
							} else if (leader && !dependantNeighbours.isEmpty()) {
								ACLMessage breakMsg = new ACLMessage(Performatives.BREAK);

								for (AID aid : dependantNeighbours)
									breakMsg.addReceiver(aid);

								send(breakMsg);

								dependantNeighbours.clear();
							}

							leader = false;

							sleep();

							break;
						}

						case Performatives.BREAK: {
							leader = true;

							break;
						}

						case Performatives.WITHDRAW: {
							dependantNeighbours.remove(msg.getSender());

							leader = true;

							break;
						}

						default:
							System.out.println("--- ERROR 2 ---");
							break;
						}
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}
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
		double eHmin = Math.pow(Math.E, Utilities.entropy(MIN_STD_DEVIATION));
		double eHmax = Math.pow(Math.E, Utilities.entropy(MAX_STD_DEVIATION));

		double varModelCertainty = 1 - ((eHj - eHmin) / (eHmax - eHmin));

		return valsSimilarity * varModelCertainty;
	}

	private double calcLeadership(double negotiatingNeighbourAdherence) {
		double prestigeSum = 0;

		for (AID dependantAID : dependantNeighbours)
			prestigeSum += neighboursAdherenceMap.get(dependantAID);
		prestigeSum += calcAdherence(getLastPollutionSample());
		prestigeSum += negotiatingNeighbourAdherence;

		double prestige = prestigeSum / (dependantNeighbours.size() + 2);

		double capacity = (energyLevel - SECURITY_ENERGY_LEVEL) / MAXIMUM_ENERGY_LEVEL;

		ArrayList<Double> groupSamples = new ArrayList<Double>(neighboursLastSampleMap.values());
		groupSamples.add(getLastPollutionSample());

		double groupSamplesMean = Utilities.mean(groupSamples);

		double representativeness = 1 / (Math.pow(Math.E, Math.abs(getLastPollutionSample() - groupSamplesMean)
				* Utilities.cv(new ArrayList<Double>(neighboursAdherenceMap.values()))));

		return prestige * capacity * representativeness;
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
			if (energyLevel < 20)
				color = Color.RED;
			else if (energyLevel < 60)
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
		return energyLevel;
	}

}
