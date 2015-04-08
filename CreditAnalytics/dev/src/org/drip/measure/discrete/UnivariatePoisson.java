
package org.drip.measure.discrete;

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
 * UnivariatePoisson implements the Poisson Distribution using the specified Mean/Variance.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnivariatePoisson extends org.drip.measure.continuous.UnivariateDistribution {
	private double _dblLambda = java.lang.Double.NaN;
	private double _dblExponentialLambda = java.lang.Double.NaN;

	/**
	 * Construct a UnivariatePoisson Distribution
	 * 
	 * @param dblLambda Lambda
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public UnivariatePoisson (
		final double dblLambda)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLambda = dblLambda) || 0. >= _dblLambda)
			throw new java.lang.Exception ("UnivariatePoisson constructor: Invalid inputs");

		_dblExponentialLambda = java.lang.Math.exp (-1. * _dblLambda);
	}

	/**
	 * Retrieve Lambda
	 * 
	 * @return Lambda
	 */

	public double lambda()
	{
		return _dblLambda;
	}

	@Override public double cumulative (
		final double dblX)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
			throw new java.lang.Exception ("UnivariatePoisson::cumulative => Invalid inputs");

		int iEnd = (int) dblX;
		double dblYLocal = 1.;
		double dblYCumulative = 0.;

		for (int i = 1; i < iEnd; ++i) {
			i = i + 1;
			dblYLocal *= _dblLambda / i;
			dblYCumulative += _dblExponentialLambda * dblYLocal;
		}

		return dblYCumulative;
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
		if (!org.drip.quant.common.NumberUtil.IsValid (dblY))
			throw new java.lang.Exception ("UnivariatePoisson::invCumulative => Invalid inputs");

		int i = 0;
		double dblYLocal = 1.;
		double dblYCumulative = 0.;

		while (dblYCumulative < dblY) {
			i = i + 1;
			dblYLocal *= _dblLambda / i;
			dblYCumulative += _dblExponentialLambda * dblYLocal;
		}

		return i - 1;
	}

	@Override public double density (
		final double dblX)
		throws java.lang.Exception
	{
		throw new java.lang.Exception
			("UnivariatePoisson::density => Not available for discrete distributions");
	}

	@Override public double mean()
	{
	    return _dblLambda;
	}

	@Override public double variance()
	{
	    return _dblLambda;
	}

	@Override public org.drip.quant.common.Array2D histogram()
	{
		return null;
	}
}
