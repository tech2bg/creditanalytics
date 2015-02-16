
package org.drip.quant.distribution;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * UnivariateNormal implements the univariate normal distribution. It implements incremental, cumulative, and
 *  inverse cumulative distribution densities.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnivariateNormal extends org.drip.quant.distribution.Univariate {
	private double _dblMean = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;

	/**
	 * Generate a N (0, 1) distribution
	 * 
	 * @return The N (0, 1) distribution
	 */

	public static final org.drip.quant.distribution.Univariate GenerateStandardNormal()
	{
		try {
			return new UnivariateNormal (0., 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a univariate Gaussian/normal distribution
	 * 
	 * @param dblMean Mean of the Distribution
	 * @param dblSigma Sigma of the DIstribution
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public UnivariateNormal (
		final double dblMean,
		final double dblSigma)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblMean = dblMean) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblSigma = dblSigma) || 0. >= _dblSigma)
			throw new java.lang.Exception ("UnivariateNormal constructor: Invalid inputs");
	}

	@Override public double cumulative (
		final double dblX)
		throws java.lang.Exception
	{
		return org.drip.quant.distribution.Gaussian.CDF ((dblX - _dblMean) / _dblSigma);
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
	    return org.drip.quant.distribution.Gaussian.InverseCDF (dblY) * _dblSigma + _dblMean;
	}

	@Override public double mean()
	{
	    return _dblMean;
	}

	@Override public double variance()
	{
	    return _dblSigma * _dblSigma;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		UnivariateNormal nd = new UnivariateNormal (10., 10.);

		double dblX = 1.;

		double dblCumulative = nd.cumulative (dblX);

		System.out.println ("Cumulative: " + dblCumulative);

		System.out.println ("Incremental: " + nd.incremental (-5., 5.));

		System.out.println ("Inverse Cumulative: " + nd.invCumulative (dblCumulative));
	}
}
