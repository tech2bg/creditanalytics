
package org.drip.sequence.functional;

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
 * FunctionSupremumUnivariateRandom contains the Implementation of the FunctionClassSupremum Objective
 *  Function dependent on Univariate Random Variable.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FunctionSupremumUnivariateRandom extends org.drip.function.deterministic1D.FunctionClassSupremum
{
	private org.drip.measure.continuous.UnivariateDistribution _dist = null;

	/**
	 * FunctionSupremumUnivariateRandom Constructor
	 * 
	 * @param aAUClass Array of Functions in the Class
	 * @param dist The Underlying Distribution
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public FunctionSupremumUnivariateRandom (
		final org.drip.function.deterministic.AbstractUnivariate[] aAUClass,
		final org.drip.measure.continuous.UnivariateDistribution dist)
		throws java.lang.Exception
	{
		super (aAUClass);

		_dist = dist;
	}

	/**
	 * Generate the Function Metrics for the specified Variate Sequence and its corresponding Weight
	 * 
	 * @param adblVariateSequence The specified Variate Sequence
	 * @param adblVariateWeight The specified Variate Weight
	 * 
	 * @return The Function Sequence Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequenceMetrics (
		final double[] adblVariateSequence,
		final double[] adblVariateWeight)
	{
		if (null == adblVariateSequence || null == adblVariateWeight) return null;

		int iNumVariate = adblVariateSequence.length;
		double[] adblFunctionSequence = new double[iNumVariate];

		if (0 == iNumVariate || iNumVariate != adblVariateWeight.length) return null;

		try {
			for (int i = 0; i < iNumVariate; ++i)
				adblFunctionSequence[i] = adblVariateWeight[i] * evaluate (adblVariateSequence[i]);

			return new org.drip.sequence.metrics.SingleSequenceAgnosticMetrics (adblFunctionSequence, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the Function Metrics for the specified Variate Sequence
	 * 
	 * @param adblVariateSequence The specified Variate Sequence
	 * 
	 * @return The Function Sequence Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequenceMetrics (
		final double[] adblVariateSequence)
	{
		if (null == adblVariateSequence) return null;

		int iNumVariate = adblVariateSequence.length;
		double[] adblVariateWeight = new double[iNumVariate];

		for (int i = 0; i < iNumVariate; ++i)
			adblVariateWeight[i] = 1.;

		return sequenceMetrics (adblVariateSequence, adblVariateWeight);
	}

	/**
	 * Generate the Function Metrics using the Underlying Variate Distribution
	 * 
	 * @return The Function Sequence Metrics
	 */

	public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequenceMetrics()
	{
		if (null == _dist) return null;

		org.drip.quant.common.Array2D a2DHistogram = _dist.histogram();

		return null == a2DHistogram ? null : sequenceMetrics (a2DHistogram.x(), a2DHistogram.y());
	}

	/**
	 * Retrieve the Underlying Distribution
	 * 
	 * @return The Underlying Distribution
	 */

	public org.drip.measure.continuous.UnivariateDistribution underlyingDistribution()
	{
		return _dist;
	}
}
