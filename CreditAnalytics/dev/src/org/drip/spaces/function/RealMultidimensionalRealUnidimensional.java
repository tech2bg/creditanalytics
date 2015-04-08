
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
 * RealMultidimensionalRealUnidimensional implements the f : R^d -> R^1 Function-level Normed Function Space.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RealMultidimensionalRealUnidimensional {
	private int _iPNorm = -1;
	private org.drip.function.deterministic.AbstractMultivariate _am = null;
	private org.drip.spaces.tensor.ContinuousRealUnidimensionalVector _cruOutput = null;
	private org.drip.spaces.tensor.ContinuousRealMultidimensionalVector _crmInput = null;

	/**
	 * RealMultidimensionalRealUnidimensional Function Space Constructor
	 * 
	 * @param am The Multivariate Function
	 * @param crmInput The R^d Input Vector Space (may/may not be Normed)
	 * @param cruOutput The R^1 Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public RealMultidimensionalRealUnidimensional (
		final org.drip.function.deterministic.AbstractMultivariate am,
		final org.drip.spaces.tensor.ContinuousRealMultidimensionalVector crmInput,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		if (null == (_am = am) || null == (_crmInput = crmInput) || null == (_cruOutput = cruOutput) || 0 >
			(_iPNorm = iPNorm))
			throw new java.lang.Exception ("RealMultidimensionalRealUnidimensional ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Multivariate Function
	 * 
	 * @return The Underlying Multivariate Function
	 */

	public org.drip.function.deterministic.AbstractMultivariate function()
	{
		return _am;
	}

	/**
	 * Retrieve the R^d Input Vector Space
	 * 
	 * @return The R^d Input Vector Space
	 */

	public org.drip.spaces.tensor.ContinuousRealMultidimensionalVector input()
	{
		return _crmInput;
	}

	/**
	 * Retrieve the R^1 Output Vector Space
	 * 
	 * @return The R^1 Output Vector Space
	 */

	public org.drip.spaces.tensor.ContinuousRealUnidimensionalVector output()
	{
		return _cruOutput;
	}

	/**
	 * Retrieve the P-Norm Index
	 * 
	 * @return The P-Norm Index
	 */

	public int pNorm()
	{
		return _iPNorm;
	}

	/**
	 * Retrieve the Sample Supremum Norm
	 * 
	 * @param vcrmInstance The Validated Continuous Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Supremum Norm
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be computed
	 */

	public double sampleSupremumNorm (
		final org.drip.spaces.instance.ValidatedContinuousRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (_crmInput))
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::sampleSupremumNorm => Invalid Input");

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;

		double dblSupremumNorm = java.lang.Math.abs (_am.evaluate (aadblInstance[0]));

		for (int i = 1; i < iNumSample; ++i) {
			double dblResponse = java.lang.Math.abs (_am.evaluate (aadblInstance[i]));

			if (dblResponse > dblSupremumNorm) dblSupremumNorm = dblResponse;
		}

		return dblSupremumNorm;
	}

	/**
	 * Retrieve the Sample Metric Norm
	 * 
	 * @param vcrmInstance The Validated Continuous Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Sample Metric Norm cannot be computed
	 */

	public double sampleMetricNorm (
		final org.drip.spaces.instance.ValidatedContinuousRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (_crmInput))
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::sampleMetricNorm => Invalid Input");

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;
		double dblNorm = java.lang.Double.NaN;

		for (int i = 0; i < iNumSample; ++i)
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (_am.evaluate (aadblInstance[i])), _iPNorm);

		return java.lang.Math.pow (dblNorm, 1. / _iPNorm);
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
		if (!(_crmInput instanceof org.drip.spaces.metric.ContinuousRealMultidimensionalBanach))
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::populationESS => Invalid Input Vector Space");

		final org.drip.measure.continuous.MultivariateDistribution multiDist =
			((org.drip.spaces.metric.ContinuousRealMultidimensionalBanach) _crmInput).borelSigmaMeasure();

		if (null == multiDist)
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::populationESS => Measure not specified");

		org.drip.function.deterministic.AbstractMultivariate am = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				return multiDist.density (adblX) * _am.evaluate (adblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMode = am.maxima (_crmInput.leftEdge(),
			_crmInput.rightEdge());

		return null == vopMode ? null : vopMode.output() / multiDist.density (vopMode.variates());
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
		if (!(_crmInput instanceof org.drip.spaces.metric.ContinuousRealMultidimensionalBanach))
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::populationMetricNorm => Invalid Input Vector Space");

		final org.drip.measure.continuous.MultivariateDistribution multiDist =
			((org.drip.spaces.metric.ContinuousRealMultidimensionalBanach) _crmInput).borelSigmaMeasure();

		if (null == multiDist)
			throw new java.lang.Exception
				("RealMultidimensionalRealUnidimensional::populationMetricNorm => Measure not specified");

		org.drip.function.deterministic.AbstractMultivariate am = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				double dblNorm = 0.;
				int iDimension = adblX.length;

				for (int i = 0; i < iDimension; ++i)
					dblNorm += java.lang.Math.pow (java.lang.Math.abs (adblX[i]), _iPNorm);

				return dblNorm * multiDist.density (adblX) * _am.evaluate (adblX);
			}
		};

		return java.lang.Math.pow (am.integrate (_crmInput.leftEdge(), _crmInput.rightEdge()), 1. / _iPNorm);
	}
}
