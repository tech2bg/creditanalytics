
package org.drip.quant.distribution;

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

public class UnivariateBoundedUniform extends org.drip.quant.distribution.Univariate {
	private double _dblLowerBound = java.lang.Double.NaN;
	private double _dblUpperBound = java.lang.Double.NaN;

	/**
	 * Construct a univariate Bounded Uniform distribution
	 * 
	 * @param dblLowerBound The Lower Bound
	 * @param dblUpperBound The Upper Bound
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public UnivariateBoundedUniform (
		final double dblLowerBound,
		final double dblUpperBound)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLowerBound = dblLowerBound) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblUpperBound = dblUpperBound) || dblUpperBound <=
				dblLowerBound)
			throw new java.lang.Exception ("UnivariateBoundedUniform constructor: Invalid inputs");
	}

	@Override public double cumulative (
		final double dblX)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
			throw new java.lang.Exception ("UnivariateBoundedUniform::cumulative => Invalid inputs");

		if (dblX <= _dblLowerBound) return 0.;

		if (dblX >= _dblUpperBound) return 1.;

		return (dblX - _dblLowerBound) / (_dblUpperBound - _dblLowerBound);
	}

	@Override public double incremental (
		final double dblXLeft,
		final double dblXRight)
		throws java.lang.Exception
	{
		return cumulative (dblXRight) - cumulative (dblXLeft);
	}

	@Override public double invCumulative (
		final double dblY)
		throws java.lang.Exception
	{
	    return org.drip.quant.distribution.Gaussian.InverseCDF (dblY) * (_dblUpperBound - _dblLowerBound) +
	    	_dblLowerBound;
	}

	@Override public double mean()
	{
	    return 0.5 * (_dblUpperBound + _dblLowerBound);
	}

	@Override public double variance()
	{
	    return (_dblUpperBound - _dblLowerBound) * (_dblUpperBound - _dblLowerBound) / 12.;
	}
}
