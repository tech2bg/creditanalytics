
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
 * RdCombinatorialToR1Continuous implements the f : R^d Combinatorial -> R^1 Continuous Normed Function
 *  Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RdCombinatorialToR1Continuous extends org.drip.spaces.function.RdToR1 {
	private org.drip.spaces.tensor.ContinuousRealUnidimensionalVector _cruvOutput = null;
	private org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector _crmvInput = null;

	/**
	 * RdCombinatorialToR1Continuous Function Space Constructor
	 * 
	 * @param am The Multivariate Function
	 * @param crmvInput The Combinatorial R^d Input Vector Space (may/may not be Normed)
	 * @param cruvOutput The Continuous R^1 Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public RdCombinatorialToR1Continuous (
		final org.drip.function.deterministic.AbstractMultivariate am,
		final org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector crmvInput,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruvOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (am, iPNorm);

		if (null == (_crmvInput = crmvInput) || null == (_cruvOutput = cruvOutput))
			throw new java.lang.Exception ("RdCombinatorialToR1Continuous ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Combinatorial R^d Input Vector Space
	 * 
	 * @return The Combinatorial R^d Input Vector Space
	 */

	public org.drip.spaces.tensor.CombinatorialRealMultidimensionalVector input()
	{
		return _crmvInput;
	}

	/**
	 * Retrieve the Continuous R^1 Output Vector Space
	 * 
	 * @return The Continuous R^1 Output Vector Space
	 */

	public org.drip.spaces.tensor.ContinuousRealUnidimensionalVector output()
	{
		return _cruvOutput;
	}

	/**
	 * Retrieve the Sample Supremum Norm
	 * 
	 * @param vcrmInstance The Validated Combinatorial Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Supremum Norm
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be computed
	 */

	public double sampleSupremumNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (_crmvInput))
			throw new java.lang.Exception
				("RdCombinatorialToR1Continuous::sampleSupremumNorm => Invalid Input");

		org.drip.function.deterministic.AbstractMultivariate am = function();

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;

		double dblSupremumNorm = java.lang.Math.abs (am.evaluate (aadblInstance[0]));

		for (int i = 1; i < iNumSample; ++i) {
			double dblResponse = java.lang.Math.abs (am.evaluate (aadblInstance[i]));

			if (dblResponse > dblSupremumNorm) dblSupremumNorm = dblResponse;
		}

		return dblSupremumNorm;
	}

	/**
	 * Retrieve the Sample Metric Norm
	 * 
	 * @param vcrmInstance The Validated Combinatorial Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Sample Metric Norm cannot be computed
	 */

	public double sampleMetricNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (_crmvInput))
			throw new java.lang.Exception
				("RdCombinatorialToR1Continuous::sampleMetricNorm => Invalid Input");

		org.drip.function.deterministic.AbstractMultivariate am = function();

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;
		double dblNorm = java.lang.Double.NaN;

		int iPNorm = pNorm();

		for (int i = 0; i < iNumSample; ++i)
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (am.evaluate (aadblInstance[i])), iPNorm);

		return java.lang.Math.pow (dblNorm, 1. / iPNorm);
	}

	/**
	 * Retrieve the Population ESS (Essential Spectrum)
	 * 
	 * @return The Population ESS (Essential Spectrum)
	 * 
	 * @throws java.lang.Exception Thrown if the Population ESS (Essential Spectrum) cannot be computed
	 */

	public double populationESS()
		throws java.lang.Exception
	{
		if (!(_crmvInput instanceof org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach))
			throw new java.lang.Exception
				("RdCombinatorialToR1Continuous::populationESS => Incomptabile Input Vector Space");

		return function().evaluate (((org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach)
			_crmvInput).populationMode());
	}

	/**
	 * Retrieve the Population Metric Norm
	 * 
	 * @return The Population Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Population Metric Norm cannot be computed
	 */

	public double populationMetricNorm()
		throws java.lang.Exception
	{
		if (!(_crmvInput instanceof org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach))
			throw new java.lang.Exception
				("RdCombinatorialToR1Continuous::populationMetricNorm => Incomptabile Input Vector Space");

		org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach crmb =
			(org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach) _crmvInput;

		org.drip.measure.continuous.MultivariateDistribution multiDist = crmb.borelSigmaMeasure();

		if (null == multiDist)
			throw new java.lang.Exception
				("RdCombinatorialToR1Continuous::populationMetricNorm => No Multivariate Distribution");

		org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator crmi = crmb.iterator();

		org.drip.function.deterministic.AbstractMultivariate am = function();

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
