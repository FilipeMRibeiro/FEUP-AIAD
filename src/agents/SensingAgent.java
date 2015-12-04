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

	private int x, y;
	private Color color;
	private Object2DGrid space;

	public SensingAgent(int x, int y, Color color, Object2DGrid space, OurModel model) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.space = space;

		this.model = model;
	}

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				System.out.println("Ticks: " + model.getTickCount());

				sampleEnvironment();
			}

		});
	}

	public void sampleEnvironment() {
		System.out.println(getLocalName() + " is sampling the environment...");

		Vector<?> neighbors = space.getMooreNeighbors(x, y, false);

		for (Object neighbor : neighbors)
			if (neighbor instanceof Water)
				System.out.println(((Water) neighbor).getPolution());
		System.out.println();
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
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
