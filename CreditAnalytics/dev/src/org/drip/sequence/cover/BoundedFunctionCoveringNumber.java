
package org.drip.sequence.cover;

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
 * BoundedFunctionCoveringNumber implements the Lower/Upper Bounds for the Class of Non-decreasing Functions
 *  that are:
 * 	- Absolutely Bounded
 * 	- Have Bounded Variation.
 * 
 * The References are:
 * 
 * 	1) L. Birge (1987): Estimating a Density Under Order Restrictions: Non-asymptotic Minimax Risk, Annals of
 * 		Statistics 15 995-1012.
 * 
 * 	2) P. L. Bartlett, S. R. Kulkarni, and S. E. Posner (1997): Covering Numbers for Real-valued Function
 * 		Classes, IEEE Transactions on Information Theory 43 (5) 1721-1724.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedFunctionCoveringNumber implements org.drip.sequence.cover.CoveringNumber {
	private double _dblBound = java.lang.Double.NaN;
	private double _dblSupport = java.lang.Double.NaN;
	private double _dblVariation = java.lang.Double.NaN;

	/**
	 * BoundedFunctionCoveringNumber Constructor
	 * 
	 * @param dblSupport The Ordinate Support
	 * @param dblVariation The Function Variation
	 * @param dblBound The Function Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BoundedFunctionCoveringNumber (
		final double dblSupport,
		final double dblVariation,
		final double dblBound)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSupport = dblSupport) || 0. >= _dblSupport ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblVariation = dblVariation) || 0. >= _dblVariation)
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber ctr: Invalid Inputs");

		if (org.drip.quant.common.NumberUtil.IsValid (_dblBound = dblBound) && _dblBound <= 0.5 *
			_dblVariation)
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Ordinate Support
	 * 
	 * @return The Ordinate Support
	 */

	public double support()
	{
		return _dblSupport;
	}

	/**
	 * Retrieve the Function Variation
	 * 
	 * @return The Function Variation
	 */

	public double variation()
	{
		return _dblVariation;
	}

	/**
	 * Retrieve the Function Bound
	 * 
	 * @return The Function Bound
	 */

	public double bound()
	{
		return _dblBound;
	}

	@Override public double logLowerBound (
		final double dblCover)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCover) || 0. == dblCover)
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber::logLowerBound => Invalid Inputs");

		double dblVariationCoverScale = dblCover / (_dblSupport * _dblVariation);

		if (1. < 12. * dblVariationCoverScale)
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber::logLowerBound => Invalid Inputs");

		double dblVariationLogLowerBound = 1. / (54. * dblVariationCoverScale);

		return !org.drip.quant.common.NumberUtil.IsValid (_dblBound) ? dblVariationLogLowerBound : 1. +
			dblVariationLogLowerBound * java.lang.Math.log (2.) + java.lang.Math.log (_dblSupport * _dblBound
				/ (6. * dblCover));
	}

	@Override public double logUpperBound (
		final double dblCover)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCover))
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber::logUpperBound => Invalid Inputs");

		double dblVariationCoverScale = dblCover / (_dblSupport * _dblVariation);

		if (1. < 12. * dblVariationCoverScale)
			throw new java.lang.Exception ("BoundedFunctionCoveringNumber::logUpperBound => Invalid Inputs");

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblBound))
			return java.lang.Math.log (2.) * 12. / dblVariationCoverScale;

		return java.lang.Math.log (2.) * 18. / dblVariationCoverScale + 3. * _dblSupport * (2. * _dblBound -
			_dblVariation) / (8. * dblCover);
	}
}
