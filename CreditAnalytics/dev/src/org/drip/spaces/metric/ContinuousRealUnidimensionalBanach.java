
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
 * ContinuousRealUnidimensionalBanach implements the normed, bounded/unbounded Continuous l^p R^1 Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealUnidimensionalBanach extends
	org.drip.spaces.tensor.ContinuousRealUnidimensional implements
		org.drip.spaces.metric.GeneralizedMetricSpace {
	private int _iPNorm = -1;

	/**
	 * Construct the Standard l^p R^1 Continuous Banach Space Instance
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @return The Standard l^p R^1 Continuous Banach Space Instance
	 */

	public static final ContinuousRealUnidimensionalBanach StandardBanach (
		final double dblLeftEdge,
		final double dblRightEdge,
		final int iPNorm)
	{
		try {
			return new ContinuousRealUnidimensionalBanach (dblLeftEdge, dblRightEdge, iPNorm);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct the Supremum (i.e., l^Infinity) R^1 Continuous Banach Space Instance
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * 
	 * @return The Supremum (i.e., l^Infinity) R^1 Continuous Banach Space Instance
	 */

	public static final ContinuousRealUnidimensionalBanach SupremumBanach (
		final double dblLeftEdge,
		final double dblRightEdge)
	{
		try {
			return new ContinuousRealUnidimensionalBanach (dblLeftEdge, dblRightEdge, 0);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealUnidimensionalBanach Space Constructor
	 * 
	 * @param dblLeftEdge The Left Edge
	 * @param dblRightEdge The Right Edge
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealUnidimensionalBanach (
		final double dblLeftEdge,
		final double dblRightEdge,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (dblLeftEdge, dblRightEdge);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("ContinuousRealUnidimensionalBanach Constructor: Invalid p-norm");
	}

	@Override public int pNorm()
	{
		return _iPNorm;
	}

	/**
	 * Compute the Metric Norm of the Sample
	 * 
	 * @param dblX The Sample
	 * 
	 * @return The Metric Norm of the Sample
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double sampleMetricNorm (
		final double dblX)
		throws java.lang.Exception
	{
		if (!validateInstance (dblX))
			throw new java.lang.Exception
				("ContinuousRealMultidimensionalBanach::sampleMetricNorm => Invalid Inputs");

		return java.lang.Math.abs (dblX);
	}
}
