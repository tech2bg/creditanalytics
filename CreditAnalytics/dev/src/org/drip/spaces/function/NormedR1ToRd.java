
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
 * NormedR1ToRd is the abstract class underlying the f : Post-Validated R^1 -> Post-Validated R^d Normed
 *  Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class NormedR1ToRd extends org.drip.spaces.function.NormedR1Input {
	private org.drip.function.deterministic.R1ToRd _funcR1ToRd = null;
	private org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace _gmvsOutput = null;

	protected NormedR1ToRd (
		final org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsInput,
		final org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsOutput,
		final org.drip.function.deterministic.R1ToRd funcR1ToRd,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (guvsInput, iPNorm);

		if (null == (_gmvsOutput = gmvsOutput) || null == (_funcR1ToRd = funcR1ToRd))
			throw new java.lang.Exception ("NormedR1ToRd ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying R1ToRd Function
	 * 
	 * @return The Underlying R1ToR1 Function
	 */

	public org.drip.function.deterministic.R1ToRd function()
	{
		return _funcR1ToRd;
	}

	/**
	 * Retrieve the Sample Supremum R^d Norm Array
	 * 
	 * @param vruInstance The Validated Real Valued Unidimensional Instance
	 * 
	 * @return The Sample Supremum R^d Norm Array
	 */

	public double[] sampleRdSupremumNorm (
		final org.drip.spaces.instance.ValidatedRealUnidimensional vruInstance)
		throws java.lang.Exception
	{
		if (null == vruInstance || !vruInstance.tensorSpaceType().match (input())) return null;

		double[] adblInstance = vruInstance.instance();

		int iNumSample = adblInstance.length;

		int iOutputDimension = _gmvsOutput.dimension();

		double[] adblSupremumNorm = _funcR1ToRd.evaluate (adblInstance[0]);

		if (null == adblSupremumNorm || iOutputDimension != adblSupremumNorm.length ||
			!org.drip.quant.common.NumberUtil.IsValid (adblSupremumNorm))
			return null;

		for (int i = 0; i < iOutputDimension; ++i)
			adblSupremumNorm[i] = java.lang.Math.abs (adblSupremumNorm[i]);

		for (int i = 1; i < iNumSample; ++i) {
			double[] adblSampleNorm = _funcR1ToRd.evaluate (adblInstance[i]);

			if (null == adblSampleNorm || iOutputDimension != adblSampleNorm.length) return null;

			for (int j = 0; j < iOutputDimension; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblSampleNorm[j])) return null;

				if (adblSampleNorm[j] > adblSupremumNorm[j]) adblSupremumNorm[j] = adblSampleNorm[j];
			}
		}

		return adblSupremumNorm;
	}

	/**
	 * Retrieve the Sample R^d Metric Norm Array
	 * 
	 * @param vruInstance The Validated Real Valued Unidimensional Instance
	 * 
	 * @return The Sample R^d Metric Norm Array
	 */

	public double[] sampleRdMetricNorm (
		final org.drip.spaces.instance.ValidatedRealUnidimensional vruInstance)
	{
		if (null == vruInstance || !vruInstance.tensorSpaceType().match (input())) return null;

		double[] adblInstance = vruInstance.instance();

		int iOutputDimension = _gmvsOutput.dimension();

		double[] adblMetricNorm = new double[iOutputDimension];
		int iNumSample = adblInstance.length;

		int iPNorm = pNorm();

		for (int i = 0; i < iNumSample; ++i)
			adblMetricNorm[i] = 0.;

		for (int i = 0; i < iNumSample; ++i) {
			double[] adblPointValue = _funcR1ToRd.evaluate (adblInstance[i]);

			if (null == adblPointValue || iOutputDimension != adblPointValue.length) return null;

			for (int j = 0; j < iOutputDimension; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblPointValue[j])) return null;

				adblMetricNorm[j] += java.lang.Math.pow (java.lang.Math.abs (adblPointValue[j]), iPNorm);
			}
		}

		for (int i = 0; i < iNumSample; ++i)
			adblMetricNorm[i] = java.lang.Math.pow (adblMetricNorm[i], 1. / iPNorm);

		return adblMetricNorm;
	}

	/**
	 * Retrieve the Population R^d ESS (Essential Spectrum) Array
	 * 
	 * @return The Population R^d ESS (Essential Spectrum) Array
	 */

	public double[] populationRdESS()
	{
		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsInput = input();

		try {
			return guvsInput instanceof org.drip.spaces.metric.ContinuousRealUnidimensional ?
				_funcR1ToRd.evaluate (((org.drip.spaces.metric.ContinuousRealUnidimensional)
					guvsInput).populationMode()) : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Population R^d Metric Norm Array
	 * 
	 * @return The Population R^d Metric Norm Array
	 */

	public abstract double[] populationRdMetricNorm();

	@Override public org.drip.spaces.tensor.GeneralizedVectorSpace output()
	{
		return _gmvsOutput;
	}

	@Override public double sampleSupremumNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvviInstance)
		throws java.lang.Exception
	{
		if (null == gvviInstance || !gvviInstance.tensorSpaceType().match (input()))
			throw new java.lang.Exception ("NormedR1ToRd::sampleSupremumNorm => Invalid Input");

		double[] adblSampleSupremumNorm = sampleRdSupremumNorm
			((org.drip.spaces.instance.ValidatedRealUnidimensional) gvviInstance);

		if (null == adblSampleSupremumNorm)
			throw new java.lang.Exception
				("NormedR1ToRd::sampleSupremumNorm => Cannot compute Sample Supremum Array");

		double dblSampleSupremumNorm = java.lang.Double.NaN;
		int iOutputDimension = adblSampleSupremumNorm.length;

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedR1ToRd::sampleSupremumNorm => Cannot compute Sample Supremum Array");

		for (int i = 0; i < iOutputDimension; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (dblSampleSupremumNorm))
				dblSampleSupremumNorm = adblSampleSupremumNorm[i];
			else {
				if (dblSampleSupremumNorm < adblSampleSupremumNorm[i])
					dblSampleSupremumNorm = adblSampleSupremumNorm[i];
			}
		}

		return dblSampleSupremumNorm;
	}

	@Override public double sampleMetricNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvviInstance)
		throws java.lang.Exception
	{
		if (null == gvviInstance || !gvviInstance.tensorSpaceType().match (input()))
			throw new java.lang.Exception ("NormedR1ToRd::sampleSupremumNorm => Invalid Input");

		double[] adblSampleMetricNorm = sampleRdMetricNorm
			((org.drip.spaces.instance.ValidatedRealUnidimensional) gvviInstance);

		if (null == adblSampleMetricNorm)
			throw new java.lang.Exception
				("NormedR1ToRd::sampleMetricNorm => Cannot compute Sample Metric Array");

		int iOutputDimension = adblSampleMetricNorm.length;
		double dblSampleMetricNorm = 0.;

		int iPNorm = pNorm();

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedR1ToRd::sampleMetricNorm => Cannot compute Sample Metric Array");

		for (int i = 0; i < iOutputDimension; ++i)
			dblSampleMetricNorm += java.lang.Math.pow (java.lang.Math.abs (adblSampleMetricNorm[i]), iPNorm);

		return java.lang.Math.pow (dblSampleMetricNorm, 1. / iPNorm);
	}

	@Override public double populationESS()
		throws java.lang.Exception
	{
		double[] adblPopulationRdESS = populationRdESS();

		if (null == adblPopulationRdESS)
			throw new java.lang.Exception
				("NormedR1ToRd::populationRdESS => Cannot compute Population Rd ESS Array");

		double dblPopulationESS = java.lang.Double.NaN;
		int iOutputDimension = adblPopulationRdESS.length;

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedR1ToRd::populationRdESS => Cannot compute Population Rd ESS Array");

		for (int i = 0; i < iOutputDimension; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (dblPopulationESS ))
				dblPopulationESS = adblPopulationRdESS[i];
			else {
				if (dblPopulationESS < adblPopulationRdESS[i]) dblPopulationESS = adblPopulationRdESS[i];
			}
		}

		return dblPopulationESS;
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		double[] adblPopulationMetricNorm = populationRdMetricNorm();

		if (null == adblPopulationMetricNorm)
			throw new java.lang.Exception
				("NormedR1ToRd::populationMetricNorm => Cannot compute Population Metric Array");

		int iPNorm = pNorm();

		double dblPopulationMetricNorm = 0.;
		int iOutputDimension = adblPopulationMetricNorm.length;

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedR1ToRd::populationMetricNorm => Cannot compute Population Metric Array");

		for (int i = 0; i < iOutputDimension; ++i)
			dblPopulationMetricNorm += java.lang.Math.pow (java.lang.Math.abs (adblPopulationMetricNorm[i]),
				iPNorm);

		return java.lang.Math.pow (dblPopulationMetricNorm, 1. / iPNorm);
	}
}
