
package org.drip.product.option;

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
 * EuropeanCallPut implements a simple European Call/Put Option, and its Black Scholes Price.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EuropeanCallPut {
	private double _dblStrike = java.lang.Double.NaN;
	private org.drip.analytics.date.JulianDate _dtMaturity = null;

	/**
	 * EuropeanCallPut constructor
	 * 
	 * @param dtMaturity Option Maturity
	 * @param dblStrike Option Strike
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public EuropeanCallPut (
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblStrike)
		throws java.lang.Exception
	{
		if (null == (_dtMaturity = dtMaturity) || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
			dblStrike) || 0. >= _dblStrike)
			throw new java.lang.Exception ("EuropeanCallPut ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Option Maturity
	 * 
	 * @return The Option Maturity
	 */

	public org.drip.analytics.date.JulianDate maturity()
	{
		return _dtMaturity;
	}

	/**
	 * Retrieve the Option Strike
	 * 
	 * @return The Option Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}

	/**
	 * Generate the Measure Set for the Option
	 * 
	 * @param valParams The Valuation Parameters
	 * @param dblUnderlier The Underlier
	 * @param bIsForward TRUE => The Underlier represents the Forward, FALSE => it represents Spot
	 * @param dc Discount Curve
	 * @param dblVolatility The Option Volatility
	 * @param fpg The Fokker Planck-based Option Pricer
	 * 
	 * @return The Map of the Measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final double dblUnderlier,
		final boolean bIsForward,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility,
		final org.drip.pricer.option.FokkerPlanckGenerator fpg)
	{
		if (null == valParams || null == dc || null == auVolatility || null == fpg) return null;

		double dblValueDate = valParams.valueDate();

		double dblMaturity = _dtMaturity.julian();

		if (dblValueDate >= dblMaturity) return null;

		long lStartTime = System.nanoTime();

		double dblRiskFreeRate = java.lang.Double.NaN;
		double dblTTE = (dblMaturity - dblValueDate) / 365.25;
		double dblImpliedCallVolatility = java.lang.Double.NaN;
		double dblTimeAveragedVolatility = java.lang.Double.NaN;

		try {
			dblRiskFreeRate = dc.zero (dblMaturity);

			dblTimeAveragedVolatility = auVolatility.integrate (dblValueDate, dblMaturity) / (dblMaturity -
				dblValueDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (!fpg.compute (_dblStrike, dblTTE, dblRiskFreeRate, dblUnderlier, bIsForward,
			dblTimeAveragedVolatility))
			return null;

		double dblCallPrice = fpg.callPrice();

		try {
			dblImpliedCallVolatility = new
				org.drip.pricer.option.BlackScholesAlgorithm().implyBlackScholesVolatility (_dblStrike,
					dblTTE, dblRiskFreeRate, dblUnderlier, bIsForward, dblCallPrice);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasure = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapMeasure.put ("CalcTime", (System.nanoTime() - lStartTime) * 1.e-09);

		mapMeasure.put ("CallDelta", fpg.callDelta());

		mapMeasure.put ("CallPrice", dblCallPrice);

		mapMeasure.put ("CallProb1", fpg.callProb1());

		mapMeasure.put ("CallProb2", fpg.callProb2());

		mapMeasure.put ("DF", fpg.df());

		mapMeasure.put ("ImpliedCallVolatility", dblImpliedCallVolatility);

		mapMeasure.put ("PutDelta", fpg.putDelta());

		mapMeasure.put ("PutPrice", fpg.putPrice());

		mapMeasure.put ("PutPriceFromParity", fpg.putPriceFromParity());

		mapMeasure.put ("PutProb1", fpg.putProb1());

		mapMeasure.put ("PutProb2", fpg.putProb2());

		mapMeasure.put ("TTE", dblTTE);

		return mapMeasure;
	}

	/**
	 * Imply the Option Volatility given the Call Price
	 * 
	 * @param valParams The Valuation Parameters
	 * @param dblUnderlier The Underlier
	 * @param bIsForward TRUE => The Underlier represents the Forward, FALSE => it represents Spot
	 * @param dc Discount Curve
	 * @param dblCallPrice The Option Call Price
	 * 
	 * @return The Option's Implied Volatility
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public double implyVolatility (
		final org.drip.param.valuation.ValuationParams valParams,
		final double dblUnderlier,
		final boolean bIsForward,
		final org.drip.analytics.rates.DiscountCurve dc,
		final double dblCallPrice)
		throws java.lang.Exception
	{
		if (null == valParams || null == dc)
			throw new java.lang.Exception ("EuropeanCallPut::implyVolatility => Invalid Inputs");

		double dblValueDate = valParams.valueDate();

		double dblMaturity = _dtMaturity.julian();

		if (dblValueDate >= dblMaturity)
			throw new java.lang.Exception ("EuropeanCallPut::implyVolatility => Invalid Inputs");

		double dblTTE = (dblMaturity - dblValueDate) / 365.25;

		return new org.drip.pricer.option.BlackScholesAlgorithm().implyBlackScholesVolatility (_dblStrike,
			dblTTE, dc.zero (dblMaturity), dblUnderlier, bIsForward, dblCallPrice);
	}

	/**
	 * Retrieve the Set of the Measure Names
	 * 
	 * @return The Set of the Measure Names
	 */

	public java.util.Set<java.lang.String> getMeasureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CallDelta");

		setstrMeasureNames.add ("CallPrice");

		setstrMeasureNames.add ("CallProb1");

		setstrMeasureNames.add ("CallProb2");

		setstrMeasureNames.add ("DF");

		setstrMeasureNames.add ("ImpliedCallVolatility");

		setstrMeasureNames.add ("PutDelta");

		setstrMeasureNames.add ("PutPrice");

		setstrMeasureNames.add ("PutPriceFromParity");

		setstrMeasureNames.add ("PutProb1");

		setstrMeasureNames.add ("PutProb2");

		setstrMeasureNames.add ("TTE");

		return setstrMeasureNames;
	}
}
