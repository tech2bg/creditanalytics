
package org.drip.feed.loader;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * RatesClosesLoader Loads the closing marks for a given Rates Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesClosesLoader {
	private static java.util.Map<java.lang.String, java.lang.String> _mapFixedDC = new
		java.util.HashMap<java.lang.String, java.lang.String>();

	private static java.util.Map<java.lang.String, java.lang.String> _mapFixedTenor = new
		java.util.HashMap<java.lang.String, java.lang.String>();

	private static java.util.Map<java.lang.String, java.lang.String> _mapFloatingDC = new
		java.util.HashMap<java.lang.String, java.lang.String>();

	private static java.util.Map<java.lang.String, java.lang.String> _mapFloatingTenor = new
		java.util.HashMap<java.lang.String, java.lang.String>();

	private static java.util.Map<java.lang.String, java.lang.Integer> _mapFixedFrequency = new
		java.util.HashMap<java.lang.String, java.lang.Integer>();

	private static java.util.Map<java.lang.String, java.lang.Integer> _mapFloatingFrequency = new
		java.util.HashMap<java.lang.String, java.lang.Integer>();

	private static java.io.BufferedWriter _writeCOB = null;
	private static final java.lang.String[] s_astrFwdTenor = new java.lang.String[] {"1Y", "2Y", "3Y", "4Y",
		"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y"};

	public static final boolean InitStatic()
	{
		_mapFixedDC.put ("CLP", "Act/360");

		_mapFixedTenor.put ("CLP", "6M");

		_mapFloatingDC.put ("CLP", "Act/360");

		_mapFloatingTenor.put ("CLP", "6M");

		_mapFixedFrequency.put ("CLP", 2);

		_mapFloatingFrequency.put ("CLP", 2);

		_mapFixedDC.put ("CZK", "Act/360");

		_mapFixedTenor.put ("CZK", "12M");

		_mapFloatingDC.put ("CZK", "Act/360");

		_mapFloatingTenor.put ("CZK", "6M");

		_mapFixedFrequency.put ("CZK", 1);

		_mapFloatingFrequency.put ("CZK", 2);

		_mapFixedDC.put ("EUR", "30/360");

		_mapFixedTenor.put ("EUR", "12M");

		_mapFloatingDC.put ("EUR", "Act/360");

		_mapFloatingTenor.put ("EUR", "6M");

		_mapFixedFrequency.put ("EUR", 1);

		_mapFloatingFrequency.put ("EUR", 2);

		_mapFixedDC.put ("GBP", "Act/365");

		_mapFixedTenor.put ("GBP", "6M");

		_mapFloatingDC.put ("GBP", "Act/365");

		_mapFloatingTenor.put ("GBP", "6M");

		_mapFixedFrequency.put ("GBP", 2);

		_mapFloatingFrequency.put ("GBP", 2);

		_mapFixedDC.put ("HKD", "Act/365");

		_mapFixedTenor.put ("HKD", "3M");

		_mapFloatingDC.put ("HKD", "Act/365");

		_mapFloatingTenor.put ("HKD", "3M");

		_mapFixedFrequency.put ("HKD", 4);

		_mapFloatingFrequency.put ("HKD", 4);

		_mapFixedDC.put ("HUF", "Act/365");

		_mapFixedTenor.put ("HUF", "12M");

		_mapFloatingDC.put ("HUF", "Act/360");

		_mapFloatingTenor.put ("HUF", "6M");

		_mapFixedFrequency.put ("HUF", 1);

		_mapFloatingFrequency.put ("HUF", 2);

		_mapFixedDC.put ("JPY", "Act/365");

		_mapFixedTenor.put ("JPY", "6M");

		_mapFloatingDC.put ("JPY", "Act/360");

		_mapFloatingTenor.put ("JPY", "6M");

		_mapFixedFrequency.put ("JPY", 2);

		_mapFloatingFrequency.put ("JPY", 2);

		_mapFixedDC.put ("MXN", "Act/360");

		_mapFixedTenor.put ("MXN", "1M");

		_mapFloatingDC.put ("MXN", "Act/360");

		_mapFloatingTenor.put ("MXN", "1M");

		_mapFixedFrequency.put ("MXN", 12);

		_mapFloatingFrequency.put ("MXN", 12);

		_mapFixedDC.put ("PLN", "Act/Act");

		_mapFixedTenor.put ("PLN", "12M");

		_mapFloatingDC.put ("PLN", "Act/365");

		_mapFloatingTenor.put ("PLN", "6M");

		_mapFixedFrequency.put ("PLN", 1);

		_mapFloatingFrequency.put ("PLN", 2);

		_mapFixedDC.put ("SGD", "Act/365");

		_mapFixedTenor.put ("SGD", "6M");

		_mapFloatingDC.put ("SGD", "Act/365");

		_mapFloatingTenor.put ("SGD", "6M");

		_mapFixedFrequency.put ("SGD", 2);

		_mapFloatingFrequency.put ("SGD", 2);

		_mapFixedDC.put ("USD", "30/360");

		_mapFixedTenor.put ("USD", "6M");

		_mapFloatingDC.put ("USD", "Act/360");

		_mapFloatingTenor.put ("USD", "3M");

		_mapFixedFrequency.put ("USD", 2);

		_mapFloatingFrequency.put ("USD", 4);

		_mapFixedDC.put ("ZAR", "Act/365");

		_mapFixedTenor.put ("ZAR", "3M");

		_mapFloatingDC.put ("ZAR", "Act/365");

		_mapFloatingTenor.put ("ZAR", "3M");

		_mapFixedFrequency.put ("ZAR", 4);

		_mapFloatingFrequency.put ("ZAR", 4);

		return true;
	}

	private static final org.drip.product.definition.RatesComponent CreateIRS (
		final org.drip.analytics.date.JulianDate dtEffectiveUnadjusted,
		final java.lang.String strMaturityTenor,
		final int iNumDaysSubtract,
		final double dblCoupon,
		final java.lang.String strCurrency)
	{
		if (null == dtEffectiveUnadjusted || null == strMaturityTenor || strMaturityTenor.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblCoupon))
			return null;

		org.drip.analytics.date.JulianDate dtEffective = 0 == iNumDaysSubtract ? dtEffectiveUnadjusted :
			dtEffectiveUnadjusted.subtractDays (iNumDaysSubtract);

		org.drip.analytics.date.JulianDate dtMaturity = dtEffective.addTenorAndAdjust (strMaturityTenor,
			strCurrency);

		if (null == dtMaturity) return null;

		java.lang.String strFixedDC = _mapFixedDC.get (strCurrency);

		java.lang.String strFloatingDC = _mapFloatingDC.get (strCurrency);

		boolean bApplyEOMAdjustmentFixed = "30/360".equalsIgnoreCase (strFixedDC);

		boolean bApplyEOMAdjustmentFloating = "30/360".equalsIgnoreCase (strFloatingDC);

		try {
			org.drip.analytics.daycount.DateAdjustParams dap = new
				org.drip.analytics.daycount.DateAdjustParams (org.drip.analytics.daycount.Convention.DR_FOLL,
					strCurrency);

			org.drip.product.rates.FixedStream fixStream = new org.drip.product.rates.FixedStream
				(dtEffective.getJulian(), strMaturityTenor, dblCoupon, _mapFixedFrequency.get (strCurrency),
					strFixedDC, bApplyEOMAdjustmentFixed, strFixedDC, bApplyEOMAdjustmentFixed, false, dap,
						null, 1., strCurrency, strCurrency);

			org.drip.product.rates.FloatingStream floatStream = org.drip.product.rates.FloatingStream.Create
				(dtEffective.getJulian(), strMaturityTenor, 0., false,
					org.drip.product.params.FloatingRateIndex.Create (strCurrency, "LIBOR",
						_mapFloatingTenor.get (strCurrency)), _mapFloatingFrequency.get (strCurrency),
							strFloatingDC, bApplyEOMAdjustmentFloating, strFloatingDC,
								bApplyEOMAdjustmentFloating, dap, null, -1., strCurrency, strCurrency);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + "." + strCurrency);

			return irs;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final double calcMeasure (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strMeasure,
		final java.lang.String strCurrency,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
		throws java.lang.Exception
	{
		if (comp.getMaturityDate().getJulian() <= dt.getJulian()) return 0.;

		return comp.value (new org.drip.param.valuation.ValuationParams (dt, dt, strCurrency), null,
			org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null,
				null, null, null, mmFixings), null).get (strMeasure);
	}

	private static final double calcCleanPnL (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc1,
		final org.drip.analytics.rates.DiscountCurve dc2,
		final java.lang.String strCurrency,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt2, dc2, "CleanPV", strCurrency, mmFixings) - calcMeasure (comp, dt1, dc1,
			"CleanPV", strCurrency, mmFixings);
	}

	private static final double calcDirtyPnL (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc1,
		final org.drip.analytics.rates.DiscountCurve dc2,
		final java.lang.String strCurrency,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt2, dc2, "DirtyPV", strCurrency, mmFixings) - calcMeasure (comp, dt1, dc1,
			"DirtyPV", strCurrency, mmFixings);
	}

	private static final double Forward (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (dt1.getJulian() >= dt2.getJulian()) return -0.;

		return dc.forward (dt1.getJulian(), dt2.getJulian());
	}

	private static final java.lang.String ComputePnLMetrics (
		final org.drip.analytics.date.JulianDate dtPrev,
		final org.drip.analytics.date.JulianDate dtCurr,
		final org.drip.analytics.date.JulianDate dt1MPast,
		final org.drip.analytics.date.JulianDate dt3MPast,
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.product.definition.FixedIncomeComponent compPrevMat,
		final org.drip.analytics.rates.DiscountCurve dcDatePastQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDatePrevQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDateCurrQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDatePrevQuoteCurr,
		final org.drip.analytics.rates.DiscountCurve dcDateCurrQuoteCurr,
		final org.drip.analytics.rates.DiscountCurve dcDate1MPastQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDate3MPastQuotePrev,
		final double dblBaselineSwapRate,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		org.drip.product.rates.IRSComponent irs = (org.drip.product.rates.IRSComponent) comp;

		double dblFixedCoupon = irs.getFixedStream().getCoupon (dtPrev.getJulian(), null);

		boolean bApplyFixedCouponEOMAdj = "30/360".equalsIgnoreCase (_mapFixedDC.get (strCurrency));

		boolean bApplyFloatingCouponEOMAdj = "30/360".equalsIgnoreCase (_mapFloatingDC.get (strCurrency));

		int i1DFixedAccrualDays = org.drip.analytics.daycount.Convention.DaysAccrued (dtPrev.getJulian(),
			dtCurr.getJulian(), _mapFixedDC.get (strCurrency), bApplyFixedCouponEOMAdj, java.lang.Double.NaN,
				null, strCurrency);

		double dbl1DFixedDCF = (calcMeasure (irs.getFixedStream(), dtCurr, dcDatePrevQuotePrev, "Accrued01",
			strCurrency, null) - calcMeasure (irs.getFixedStream(), dtPrev, dcDatePrevQuotePrev, "Accrued01",
				strCurrency, null)) * 10000.;

		double dblProductFloatingRate = irs.getFloatStream().getCoupon (dtPrev.getJulian(),
			org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
				(dcDatePrevQuotePrev, null, null, null, null, null, null));

		double dblCurveFloatingRate = dcDatePrevQuotePrev.libor (dtPrev.getJulian(),
			org.drip.product.params.FloatingRateIndex.Create (irs.getForwardCurveName()[0]).tenor());

		int i1DFloatingAccrualDays = org.drip.analytics.daycount.Convention.DaysAccrued (dtPrev.getJulian(),
			dtCurr.getJulian(), _mapFloatingDC.get (strCurrency), bApplyFloatingCouponEOMAdj,
				java.lang.Double.NaN, null, strCurrency);

		double dbl1DFloatingDCF = (calcMeasure (irs.getFloatStream(), dtPrev, dcDatePrevQuotePrev,
			"Accrued01", strCurrency, null) - calcMeasure (irs.getFloatStream(), dtCurr, dcDatePrevQuotePrev,
				"Accrued01", strCurrency, null)) * 10000.;

		double dblCleanFixedDV01 = calcMeasure (comp, dtPrev, dcDatePrevQuotePrev, "CleanFixedDV01",
			strCurrency, null);

		double dblCleanFloatDV01 = calcMeasure (comp, dtPrev, dcDatePrevQuotePrev, "Fixing01", strCurrency,
			null);

		double dblDV01 = dblCleanFixedDV01 + dblCleanFloatDV01;
		double dbl1DCarry = dblFixedCoupon * dbl1DFixedDCF - dblProductFloatingRate * dbl1DFloatingDCF;

		double dbl1DCleanPnL = calcCleanPnL (comp, dtPrev, dtCurr, dcDatePrevQuotePrev, dcDateCurrQuoteCurr,
			strCurrency, null);

		double dbl1DDirtyPnL = calcDirtyPnL (comp, dtPrev, dtCurr, dcDatePrevQuotePrev, dcDateCurrQuoteCurr,
			strCurrency, null);

		double dbl1DTotalPnL = dbl1DCleanPnL + dbl1DCarry;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixing = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapFixing.put (irs.getFloatStream().getForwardCurveName()[0], dblProductFloatingRate);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.TreeMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (dtPrev, mapFixing);

		double dbl1DCleanPnLWithFixing = calcCleanPnL (comp, dtPrev, dtCurr, dcDatePrevQuotePrev,
			dcDateCurrQuoteCurr, strCurrency, mmFixings);

		double dbl1DDirtyPnLWithFixing = calcDirtyPnL (comp, dtPrev, dtCurr, dcDatePrevQuotePrev,
			dcDateCurrQuoteCurr, strCurrency, mmFixings);

		double dbl1DTotalPnLWithFixing = dbl1DCleanPnLWithFixing + dbl1DCarry;

		double dblFloatingRateUsed = irs.getFloatStream().getCoupon (dtPrev.getJulian(),
			org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
				(dcDatePrevQuotePrev, null, null, null, null, null, mmFixings));

		double dblCleanFloatDV01WithFixing = calcMeasure (comp, dtPrev, dcDatePrevQuotePrev, "Fixing01",
			strCurrency, mmFixings);

		double dblDV01WithFixing = dblCleanFixedDV01 + dblCleanFloatDV01WithFixing;

		double dbl1DTimeRollSwapRate = calcMeasure (comp, dtPrev, dcDatePastQuotePrev, "CalibSwapRate",
			strCurrency, null);

		double dbl1DTimeRollPnL = (dblBaselineSwapRate - dbl1DTimeRollSwapRate) * 10000. * dblDV01;

		double dbl1DMaturityRollDownFairPremium = calcMeasure (compPrevMat, dtPrev, dcDatePrevQuotePrev,
			"FairPremium", strCurrency, null);

		double dbl1DMaturityRollDownFairPremiumPnL = (dblBaselineSwapRate - dbl1DMaturityRollDownFairPremium)
			* 10000. * dblDV01;

		double dbl1DMaturityRollUpSwapRate = calcMeasure (comp, dtCurr, dcDatePrevQuotePrev, "CalibSwapRate",
			strCurrency, null);

		double dbl1DMaturityRollUpSwapRatePnL = (dblBaselineSwapRate - dbl1DMaturityRollUpSwapRate) * 10000.
			* dblDV01;

		double dbl1DMaturityRollUpFairPremium = calcMeasure (comp, dtCurr, dcDatePrevQuotePrev, "FairPremium",
			strCurrency, null);

		double dbl1DMaturityRollUpFairPremiumPnL = (dblBaselineSwapRate - dbl1DMaturityRollUpFairPremium) *
			10000. * dblDV01;

		double dbl1DMaturityRollUpFairPremiumWithFixing = calcMeasure (comp, dtCurr, dcDatePrevQuotePrev,
			"FairPremium", strCurrency, mmFixings);

		double dbl1DMaturityRollUpFairPremiumWithFixingPnL = (dblBaselineSwapRate -
			dbl1DMaturityRollUpFairPremiumWithFixing) * 10000. * dblDV01;

		double dbl1DCurveShiftSwapRate = calcMeasure (comp, dtPrev, dcDatePrevQuoteCurr, "CalibSwapRate",
			strCurrency, null);

		double dbl1DCurveShiftPnL = (dblBaselineSwapRate - dbl1DCurveShiftSwapRate) * 10000. * dblDV01;

		double dbl1MFixedDCF = org.drip.analytics.daycount.Convention.YearFraction (dt1MPast.getJulian(),
			dtPrev.getJulian(), _mapFixedDC.get (strCurrency), bApplyFixedCouponEOMAdj,
				java.lang.Double.NaN, null, strCurrency);

		double dbl1MFloatingDCF = org.drip.analytics.daycount.Convention.YearFraction (dt1MPast.getJulian(),
			dtPrev.getJulian(), _mapFloatingDC.get (strCurrency), bApplyFloatingCouponEOMAdj,
				java.lang.Double.NaN, null, strCurrency);

		double dbl1MCarryPnL = dblFixedCoupon * dbl1MFixedDCF - dblCurveFloatingRate * dbl1MFloatingDCF;

		double dbl1MTimeRollSwapRate = calcMeasure (comp, dtPrev, dcDate1MPastQuotePrev, "CalibSwapRate",
			strCurrency, null);

		double dbl1MTimeRollPnL = (dblBaselineSwapRate - dbl1MTimeRollSwapRate) * 10000. * dblDV01;

		double dbl3MFixedDCF = org.drip.analytics.daycount.Convention.YearFraction (dt3MPast.getJulian(),
			dtPrev.getJulian(), _mapFixedDC.get (strCurrency), bApplyFixedCouponEOMAdj,
				java.lang.Double.NaN, null, strCurrency);

		double dbl3MFloatingDCF = org.drip.analytics.daycount.Convention.YearFraction (dt3MPast.getJulian(),
			dtPrev.getJulian(), _mapFloatingDC.get (strCurrency), bApplyFloatingCouponEOMAdj,
				java.lang.Double.NaN, null, strCurrency);

		double dbl3MCarryPnL = dblFixedCoupon * dbl3MFixedDCF - dblCurveFloatingRate * dbl3MFloatingDCF;

		double dbl3MTimeRollSwapRate = calcMeasure (comp, dtPrev, dcDate3MPastQuotePrev, "CalibSwapRate",
			strCurrency, null);

		double dbl3MTimeRollPnL = (dblBaselineSwapRate - dbl3MTimeRollSwapRate) * 10000. * dblDV01;

		try {
			return new org.drip.service.api.ProductDailyPnL (dbl1DTotalPnL, dbl1DCleanPnL, dbl1DDirtyPnL,
				dbl1DTotalPnLWithFixing, dbl1DCleanPnLWithFixing, dbl1DDirtyPnLWithFixing, dbl1DCarry,
					dbl1DTimeRollPnL, dbl1DMaturityRollDownFairPremiumPnL, dbl1DMaturityRollUpSwapRatePnL,
						dbl1DMaturityRollUpFairPremiumPnL, dbl1DMaturityRollUpFairPremiumWithFixingPnL,
							dbl1DCurveShiftPnL, dbl1MCarryPnL, dbl1MTimeRollPnL, dbl3MCarryPnL,
								dbl3MTimeRollPnL, dblDV01, dblDV01WithFixing, dblCleanFixedDV01,
									dblCleanFloatDV01, dblCleanFloatDV01WithFixing, dblBaselineSwapRate,
										dbl1DTimeRollSwapRate, dbl1MTimeRollSwapRate, dbl3MTimeRollSwapRate,
											dbl1DMaturityRollDownFairPremium, dbl1DMaturityRollUpSwapRate,
												dbl1DMaturityRollUpFairPremium,
													dbl1DMaturityRollUpFairPremiumWithFixing,
														dbl1DCurveShiftSwapRate, dblFixedCoupon,
															dblCurveFloatingRate, dblProductFloatingRate,
																dblFloatingRateUsed, i1DFixedAccrualDays,
																	i1DFloatingAccrualDays, dbl1DFixedDCF,
																		dbl1DFloatingDCF, dbl1MFixedDCF,
																			dbl1MFloatingDCF, dbl3MFixedDCF,
																				dbl3MFloatingDCF).toString();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final java.lang.String ComputeForwardMetric (
		final org.drip.product.definition.FixedIncomeComponent[] aComp,
		final org.drip.analytics.rates.DiscountCurve dc)
		throws java.lang.Exception
	{
		org.drip.service.api.ForwardRates fmOP = new org.drip.service.api.ForwardRates();

		for (int i = 0; i < aComp.length; ++i) {
			for (int j = 0; j < aComp.length; ++j) {
				double dblForward = Forward (dc, aComp[j].getMaturityDate(), aComp[i].getMaturityDate());

				if (0 != dblForward) fmOP.addForward (dblForward);
			}
		}

		return fmOP.toString();
	}

	private static final java.util.List<java.lang.String> GenerateMetrics (
		final org.drip.analytics.date.JulianDate dtPrev,
		final org.drip.analytics.date.JulianDate dtCurr,
		final org.drip.analytics.date.JulianDate dt1MPast,
		final org.drip.analytics.date.JulianDate dt3MPast,
		final org.drip.analytics.rates.DiscountCurve dcDatePastQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDatePrevQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDateCurrQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDatePrevQuoteCurr,
		final org.drip.analytics.rates.DiscountCurve dcDateCurrQuoteCurr,
		final org.drip.analytics.rates.DiscountCurve dcDate1MPastQuotePrev,
		final org.drip.analytics.rates.DiscountCurve dcDate3MPastQuotePrev,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		java.util.List<java.lang.String> lsstrDump = new java.util.ArrayList<java.lang.String>();

		double[] adblBaselineSwapRate = new double[s_astrFwdTenor.length];
		org.drip.product.definition.FixedIncomeComponent[] aComp = new
			org.drip.product.definition.FixedIncomeComponent[s_astrFwdTenor.length];
		org.drip.product.definition.FixedIncomeComponent[] aCompPrevMat = new
			org.drip.product.definition.FixedIncomeComponent[s_astrFwdTenor.length];

		int iNumDaysDiff = dtCurr.daysDiff (dtPrev);

		for (int i = 0; i < s_astrFwdTenor.length; ++i) {
			org.drip.analytics.date.JulianDate dtMaturity = dtPrev.addTenorAndAdjust (s_astrFwdTenor[i],
				strCurrency);

			if (null == dtMaturity) return null;

			org.drip.product.definition.FixedIncomeComponent comp = CreateIRS (dtPrev, s_astrFwdTenor[i], 0,
				0.01, strCurrency);

			adblBaselineSwapRate[i] = calcMeasure (comp, dtPrev, dcDatePrevQuotePrev, "CalibSwapRate",
				strCurrency, null);

			aComp[i] = CreateIRS (dtPrev, s_astrFwdTenor[i], 0, adblBaselineSwapRate[i], strCurrency);

			aCompPrevMat[i] = CreateIRS (dtPrev, s_astrFwdTenor[i], iNumDaysDiff, adblBaselineSwapRate[i],
				strCurrency);
		}

		for (int i = 0; i < s_astrFwdTenor.length; ++i)
			lsstrDump.add (dtCurr.toString() + "," + aComp[i].tenor() + "," + ComputePnLMetrics (dtPrev,
				dtCurr, dt1MPast, dt3MPast, aComp[i], aCompPrevMat[i], dcDatePastQuotePrev,
					dcDatePrevQuotePrev, dcDateCurrQuotePrev, dcDatePrevQuoteCurr, dcDateCurrQuoteCurr,
						dcDate1MPastQuotePrev, dcDate3MPastQuotePrev, adblBaselineSwapRate[i], strCurrency) +
							"," + ComputeForwardMetric (aComp, dcDateCurrQuoteCurr));

		return lsstrDump;
	}

	private static final org.drip.product.definition.CalibratableFixedIncomeComponent[] CashInstrumentsFromTenor (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String[] astrTenor,
		final java.lang.String strCurrency)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumTenor];

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			aCalibComp[i] = org.drip.product.creator.DepositBuilder.CreateDeposit (dtEffective, astrTenor[i],
				strCurrency);

		return aCalibComp;
	}

	private static final org.drip.product.definition.CalibratableFixedIncomeComponent[] SwapInstrumentsFromTenor (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String[] astrTenor,
		final double[] adblQuote,
		final java.lang.String strCurrency)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumTenor];

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			aCalibComp[i] = CreateIRS (dtEffective, astrTenor[i], 0, adblQuote[i], strCurrency);

		return aCalibComp;
	}

	public static final org.drip.analytics.rates.DiscountCurve BuildCurve (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String[] astrCashTenor,
		final double[] adblCashQuote,
		final java.lang.String[] astrSwapTenor,
		final double[] adblSwapQuote,
		final java.lang.String strCurrency)
	{
		org.drip.state.estimator.StretchRepresentationSpec rbsCash =
			org.drip.state.estimator.StretchRepresentationSpec.CreateStretchBuilderSet ("CASH",
				org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						CashInstrumentsFromTenor (dt, astrCashTenor, strCurrency), "Rate", adblCashQuote,
							null);

		org.drip.state.estimator.StretchRepresentationSpec rbsSwap =
			org.drip.state.estimator.StretchRepresentationSpec.CreateStretchBuilderSet ("SWAP",
				org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						SwapInstrumentsFromTenor (dt, astrSwapTenor, adblSwapQuote, strCurrency), "Rate",
							adblSwapQuote, null);

		if (null == rbsCash && null == rbsSwap) return null;

		org.drip.state.estimator.StretchRepresentationSpec[] aRRS = null;

		if (null == rbsCash)
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsSwap};
		else if (null == rbsSwap)
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsCash};
		else
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsCash, rbsSwap};

		org.drip.analytics.rates.DiscountCurve dcShapePreserving = null;

		try {
			org.drip.param.valuation.ValuationParams valParams = new org.drip.param.valuation.ValuationParams
				(dt, dt, strCurrency);

			org.drip.spline.params.ResponseScalingShapeControl rssc = new
				org.drip.spline.params.ResponseScalingShapeControl (true, new
					org.drip.quant.function1D.QuadraticRationalShapeControl (0.));

			org.drip.spline.params.SegmentInelasticDesignControl sdic =
				org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2);

			org.drip.state.estimator.LinearCurveCalibrator lcc = new
				org.drip.state.estimator.LinearCurveCalibrator (new
					org.drip.spline.params.SegmentCustomBuilderControl
						(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new org.drip.spline.basis.ExponentialTensionSetParams (1.), sdic, rssc, null),
					org.drip.spline.stretch.BoundarySettings.FinancialStandard(),
						org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, null);

			if (null == (dcShapePreserving =
				org.drip.param.creator.ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (lcc, aRRS,
					valParams, null, null, null, 1.0)))
				return null;

			/* org.drip.analytics.rates.DiscountCurve dcHyman83Smooth =
				org.drip.param.creator.RatesScenarioCurveBuilder.SmoothingLocalControlBuild
					(dcShapePreserving, lcc, new org.drip.state.estimator.LocalControlCurveParams
						(org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN83,
							org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE, new
								org.drip.spline.params.SegmentCustomBuilderControl
									(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new org.drip.spline.basis.PolynomialFunctionSetParams (4), sdic, rssc, null),
					org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, null, true, true), aRRS,
						valParams, null, null, null);

			return null == dcHyman83Smooth ? dcShapePreserving : dcHyman83Smooth; */
		} catch (java.lang.Exception e) {
			// e.printStackTrace();
		}

		return dcShapePreserving;
	}

	private static final org.drip.service.api.DiscountCurveInputInstrument ProcessCOBInput (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord)
	{
		if (null == astrCOBRecord) return null;

		int iNumQuote = astrCOBRecord.length;

		if (iNumQuote != astrTenor.length + 1) return null;

		java.util.List<java.lang.String> lsCashTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.String> lsSwapTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.Double> lsCashQuote = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dt = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord[0], "/");

		if (null == dt) return null;

		for (int i = 1; i < iNumQuote; ++i) {
			double dblQuote = java.lang.Double.NaN;

			try {
				dblQuote = 0.01 * Double.parseDouble (astrCOBRecord[i]);
			} catch (java.lang.Exception e) {
				dblQuote = java.lang.Double.NaN;
			}

			if (org.drip.quant.common.NumberUtil.IsValid (dblQuote)) {
				if (astrTenor[i - 1].endsWith ("M")) {
					lsCashTenor.add (astrTenor[i - 1]);

					lsCashQuote.add (dblQuote);
				} else {
					lsSwapTenor.add (astrTenor[i - 1]);

					lsSwapQuote.add (dblQuote);
				}
			}
		}

		try {
			return new org.drip.service.api.DiscountCurveInputInstrument (dt, lsCashTenor, lsCashQuote,
				lsSwapTenor, lsSwapQuote);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final org.drip.service.api.DiscountCurveInputInstrument[] ProcessCOBInput (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord1,
		final java.lang.String[] astrCOBRecord2)
	{
		if (null == astrCOBRecord1 || null == astrCOBRecord2) return null;

		int iNumQuote1 = astrCOBRecord1.length;
		int iNumQuote2 = astrCOBRecord2.length;

		if (iNumQuote2 != astrTenor.length + 1 || iNumQuote1 != iNumQuote2) return null;

		java.util.List<java.lang.String> lsCashTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.String> lsSwapTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.Double> lsCashQuote1 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote1 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsCashQuote2 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote2 = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dt1 = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord1[0], "/");

		org.drip.analytics.date.JulianDate dt2 = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord2[0], "/");

		if (null == dt1 || null == dt2) return null;

		for (int i = 1; i < iNumQuote2; ++i) {
			double dblQuote1 = java.lang.Double.NaN;
			double dblQuote2 = java.lang.Double.NaN;

			try {
				dblQuote1 = 0.01 * Double.parseDouble (astrCOBRecord1[i]);

				dblQuote2 = 0.01 * Double.parseDouble (astrCOBRecord2[i]);
			} catch (java.lang.Exception e) {
				dblQuote1 = java.lang.Double.NaN;
				dblQuote2 = java.lang.Double.NaN;
			}

			if (org.drip.quant.common.NumberUtil.IsValid (dblQuote1) &&
				org.drip.quant.common.NumberUtil.IsValid (dblQuote2)) {
				if (astrTenor[i - 1].endsWith ("M")) {
					lsCashTenor.add (astrTenor[i - 1]);

					lsCashQuote1.add (dblQuote1);

					lsCashQuote2.add (dblQuote2);
				} else {
					lsSwapTenor.add (astrTenor[i - 1]);

					lsSwapQuote1.add (dblQuote1);

					lsSwapQuote2.add (dblQuote2);
				}
			}
		}

		if ((null == lsCashTenor || 0 == lsCashTenor.size()) && (null == lsSwapTenor || 0 ==
			lsSwapTenor.size())) {
			System.out.println ("\t\tBad Tenor Straddle: " + dt1 + " | " + dt2);

			return null;
		}

		try {
			return new org.drip.service.api.DiscountCurveInputInstrument[] {new
				org.drip.service.api.DiscountCurveInputInstrument (dt1, lsCashTenor, lsCashQuote1,
					lsSwapTenor, lsSwapQuote1), new org.drip.service.api.DiscountCurveInputInstrument (dt2,
						lsCashTenor, lsCashQuote2, lsSwapTenor, lsSwapQuote2)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final java.util.List<java.lang.String> ProcessRecord (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord1,
		final java.lang.String[] astrCOBRecord2,
		final java.lang.String strCurrency,
		boolean bDumpOnDemand)
	{
		org.drip.service.api.DiscountCurveInputInstrument[] aDCII = ProcessCOBInput (astrTenor,
			astrCOBRecord1, astrCOBRecord2);

		if (null == aDCII || 2 != aDCII.length) return null;

		org.drip.service.api.DiscountCurveInputInstrument dciiPrev = aDCII[0];
		org.drip.service.api.DiscountCurveInputInstrument dciiCurr = aDCII[1];

		if (null == dciiPrev || null == dciiCurr) return null;

		org.drip.analytics.date.JulianDate dtPrev = dciiPrev.date();

		org.drip.analytics.date.JulianDate dtCurr = dciiCurr.date();

		java.lang.String[] astrCashTenorPrev = dciiPrev.cashTenor();

		java.lang.String[] astrSwapTenorPrev = dciiPrev.swapTenor();

		double[] adblCashQuotePrev = dciiPrev.cashQuote();

		double[] adblSwapQuotePrev = dciiPrev.swapQuote();

		java.lang.String[] astrCashTenorCurr = dciiCurr.cashTenor();

		java.lang.String[] astrSwapTenorCurr = dciiCurr.swapTenor();

		double[] adblCashQuoteCurr = dciiCurr.cashQuote();

		double[] adblSwapQuoteCurr = dciiCurr.swapQuote();

		if (null == dtPrev || null == dtCurr) return null;

		org.drip.analytics.date.JulianDate dtPast = dtPrev.subtractBusDays (1, strCurrency);

		if (null == dtPast) return null;

		org.drip.analytics.rates.DiscountCurve dcDatePastQuotePrev = BuildCurve (dtPast, astrCashTenorPrev,
			adblCashQuotePrev, astrSwapTenorPrev, adblSwapQuotePrev, strCurrency);

		if (null == dcDatePastQuotePrev) return null;

		org.drip.analytics.date.JulianDate dt1MPast = dtPrev.subtractTenor ("1M");

		if (null == dt1MPast) return null;

		org.drip.analytics.rates.DiscountCurve dcDate1MPastQuotePrev= BuildCurve (dt1MPast,
			astrCashTenorPrev, adblCashQuotePrev, astrSwapTenorPrev, adblSwapQuotePrev, strCurrency);

		if (null == dcDate1MPastQuotePrev) return null;

		org.drip.analytics.date.JulianDate dt3MPast = dtPrev.subtractTenor ("3M");

		if (null == dt3MPast) return null;

		org.drip.analytics.rates.DiscountCurve dcDate3MPastQuotePrev = BuildCurve (dt3MPast,
			astrCashTenorPrev, adblCashQuotePrev, astrSwapTenorPrev, adblSwapQuotePrev, strCurrency);

		if (null == dcDate3MPastQuotePrev) return null;

		org.drip.analytics.rates.DiscountCurve dcDatePrevQuotePrev = BuildCurve (dtPrev, astrCashTenorPrev,
			adblCashQuotePrev, astrSwapTenorPrev, adblSwapQuotePrev, strCurrency);

		if (null == dcDatePrevQuotePrev) return null;

		org.drip.analytics.rates.DiscountCurve dcDateCurrQuotePrev = BuildCurve (dtCurr, astrCashTenorPrev,
			adblCashQuotePrev, astrSwapTenorPrev, adblSwapQuotePrev, strCurrency);

		if (null == dcDateCurrQuotePrev) return null;

		org.drip.analytics.rates.DiscountCurve dcDatePrevQuoteCurr = BuildCurve (dtPrev, astrCashTenorCurr,
			adblCashQuoteCurr, astrSwapTenorCurr, adblSwapQuoteCurr, strCurrency);

		if (null == dcDatePrevQuoteCurr) return null;

		org.drip.analytics.rates.DiscountCurve dcDateCurrQuoteCurr = BuildCurve (dtCurr, astrCashTenorCurr,
			adblCashQuoteCurr, astrSwapTenorCurr, adblSwapQuoteCurr, strCurrency);

		if (null == dcDateCurrQuoteCurr) return null;

		java.util.List<java.lang.String> lsstrDump = null;

		if (null != dtPast && null != dtPrev && null != dtCurr && null != dcDatePastQuotePrev && null !=
			dcDatePrevQuotePrev && null != dcDateCurrQuotePrev && null != dcDatePrevQuoteCurr && null !=
				dcDateCurrQuoteCurr && null != dcDate1MPastQuotePrev && null != dcDate3MPastQuotePrev) {
			System.out.println ("\t" + strCurrency + "[" + dtPrev + "]");

			try {
				lsstrDump = GenerateMetrics (dtPrev, dtCurr, dt1MPast, dt3MPast, dcDatePastQuotePrev,
					dcDatePrevQuotePrev, dcDateCurrQuotePrev, dcDatePrevQuoteCurr, dcDateCurrQuoteCurr,
						dcDate1MPastQuotePrev, dcDate3MPastQuotePrev, strCurrency);

				for (java.lang.String strDump : lsstrDump) {
					if (bDumpOnDemand) {
						_writeCOB.write (strDump);

						_writeCOB.newLine();

						_writeCOB.flush();
					}
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsstrDump;
	}

	public static final boolean ExecUnitSequence()
	{
		java.lang.String[] astrTenor = new java.lang.String[] {"1M", "1Y", "5Y", "10Y"};
		java.lang.String[][] aastrCOBRecord = new java.lang.String[][] {
			new java.lang.String[] {"3/28/2013", "0.21", "0.52", "1.17", "1.68"},
			new java.lang.String[] {"3/29/2013", "0.21", "0.53", "1.19", "1.71"}
		};

		for (int i = 1; i < aastrCOBRecord.length; ++i) {
			java.util.List<java.lang.String> lsstrDump = ProcessRecord (astrTenor, aastrCOBRecord[i - 1],
				aastrCOBRecord[i], "USD", false);

			if (null == lsstrDump || 0 == lsstrDump.size()) return false;

			for (java.lang.String strDump : lsstrDump)
				System.out.println (strDump);
		}

		return true;
	}

	public static final void GenerateDiscountCurveMetrics (
		final java.lang.String strCurrency)
	{
		boolean bIsHeader = true;
		java.lang.String strCOBQuote = "";
		java.lang.String[] astrTenor = null;
		java.io.BufferedReader brSwapCOB = null;
		java.lang.String[] astrCOBRecordPrev = null;

		try {
			brSwapCOB = new java.io.BufferedReader (new java.io.FileReader ("C:\\IFA\\G10Rates\\" +
				strCurrency + "_Clean_Input.txt"));

			_writeCOB = new java.io.BufferedWriter (new java.io.FileWriter
				("C:\\IFA\\G10Rates\\FinancialBoundary\\" + strCurrency + "_PnL.csv"));

			while (null != (strCOBQuote = brSwapCOB.readLine())) {
				java.lang.String[] astrCOBRecord = strCOBQuote.split (",");

				if (null == astrCOBRecord) {
					brSwapCOB.close();

					return;
				}

				int iNumQuote = astrCOBRecord.length;

				if (0 == iNumQuote) {
					brSwapCOB.close();

					return;
				}

				if (bIsHeader) {
					bIsHeader = false;
					astrTenor = new java.lang.String[iNumQuote - 1];

					for (int i = 1; i < iNumQuote; ++i)
						astrTenor[i - 1] = astrCOBRecord[i];

					_writeCOB.write ("Date, Instrument, 1DTotalPnL, 1DCleanPnL, 1DDirtyPnL, 1DTotalPnLWithFixing, 1DCleanPnLWithFixing, 1DDirtyPnLWithFixing, ");

					_writeCOB.write ("1DCarryPnL, 1DTimeRollPnL, 1DMaturityRollDownFairPremiumPnL, 1DMaturityRollUpSwapRatePnL, 1DMaturityRollUpFairPremiumPnL, 1DMaturityRollUpFairPremiumWithFixingPnL, 1DCurveShiftPnL, ");

					_writeCOB.write ("1MCarryPnL, 1MTimeRollPnL, 3MCarryPnL, 3MTimeRollPnL, DV01, DV01WithFixing, CleanFixedDV01, CleanFloatDV01, CleanFloatDV01WithFixing, ");

					_writeCOB.write ("BaselineSwapRate, 1DTimeRollSwapRate, 1MTimeRollSwapRate, 3MTimeRollSwapRate, 1DMaturityRollDownFairPremium, ");

					_writeCOB.write ("1DMaturityRollUpSwapRate, 1DMaturityRollUpFairPremium, 1DMaturityRollUpFairPremiumWithFixing, 1DCurveShiftSwapRate, ");

					_writeCOB.write ("PeriodFixedRate, PeriodCurveFloatingRate, PeriodProductFloatingRate, PeriodFloatingRateUsed, FixedDays, FloatingDays, ");

					_writeCOB.write ("1DFixedDCF, 1DFloatingDCF, 1MFixedDCF, 1MFloatingDCF, 3MFixedDCF, 3MFloatingDCF, ");

					_writeCOB.write ("1Y1YF, 1Y2YF, 1Y3YF, 1Y4YF, 1Y5YF, 1Y6YF, 1Y7YF, 1Y8YF, 1Y9YF, 1Y10YF, 1Y11YF, ");

					_writeCOB.write ("2Y1YF, 2Y2YF, 2Y3YF, 2Y4YF, 2Y5YF, 2Y6YF, 2Y7YF, 2Y8YF, 2Y9YF, 2Y10YF, ");

					_writeCOB.write ("3Y1YF, 3Y2YF, 3Y3YF, 3Y4YF, 3Y5YF, 3Y6YF, 3Y7YF, 3Y8YF, 3Y9YF, ");

					_writeCOB.write ("4Y1YF, 4Y2YF, 4Y3YF, 4Y4YF, 4Y5YF, 4Y6YF, 4Y7YF, 4Y8YF, ");

					_writeCOB.write ("5Y1YF, 5Y2YF, 5Y3YF, 5Y4YF, 5Y5YF, 5Y6YF, 5Y7YF, ");

					_writeCOB.write ("6Y1YF, 6Y2YF, 6Y3YF, 6Y4YF, 6Y5YF, 6Y6YF, ");

					_writeCOB.write ("7Y1YF, 7Y2YF, 7Y3YF, 7Y4YF, 7Y5YF, ");

					_writeCOB.write ("8Y1YF, 8Y2YF, 8Y3YF, 8Y4YF, ");

					_writeCOB.write ("9Y1YF, 9Y2YF, 9Y3YF, ");

					_writeCOB.write ("10Y1YF, 10Y2YF, ");

					_writeCOB.write ("11Y1YF, ");

					_writeCOB.newLine();

					_writeCOB.flush();
				} else
					ProcessRecord (astrTenor, astrCOBRecordPrev, astrCOBRecord, strCurrency, true);

				astrCOBRecordPrev = astrCOBRecord;
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return;
		}

		try {
			brSwapCOB.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static final java.util.Map<org.drip.analytics.date.JulianDate,
		java.util.List<org.drip.service.api.CDXCOB>> LoadCDXCloses (
			final java.lang.String strFile)
	{
		int iNumCDX = 0;
		int iLineNum = 0;
		boolean bHeader = true;
		java.lang.String strCDXCloses = "";
		java.lang.String[] astrCDXName = null;
		java.io.BufferedReader brCDXCloses = null;

		java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				java.util.List<org.drip.service.api.CDXCOB>>();

		try {
			brCDXCloses = new java.io.BufferedReader (new java.io.FileReader (strFile));

			while (null != (strCDXCloses = brCDXCloses.readLine())) {
				java.lang.String[] astrCDXCloses = strCDXCloses.split (",");

				if (null == astrCDXCloses || 0 == astrCDXCloses.length) continue;

				++iLineNum;

				System.out.println ("Processing line " + iLineNum);

				if (bHeader) {
					bHeader = false;
					iNumCDX = astrCDXCloses.length;
					astrCDXName = new java.lang.String[iNumCDX - 1];

					for (int i = 1; i < iNumCDX; ++i) {
						astrCDXName[i - 1] = astrCDXCloses[i];

						System.out.println ("\tCDX: " + astrCDXName[i - 1]);
					}
				} else {
					org.drip.analytics.date.JulianDate dt = org.drip.analytics.date.JulianDate.CreateFromMDY
						(astrCDXCloses[0], "/");

					if (null == dt) continue;

					java.util.List<org.drip.service.api.CDXCOB> lsCDXNamedPrice = new
						java.util.ArrayList<org.drip.service.api.CDXCOB>();

					for (int i = 1; i < iNumCDX; ++i) {
						double dblQuote = java.lang.Double.NaN;

						try {
							dblQuote = 0.01 * Double.parseDouble (astrCDXCloses[i]);
						} catch (java.lang.Exception e) {
							dblQuote = java.lang.Double.NaN;
						}

						if (org.drip.quant.common.NumberUtil.IsValid (dblQuote)) {
							org.drip.service.api.CDXCOB cdxNP = new org.drip.service.api.CDXCOB
								(astrCDXName[i - 1], dblQuote);

							lsCDXNamedPrice.add (cdxNP);

							System.out.println ("\tAdding CDX: " + cdxNP.display());
						}
					}

					if (0 < lsCDXNamedPrice.size()) mapDatedCDXClose.put (dt, lsCDXNamedPrice);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (null != brCDXCloses) brCDXCloses.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return mapDatedCDXClose;
	}

	public static final void ProcessCDXQuote (
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose)
	{
		boolean bIsHeader = true;
		java.lang.String strCOBQuote = "";
		java.lang.String[] astrTenor = null;
		java.io.BufferedReader brSwapCOB = null;

		org.drip.param.pricer.PricerParams pricerParams = 
			org.drip.param.pricer.PricerParams.MakeStdPricerParams();

		try {
			brSwapCOB = new java.io.BufferedReader (new java.io.FileReader
				("C:\\IFA\\CDXOP\\USD_CDS_Fixing_Curve_Orig_3.txt"));

			_writeCOB = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\IFA\\CDXOP\\HY5Y.LAST"));

			while (null != (strCOBQuote = brSwapCOB.readLine())) {
				java.lang.String[] astrCOBRecord = strCOBQuote.split (",");

				if (null == astrCOBRecord) {
					brSwapCOB.close();

					return;
				}

				int iNumQuote = astrCOBRecord.length;

				if (0 == iNumQuote) {
					brSwapCOB.close();

					return;
				}

				if (bIsHeader) {
					bIsHeader = false;
					astrTenor = new java.lang.String[iNumQuote - 1];

					for (int i = 1; i < iNumQuote; ++i)
						astrTenor[i - 1] = astrCOBRecord[i];
				} else {
					org.drip.service.api.DiscountCurveInputInstrument dcci = ProcessCOBInput (astrTenor,
						astrCOBRecord);

					if (null != dcci) {
						org.drip.analytics.date.JulianDate dtCOB = dcci.date();

						org.drip.param.valuation.ValuationParams valParams = new
							org.drip.param.valuation.ValuationParams (dtCOB, dtCOB, "USD");

						org.drip.analytics.rates.DiscountCurve dc = BuildCurve (dtCOB, dcci.cashTenor(),
							dcci.cashQuote(), dcci.swapTenor(), dcci.swapQuote(), "USD");

						System.out.println (dtCOB + " => " + dc);

						java.util.List<org.drip.service.api.CDXCOB> lsCDXNamedPrice = mapDatedCDXClose.get
							(dtCOB);

						if (null != lsCDXNamedPrice && 0 != lsCDXNamedPrice.size()) {
							for (org.drip.service.api.CDXCOB cdxNP : lsCDXNamedPrice) {
								org.drip.product.definition.CreditDefaultSwap cdx =
									org.drip.product.creator.CDSBuilder.CreateSNAC (dtCOB, "5Y", 0.05,
										cdxNP.name());

								org.drip.analytics.definition.CreditCurve cc =
									org.drip.param.creator.CreditScenarioCurveBuilder.CreateCreditCurve
										("CC", dtCOB, new org.drip.product.definition.CreditDefaultSwap[]
											{cdx}, dc, new double[] {100. * cdxNP.price()}, new
												java.lang.String[] {"Price"}, 0.04, false);

								org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
									mapCDXMeasures = cdx.value (valParams, pricerParams,
										org.drip.param.creator.ComponentMarketParamsBuilder.MakeCreditCMP
											(dc, cc), null);

								_writeCOB.write (dtCOB + "," + cdxNP.name() + "," + cdxNP.price() + "," +
									org.drip.quant.common.FormatUtil.FormatDouble (mapCDXMeasures.get
										("FairPremium"), 1, 2, 1.));

								_writeCOB.newLine();

								_writeCOB.flush();
							}
						}
					}
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return;
		}

		try {
			brSwapCOB.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static final void main (
		final java.lang.String[] astrArgs)
	{
		long lStartTime = System.nanoTime();

		org.drip.service.api.CreditAnalytics.Init ("");

		InitStatic();

		/* java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose = LoadCDXCloses ("c:\\IFA\\CDXOP\\CDX_HY_PX_5Y_CONTRACTS_LAST_Orig.txt");

		ProcessCDXQuote (mapDatedCDXClose); */

		GenerateDiscountCurveMetrics ("GBP");

		// ExecUnitSequence();

		System.out.println ("Time Taken: " + ((System.nanoTime() - lStartTime) * 1.e-9) + " sec");
	}
}
