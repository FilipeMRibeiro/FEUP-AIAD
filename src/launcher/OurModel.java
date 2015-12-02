package launcher;

import java.awt.Color;
import java.util.ArrayList;

import agents.SensingAgent;
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
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.Random;

public class OurModel extends Repast3Launcher {

	private ContainerController mainContainer;

	private DisplaySurface displaySurface;
	private OpenSequenceGraph plot;

	private Object2DGrid river;
	private ArrayList<SensingAgent> sensors;

	private int riverWidth, riverHeight;
	private int numberOfSensors;

	public OurModel() {
		this.numberOfSensors = 10;

		this.riverWidth = 100;
		this.riverHeight = 20;
	}

	@Override
	public String getName() {
		return "AIAD - MA Sensing Network";
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfSensors", "riverWidth", "riverHeight" };
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
		return riverWidth;
	}

	public void setRiverHeight(int riverHeight) {
		this.riverHeight = riverHeight;
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
				mainContainer.acceptNewAgent("Bot" + i, spawnSensor()).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	public void begin() {
		buildModel();
		buildDisplay();

		super.begin();

		buildSchedule();
	}

	private void buildModel() {
		sensors = new ArrayList<SensingAgent>();
		river = new Object2DGrid(riverWidth, riverHeight);
	}

	SensingAgent spawnSensor() {
		int x, y;

		do {
			x = Random.uniform.nextIntFromTo(0, river.getSizeX() - 1);
			y = Random.uniform.nextIntFromTo(0, river.getSizeY() - 1);
		} while (river.getObjectAt(x, y) != null);

		SensingAgent agent = new SensingAgent(x, y, Color.RED, river, this);

		river.putObjectAt(x, y, agent);
		sensors.add(agent);

		return agent;
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(river);
		display.setObjectList(sensors);
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
				return sensors.size();
			}
		});

		plot.display();
	}

	private void buildSchedule() {
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
