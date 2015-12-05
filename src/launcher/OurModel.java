package launcher;

import java.awt.Color;
import java.util.ArrayList;

import agents.SensingAgent;
import entities.Water;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
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

	private ContainerController mainContainer;

	private DisplaySurface displaySurface;
	private OpenSequenceGraph plot;

	private Object2DGrid river;
	private ArrayList<Object> riverCells;

	private int numberOfSensors;
	private int riverWidth, riverHeight;

	private float sedimentationFactor;
	private float alpha, beta, gamma;

	public OurModel() {
		numberOfSensors = 100;

		int cellsPerKm = 10;
		riverWidth = 50 * cellsPerKm;
		riverHeight = 2 * cellsPerKm;

		int cellSize = (int) Math.max(1, -0.8 * cellsPerKm + 11);
		DisplayConstants.CELL_WIDTH = cellSize;
		DisplayConstants.CELL_HEIGHT = cellSize;

		sedimentationFactor = 0.03f;
		alpha = .4f;
		beta = .3f;
		gamma = .4f;
	}

	@Override
	public String getName() {
		return "AIAD - MA Sensing Network";
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfSensors", "riverWidth", "riverHeight", "sedimentationFactor", "alpha", "beta",
				"gamma" };
	}

	public int getNumberOfSensors() {
		return numberOfSensors;
	}

	public void setNumberOfSensors(int numberOfSensors) {
		this.numberOfSensors = numberOfSensors;
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

	public void setup() {
		super.setup();

		if (displaySurface != null)
			displaySurface.dispose();

		String displaySurfaceName = "River - top view";

		displaySurface = new DisplaySurface(this, displaySurfaceName);
		registerDisplaySurface(displaySurfaceName, displaySurface);
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		launchAgents();
	}

	public void launchAgents() {
		try {
			for (int i = 1; i <= numberOfSensors; i++)
				mainContainer.acceptNewAgent("Bot-" + i, spawnSensor()).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		pourRiverWater();
	}

	SensingAgent spawnSensor() {
		int x, y;

		do {
			x = Random.uniform.nextIntFromTo(0, river.getSizeX() - 1);
			y = Random.uniform.nextIntFromTo(0, river.getSizeY() - 1);
		} while (river.getObjectAt(x, y) != null);

		SensingAgent agent = new SensingAgent(x, y, Color.YELLOW, river, this);

		river.putObjectAt(x, y, agent);
		riverCells.add(agent);

		return agent;
	}

	private void pourRiverWater() {
		for (int x = 0; x < riverWidth; x++) {
			for (int y = 0; y < riverHeight; y++) {
				Water water = new Water(x, y);

				river.putObjectAt(x, y, water);
				riverCells.add(water);
			}
		}
	}

	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();

		super.begin();
	}

	private void buildModel() {
		river = new Object2DGrid(riverWidth, riverHeight);
		riverCells = new ArrayList<Object>();
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(river);
		display.setObjectList(riverCells);
		displaySurface.addDisplayableProbeable(display, "Agents Space");
		displaySurface.display();

		// graph
		if (plot != null)
			plot.dispose();

		plot = new OpenSequenceGraph("Colors and Agents", this);
		plot.setAxisTitles("time", "n");

		// plot number of different existing colors
		plot.addSequence("Number of agents", new Sequence() {
			public double getSValue() {
				return riverCells.size();
			}
		});

		plot.display();
	}

	public int polutionAt(int x, int y) {
		return ((Water) river.getObjectAt(x, y)).getPolution();
	}

	public void testStep() {
		for (int x = 1; x < riverWidth - 1; x++) {
			for (int y = 1; y < riverHeight; y++) {
				/*
				 * River(x, y) = (1 - ρ)River(x, y) + ρ(α(River(x - 1, y - 1)) +
				 * β(River(x, y - 1)) + γ(River(x + 1, y - 1)))
				 */
				Water water = (Water) river.getObjectAt(x, y);

				float p = (1 - sedimentationFactor) * polutionAt(x, y)
						+ sedimentationFactor * (alpha * polutionAt(x - 1, y - 1) + beta * polutionAt(x, y - 1)
								+ gamma * polutionAt(x + 1, y - 1));

				water.setPolution((int) p);
			}
		}
	}

	private void buildSchedule() {
		getSchedule().scheduleActionBeginning(1, this, "testStep");
		getSchedule().scheduleActionAtInterval(1, displaySurface, "updateDisplay", Schedule.LAST);
		getSchedule().scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	}

	public static void main(String[] args) {
		boolean BATCH_MODE = false;

		SimInit init = new SimInit();
		init.setNumRuns(1); // works only in batch mode
		init.loadModel(new OurModel(), null, BATCH_MODE);
	}

}
