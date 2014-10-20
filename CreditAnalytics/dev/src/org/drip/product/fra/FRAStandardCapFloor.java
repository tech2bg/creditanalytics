
package org.drip.product.fra;

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
 * FRAStandardCapFloor implements the Caps and Floors on the Standard FRA.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FRAStandardCapFloor extends org.drip.product.definition.FixedIncomeOptionComponent {
	private java.util.List<org.drip.product.fra.FRAStandardCapFloorlet> _lsFRACapFloorlet = new
		java.util.ArrayList<org.drip.product.fra.FRAStandardCapFloorlet>();

	/**
	 * FRAStandardCapFloor constructor
	 * 
	 * @param comp The Underlying Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param bIsCap Is the FRA Option a Cap? TRUE => YES
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param dblNotional Option Notional
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FRAStandardCapFloor (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strManifestMeasure,
		final boolean bIsCap,
		final double dblStrike,
		final double dblNotional,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		super (comp, strManifestMeasure, dblStrike, dblNotional, strDayCount, strCalendar);

		java.lang.String strIR = comp.payCurrency()[0];

		org.drip.state.identifier.ForwardLabel fri = comp.forwardLabel()[0];

		java.lang.String strFRACodePrefix = fri.fullyQualifiedName();

		for (org.drip.analytics.cashflow.CompositePeriod period : comp.cashFlowPeriod()) {
			double dblFRAStartDate = period.startDate();

			org.drip.product.fra.FRAStandardComponent fra = new org.drip.product.fra.FRAStandardComponent
				(dblNotional, strIR, strFRACodePrefix + new org.drip.analytics.date.JulianDate
					(dblFRAStartDate), strCalendar, dblFRAStartDate, fri, dblStrike, strDayCount);

			_lsFRACapFloorlet.add (new org.drip.product.fra.FRAStandardCapFloorlet (fra, strManifestMeasure,
				bIsCap, dblStrike, dblNotional, strDayCount, strCalendar));
		}
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		return _lsFRACapFloorlet.get (0).cashflowCurrencySet();
	}

	@Override public java.lang.String[] payCurrency()
	{
		return _lsFRACapFloorlet.get (0).payCurrency();
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return _lsFRACapFloorlet.get (0).principalCurrency();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		double dblPV = 0.;
		double dblPrice = 0.;
		double dblUpfront = 0.;

		long lStart = System.nanoTime();

		for (org.drip.product.fra.FRAStandardCapFloorlet fracfl : _lsFRACapFloorlet) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRAResult = fracfl.value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapFRAResult) continue;

			if (mapFRAResult.containsKey ("Price")) dblPrice += mapFRAResult.get ("Price");

			if (mapFRAResult.containsKey ("PV")) dblPV += mapFRAResult.get ("PV");

			if (mapFRAResult.containsKey ("Upfront")) dblUpfront += mapFRAResult.get ("Upfront");
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		mapResult.put ("Price", dblPrice);

		mapResult.put ("PV", dblPV);

		mapResult.put ("Upfront", dblUpfront);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}
}
