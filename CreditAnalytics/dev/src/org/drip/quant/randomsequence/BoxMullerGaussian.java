
package org.drip.quant.randomsequence;

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
 * BoxMullerGaussian implements the Univariate Gaussian Random Number Generator.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoxMullerGaussian extends org.drip.quant.randomsequence.SequenceGenerator {
	private double _dblMean = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;
	private double _dblVariance = java.lang.Double.NaN;

	private java.util.Random _rng = new java.util.Random();

	/**
	 * BoxMullerGaussian Constructor
	 * 
	 * @param dblMean The Mean
	 * @param dblVariance The Variance
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BoxMullerGaussian (
		final double dblMean,
		final double dblVariance)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblMean = dblMean) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblVariance = dblVariance) || _dblVariance <= 0.)
			throw new java.lang.Exception ("BoxMullerGaussian ctr: Invalid Inputs");

		_dblSigma = java.lang.Math.sqrt (_dblVariance);
	}

	/**
	 * Retrieve the Mean of the Box-Muller Gaussian
	 * 
	 * @return Mean of the Box-Muller Gaussian
	 */

	public double mean()
	{
		return _dblMean;
	}

	/**
	 * Retrieve the Variance of the Box-Muller Gaussian
	 * 
	 * @return Variance of the Box-Muller Gaussian
	 */

	public double variance()
	{
		return _dblVariance;
	}

	@Override public double random()
	{
		return _dblMean + _dblSigma * java.lang.Math.sqrt (-2. * java.lang.Math.log (_rng.nextDouble())) *
			java.lang.Math.cos (2. * java.lang.Math.PI * _rng.nextDouble());
	}
}
