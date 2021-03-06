// < begin copyright > 
// Copyright Ryan Marcus 2017
// 
// This file is part of birkhoffvonneumann.
// 
// birkhoffvonneumann is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// birkhoffvonneumann is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with birkhoffvonneumann.  If not, see <http://www.gnu.org/licenses/>.
// 
// < end copyright > 
 
package info.rmarcus.birkhoffvonneumann;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.jdt.annotation.NonNull;

import info.rmarcus.NullUtils;
import info.rmarcus.birkhoffvonneumann.exceptions.BVNException;
import info.rmarcus.birkhoffvonneumann.exceptions.BVNRuntimeException;

public class MatrixUtils {
	public static void multiply(double[][] dest, double[][] a, double[][] b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] * b[i][j];
			}
		}
	}

	public static void multiply(double[][] dest, double[][] a, double b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] * b;
			}
		}
	}

	public static void multiply(double[][] dest, double[] a, double b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i*dest.length + j] * b;
			}
		}
	}


	public static void add(double[][] dest, double[][] a, double[][] b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] + b[i][j];
			}
		}
	}

	public static void add(double[][] dest, double[][] a, double b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] + b;
			}
		}
	}

	public static void add(double[][] dest, double[][] a, double[] b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] + b[i*dest.length + j];
			}
		}
	}
	
	public static void subtract(double[][] dest, double[][] a, double[][] b) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = a[i][j] - b[i][j];
			}
		}
	}

	public static void apply(double[][] dest, double[][] a, DoubleUnaryOperator f) {
		for (int i = 0; i < dest.length; i++) {
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = f.applyAsDouble(a[i][j]);
			}
		}
	}
	
	public static double dot(double[] a, double[] b) {
		double accum = 0.0;
		for (int i = 0; i < a.length && i < b.length; i++) {
			accum += a[i] * b[i];
		}
		
		return accum;
	}
	
	public static double[] flatten(double[][] a) {
		List<Double> l = new ArrayList<Double>(a.length * a.length);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				l.add(a[i][j]);
			}
		}
		
		final double[] toR = l.stream().mapToDouble(d -> d).toArray();
		if (toR != null)
			return toR;
		
		throw new BVNRuntimeException("Unable to map double stream to array!");
	}

	public static double permanent(double[][] input) {
		int n = input.length;

		double collector = 0.0;
		Iterator<BitSet> i = bitStringsOfSize(n);
		while (i.hasNext()) {
			BitSet nxt = NullUtils.orThrow(i.next(), () -> new BVNRuntimeException("Iterator returned null!"));
			if (nxt.cardinality() == 0)
				continue;

			int mult = (int)Math.pow(-1, nxt.cardinality());
			double accum = 1.0;

			for (int row = 0; row < n; row++) {
				double x = 0;
				for (int col = 0; col < n; col++) {
					if (!nxt.get(col))
						continue;

					x += input[row][col];
				}

				accum *= x;
			}

			collector += mult * accum;
		}

		int mult = (int)Math.pow(-1, n);
		return mult * collector;

	}
	
	public static double[] normalize(double[] input) {
		ArrayRealVector arv = new ArrayRealVector(input);
		arv.mapDivideToSelf(arv.getNorm());
		return NullUtils.orThrow(arv.toArray(),
				() -> new BVNRuntimeException("Could not normalize array!"));
	}

	private static Iterator<BitSet> bitStringsOfSize(int n) {
		return new Iterator<BitSet>() {
			private BigInteger b = new BigInteger("0");
			@Override
			public boolean hasNext() {
				return b.compareTo((new BigInteger("2")).pow(n)) < 0;
			}

			@Override
			public @NonNull BitSet next() {
				byte[] bytes = b.toByteArray();
				BitSet toR = BitSet.valueOf(bytes);
				BigInteger nxt = b.add(new BigInteger("1"));
				if (nxt == null || toR == null) {
					throw new NoSuchElementException("Unable to increment big int");
				}
				b = nxt;
				return toR;
			}

		};
	}


	public static double[][] clone(double[][] m) {
		double[][] toR = new double[m.length][];

		for (int i = 0; i < m.length; i++) {
			toR[i] = Arrays.copyOf(m[i], m[i].length);
		}

		return toR;
	}

	public static double[] randomDirectionInNDSpace(Random r, int n) {
		double[] toR = new double[n];

		double sumSquared = 0.0;
		for (int i = 0; i < n; i++) {
			toR[i] = r.nextGaussian();
			sumSquared += Math.pow(toR[i], 2);
		}

		sumSquared = Math.sqrt(sumSquared);

		for (int i = 0; i < n; i++)
			toR[i] /= sumSquared;

		return toR;
	}

	public static double[][] identity(int n) {
		double[][] toR = new double[n][n];

		for (int i = 0; i < n; i++)
			toR[i][i] = 1.0;

		return toR;
	}
	
	public static double[][] uniformBistoc(int n) {
		double[][] toR = new double[n][n];
		
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				toR[i][j] = 1.0 / (double)n;
		
		return toR;
	}

	public static double[][] randomPermutation(Random r, int n) {
		return CoeffAndMatrix.fromFlatPerm(randomPermutationSparse(r, n));
	}

	public static int[] randomPermutationSparse(Random r, int n) {
		List<Integer> s = IntStream.range(0, n).mapToObj(i -> i).collect(Collectors.toCollection(() -> new ArrayList<Integer>(n)));
		Collections.shuffle(s, r);
		return NullUtils.orThrow(s.stream().mapToInt(i -> i).toArray(),
				() -> new BVNRuntimeException("Could not convert ArrayList to array!"));
	}

	public static boolean isPermutation(double[][] matrix) {
		for (int row = 0; row < matrix.length; row++) {
			int numOnes = 0;
			for (int col = 0; col < matrix[row].length; col++) {
				if (matrix[row][col] == 1.0)
					numOnes++;

				if (matrix[row][col] != 1.0 && matrix[row][col] != 0.0)
					return false;
			}

			if (numOnes != 1)
				return false;
		}

		for (int col = 0; col < matrix.length; col++) {
			int numOnes = 0;
			
			for (int row = 0; row < matrix.length; row++) {
				if (matrix[row][col] == 1.0)
					numOnes++;

				if (matrix[row][col] != 1.0 && matrix[row][col] != 0.0)
					return false;
			}

			if (numOnes != 1)
				return false;
		}

		return true;
	}
	
	public static double[][] randomMatrix(Random r, int n) {
		double[][] toR = new double[n][n];
		
		for (int i = 0; i < toR.length; i++)
			for (int j = 0; j < toR[i].length; j++)
				toR[i][j] = r.nextDouble();
		
		return toR;
	}

	public static double[][] preconditionedBistoch(int[] perm, double alpha) throws BVNException {
		double[][] toR = new double[perm.length][perm.length];
		
		for (int i = 0; i < toR.length; i++) {
			for (int j = 0; j < toR[i].length; j++) {
				toR[i][j] = alpha;
			}
		}
		
		for (int i = 0; i < perm.length; i++) {
			toR[i][perm[i]] = 1.0;
		}
		
		SinkhornBalancer.balance(toR);
				
		return toR;
	}
	
	public static void printMatrix(double[][] matrix) {
		for (double[] row : matrix) {
			for (double itm : row) {
				System.out.printf("%.2f\t", itm);
			}
			System.out.println();
		}

		System.out.println("-------------------------------");
	}



}
