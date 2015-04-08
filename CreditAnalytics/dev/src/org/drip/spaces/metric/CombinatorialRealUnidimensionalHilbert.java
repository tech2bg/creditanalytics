
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
 * CombinatorialRealUnidimensionalHilbert implements the normed, bounded/unbounded, Combinatorial l^2 R^1
 * 	Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealUnidimensionalHilbert extends
	org.drip.spaces.metric.CombinatorialRealUnidimensionalBanach {

	/**
	 * CombinatorialRealUnidimensionalHilbert Space Constructor
	 * 
	 * @param setElementSpace The Set Space of Elements
	 * @param uniDist The Univariate Borel Sigma Measure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealUnidimensionalHilbert (
		final java.util.Set<java.lang.Double> setElementSpace,
		final org.drip.measure.continuous.UnivariateDistribution uniDist)
		throws java.lang.Exception
	{
		super (setElementSpace, uniDist, 2);
	}

	@Override public double sampleMetricNorm (
		final double dblX)
		throws java.lang.Exception
	{
		if (!validateInstance (dblX))
			throw new java.lang.Exception
				("CombinatorialRealUnidimensionalHilbert::sampleMetricNorm => Invalid Inputs");

		double dblAbsoluteX = java.lang.Math.abs (dblX);

		return dblAbsoluteX * dblAbsoluteX;
	}
}
