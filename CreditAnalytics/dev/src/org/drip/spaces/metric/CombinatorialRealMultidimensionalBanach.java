
package org.drip.spaces.metric;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * CombinatorialRealMultidimensionalBanach implements the normed, bounded/unbounded Combinatorial l^p R^d
 *  Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealMultidimensionalBanach extends
	org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector implements
		org.drip.spaces.metric.RealMultidimensionalNormedSpace {
	private int _iPNorm = -1;
	private org.drip.measure.continuous.MultivariateDistribution _multiDist = null;

	/**
	 * CombinatorialRealMultidimensionalBanach Space Constructor
	 * 
	 * @param aCURV Array of Combinatorial Real Valued Vector Spaces
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealMultidimensionalBanach (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCURV,
		final org.drip.measure.continuous.MultivariateDistribution multiDist,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (aCURV);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception
				("CombinatorialRealMultidimensionalBanach Constructor: Invalid p-norm");

		_multiDist = multiDist;
	}

	@Override public int pNorm()
	{
		return _iPNorm;
	}

	@Override public org.drip.measure.continuous.MultivariateDistribution borelSigmaMeasure()
	{
		return _multiDist;
	}

	@Override public double sampleSupremumNorm (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!validateInstance (adblX))
			throw new java.lang.Exception
				("CombinatorialRealMultidimensionalBanach::sampleSupremumNorm => Invalid Inputs");

		int iDimension = adblX.length;

		double dblNorm = java.lang.Math.abs (adblX[0]);

		for (int i = 1; i < iDimension; ++i) {
			double dblAbsoluteX = java.lang.Math.abs (adblX[i]);

			dblNorm = dblNorm > dblAbsoluteX ? dblNorm : dblAbsoluteX;
		}

		return dblNorm;
	}

	@Override public double sampleMetricNorm (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!validateInstance (adblX))
			throw new java.lang.Exception
				("CombinatorialRealMultidimensionalBanach::sampleMetricNorm => Invalid Inputs");

		if (0 == _iPNorm) return sampleSupremumNorm (adblX);

		double dblNorm = 0.;
		int iDimension = adblX.length;

		for (int i = 0; i < iDimension; ++i)
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (adblX[i]), _iPNorm);

		return java.lang.Math.pow (dblNorm, 1. / _iPNorm);
	}

	@Override public double[] populationMode()
	{
		if (null == _multiDist) return null;

		org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator crmi = iterator();

		double[] adblVariate = crmi.cursorVariates();

		int iDimension = adblVariate.length;
		double dblModeProbabilityDensity = 0.;
		double[] adblModeVariate = new double[iDimension];
		double dblProbabilityDensity = java.lang.Double.NaN;

		while (null != adblVariate) {
			try {
				dblProbabilityDensity = _multiDist.density (adblVariate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (dblProbabilityDensity > dblModeProbabilityDensity) {
				for (int i = 0; i < iDimension; ++i)
					adblModeVariate[i] = adblVariate[i];

				dblModeProbabilityDensity = dblProbabilityDensity;
			}

			adblVariate = crmi.nextVariates();
		}

		return adblModeVariate;
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		if (null == _multiDist)
			throw new java.lang.Exception
				("CombinatorialRealMultidimensionalBanach::populationMetricNorm => No Multivariate Distribution");

		org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator crmi = iterator();

		double[] adblVariate = crmi.cursorVariates();

		double dblNormalizer = 0.;
		double dblPopulationMetricNorm  = 0.;
		int iDimension = adblVariate.length;

		while (null != adblVariate) {
			double dblProbabilityDensity = _multiDist.density (adblVariate);

			dblNormalizer += dblProbabilityDensity;

			for (int i = 0; i < iDimension; ++i)
				dblPopulationMetricNorm += dblProbabilityDensity * java.lang.Math.pow (java.lang.Math.abs
					(adblVariate[i]), _iPNorm);

			adblVariate = crmi.nextVariates();
		}

		return java.lang.Math.pow (dblPopulationMetricNorm / dblNormalizer, 1. / _iPNorm);
	}
}
