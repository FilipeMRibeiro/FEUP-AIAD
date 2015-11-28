package agents;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class SensingAgent implements Drawable {

	private int x, y;
	private Color color;
	private Object2DGrid space;

	public SensingAgent(int x, int y, Color color, Object2DGrid space) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.space = space;
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
