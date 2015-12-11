package utilities;

public class Utilities {

	public static final double normalDistribution(double x, double stdDeviation) {
		return 1 / (stdDeviation * Math.sqrt(2 * Math.PI))
				* (Math.pow(Math.E, -(x * x) / (2 * stdDeviation * stdDeviation)));
	}

}
