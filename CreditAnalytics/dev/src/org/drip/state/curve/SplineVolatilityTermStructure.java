
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * SplineVolatilityTermStructure extends the BasisSplineTermStructure for the specific case of the
 * 	Implementation of the Volatility Term Structure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SplineVolatilityTermStructure extends org.drip.state.curve.BasisSplineTermStructure {

	/**
	 * SplineVolatilityTermStructure Constructor
	 * 
	 * @param dblEpochDate The Epoch Date
	 * @param strName Name of the Surface
	 * @param strCurrency The Currency
	 * @param span The Latent State Span
	 * @param collatParams Collateral Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public SplineVolatilityTermStructure (
		final double dblEpochDate,
		final java.lang.String strName,
		final java.lang.String strCurrency,
		final org.drip.spline.grid.Span span,
		final org.drip.param.valuation.CollateralizationParams collatParams)
		throws java.lang.Exception
	{
		super (dblEpochDate, strName, strCurrency, span, collatParams);
	}

	/**
	 * Compute the Deterministic Implied Volatility at the Date Node from the Volatility Term Structure
	 * 
	 * @param dblDate The Date Node
	 * 
	 * @return The Deterministic Implied Volatility at the Date Node from the Volatility Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Deterministic Implied Volatility cannot be computed
	 */

	public double impliedDeterministicVol (
		final double dblDate)
		throws java.lang.Exception
	{
		double dblImpliedVol = node (dblDate);

		return java.lang.Math.sqrt (dblImpliedVol * dblImpliedVol + 2. * dblImpliedVol * (dblDate -
			epoch().getJulian()) * nodeDerivative (dblDate, 1));
	}
}
