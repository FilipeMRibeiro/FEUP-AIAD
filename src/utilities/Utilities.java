package utilities;

import java.util.ArrayList;

public class Utilities {

	public static final double probNormalDistribution(double x, double mu, double stdDeviation) {
		return (Math.pow(Math.E, -(Math.pow(x - mu, 2)) / (2 * Math.pow(stdDeviation, 2))))
				/ (stdDeviation * Math.sqrt(2 * Math.PI));
	}

	public static final double mean(ArrayList<Double> pollutionSamples) {
		double sum = 0;

		for (double x : pollutionSamples)
			sum += x;

		return sum / pollutionSamples.size();
	}

	public static final double entropy(double sigma) {
		return Math.log(sigma * Math.sqrt(2 * Math.PI * Math.E));
	}

	/**
	 * Computes Pearson's coefficient of variation
	 */
	public static final double cv(ArrayList<Double> list) {
		return standardDeviation(list) / mean(list);
	}

	public static final double standardDeviation(ArrayList<Double> list) {
		return Math.sqrt(variance(list));
	}

	public static final double variance(ArrayList<Double> list) {
		double mean = mean(list);

		double temp = 0;

		for (double a : list)
			temp += (mean - a) * (mean - a);

		return temp / list.size();
	}

}
