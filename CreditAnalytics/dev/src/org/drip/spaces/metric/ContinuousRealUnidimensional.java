
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
 * ContinuousRealUnidimensional implements the normed, bounded/unbounded Continuous l^p R^1 Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealUnidimensional extends org.drip.spaces.tensor.ContinuousRealUnidimensionalVector
	implements org.drip.spaces.metric.RealUnidimensionalNormedSpace {
	private int _iPNorm = -1;
	private org.drip.measure.continuous.UnivariateDistribution _uniDist = null;

	/**
	 * Construct the Standard l^p R^1 Continuous Space Instance
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * @param uniDist The Univariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @return The Standard l^p R^1 Continuous Space Instance
	 */

	public static final ContinuousRealUnidimensional Standard (
		final double dblLeftEdge,
		final double dblRightEdge,
		final org.drip.measure.continuous.UnivariateDistribution uniDist,
		final int iPNorm)
	{
		try {
			return new ContinuousRealUnidimensional (dblLeftEdge, dblRightEdge, uniDist, iPNorm);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct the Supremum (i.e., l^Infinity) R^1 Continuous Space Instance
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * @param uniDist The Univariate Borel Sigma Measure
	 * 
	 * @return The Supremum (i.e., l^Infinity) R^1 Continuous Space Instance
	 */

	public static final ContinuousRealUnidimensional Supremum (
		final double dblLeftEdge,
		final double dblRightEdge,
		final org.drip.measure.continuous.UnivariateDistribution uniDist)
	{
		try {
			return new ContinuousRealUnidimensional (dblLeftEdge, dblRightEdge, uniDist, 0);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealUnidimensional Space Constructor
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * @param uniDist The Univariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealUnidimensional (
		final double dblLeftEdge,
		final double dblRightEdge,
		final org.drip.measure.continuous.UnivariateDistribution uniDist,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (dblLeftEdge, dblRightEdge);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("ContinuousRealUnidimensional Constructor: Invalid p-norm");

		_uniDist = uniDist;
	}

	@Override public int pNorm()
	{
		return _iPNorm;
	}

	@Override public org.drip.measure.continuous.UnivariateDistribution borelSigmaMeasure()
	{
		return _uniDist;
	}

	@Override public double sampleMetricNorm (
		final double dblX)
		throws java.lang.Exception
	{
		if (!validateInstance (dblX))
			throw new java.lang.Exception
				("ContinuousRealUnidimensional::sampleMetricNorm => Invalid Inputs");

		return java.lang.Math.abs (dblX);
	}

	@Override public double populationMode()
		throws java.lang.Exception
	{
		if (null == _uniDist)
			throw new java.lang.Exception ("ContinuousRealUnidimensional::populationMode => Invalid Inputs");

		org.drip.function.deterministic.R1ToR1 au = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return _uniDist.density (dblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMode = au.maxima (leftEdge(), rightEdge());

		if (null == vopMode)
			throw new java.lang.Exception
				("ContinuousRealUnidimensional::populationMode => Cannot compute VOP Mode");

		return vopMode.variates()[0];
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		if (null == _uniDist)
			throw new java.lang.Exception
				("ContinuousRealUnidimensional::populationMetricNorm => Invalid Inputs");

		org.drip.function.deterministic.R1ToR1 au = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return sampleMetricNorm (dblX) * _uniDist.density (dblX);
			}
		};

		return au.integrate (leftEdge(), rightEdge());
	}
}
