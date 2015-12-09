package launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import agents.SensingAgent;
import entities.Water;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplayConstants;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.Random;

public class OurModel extends Repast3Launcher {

	private static enum Scenario {
		ChainAlongRiver, GridAtTheEnd, RandomPositions
	}

	private ContainerController mainContainer;

	private DisplaySurface displaySurface;
	private OpenSequenceGraph pollutionGraph;
	private OpenSequenceGraph energyPlot;

	private Object2DGrid river;
	private ArrayList<Water> waterCellsList;
	private ArrayList<SensingAgent> sensorsList;

	private Scenario scenario;

	private int numberOfSensors;
	private int riverWidth, riverHeight;

	private int pollutionStainVerticalPosition;
	private float sedimentationFactor;
	private float alpha, beta, gamma;

	public OurModel() {
		scenario = Scenario.ChainAlongRiver;

		int cellsPerKm = 10;
		riverWidth = 50 * cellsPerKm;
		riverHeight = 2 * cellsPerKm;

		int cellSize = (int) Math.max(2, -0.8 * cellsPerKm + 11);
		DisplayConstants.CELL_WIDTH = cellSize;
		DisplayConstants.CELL_HEIGHT = cellSize;

		pollutionStainVerticalPosition = riverHeight / 3;
		sedimentationFactor = 0.99f;

		// the following values must add up to exactly 1.0
		alpha = 0.1f;
		beta = 0.8f;
		gamma = 0.1f;
	}

	/*
	 * This is called after the constructor.
	 */
	@SuppressWarnings("unchecked")
	public void setup() {
		super.setup();

		if (displaySurface != null)
			displaySurface.dispose();

		String displaySurfaceName = "River - top view";

		displaySurface = new DisplaySurface(this, displaySurfaceName);
		registerDisplaySurface(displaySurfaceName, displaySurface);

		// property descriptors
		Vector<Scenario> vecScenarios = new Vector<Scenario>();
		for (int i = 0; i < Scenario.values().length; i++)
			vecScenarios.add(Scenario.values()[i]);
		descriptors.put("Scenario", new ListPropertyDescriptor("Scenario", vecScenarios));
	}

	/*
	 * This is called when the 'Play' button is pressed on the Repast GUI.
	 */
	public void begin() {
		numberOfSensors = scenario == Scenario.GridAtTheEnd ? 30 : 50;

		buildModel();
		buildDisplay();
		buildSchedule();

		super.begin();
	}

	private void buildModel() {
		river = new Object2DGrid(riverWidth, riverHeight);
		waterCellsList = new ArrayList<Water>();
		sensorsList = new ArrayList<SensingAgent>();
	}

	private void buildDisplay() {
		Object2DDisplay displayWater = new Object2DDisplay(river);
		displayWater.setObjectList(waterCellsList);
		displaySurface.addDisplayableProbeable(displayWater, "Show river");

		Object2DDisplay displaySensors = new Object2DDisplay(river);
		displaySensors.setObjectList(sensorsList);
		displaySurface.addDisplayableProbeable(displaySensors, "Show sensors");

		displaySurface.display();

		initPollutionPlot();
		initEnergyPlot();
	}

	private void initPollutionPlot() {
		if (pollutionGraph != null)
			pollutionGraph.dispose();

		pollutionGraph = new OpenSequenceGraph("Pollution Detected", this);
		pollutionGraph.setAxisTitles("time", "pollution");

		for (int i = 0; i < numberOfSensors; i++) {
			final int iPrime = i;

			pollutionGraph.addSequence("S-" + i, new Sequence() {

				@Override
				public double getSValue() {
					return sensorsList.get(iPrime).getLastSamplePollutionLevel();
				}

			});
		}

		pollutionGraph.display();
	}

	private void initEnergyPlot() {
		if (energyPlot != null)
			energyPlot.dispose();

		energyPlot = new OpenSequenceGraph("Agents energy median", this);
		energyPlot.setAxisTitles("time", "energy");

		energyPlot.addSequence("Energy median", new Sequence() {

			@Override
			public double getSValue() {
				List<Float> batteryLevels = new ArrayList<Float>();

				for (SensingAgent sensor : sensorsList)
					batteryLevels.add(sensor.getBatteryLevel());

				Collections.sort(batteryLevels);

				int listSize = batteryLevels.size();
				int listMiddle = listSize / 2;

				return listSize % 2 == 0 ? (batteryLevels.get(listMiddle) + batteryLevels.get(listMiddle - 1)) / 2
						: batteryLevels.get(listMiddle);
			}

		});

		energyPlot.display();
	}

	private void buildSchedule() {
		getSchedule().scheduleActionBeginning(1, this, "updateRiver");

		getSchedule().scheduleActionAtInterval(1, displaySurface, "updateDisplay", Schedule.LAST);

		getSchedule().scheduleActionAtInterval(1, pollutionGraph, "step", Schedule.LAST);
		getSchedule().scheduleActionAtInterval(1, energyPlot, "step", Schedule.LAST);
	}

