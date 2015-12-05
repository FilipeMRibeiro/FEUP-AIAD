package entities;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.Random;

public class Water implements Drawable {

	private int x, y;
	private int polution;
	private Color color;

	public Water(int x, int y) {
		this.x = x;
		this.y = y;

		setPolution(Random.uniform.nextIntFromTo(0, 255));
	}

	public int getPolution() {
		return polution;
	}

	public void setPolution(int polution) {
		this.polution = polution;
		this.color = new Color(polution, 0, 255);
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
		// g.drawFastRect(color);
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
