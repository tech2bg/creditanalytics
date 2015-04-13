
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
 * NormedRdSpaceToRdSpace is the abstract class underlying the f : Post-Validated R^d -> Post-Validated R^d
 *  Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class NormedRdSpaceToRdSpace extends org.drip.spaces.function.NormedRdInputSpace {
	private org.drip.function.deterministic.RdToRd _funcRdToRd = null;
	private org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace _gmvsOutput = null;

	protected NormedRdSpaceToRdSpace (
		final org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput,
		final org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsOutput,
		final org.drip.function.deterministic.RdToRd funcRdToRd,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (gmvsInput, iPNorm);

		if (null == (_gmvsOutput = gmvsOutput) || null == (_funcRdToRd = funcRdToRd))
			throw new java.lang.Exception ("NormedRdSpaceToRdSpace ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying RdToRd Function
	 * 
	 * @return The Underlying RdToRd Function
	 */

	public org.drip.function.deterministic.RdToRd function()
	{
		return _funcRdToRd;
	}

	/**
	 * Retrieve the Sample Supremum R^d Norm Array
	 * 
	 * @param vcrmInstance The Validated Combinatorial Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Supremum R^d Norm Array
	 */

	public double[] sampleRdSupremumNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (input())) return null;

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;

		int iOutputDimension = _gmvsOutput.dimension();

		double[] adblSupremumNorm = _funcRdToRd.evaluate (aadblInstance[0]);

		if (null == adblSupremumNorm || iOutputDimension != adblSupremumNorm.length ||
			!org.drip.quant.common.NumberUtil.IsValid (adblSupremumNorm))
			return null;

		for (int i = 0; i < iOutputDimension; ++i)
			adblSupremumNorm[i] = java.lang.Math.abs (adblSupremumNorm[i]);

		for (int i = 1; i < iNumSample; ++i) {
			double[] adblSampleNorm = _funcRdToRd.evaluate (aadblInstance[i]);

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
	 * @param vcrmInstance The Validated Combinatorial Real Valued Multidimensional Instance
	 * 
	 * @return The Sample R^d Metric Norm Array
	 */

	public double[] sampleRdMetricNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (input())) return null;

		double[][] aadblInstance = vcrmInstance.instance();

		int iOutputDimension = _gmvsOutput.dimension();

		double[] adblMetricNorm = new double[iOutputDimension];
		int iNumSample = aadblInstance.length;

		int iPNorm = pNorm();

		for (int i = 0; i < iNumSample; ++i)
			adblMetricNorm[i] = 0.;

		for (int i = 0; i < iNumSample; ++i) {
			double[] adblPointValue = _funcRdToRd.evaluate (aadblInstance[i]);

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
		org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput = input();

		return gmvsInput instanceof org.drip.spaces.metric.ContinuousRealMultidimensionalBanach ?
			_funcRdToRd.evaluate (((org.drip.spaces.metric.ContinuousRealMultidimensionalBanach)
				gmvsInput).populationMode()) : null;
	}

	@Override public org.drip.spaces.tensor.GeneralizedVectorSpace output()
	{
		return _gmvsOutput;
	}

	/**
	 * Retrieve the Population R^d Metric Norm Array
	 * 
	 * @return The Population R^d Metric Norm Array
	 */

	public abstract double[] populationRdMetricNorm();
}