	public void updateRiver() {
		float nextPollutionLevels[][] = new float[riverWidth][riverHeight];

		for (int x = 0; x < riverWidth; x++)
			for (int y = 0; y < riverHeight; y++)
				nextPollutionLevels[x][y] = nextPollutionLevelAt(x, y);

		for (int x = 0; x < riverWidth; x++)
			for (int y = 0; y < riverHeight; y++)
				((Water) river.getObjectAt(x, y)).setPollution(nextPollutionLevels[x][y]);
	}

	/*
	 * The river flows according to this expression.
	 */
	private float nextPollutionLevelAt(int x, int y) {
		return (1 - sedimentationFactor) * pollutionAt(x, y) + sedimentationFactor * (alpha * pollutionAt(x + 1, y - 1)
				+ beta * pollutionAt(x + 1, y) + gamma * pollutionAt(x + 1, y + 1));
	}

	public float pollutionAt(int x, int y) {
		float pollution = 0;

		if (0 <= x && x < river.getSizeX() && 0 <= y && y < river.getSizeY()) {
			// if the position is inside the river boundaries

			pollution = ((Water) river.getObjectAt(x, y)).getPollution();
		} else if (river.getSizeX() - 1 < x && Math.abs(pollutionStainVerticalPosition - y) < riverHeight / 4) {
			// if the position is near the pollution stain

			float oscillation = (float) Math.sin(getTickCount() / 4);

			pollution = oscillation <= 0 ? 0 : oscillation * Water.MAX_POLLUTION;
		}

		return pollution;
	}

	/*
	 * This is called by the begin.super() method.
	 */
	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		launchAgents();
	}

	public void launchAgents() {
		try {
			switch (scenario) {
			case ChainAlongRiver: {
				int y = river.getSizeY() / 2;
				float spacing = (float) river.getSizeX() / numberOfSensors;

				for (int i = 0; i < numberOfSensors; i++) {
					int x = (int) (i * spacing);

					SensingAgent agent = new SensingAgent(x, y, river, this);

					river.putObjectAt(x, y, agent);
					sensorsList.add(agent);

					mainContainer.acceptNewAgent("S-" + i, agent).start();
				}

				break;
			}

			case GridAtTheEnd: {
				for (int i = 0; i < numberOfSensors / 3; i++) {
					for (int j = 0; j < 3; j++) {
						int x = i * riverHeight / 4 + riverHeight / 4;
						int y = (j + 1) * riverHeight / 4;

						SensingAgent agent = new SensingAgent(x, y, river, this);

						river.putObjectAt(x, y, agent);
						sensorsList.add(agent);

						mainContainer.acceptNewAgent("S-" + (i * 3 + j), agent).start();
					}
				}

				break;
			}

			case RandomPositions: {
				for (int i = 0; i < numberOfSensors; i++) {
					int x = Random.uniform.nextIntFromTo(0, river.getSizeX() - 1);
					int y = Random.uniform.nextIntFromTo(0, river.getSizeY() - 1);

					SensingAgent agent = new SensingAgent(x, y, river, this);

					river.putObjectAt(x, y, agent);
					sensorsList.add(agent);

					mainContainer.acceptNewAgent("S-" + i, agent).start();
				}

				break;
			}

			default:
				break;
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		pourRiverWater();
	}

	private void pourRiverWater() {
		for (int x = 0; x < riverWidth; x++) {
			for (int y = 0; y < riverHeight; y++) {
				Water water = new Water(x, y);

				river.putObjectAt(x, y, water);
				waterCellsList.add(water);
			}
		}
	}

	/*
	 * Main, app name, parameters, and getters/setters section.
	 */

	public static void main(String[] args) {
		boolean BATCH_MODE = false;

		SimInit init = new SimInit();
		init.setNumRuns(1); // works only in batch mode
		init.loadModel(new OurModel(), null, BATCH_MODE);
	}

	@Override
	public String getName() {
		return "AIAD - MA Sensing Network";
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "scenario", "riverWidth", "riverHeight", "sedimentationFactor", "alpha", "beta",
				"gamma" };
	}

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public int getRiverWidth() {
		return riverWidth;
	}

	public void setRiverWidth(int riverWidth) {
		this.riverWidth = riverWidth;
	}

	public int getRiverHeight() {
		return riverHeight;
	}

	public void setRiverHeight(int riverHeight) {
		this.riverHeight = riverHeight;
	}

	public float getSedimentationFactor() {
		return sedimentationFactor;
	}

	public void setSedimentationFactor(float sedimentationFactor) {
		this.sedimentationFactor = sedimentationFactor;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getBeta() {
		return beta;
	}

	public void setBeta(float beta) {
		this.beta = beta;
	}

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
	}

}
