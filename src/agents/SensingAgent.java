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
	private double stdDeviation, maxAdherence, maxLeadership;
	private ArrayList<Double> pollutionSamples;

	private boolean leader;
	private HashMap<AID, Double> neighboursLastSampleMap, neighboursAdherenceMap;
	private Set<AID> dependantNeighbours;
	private AID nodeLeaderOfMe;

	public SensingAgent(int x, int y, Color color, OurModel model) {
		this.x = x;
		this.y = y;
		this.state = State.ON;
		this.energyLevel = MAXIMUM_ENERGY_LEVEL;
		this.color = color;
		this.stdDeviation = Random.uniform.nextDoubleFromTo(MIN_STD_DEVIATION, MAX_STD_DEVIATION);
		this.maxAdherence = 0;
		this.maxLeadership = 0;
		this.pollutionSamples = new ArrayList<Double>();

		this.leader = false;
		this.neighboursLastSampleMap = new HashMap<AID, Double>();
		this.neighboursAdherenceMap = new HashMap<AID, Double>();
		this.dependantNeighbours = new TreeSet<AID>();
		this.nodeLeaderOfMe = null;

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
			if (energyLevel > 0) {
				sampleEnvironment();
				energyLevel -= 0.1;
			} else {
				energyLevel = 0;
				state = State.OFF;
			}

			break;

		case SLEEP:
			if (energyLevel <= 0) {
				energyLevel = 0;
				state = State.OFF;
			} else {
				sleepCountdown--;

				if (sleepCountdown <= 0) {
					sleepCountdown = 0;
					state = State.ON;
				}
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

		try {
			ACLMessage msg = new ACLMessage(Performatives.INFORM);

			msg.setContentObject(new Sample(getLastPollutionSample()));

			for (AID aid : neighboursAdherenceMap.keySet())
				msg.addReceiver(aid);

			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

				if (state == State.ON && msg != null) {
					try {
						switch (msg.getPerformative()) {
						case Performatives.INFORM: {
							Inform message = (Inform) msg.getContentObject();

							if (message instanceof Sample) {
								double receivedSample = message.getValue();

								neighboursLastSampleMap.put(msg.getSender(), receivedSample);

								double adherence = adherence2NeighbourEvaluation(receivedSample);

								// updateOwnMaxAdherence();
								if (maxAdherence < adherence) {
									maxAdherence = adherence;

									neighboursAdherenceMap.put(msg.getSender(), maxAdherence);

									try {
										// inform(me, al, maxAdh, t);
										ACLMessage reply = msg.createReply();
										reply.setContentObject(new Adherence(maxAdherence));
										send(reply);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} else if (message instanceof Adherence) {
								double leadership = calcLead(message.getValue());

								try {
									// inform(me, ar, lead);
									ACLMessage reply = msg.createReply();
									reply.setContentObject(new Leadership(leadership));
									send(reply);
								} catch (IOException e) {
									e.printStackTrace();
								}

								double adherence = adherence2NeighbourEvaluation(
										neighboursLastSampleMap.get(msg.getSender()));

								// updateOwnMaxAdherence();
								if (maxAdherence < adherence) {
									maxAdherence = adherence;

									neighboursAdherenceMap.put(msg.getSender(), maxAdherence);

									try {
										// inform(me, al, maxAdh, t);
										ACLMessage reply = msg.createReply();
										reply.setContentObject(new Adherence(maxAdherence));
										send(reply);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} else if (message instanceof Leadership) {
								// if I have no leader
								if (nodeLeaderOfMe == null) {
									if (maxLeadership < message.getValue()) {
										maxLeadership = message.getValue();

										// firmAdherence(me, al);
										ACLMessage reply = msg.createReply();
										reply.setPerformative(Performatives.FIRM_ADHERENCE);
										send(reply);
									}
								}
							} else {
								System.out.println("--- ERROR ---");
							}

							break;
						}

							/**
							 * Expresses the desire of the sending agent to
							 * adhere to the addressee agent.
							 */
						case Performatives.FIRM_ADHERENCE: {
							// If I can become a leader (or keep being one)
							if (nodeLeaderOfMe == null) {
								// ackAdherence(me, ar);
								ACLMessage reply = msg.createReply();
								reply.setPerformative(Performatives.ACK_ADHERENCE);
								send(reply);

								// updateOwnLeadValue();
								leader = true;

								// updateDependentGroup();
								dependantNeighbours.add(msg.getSender());
							}

							break;
						}

							/**
							 * Acknowledgement to a previously received
							 * firmAdherence message.
							 */
						case Performatives.ACK_ADHERENCE: {
							/*
							 * If I am not a leader and I already have a leader
							 * different than the one I want to adhere too.
							 */
							if (!leader && nodeLeaderOfMe != null && nodeLeaderOfMe != msg.getSender()) {
								// withdraw(me, aL);
								ACLMessage withdrawMsg = new ACLMessage(Performatives.WITHDRAW);
								withdrawMsg.addReceiver(nodeLeaderOfMe);
								send(withdrawMsg);
							}

							/*
							 * If I am a leader and there are nodes dependant on
							 * me.
							 */
							if (leader && !dependantNeighbours.isEmpty()) {
								ACLMessage breakMsg = new ACLMessage(Performatives.BREAK);

								// break(me, ap);
								for (AID aid : dependantNeighbours)
									breakMsg.addReceiver(aid);

								send(breakMsg);

								dependantNeighbours.clear();
							}

							/*
							 * I become dependant, I now have a leader, and I
							 * can go to sleep.
							 */
							leader = false;
							nodeLeaderOfMe = msg.getSender();
							sleep();

							break;
						}

							/**
							 * Allows a leader agent to break a leadership
							 * relationship.
							 */
						case Performatives.BREAK: {
							if (leader)
								System.err.println("BREAK received and I am a leader!");

							/*
							 * If a leader breaks its relationship with me, I no
							 * longer have a leader.
							 */
							nodeLeaderOfMe = null;

							break;
						}

							/**
							 * Message sent by a dependant agent to break a
							 * leadership relationship.
							 */
						case Performatives.WITHDRAW: {
							if (!leader || nodeLeaderOfMe != null)
								System.err.println("WITHDRAW received and I am not a leader");

							// remove node from my dependants list
							dependantNeighbours.remove(msg.getSender());

							/*
							 * If I have no more dependant nodes, I am no longer
							 * a leader.
							 */
							if (dependantNeighbours.isEmpty())
								leader = false;

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

	private double adherence2NeighbourEvaluation(double pollutionSample) {
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

	private double calcLead(double negotiatingNeighbourMaxAdherence) {
		double prestigeSum = 0;

		for (AID dependantAID : dependantNeighbours)
			prestigeSum += neighboursAdherenceMap.get(dependantAID);
		prestigeSum += adherence2NeighbourEvaluation(getLastPollutionSample());
		prestigeSum += negotiatingNeighbourMaxAdherence;

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

		sleepCountdown = Random.uniform.nextIntFromTo(50, 500);
	}

	@Override
	public void draw(SimGraphics g) {
		switch (state) {
		case OFF:
			g.drawFastRect(Color.BLACK);
			break;

		case ON:
		case SLEEP:
			// System.out.println("Leader of " + getAID() + ": " +
			// nodeLeaderOfMe);

			if (nodeLeaderOfMe != null && nodeLeaderOfMe != getAID())
				g.drawFastRect(model.getSensorCoalitionColor(nodeLeaderOfMe));
			else
				g.drawFastRect(color);

			break;

		default:
			break;
		}
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public float getEnergyLevel() {
		return energyLevel;
	}

	public Color getColor() {
		return color;
	}

}
