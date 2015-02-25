
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * BasisSplineDeterministicVolatility extends the BasisSplineTermStructure for the specific case of the
 * 	Implementation of the Deterministic Volatility Term Structure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineDeterministicVolatility extends org.drip.analytics.definition.VolatilityTermStructure
{
	private org.drip.spline.grid.Span _spanImpliedVolatility = null;
	private org.drip.param.valuation.CollateralizationParams _collatParams = null;

	/**
	 * BasisSplineDeterministicVolatility Constructor
	 * 
	 * @param dblEpochDate The Epoch Date
	 * @param label Latent State Label
	 * @param strCurrency The Currency
	 * @param spanImpliedVolatility The Implied Volatility Span
	 * @param collatParams Collateral Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BasisSplineDeterministicVolatility (
		final double dblEpochDate,
		final org.drip.state.identifier.CustomMetricLabel label,
		final java.lang.String strCurrency,
		final org.drip.spline.grid.Span spanImpliedVolatility,
		final org.drip.param.valuation.CollateralizationParams collatParams)
		throws java.lang.Exception
	{
		super (dblEpochDate, label, strCurrency);

		if (null == (_spanImpliedVolatility = spanImpliedVolatility))
			throw new java.lang.Exception ("BasisSplineDeterministicVolatility ctr: Invalid Inputs");

		_collatParams = collatParams;
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return _collatParams;
	}

	@Override public double impliedVol (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("BasisSplineDeterministicVolatility::node => Invalid Inputs");

		double dblSpanLeft = _spanImpliedVolatility.left();

		if (dblSpanLeft >= dblDate) return _spanImpliedVolatility.calcResponseValue (dblSpanLeft);

		double dblSpanRight = _spanImpliedVolatility.right();

		if (dblSpanRight <= dblDate) return _spanImpliedVolatility.calcResponseValue (dblSpanRight);

		return _spanImpliedVolatility.calcResponseValue (dblDate);
	}

	@Override public double node (
		final double dblDate)
		throws java.lang.Exception
	{
		double dblImpliedVol = impliedVol (dblDate);

		return java.lang.Math.sqrt (dblImpliedVol * dblImpliedVol + 2. * dblImpliedVol * (dblDate -
			epoch().julian()) / 365.25 * _spanImpliedVolatility.calcResponseValueDerivative (dblDate, 1));
	}

	@Override public double vol (
		final double dblDate)
		throws java.lang.Exception
	{
		return node (dblDate);
	}

	@Override public double nodeDerivative (
		final double dblDate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception
				("BasisSplineDeterministicVolatility::nodeDerivative => Invalid Inputs");

		org.drip.function.deterministic.AbstractUnivariate au = new org.drip.function.deterministic.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				double dblX)
				throws java.lang.Exception
			{
				return node (dblX);
			}
		};

		return au.calcDerivative (dblDate, iOrder);
	}
}
