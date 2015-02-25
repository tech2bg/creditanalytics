
package org.drip.sequence.random;

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
 * MultivariateFunction contains the implementation of the objective Function dependent on Multivariate
 *  Random Variables.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class MultivariateFunction extends org.drip.function.deterministic.AbstractMultivariate {

	/**
	 * Compute the Target Variate Function Metrics conditional on the specified Input Non-Target Variate
	 *  Parameter Sequence
	 * 
	 * @param adblNonTargetVariate The Non-Target Variate Parameters
	 * @param iTargetVariateIndex Target Variate Index
	 * @param ssamTarget Target Variate Metrics
	 * 
	 * @return The Variate-specific Function Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics conditionalTargetVariateMetrics (
		final double[] adblNonTargetVariate,
		final int iTargetVariateIndex,
		final org.drip.sequence.metrics.SingleSequenceAgnosticMetrics ssamTarget)
	{
		if (!org.drip.function.deterministic.AbstractMultivariate.ValidateInput (adblNonTargetVariate) ||
			null == ssamTarget)
			return null;

		int iNumNonTargetVariate = adblNonTargetVariate.length;

		if (0 > iTargetVariateIndex || iTargetVariateIndex > iNumNonTargetVariate) return null;

		double[] adblTargetVariateSample = ssamTarget.sequence();

		int iNumTargetVariateSample = adblTargetVariateSample.length;
		double[] adblFunctionArgs = new double[iNumNonTargetVariate + 1];
		double[] adblFunctionSequence = new double[iNumTargetVariateSample];

		for (int i = 0; i < iNumNonTargetVariate; ++i) {
			if (i < iTargetVariateIndex)
				adblFunctionArgs[i] = adblNonTargetVariate[i];
			else if (i >= iTargetVariateIndex)
				adblFunctionArgs[i + 1] = adblNonTargetVariate[i];
		}

		try {
			for (int i = 0; i < iNumTargetVariateSample; ++i) {
				adblFunctionArgs[iTargetVariateIndex] = adblTargetVariateSample[i];

				adblFunctionSequence[i] = evaluate (adblFunctionArgs);
			}

			return new org.drip.sequence.metrics.SingleSequenceAgnosticMetrics (adblFunctionSequence, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Target Variate Function Metrics conditional on the specified Input Non-target Variate
	 *  Parameter Sequence
	 * 
	 * @param aSSAM Array of Variate Sequence Metrics
	 * @param aiNonTargetVariateSequenceIndex Array of Non-Target Variate Sequence Indexes
	 * @param iTargetVariateIndex Target Variate Index
	 * 
	 * @return The Variate-specific Function Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics conditionalTargetVariateMetrics (
		final org.drip.sequence.metrics.SingleSequenceAgnosticMetrics[] aSSAM,
		final int[] aiNonTargetVariateSequenceIndex,
		final int iTargetVariateIndex)
	{
		if (null == aSSAM || null == aiNonTargetVariateSequenceIndex || 0 > iTargetVariateIndex) return null;

		int iNumNonTargetVariate = aSSAM.length - 1;
		double[] adblNonTargetVariate = new double[iNumNonTargetVariate];

		if (0 >= iNumNonTargetVariate || iNumNonTargetVariate != aiNonTargetVariateSequenceIndex.length ||
			iTargetVariateIndex > iNumNonTargetVariate)
			return null;

		for (int i = 0; i < iNumNonTargetVariate; ++i)
			adblNonTargetVariate[i] = aSSAM[i < iTargetVariateIndex ? i : i +
				1].sequence()[aiNonTargetVariateSequenceIndex[i]];

		return conditionalTargetVariateMetrics (adblNonTargetVariate, iTargetVariateIndex,
			aSSAM[iTargetVariateIndex]);
	}

	/**
	 * Compute the Target Variate Function Metrics over the full Non-target Variate Empirical Distribution
	 * 
	 * @param aSSAM Array of Variate Sequence Metrics
	 * @param iTargetVariateIndex Target Variate Index
	 * 
	 * @return The Variate-specific Function Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics unconditionalTargetVariateMetrics (
		final org.drip.sequence.metrics.SingleSequenceAgnosticMetrics[] aSSAM,
		final int iTargetVariateIndex)
	{
		if (null == aSSAM || 0 > iTargetVariateIndex) return null;

		int iTargetVariateVarianceIndex = 0;
		int iNumNonTargetVariate = aSSAM.length - 1;

		if (0 >= iNumNonTargetVariate) return null;

		org.drip.analytics.support.SequenceIndexIterator sii =
			org.drip.analytics.support.SequenceIndexIterator.Standard (iNumNonTargetVariate,
				aSSAM[0].sequence().length);

		if (null == sii) return null;

		double[] adblTargetVariateVariance = new double[sii.size()];

		int[] aiNonTargetVariateSequenceIndex = sii.first();

		while (null != aiNonTargetVariateSequenceIndex && aiNonTargetVariateSequenceIndex.length ==
			iNumNonTargetVariate) {
			org.drip.sequence.metrics.SingleSequenceAgnosticMetrics ssamConditional =
				conditionalTargetVariateMetrics (aSSAM, aiNonTargetVariateSequenceIndex,
					iTargetVariateIndex);

			if (null == ssamConditional) return null;

			adblTargetVariateVariance[iTargetVariateVarianceIndex++] = ssamConditional.empiricalVariance();

			aiNonTargetVariateSequenceIndex = sii.next();
		}

		try {
			return new org.drip.sequence.metrics.SingleSequenceAgnosticMetrics (adblTargetVariateVariance,
				null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
