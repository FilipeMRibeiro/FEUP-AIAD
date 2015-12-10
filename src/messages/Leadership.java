package messages;

import java.io.Serializable;

public class Leadership implements Serializable {

	private static final long serialVersionUID = 1L;

	private double value;

	public Leadership(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
