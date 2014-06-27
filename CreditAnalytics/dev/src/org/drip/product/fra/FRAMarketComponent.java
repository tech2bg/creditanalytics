
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
 * FRAMarketComponent contains the implementation of the Standard Multi-Curve FRA product whose payoff is
 * 	dictated off of Market FRA Conventions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAMarketComponent extends org.drip.product.fra.FRAStandardComponent {

	/**
	 * FRAMarketComponent constructor
	 * 
	 * @param dblNotional Component Notional
	 * @param strIR IR Curve
	 * @param strCode FRA Product Code
	 * @param strCalendar FRA Calendar
	 * @param dblEffectiveDate FRA Effective Date
	 * @param fri FRA Floating Rate Index
	 * @param dblStrike FRA Strike
	 * @param strDayCount Day Count Convention
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public FRAMarketComponent (
		final double dblNotional,
		final java.lang.String strIR,
		final java.lang.String strCode,
		final java.lang.String strCalendar,
		final double dblEffectiveDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final double dblStrike,
		java.lang.String strDayCount)
		throws java.lang.Exception
	{
		super (dblNotional, strIR, strCode, strCalendar, dblEffectiveDate, fri, dblStrike, strDayCount);
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams) return null;

		long lStart = System.nanoTime();

		double dblParStandardFRA = java.lang.Double.NaN;

		double dblValueDate = valParams.valueDate();

		double dblEffectiveDate = effective().getJulian();

		if (dblValueDate > dblEffectiveDate) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = mktParams.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		org.drip.analytics.date.JulianDate dtMaturity = maturity();

		double dblMaturity = dtMaturity.getJulian();

		org.drip.product.params.FloatingRateIndex fri = fri();

		org.drip.analytics.rates.ForwardRateEstimator fc = mktParams.forwardCurve (fri);

		if (null == fc || !fri.match (fc.index())) return null;

		java.lang.String strFRI = fri.fullyQualifiedName();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = super.value
			(valParams, pricerParams, mktParams, quotingParams);

		if (null == mapResult || 0 == mapResult.size()) return null;

		try {
			java.util.Map<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
					mktParams.fixings();

			if (null != mapFixings && mapFixings.containsKey (dtMaturity)) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixing =
					mapFixings.get (dtMaturity);

				dblParStandardFRA = null != mapFixing && mapFixing.containsKey (strFRI) ? mapFixing.get
					(strFRI) : fc.forward (dblMaturity);
			} else
				dblParStandardFRA = fc.forward (dblMaturity);

			double dblForwardDCF = org.drip.analytics.daycount.Convention.YearFraction (dblMaturity, new
				org.drip.analytics.date.JulianDate (dblMaturity).addTenor (fri.tenor()).getJulian(),
					dayCount(), false, dblMaturity, null, calendar());

			double dblParDCForward = dcFunding.libor (dblEffectiveDate, dblMaturity);

			double dblShiftedLogNormalScaler = dblForwardDCF * dblParStandardFRA;
			dblShiftedLogNormalScaler = dblShiftedLogNormalScaler / (1. + dblShiftedLogNormalScaler);

			double dblForwardPrice = dblForwardDCF * (dblParStandardFRA - strike()) / (1. + dblForwardDCF *
				dblParStandardFRA);

			/* double dblShiftedLogNormalConvexityAdjustmentExponent =
				org.drip.analytics.support.OptionHelper.IntegratedFRACrossVolConvexityAdjuster (mktParams,
					dcFunding.name() + "_VOL_TS", fri.fullyQualifiedName() + "_VOL_TS", dcFunding.name() +
						"::" + fri.fullyQualifiedName() + "_VOL_TS", dblShiftedLogNormalScaler,
							dblShiftedLogNormalScaler, dblValueDate, dblEffectiveDate); */

			double dblShiftedLogNormalConvexityAdjustmentExponent =
				org.drip.analytics.support.OptionHelper.IntegratedFRACrossVolConvexityAdjuster (mktParams,
					dcFunding.name(), fri.fullyQualifiedName(), "ab", dblShiftedLogNormalScaler,
						dblShiftedLogNormalScaler, dblValueDate, dblEffectiveDate);

			double dblShiftedLogNormalParMarketFRA = ((dblForwardDCF * dblParStandardFRA + 1.) *
				java.lang.Math.exp (dblShiftedLogNormalConvexityAdjustmentExponent) - 1.) / dblForwardDCF;

			mapResult.put ("discountcurveparforward", dblParDCForward);

			mapResult.put ("forwardprice", dblForwardPrice);

			mapResult.put ("parstandardfra", dblParStandardFRA);

			mapResult.put ("parstandardfradc", dblParDCForward);

			mapResult.put ("shiftedlognormalconvexityadjustment",
				dblShiftedLogNormalConvexityAdjustmentExponent);

			mapResult.put ("shiftedlognormalconvexitycorrection", dblShiftedLogNormalParMarketFRA -
				dblParStandardFRA);

			mapResult.put ("shiftedlognormalparmarketfra", dblShiftedLogNormalParMarketFRA);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}
}
