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
 
package info.rmarcus.birkhoffvonneumann.learners.generalized_loss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import info.rmarcus.birkhoffvonneumann.BVNDecomposer;
import info.rmarcus.birkhoffvonneumann.DecompositionType;
import info.rmarcus.birkhoffvonneumann.MatrixUtils;
import info.rmarcus.birkhoffvonneumann.SamplingAlgorithm;
import info.rmarcus.birkhoffvonneumann.exceptions.BVNException;
import info.rmarcus.birkhoffvonneumann.exceptions.BVNRuntimeException;
import info.rmarcus.birkhoffvonneumann.polytope.BirkhoffPolytope;
import info.rmarcus.birkhoffvonneumann.polytope.VertexCurveBirkhoffPolytope;

public class MHJointPermutationLearner {
	private static final int SAMPLES_PER_MATRIX = 20;

	private int numPerms;
	private int[] dimensions;
	private BirkhoffPolytope[] bp;
	private ToDoubleFunction<List<double[][]>> loss;
	private Random r;

	private double bestLoss;
	private List<double[][]> best;

	private double currentMass;

	public MHJointPermutationLearner(int[] dims, ToDoubleFunction<List<double[][]>> loss) {
		this.numPerms = dims.length;
		this.dimensions = dims;

		bp = Arrays.stream(dimensions)
				.mapToObj(i -> new VertexCurveBirkhoffPolytope(i))
				.toArray(i -> new BirkhoffPolytope[i]);

		this.loss = loss;
		r = new Random(42);

		best = Arrays.stream(dims)
				.mapToObj(i -> MatrixUtils.uniformBistoc(i))
				.collect(Collectors.toList());

		bestLoss = modifiedLoss(best);
		currentMass = 1.0 / bestLoss;

	}

	private double modifiedLoss(List<double[][]> bistocs) {
		BVNDecomposer bvn = new BVNDecomposer();
		bvn.setSamplingAlgorithm(SamplingAlgorithm.GIBBS);
		

		double collector = 0.0;

		try {
			// take SAMPLES_PER_MATRIX samples from each of the bistocs given and return the average loss
			// always include the highest probability schedule
			List<double[][]> samples = new ArrayList<>(numPerms);
			for (BirkhoffPolytope p : bp)
				samples.add(bvn.meanPermutation(p.getCurrentPoint()));
			collector += testSamples(samples);
			
			for (int i = 0; i < SAMPLES_PER_MATRIX-1; i++) {
				samples = new ArrayList<>(numPerms);
				for (BirkhoffPolytope p : bp)
					samples.add(bvn.sample(r, p.getCurrentPoint()));
				
				double sampleLoss = testSamples(samples);
				collector += sampleLoss;
			}
			
			return collector / (double)SAMPLES_PER_MATRIX;
		} catch (BVNException e) {
			e.printStackTrace();
			return Double.POSITIVE_INFINITY;
		}
	}
	
	private double testSamples(List<double[][]> samples) {
		double sampleLoss = loss.applyAsDouble(samples);

		if (sampleLoss < bestLoss) {
			System.out.println("Found new best: " + sampleLoss);
			bestLoss = sampleLoss;
			best = samples;
		}
		
		return sampleLoss;
	}

	public void iterate() {
		iterate(IntStream.range(0, numPerms)
				.mapToObj(i -> i)
				.collect(Collectors.toSet()));
	}
	
	public void iterate(int i) {
		Set<Integer> s = new HashSet<>();
		s.add(i);
		iterate(s);
	}

	public void iterate(Set<Integer> toIterate) {
		List<double[]> dirs = Arrays.stream(bp).map(p -> p.getRandomDirection(r)).collect(Collectors.toList());
		List<double[][]> currents = Arrays.stream(bp).map(p -> p.getCurrentPoint()).collect(Collectors.toList());
		double[] moveBy = r.doubles().limit(numPerms).toArray();

		for (int i = 0; i < numPerms; i++) {
			// only iterate the bistochastics that were given in the set
			if (!toIterate.contains(i))
				continue;
			bp[i].movePoint(dirs.get(i), moveBy[i]);
		}


		List<double[][]> proposed = Arrays.stream(bp).map(p -> p.getCurrentPoint()).collect(Collectors.toList());

		double proposedMass = 1.0 / modifiedLoss(proposed);


		double ratio = proposedMass / currentMass;

		if (ratio >= 1.0) {
			// accept
			currentMass = proposedMass;
			return;
		}

		// reject with probability = ratio
		if (r.nextDouble() > ratio) {
			// accept
			currentMass = proposedMass;
			return;
		}

		// reject
		try {
			for (int i = 0; i < numPerms; i++) {
				bp[i].setCurrentPoint(currents.get(i));
			}
		} catch (BVNException e) {
			throw new BVNRuntimeException("Matrix that was bistochastic is no longer!" + e);
		}
	}
	
	public void precondition(int idx, double[][] bistoch) throws BVNException {
		bp[idx].setCurrentPoint(bistoch);
	}
	
	public List<double[][]> getBest() {
		return best;
	}
}
