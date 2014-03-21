
package org.drip.product.option;

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
 * EuropeanCallPut implements a simple European Call/Put Option, and its Black Scholes Price.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EuropeanCallPut {
	private boolean _bIsPut = false;
	private double _dblStrike = java.lang.Double.NaN;
	private org.drip.analytics.date.JulianDate _dtMaturity = null;

	/**
	 * EuropeanCallPut constructor
	 * 
	 * @param dtMaturity Option Maturity
	 * @param dblStrike Option Strike
	 * @param bIsPut TRUE => Option is a PUT
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public EuropeanCallPut (
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblStrike,
		final boolean bIsPut)
		throws java.lang.Exception
	{
		if (null == (_dtMaturity = dtMaturity) || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
			dblStrike) || 0. >= _dblStrike)
			throw new java.lang.Exception ("EuropeanCallPut ctr: Invalid Inputs");

		_bIsPut = bIsPut;
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
	 * Indicate if the Option is a Put
	 * 
	 * @return TRUE - The Option is a Put
	 */

	public boolean isPut()
	{
		return _bIsPut;
	}

	/**
	 * Generate the Measure Set for the Option
	 * 
	 * @param valParams The Valuation Parameters
	 * @param dblSpot The Underlying Spot
	 * @param dblRiskFreeRate The Risk Free Rate
	 * @param dblVolatility The Option Volatility
	 * @param fpg The Fokker Planck-based Option Pricer
	 * 
	 * @return The Map of the Measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final double dblSpot,
		final double dblRiskFreeRate,
		final double dblVolatility,
		final org.drip.pricer.option.FokkerPlanckGenerator fpg)
	{
		if (null == valParams || null == fpg) return null;

		double dblValueDate = valParams.valueDate();

		double dblMaturity = _dtMaturity.getJulian();

		if (dblValueDate >= dblMaturity) return null;

		long lStartTime = System.nanoTime();

		double dblTTE = (dblMaturity - dblValueDate) / 365.25;

		/* org.drip.pricer.option.FokkerPlanckGenerator fpg = org.drip.pricer.option.BlackScholesAlgorithm.Price
			(_dblStrike, dblTTE, dblRiskFreeRate, dblSpot, dblVolatility);

		if (null == fpg) return null; */

		if (!fpg.compute (_dblStrike, dblTTE, dblRiskFreeRate, dblSpot, dblVolatility)) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasure = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapMeasure.put ("CalcTime", (System.nanoTime() - lStartTime) * 1.e-09);

		mapMeasure.put ("Delta", fpg.delta());

		mapMeasure.put ("DF", fpg.df());

		mapMeasure.put ("Prob1", fpg.prob1());

		mapMeasure.put ("Prob2", fpg.prob2());

		mapMeasure.put ("Price", fpg.price());

		mapMeasure.put ("TTE", dblTTE);

		return mapMeasure;
	}

	public java.util.Set<java.lang.String> getMeasureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("ATMSwapRate");

		setstrMeasureNames.add ("Delta");

		setstrMeasureNames.add ("DF");

		setstrMeasureNames.add ("D1");

		setstrMeasureNames.add ("D2");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("ProbabilityITM");

		setstrMeasureNames.add ("TTE");

		return setstrMeasureNames;
	}
}
