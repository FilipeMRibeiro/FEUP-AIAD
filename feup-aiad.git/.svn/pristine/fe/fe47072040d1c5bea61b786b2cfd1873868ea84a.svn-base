package agents;

import java.awt.Color;

import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class SensingAgent extends Agent implements Drawable {

	private int x, y;
	private Color color;
	private Object2DGrid space;

	public SensingAgent(int x, int y, Color color, Object2DGrid space) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.space = space;
	}

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");

		// Add the TickerBehaviour (period 1 second)
		addBehaviour(new TickerBehaviour(this, 1000) {

			private static final long serialVersionUID = 1L;

			protected void onTick() {
//				walk();
				System.out.println("asdasd");
//				System.out.println("Agent " + myAgent.getLocalName() + ": tick=" + getTickCount());
			}

		});
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
	}

	public void walk() {
		space.putObjectAt(this.x, this.y, null);

		this.x++;
		// this.y += yMove;

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
