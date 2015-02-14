
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
 * BoundedUniform implements the Bounded Uniform Distribution, with a Uniform Distribution between a lower
 *  and an upper Bound.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedUniform {
	private double _dblLowerBound = java.lang.Double.NaN;
	private double _dblUpperBound = java.lang.Double.NaN;

	private java.util.Random _rng = new java.util.Random();

	/**
	 * BoundedUniform Distribution Constructor
	 * 
	 * @param dblLowerBound The Lower Bound
	 * @param dblUpperBound The Upper Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BoundedUniform (
		final double dblLowerBound,
		final double dblUpperBound)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLowerBound = dblLowerBound) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblUpperBound = dblUpperBound) || dblUpperBound <=
				dblLowerBound)
			throw new java.lang.Exception ("BoundedUniform ctr: Invalid Inputs!");
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

	/**
	 * Generate a Random Number within the Bounds
	 * 
	 * @return Bounded Uniform Random Number
	 */

	public double random()
	{
		return _dblLowerBound + _rng.nextDouble() * (_dblUpperBound - _dblLowerBound);
	}

	/**
	 * Generate a Random Sequence within the Bounds
	 * 
	 * @param iNumEntry Number of Entries in the Sequence
	 * 
	 * @return Bounded Uniform Random Sequence (along with its Metrics)
	 */

	public org.drip.quant.discrete.SequenceMetrics sequence (
		final int iNumEntry)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.quant.discrete.SequenceMetrics (adblSequence);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
