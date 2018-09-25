package seam_carving;

import static java.util.Comparator.comparing;

import java.util.stream.IntStream;

public class Services {
	
	public static boolean arrayContains(int [] a, final int value) {
		return IntStream.of(a).anyMatch(x -> x == value);
	}
	
	//indices of the n min values
	public static int[] bottomN(final double[] input, final int n) {
	    return IntStream.range(0, input.length)
	            .boxed()
	            .sorted(comparing(i -> input[i]))
	            .mapToInt(i -> i)
	            .limit(n)
	            .toArray();
	}
}
