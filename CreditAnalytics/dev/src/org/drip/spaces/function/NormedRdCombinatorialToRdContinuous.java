
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
 * NormedRdCombinatorialToRdContinuous implements the f : Validated R^d Combinatorial -> Validated R^d
 *  Continuous Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedRdCombinatorialToRdContinuous extends org.drip.spaces.function.NormedRdToRd {

	/**
	 * NormedRdCombinatorialToRdContinuous Function Space Constructor
	 * 
	 * @param funcRdToRd The RdToRd Function
	 * @param crmvInput The Combinatorial R^d Input Vector Space (may/may not be Normed)
	 * @param crmvOutput The Continuous R^d Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedRdCombinatorialToRdContinuous (
		final org.drip.function.deterministic.RdToRd funcRdToRd,
		final org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector crmvInput,
		final org.drip.spaces.tensor.ContinuousRealMultidimensionalVector crmvOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (crmvInput, crmvOutput, funcRdToRd, iPNorm);
	}

	@Override public double[] populationRdMetricNorm()
	{
		org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput = input();

		if (!(gmvsInput instanceof org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach))
			return null;

		org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach crmb =
			(org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach) gmvsInput;

		org.drip.measure.continuous.MultivariateDistribution multiDist = crmb.borelSigmaMeasure();

		if (null == multiDist) return null;

		org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator crmi = crmb.iterator();

		org.drip.function.deterministic.RdToRd funcRdToRd = function();

		double[] adblVariate = crmi.cursorVariates();

		double dblProbabilityDensity = java.lang.Double.NaN;
		double[] adblPopulationMetricNorm = null;
		int iOutputDimension = -1;
		double dblNormalizer = 0.;

		int iPNorm = pNorm();

		while (null != adblVariate) {
			try {
				dblProbabilityDensity = multiDist.density (adblVariate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			double[] adblValue = funcRdToRd.evaluate (adblVariate);

			if (null == adblValue || 0 == (iOutputDimension = adblValue.length)) return null;

			dblNormalizer += dblProbabilityDensity;

			if (null == adblPopulationMetricNorm) {
				adblPopulationMetricNorm = new double[iOutputDimension];

				for (int i = 0; i < iOutputDimension; ++i)
					adblPopulationMetricNorm[i] = 0;
			}

			for (int i = 0; i < iOutputDimension; ++i)
				adblPopulationMetricNorm[i] += dblProbabilityDensity * java.lang.Math.pow (java.lang.Math.abs
					(adblValue[i]), iPNorm);

			adblVariate = crmi.nextVariates();
		}

		for (int i = 0; i < iOutputDimension; ++i)
			adblPopulationMetricNorm[i] += java.lang.Math.pow (adblPopulationMetricNorm[i] / dblNormalizer,
				1. / iPNorm);

		return adblPopulationMetricNorm;
	}
}
