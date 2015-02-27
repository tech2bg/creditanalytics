
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
 * Bounded implements the Bounded Random Univariate Generator with a Lower and an upper Bound.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class Bounded extends org.drip.sequence.random.RandomSequenceGenerator {
	private double _dblLowerBound = java.lang.Double.NaN;
	private double _dblUpperBound = java.lang.Double.NaN;

	protected Bounded (
		final double dblLowerBound,
		final double dblUpperBound)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLowerBound = dblLowerBound) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblUpperBound = dblUpperBound) || dblUpperBound <=
				dblLowerBound)
			throw new java.lang.Exception ("Bounded ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Lower Bound
	 * 
	 * @return The Lower Bound
	 */

	public double lowerBound()
	{
		return _dblLowerBound;
	}

	/**
	 * Retrieve the Upper Bound
	 * 
	 * @return The Upper Bound
	 */

	public double upperBound()
	{
		return _dblUpperBound;
	}

	@Override public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequence (
		final int iNumEntry,
		final org.drip.quant.distribution.Univariate distPopulation)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.sequence.metrics.UnitSequenceAgnosticMetrics (adblSequence, null ==
				distPopulation ? java.lang.Double.NaN : distPopulation.mean());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
