package entities;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.Random;

public class Water implements Drawable {

	private int x, y;
	private int polution;

	public Water(int x, int y) {
		this.x = x;
		this.y = y;
		
		polution = Random.uniform.nextIntFromTo(0, 255);
	}

	public Color getColor() {
		return new Color(polution, polution, polution);
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(getColor());
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
