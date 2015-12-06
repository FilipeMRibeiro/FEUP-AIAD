package entities;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

public class Water implements Drawable {

	public static final int MAX_POLLUTION = 1000;

	private int x, y;
	private float pollution;
	private Color color;

	public Water(int x, int y) {
		this.x = x;
		this.y = y;

		setPollution(0);
	}

	public float getPollution() {
		return pollution;
	}

	public void setPollution(float pollution) {
		this.pollution = pollution > MAX_POLLUTION ? MAX_POLLUTION : pollution;

		this.color = new Color((int) this.pollution * 255 / MAX_POLLUTION, 0, 255);
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
