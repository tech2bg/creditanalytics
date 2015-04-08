
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
 * BoundedUniformInteger implements the Bounded Uniform Distribution, with a Uniform Integer being generated
 *  between a lower and an upper Bound.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedUniformInteger extends org.drip.sequence.random.UnivariateSequenceGenerator {
	private int _iStart = -1;
	private int _iFinish = -1;

	private java.util.Random _rng = new java.util.Random();

	/**
	 * BoundedUniformInteger Distribution Constructor
	 * 
	 * @param dblLowerBound The Lower Bound
	 * @param dblUpperBound The Upper Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BoundedUniformInteger (
		final int iStart,
		final int iFinish)
		throws java.lang.Exception
	{
		if ((_iFinish = iFinish) <= (_iStart = iStart))
			throw new java.lang.Exception ("BoundedUniformInteger constructor: Invalid inputs");
	}

	/**
	 * Retrieve the Start
	 * 
	 * @return The Start
	 */

	public int start()
	{
		return _iStart;
	}

	/**
	 * Retrieve the Finish
	 * 
	 * @return The Finish
	 */

	public int finish()
	{
		return _iFinish;
	}

	@Override public double random()
	{
		return (int) (_iStart + _rng.nextDouble() * (_iFinish - _iStart) + 0.5);
	}

	@Override public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequence (
		final int iNumEntry,
		final org.drip.measure.continuous.UnivariateDistribution distPopulation)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.sequence.metrics.IntegerSequenceAgnosticMetrics (adblSequence, distPopulation);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
