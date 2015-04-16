
package org.drip.spaces.function;

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
 * NormedR1ContinuousToR1Continuous implements the f : Validated R^1 Continuous -> Validated R^1 Continuous
 *  Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedR1ContinuousToR1Continuous extends org.drip.spaces.function.NormedR1ToR1 {

	/**
	 * NormedR1ContinuousToR1Continuous Function Space Constructor
	 * 
	 * @param funcR1ToR1 The R^1 -> R^1 Function
	 * @param cruvInput The R^1 Input Vector Space (may/may not be Normed)
	 * @param cruvOutput The R^1 Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedR1ContinuousToR1Continuous (
		final org.drip.function.deterministic.R1ToR1 funcR1ToR1,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruvInput,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruvOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (cruvInput, cruvOutput, funcR1ToR1, iPNorm);
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsInput = input();

		if (!(guvsInput instanceof org.drip.spaces.metric.ContinuousRealUnidimensional))
			throw new java.lang.Exception
				("NormedR1ContinuousToR1Continuous::populationMetricNorm => Invalid Input Vector Space");

		org.drip.spaces.metric.ContinuousRealUnidimensional cru =
			(org.drip.spaces.metric.ContinuousRealUnidimensional) guvsInput;

		final org.drip.measure.continuous.UnivariateDistribution uniDist = cru.borelSigmaMeasure();

		if (null == uniDist)
			throw new java.lang.Exception
				("NormedR1ContinuousToR1Continuous::populationMetricNorm => Measure not specified");

		final org.drip.function.deterministic.R1ToR1 funcR1ToR1 = function();

		final int iPNorm = pNorm();

		org.drip.function.deterministic.R1ToR1 am = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return java.lang.Math.pow (java.lang.Math.abs (funcR1ToR1.evaluate (dblX)), iPNorm) *
					uniDist.density (dblX);
			}
		};

		return java.lang.Math.pow (am.integrate (cru.leftEdge(), cru.rightEdge()), 1. / iPNorm);
	}
}
