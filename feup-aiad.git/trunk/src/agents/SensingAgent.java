package agents;

import java.awt.Color;
import java.util.Vector;

import entities.Water;
import launcher.OurModel;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class SensingAgent extends Agent implements Drawable {

	private final OurModel model;
	private Object2DGrid space;

	private int x, y;
	private float batteryLevel;
	private Color color;

	public SensingAgent(int x, int y, Object2DGrid space, OurModel model) {
		this.x = x;
		this.y = y;
		this.batteryLevel = 100;
		this.color = Color.GREEN;

		this.space = space;
		this.model = model;
	}

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				System.out.println(getLocalName() + " battery level: " + batteryLevel);
				System.out.println("Ticks: " + model.getTickCount());

				if (batteryLevel > 0)
					sampleEnvironment();

				updateBatteryLevel();
			}

		});
	}

	public void sampleEnvironment() {
		System.out.println(getLocalName() + " is sampling the environment...");

		Vector<?> neighbors = space.getMooreNeighbors(x, y, false);

		for (Object neighbor : neighbors)
			if (neighbor instanceof Water)
				System.out.print(((Water) neighbor).getPollution() + " ");
		System.out.println();
	}

	private void updateBatteryLevel() {
		batteryLevel -= 0.001;

		if (batteryLevel < 15)
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

}
