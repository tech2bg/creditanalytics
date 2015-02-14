
package org.drip.quant.discrete;

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
 * SequenceMetrics contains the Distribution Metrics related to the specified sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SequenceMetrics {
	private boolean _bIsPositive = true;
	private double[] _adblSequence = null;
	private double _dblVariance = java.lang.Double.NaN;
	private double _dblExpectation = java.lang.Double.NaN;

	/**
	 * Build out the Sequence and their Metrics
	 * 
	 * @param adblSequence Array of Sequence Entries
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public SequenceMetrics (
		final double[] adblSequence)
		throws java.lang.Exception
	{
		if (null == (_adblSequence = adblSequence))
			throw new java.lang.Exception ("SequenceMetrics ctr: Invalid Inputs");

		int iNumEntry = _adblSequence.length;

		if (0 == iNumEntry) throw new java.lang.Exception ("SequenceMetrics ctr: Invalid Inputs");

		for (int i = 0; i < iNumEntry; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (_adblSequence[i]))
				throw new java.lang.Exception ("SequenceMetrics ctr: Invalid Inputs");

			_dblExpectation += _adblSequence[i];
			_bIsPositive = _adblSequence[i] > 0.;
		}

		_dblExpectation /= iNumEntry;

		for (int i = 0; i < iNumEntry; ++i)
			_dblVariance += (_adblSequence[i] - _dblExpectation) * (_adblSequence[i] - _dblExpectation);

		_dblVariance /= iNumEntry;
	}

	/**
	 * Compute the Specified Moment of the Discrete Sequence
	 * 
	 * @param iMoment The Moment
	 * 
	 * @return The Specified Moment of the Discrete Sequence
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double moment (
		final int iMoment)
		throws java.lang.Exception
	{
		if (0 >= iMoment) throw new java.lang.Exception ("SequenceMetrics::moment => Invalid Moment");

		double dblMoment = 0.;
		int iNumEntry = _adblSequence.length;

		for (int i = 0; i < iNumEntry; ++i)
			dblMoment += java.lang.Math.pow (_adblSequence[i] - _dblExpectation, iMoment);

		return dblMoment;
	}

	/**
	 * Retrieve the Expectation of the Function across the realized Sequence
	 * 
	 * @param au The Function whose Expectation is to be Computed
	 * 
	 * @return The Expectation of the Function across the realized Sequence
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double functionExpectation (
		final org.drip.quant.function1D.AbstractUnivariate au)
		throws java.lang.Exception
	{
		if (null == au)
			throw new java.lang.Exception ("SequenceMetrics::functionExpectation => Invalid Input");

		double dblMean = 0.;
		int iNumEntry = _adblSequence.length;

		for (int i = 0; i < iNumEntry; ++i)
			dblMean += _adblSequence[i];

		return dblMean;
	}

	/**
	 * Retrieve the Variance of the Function across the realized Sequence
	 * 
	 * @param au The Function whose Variance is to be Computed
	 * 
	 * @return The Variance of the Function across the realized Sequence
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double functionVariance (
		final org.drip.quant.function1D.AbstractUnivariate au)
		throws java.lang.Exception
	{
		if (null == au)
			throw new java.lang.Exception ("SequenceMetrics::functionVariance => Invalid Input");

		double dblVariance = 0.;
		int iNumEntry = _adblSequence.length;

		double dblExpectation = functionExpectation (au);

		for (int i = 0; i < iNumEntry; ++i) {
			double dblMeanShift = au.evaluate (_adblSequence[i]) - dblExpectation;

			dblVariance += dblMeanShift * dblMeanShift;
		}

		return dblVariance;
	}

	/**
	 * Compute the Specified Moment of the Function across the realized Sequence
	 * 
	 * @param au The Function whose Moment is to be Computed
	 * @param iMoment The Moment
	 * 
	 * @return The Specified Moment of the Function across the realized Sequence
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double functionMoment (
		final org.drip.quant.function1D.AbstractUnivariate au,
		final int iMoment)
		throws java.lang.Exception
	{
		if (null == au || 0 >= iMoment)
			throw new java.lang.Exception ("SequenceMetrics::functionMoment => Invalid Inputs");

		double dblMoment = 0.;
		int iNumEntry = _adblSequence.length;

		double dblExpectation = functionExpectation (au);

		for (int i = 0; i < iNumEntry; ++i)
			dblMoment += java.lang.Math.pow (au.evaluate (_adblSequence[i]) - dblExpectation, iMoment);

		return dblMoment;
	}

	/**
	 * Retrieve the Distribution Expectation
	 * 
	 * @return The Distribution Expectation
	 */

	public double expectation()
	{
		return _dblExpectation;
	}

	/**
	 * Retrieve the Distribution Variance
	 * 
	 * @return The Distribution Variance
	 */

	public double variance()
	{
		return _dblVariance;
	}

	/**
	 * Retrieve the Sequence Positiveness Flag
	 * 
	 * @return TRUE => The Sequence is Positiveness
	 */

	public boolean isPositive()
	{
		return _bIsPositive;
	}

	/**
	 * Retrieve the Input Sequence
	 * 
	 * @return The Input Sequence
	 */

	public double[] sequence()
	{
		return _adblSequence;
	}

	/**
	 * Retrieve the Markov Upper Limiting Probability Bound for the Specified Level:
	 * 	- P (X >= t) <= E[f(X)] / f(t)
	 * 
	 * @param dblLevel The Specified Level
	 * @param auNonDecreasing The Non-decreasing Bounding Transformer Function
	 * 
	 * @return The Markov Upper Limiting Probability Bound for the Specified Level
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double markovUpperProbabilityBound (
		final double dblLevel,
		final org.drip.quant.function1D.AbstractUnivariate auNonDecreasing)
		throws java.lang.Exception
	{
		if (!isPositive() || !org.drip.quant.common.NumberUtil.IsValid (dblLevel) || dblLevel <= 0.)
			throw new java.lang.Exception ("SequenceMetrics::markovUpperProbabilityBound => Invalid Inputs");

		double dblUpperProbabilityBound = null == auNonDecreasing ? _dblExpectation / dblLevel :
			functionExpectation (auNonDecreasing) / auNonDecreasing.evaluate (dblLevel);

		return dblUpperProbabilityBound < 1. ? dblUpperProbabilityBound : 1.;
	}
}
