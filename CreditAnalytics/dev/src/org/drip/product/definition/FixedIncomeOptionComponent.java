
package org.drip.product.definition;

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
 * FixedIncomeOptionComponent extends ComponentMarketParamRef and provides the following methods:
 *  - Get the component's initial notional, notional, and coupon.
 *  - Get the Effective date, Maturity date, First Coupon Date.
 *  - Set the market curves - discount, TSY, forward, and Credit curves.
 *  - Retrieve the component's settlement parameters.
 *  - Value the component using standard/custom market parameters.
 *  - Retrieve the component's named measures and named measure values.
 *  - Retrieve the Underlying Fixed Income Product, Day Count, Strike, Calendar, and Manifest Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class FixedIncomeOptionComponent implements
	org.drip.product.definition.MarketParamRef {
	private java.lang.String _strCalendar = "";
	private java.lang.String _strDayCount = "";
	private double _dblStrike = java.lang.Double.NaN;
	private java.lang.String _strManifestMeasure = "";
	private double _dblNotional = java.lang.Double.NaN;
	private org.drip.product.definition.FixedIncomeComponent _comp = null;

	protected double getMeasure (
		final java.lang.String strMeasure,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalc)
		throws java.lang.Exception
	{
		if (null == strMeasure || strMeasure.isEmpty() || null == mapCalc || null == mapCalc.entrySet())
			throw new java.lang.Exception ("FixedIncomeComponent::getMeasure => Invalid Inputs");

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCalc.entrySet()) {
			if (null != me.getKey() && me.getKey().equalsIgnoreCase (strMeasure)) return me.getValue();
		}

		throw new java.lang.Exception ("FixedIncomeOptionComponent::getMeasure => Invalid Measure: " +
			strMeasure);
	}

	/**
	 * FixedIncomeOptionComponent constructor
	 * 
	 * @param comp The Underlying Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param dblNotional Option Notional
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FixedIncomeOptionComponent (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strManifestMeasure,
		final double dblStrike,
		final double dblNotional,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == (_comp = comp) || null == (_strManifestMeasure = strManifestMeasure) ||
			_strManifestMeasure.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
				dblStrike) || !org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || null
					== (_strDayCount = strDayCount) || _strDayCount.isEmpty() || null == (_strCalendar =
						strCalendar) || _strCalendar.isEmpty())
			throw new java.lang.Exception ("FixedIncomeOptionComponent ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Component
	 * 
	 * @return The Underlying Component
	 */

	public org.drip.product.definition.FixedIncomeComponent underlying()
	{
		return _comp;
	}

	/**
	 * Retrieve the Manifest Measure on which the Option's Strike is quoted
	 * 
	 * @return The Manifest Measure on which the Option's Strike is quoted
	 */

	public java.lang.String manifestMeasure()
	{
		return _strManifestMeasure;
	}

	/**
	 * Retrieve the Strike
	 * 
	 * @return The Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}

	/**
	 * Retrieve the Notional
	 * 
	 * @return The Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the Option Exercise Date
	 * 
	 * @return The Option Exercise Date
	 */

	public org.drip.analytics.date.JulianDate exercise()
	{
		return _comp.effective();
	}

	/**
	 * Retrieve the Day Count
	 * 
	 * @return The Day Count
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Holiday Calendar
	 * 
	 * @return The Holiday Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}

	@Override public java.lang.String name()
	{
		return _comp.name();
	}

	@Override public java.lang.String[] forwardCurveName()
	{
		return _comp.forwardCurveName();
	}

	@Override public java.lang.String[] creditCurveName()
	{
		return _comp.creditCurveName();
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return _comp.currencyPairCode();
	}

	/**
	 * Generate a full list of the component measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams);

	/**
	 * Retrieve the ordered set of the measure names whose values will be calculated
	 * 
	 * @return Set of Measure Names
	 */

	public abstract java.util.Set<java.lang.String> getMeasureNames();

	/**
	 * Calculate the value of the given component measure
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * @param strMeasure Measure String
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Double measure value
	 * 
	 * @throws java.lang.Exception Thrown if the measure cannot be calculated
	 */

	public double calcMeasureValue (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final java.lang.String strMeasure)
		throws java.lang.Exception
	{
		return getMeasure (strMeasure, value (valParams, pricerParams, mktParams, quotingParams));
	}
}
