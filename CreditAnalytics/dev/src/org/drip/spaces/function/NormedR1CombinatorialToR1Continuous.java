
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
 * NormedR1CombinatorialToR1Continuous implements the f : Validated R^d Combinatorial -> Validated R^1
 *  Continuous Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedR1CombinatorialToR1Continuous extends org.drip.spaces.function.NormedR1ToR1 {

	/**
	 * NormedR1CombinatorialToR1Continuous Function Space Constructor
	 * 
	 * @param funcR1ToR1 The R1ToR1 Function
	 * @param cruvInput The Combinatorial R^1 Input Vector Space (may/may not be Normed)
	 * @param cruvOutput The Continuous R^1 Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedR1CombinatorialToR1Continuous (
		final org.drip.function.deterministic.R1ToR1 funcR1ToR1,
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector cruvInput,
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

		if (!(guvsInput instanceof org.drip.spaces.metric.CombinatorialRealUnidimensional))
			throw new java.lang.Exception
				("NormedR1CombinatorialToR1Continuous::populationMetricNorm => Incomptabile Input Vector Space");

		org.drip.spaces.metric.CombinatorialRealUnidimensional cru =
			(org.drip.spaces.metric.CombinatorialRealUnidimensional) guvsInput;

		org.drip.measure.continuous.UnivariateDistribution uniDist = cru.borelSigmaMeasure();

		if (null == uniDist)
			throw new java.lang.Exception
				("NormedR1CombinatorialToR1Continuous::populationMetricNorm => No Univariate Distribution");

		org.drip.function.deterministic.R1ToR1 funcR1ToR1 = function();

		java.util.List<java.lang.Double> lsElem = cru.elementSpace();

		double dblPopulationMetricNorm  = 0.;
		double dblNormalizer = 0.;

		int iPNorm = pNorm();

		for (double dblElement : lsElem) {
			double dblProbabilityDensity = uniDist.density (dblElement);

			dblNormalizer += dblProbabilityDensity;

			dblPopulationMetricNorm += dblProbabilityDensity * java.lang.Math.pow (java.lang.Math.abs
				(funcR1ToR1.evaluate (dblElement)), iPNorm);
		}

		return java.lang.Math.pow (dblPopulationMetricNorm / dblNormalizer, 1. / iPNorm);
	}
}
