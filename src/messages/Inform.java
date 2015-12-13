package messages;

import java.io.Serializable;

public abstract class Inform implements Serializable {

	private static final long serialVersionUID = 1L;

	private double value;

	public Inform(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
