
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
 * EfronSteinAgnosticMetrics contains the Variance-based non-exponential Sample Distribution/Bounding Metrics
 *  and Agnostic Bounds related to the Functional Transformation of the specified Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class EfronSteinAgnosticMetrics {
	private org.drip.quant.function.AbstractMultivariate _am = null;
	private org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] _aSSAM = null;

	/**
	 * EfronSteinAgnosticMetrics Constructor
	 * 
	 * @param am Multivariate Objective Function
	 * @param aSSAM Array of the individual Single Sequence Metrics
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public EfronSteinAgnosticMetrics (
		final org.drip.quant.function.AbstractMultivariate am,
		final org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] aSSAM)
		throws java.lang.Exception
	{
		if (null == (_am = am) || null != (_aSSAM = aSSAM))
			throw new java.lang.Exception ("EfronSteinAgnosticMetrics ctr: Invalid Inputs");

		int iNumVariable = _am.numVariables();

		if (iNumVariable != _aSSAM.length)
			throw new java.lang.Exception ("EfronSteinAgnosticMetrics ctr: Invalid Inputs");

		int iSequenceLength = _aSSAM[0].sequence().length;

		for (int i = 1; i < iNumVariable; ++i) {
			if (null == _aSSAM[i] || _aSSAM[i].sequence().length != iSequenceLength)
				throw new java.lang.Exception ("EfronSteinAgnosticMetrics ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Multivariate Objective Function
	 * 
	 * @return The Multivariate Objective Function Instance
	 */

	public org.drip.quant.function.AbstractMultivariate function()
	{
		return _am;
	}

	/**
	 * Retrieve the Array of the Single Sequence Agnostic Metrics
	 * 
	 * @return The Array of the Single Sequence Agnostic Metrics
	 */

	public org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] randomSequenceGenerator()
	{
		return _aSSAM;
	}

	/**
	 * Compute the Function Sequence Agnostic Metrics associated with each Variate
	 * 
	 * @return The Array of the Associated Sequence Metrics
	 */

	public org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] univariateSequenceMetrics() {
		int iNumVariate = _am.numVariables();

		int iSequenceSize = _aSSAM[0].sequence().length;

		double[] adblFunctionArgs = new double[iNumVariate];
		double[] adblFunctionSequence = new double[iSequenceSize];
		org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] aSSAM = new
			org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[iNumVariate];

		try {
			for (int iVariateIndex = 0; iVariateIndex < iNumVariate; ++iVariateIndex) {
				double[] adblIndexVariateSequence = _aSSAM[iVariateIndex].sequence();

				for (int iSequenceIndex = 0; iSequenceIndex < iSequenceSize; ++iSequenceIndex) {
					double dblVariate = adblFunctionArgs[iVariateIndex];
					adblFunctionArgs[iVariateIndex] = adblIndexVariateSequence[iSequenceIndex];

					adblFunctionSequence[iSequenceIndex] = _am.evaluate (adblFunctionArgs);

					adblFunctionArgs[iVariateIndex] = dblVariate;
				}

				aSSAM[iVariateIndex] = new org.drip.sequence.bounds.SingleSequenceAgnosticMetrics
					(adblFunctionSequence, null);
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Multivariate Variance Upper Bound using the Martingale Differences Method
	 * 
	 * @return The Multivariate Variance Upper Bound using the Martingale Differences Method
	 * 
	 * @throws java.lang.Exception Thrown if the Upper Bound cannot be calculated
	 */

	public double martingaleVarianceUpperBound()
		throws java.lang.Exception
	{
		double dblVarianceUpperBound = 0.;

		int iNumVariate = _am.numVariables();

		org.drip.sequence.bounds.SingleSequenceAgnosticMetrics[] aSSAM = univariateSequenceMetrics();

		if (null == aSSAM || iNumVariate != aSSAM.length)
			throw new java.lang.Exception
				("EfronSteinAgnosticMetrics::martingaleVarianceUpperBound => Cannot compute Univariate Sequence Metrics");

		for (int i = 0; i < iNumVariate; ++i)
			dblVarianceUpperBound += aSSAM[i].populationVariance();

		return dblVarianceUpperBound;
	}
}
