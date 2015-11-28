package launcher;

import java.awt.Color;
import java.util.ArrayList;

import agents.SensingAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.Random;

public class OurModel extends Repast3Launcher {

	private ContainerController mainContainer;

	private ArrayList<SensingAgent> agentList;
	private Schedule schedule;
	private DisplaySurface displaySurface;
	private Object2DGrid space;
	private OpenSequenceGraph plot;

	private int numberOfAgents, spaceSize;

	public OurModel() {
		this.numberOfAgents = 100;
		this.spaceSize = 100;
	}

	@Override
	public String getName() {
		return "MA Sensing Network";
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "spaceSize" };
	}

	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public int getSpaceSize() {
		return spaceSize;
	}

	public void setSpaceSize(int spaceSize) {
		this.spaceSize = spaceSize;
	}

	public void setup() {
		schedule = new Schedule();

		if (displaySurface != null)
			displaySurface.dispose();

		displaySurface = new DisplaySurface(this, "Color Picking Display");
		registerDisplaySurface("Color Picking Display", displaySurface);
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		// launchAgents();
	}

	// public void launchAgents() {
	// try {
	// mainContainer.acceptNewAgent("Bot1", new SimpleAgent()).start();
	// } catch (StaleProxyException e) {
	// e.printStackTrace();
	// }
	// }

	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();
	}

	private void buildModel() {
		agentList = new ArrayList<SensingAgent>();
		space = new Object2DGrid(spaceSize, spaceSize);

		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;

			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Color color = new Color(Random.uniform.nextIntFromTo(0, 255), Random.uniform.nextIntFromTo(0, 255),
					Random.uniform.nextIntFromTo(0, 255));

			SensingAgent agent = new SensingAgent(x, y, color, space);

			space.putObjectAt(x, y, agent);
			agentList.add(agent);
		}
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(space);
		display.setObjectList(agentList);
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
				return agentList.size();
			}
		});

		plot.display();
	}

	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionAtInterval(1, displaySurface, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	}

	class MainAction extends BasicAction {

		public void execute() {
			for (SensingAgent agent : agentList)
				agent.walk();
		}

	}

	/**
	 * Launching Repast3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean BATCH_MODE = false;

		SimInit init = new SimInit();
		init.setNumRuns(1); // works only in batch mode
		init.loadModel(new OurModel(), null, BATCH_MODE);
	}

}
