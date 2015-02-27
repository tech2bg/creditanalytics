
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
 * Binary implements the Standard {0, 1}-valued Binary Random Number Generator.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Binary extends org.drip.sequence.random.Bounded {
	private double _dblPositiveProbability = java.lang.Double.NaN;

	private java.util.Random _rng = new java.util.Random();

	/**
	 * Binary Distribution Constructor
	 * 
	 * @param dblPositiveProbability Probability of Generating ONE
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public Binary (
		final double dblPositiveProbability)
		throws java.lang.Exception
	{
		super (0.,1.);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblPositiveProbability = dblPositiveProbability) || 0.
			> _dblPositiveProbability || 1. < _dblPositiveProbability)
			throw new java.lang.Exception ("BoundedUniform ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Positive Instance Probability
	 * 
	 * @return The Positive Instance Probability
	 */

	public double positiveProbability()
	{
		return _dblPositiveProbability;
	}

	@Override public double random()
	{
		return _rng.nextDouble() < _dblPositiveProbability ? 0. : 1.;
	}
}
