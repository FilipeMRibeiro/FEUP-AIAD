package agents;

import java.awt.Color;

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

		// Add the TickerBehaviour (period 1 second)
		addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				walk();
				System.out.println(model.getTickCount());
			}

		});
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
	}

	public void walk() {
		System.out.println("Agent " + getLocalName() + " started.");
		space.putObjectAt(this.x, this.y, null);


		if (x >= space.getSizeX())
			this.doDelete();
		else
			space.putObjectAt(this.x, this.y, this);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public Color getColor() {
		return color;
	}

}
