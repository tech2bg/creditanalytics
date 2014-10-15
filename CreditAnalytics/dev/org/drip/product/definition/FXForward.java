
package org.drip.product.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * FXForward is the abstract class exposes the functionality behind the FXForward Contract. Derived
 * 	implementations expose the primary/secondary codes, the effective/maturity dates, the currency pair,
 * 	imply the discount curve basis and the FX Forward from a set of market parameters. The value function
 * 	carries out a full valuation.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class FXForward {

	/**
	 * Get the primary code
	 * 
	 * @return Primary Code string
	 */

	public abstract java.lang.String getPrimaryCode();

	/**
	 * Set the primary code
	 * 
	 * @param strCode Primary Code String
	 */

	public abstract void setPrimaryCode (
		final java.lang.String strCode);

	/**
	 * Get the array of secondary code
	 * 
	 * @return Array of secondary code string
	 */

	public abstract java.lang.String[] getSecondaryCode();

	/**
	 * Get the Effective Date
	 * 
	 * @return Effective Date
	 */

	public abstract org.drip.analytics.date.JulianDate getEffectiveDate();

	/**
	 * Get the Maturity Date
	 * 
	 * @return Maturity Date
	 */

	public abstract org.drip.analytics.date.JulianDate getMaturityDate();

	/**
	 * Get the Currency Pair
	 * 
	 * @return CurrencyPair
	 */

	public abstract org.drip.product.params.CurrencyPair getCcyPair();

	/**
	 * Imply the FX Forward
	 * 
	 * @param valParams Valuation Parameters
	 * @param dcNum Discount Curve for the numerator
	 * @param dcDenom Discount Curve for the denominator
	 * @param dblFXSpot FXSpot
	 * @param bFwdAsPIP Calculate FXFwd as a PIP
	 * 
	 * @return FXForward
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public abstract double implyFXForward (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot,
		final boolean bFwdAsPIP)
		throws java.lang.Exception;

	/**
	 * Calculate the basis to either the numerator or the denominator discount curve
	 * 
	 * @param valParams ValuationParams
	 * @param dcNum Discount Curve for the numerator
	 * @param dcDenom Discount Curve for the denominator
	 * @param dblFXSpot FXSpot
	 * @param dblMarketFXFwdPrice FXForward Market Value
	 * @param bBasisOnDenom Boolean indicating whether the basis is applied on the denominator (true) or
	 * 			denominator
	 * 
	 * @return Basis
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public abstract double calcDCBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot,
		final double dblMarketFXFwdPrice,
		final boolean bBasisOnDenom)
		throws java.lang.Exception;

	/**
	 * Calculation of the full set of measures of FXForward
	 * 
	 * @param valParams ValuationParams
	 * @param dcNum Discount Curve for the numerator
	 * @param dcDenom Discount Curve for the denominator
	 * @param dblFXSpot FXSpot
	 * 
	 * @return Map containing measure names and values
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot);
}
