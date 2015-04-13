
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
 * ContinuousRealMultidimensionalBanach implements the normed, bounded/unbounded Continuous l^p R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealMultidimensionalBanach extends
	org.drip.spaces.tensor.ContinuousRealMultidimensionalVector implements
		org.drip.spaces.metric.RealMultidimensionalNormedSpace {
	private int _iPNorm = -1;
	private org.drip.measure.continuous.MultivariateDistribution _multiDist = null;

	/**
	 * Construct the Standard l^p R^d Continuous Banach Space Instance
	 * 
	 * @param iDimension The Space Dimension
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @return The Standard l^p R^d Continuous Banach Space Instance
	 */

	public static final ContinuousRealMultidimensionalBanach StandardBanach (
		final int iDimension,
		final org.drip.measure.continuous.MultivariateDistribution multiDist,
		final int iPNorm)
	{
		try {
			return 0 >= iDimension ? null : new ContinuousRealMultidimensionalBanach (new
				org.drip.spaces.tensor.ContinuousRealUnidimensionalVector[iDimension], multiDist, iPNorm);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct the Supremum (i.e., l^Infinity) R^d Continuous Banach Space Instance
	 * 
	 * @param iDimension The Space Dimension
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * 
	 * @return The Supremum (i.e., l^Infinity) R^d Continuous Banach Space Instance
	 */

	public static final ContinuousRealMultidimensionalBanach SupremumBanach (
		final int iDimension,
		final org.drip.measure.continuous.MultivariateDistribution multiDist)
	{
		try {
			return 0 >= iDimension ? null : new ContinuousRealMultidimensionalBanach (new
				org.drip.spaces.tensor.ContinuousRealUnidimensionalVector[iDimension], multiDist, 0);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealMultidimensionalBanach Space Constructor
	 * 
	 * @param aCURV Array of Continuous Real Valued Vector Spaces
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealMultidimensionalBanach (
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector[] aCURV,
		final org.drip.measure.continuous.MultivariateDistribution multiDist,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (aCURV);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception
				("ContinuousRealMultidimensionalBanach Constructor: Invalid p-norm");

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
				("ContinuousRealMultidimensionalBanach::sampleSupremumNorm => Invalid Inputs");

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
				("ContinuousRealMultidimensionalBanach::sampleMetricNorm => Invalid Inputs");

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

		org.drip.function.deterministic.RdToR1 am = new
			org.drip.function.deterministic.RdToR1 (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				return _multiDist.density (adblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMode = am.maxima (leftEdge(), rightEdge());

		return null == vopMode ? null : vopMode.variates();
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		if (null == _multiDist)
			throw new java.lang.Exception
				("ContinuousRealMultidimensionalBanach::populationMetricNorm => Invalid Inputs");

		org.drip.function.deterministic.RdToR1 am = new
			org.drip.function.deterministic.RdToR1 (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				double dblNorm = 0.;
				int iDimension = adblX.length;

				for (int i = 0; i < iDimension; ++i)
					dblNorm += java.lang.Math.pow (java.lang.Math.abs (adblX[i]), _iPNorm);

				return dblNorm * _multiDist.density (adblX);
			}
		};

		return java.lang.Math.pow (am.integrate (leftEdge(), rightEdge()), 1. / _iPNorm);
	}
}
