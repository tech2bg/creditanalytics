
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
 * NormedRdCombinatorialToR1Continuous implements the f : Validated R^d Combinatorial -> Validated R^1
 *  Continuous Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedRdCombinatorialToR1Continuous extends org.drip.spaces.function.NormedRdSpaceToR1Space {

	/**
	 * NormedRdCombinatorialToR1Continuous Function Space Constructor
	 * 
	 * @param am The Multivariate Function
	 * @param crmvInput The Combinatorial R^d Input Vector Space (may/may not be Normed)
	 * @param cruvOutput The Continuous R^1 Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedRdCombinatorialToR1Continuous (
		final org.drip.function.deterministic.RdToR1 am,
		final org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector crmvInput,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruvOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (crmvInput, cruvOutput, am, iPNorm);
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput = input();

		if (!(gmvsInput instanceof org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach))
			throw new java.lang.Exception
				("NormedRdCombinatorialToR1Continuous::populationMetricNorm => Incomptabile Input Vector Space");

		org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach crmb =
			(org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach) gmvsInput;

		org.drip.measure.continuous.MultivariateDistribution multiDist = crmb.borelSigmaMeasure();

		if (null == multiDist)
			throw new java.lang.Exception
				("NormedRdCombinatorialToR1Continuous::populationMetricNorm => No Multivariate Distribution");

		org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator crmi = crmb.iterator();

		org.drip.function.deterministic.RdToR1 am = function();

		double[] adblVariate = crmi.cursorVariates();

		double dblPopulationMetricNorm  = 0.;
		int iDimension = adblVariate.length;
		double dblNormalizer = 0.;

		int iPNorm = pNorm();

		while (null != adblVariate) {
			double dblProbabilityDensity = multiDist.density (adblVariate);

			double dblInstanceNorm = 0.;
			dblNormalizer += dblProbabilityDensity;

			for (int i = 0; i < iDimension; ++i)
				dblInstanceNorm += dblProbabilityDensity * java.lang.Math.pow (java.lang.Math.abs
					(adblVariate[i]), iPNorm);

			dblPopulationMetricNorm += dblInstanceNorm * am.evaluate (adblVariate);

			adblVariate = crmi.nextVariates();
		}

		return java.lang.Math.pow (dblPopulationMetricNorm / dblNormalizer, 1. / iPNorm);
	}
}
