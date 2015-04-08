
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
 * BoundedIdempotentUnivariateRandom contains the Implementation of the Objective Function dependent on
 * 	Bounded Idempotent Univariate Random Variable.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedIdempotentUnivariateRandom extends
	org.drip.sequence.functional.IdempotentUnivariateRandom {
	private double _dblVariateBound = java.lang.Double.NaN;

	/**
	 * BoundedIdempotentUnivariateRandom Constructor
	 * 
	 * @param dblOffset The Idempotent Offset
	 * @param dist The Underlying Distribution
	 * @param dblVariateBound The Variate Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BoundedIdempotentUnivariateRandom (
		final double dblOffset,
		final org.drip.measure.continuous.UnivariateDistribution dist,
		final double dblVariateBound)
		throws java.lang.Exception
	{
		super (dblOffset, dist);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblVariateBound = dblVariateBound))
			throw new java.lang.Exception ("BoundedIdempotentUnivariateRandom ctr => Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Variate Bound
	 * 
	 * @return The Underlying Variate Bound
	 */

	public double variateBound()
	{
		return _dblVariateBound;
	}

	/**
	 * Retrieve the Maximal Agnostic Variance Bound Over the Variate Range
	 * 
	 * @return The Maximal Agnostic Bound over the Variate Range
	 */

	public double agnosticVarianceBound()
	{
		return _dblVariateBound * _dblVariateBound;
	}
}
