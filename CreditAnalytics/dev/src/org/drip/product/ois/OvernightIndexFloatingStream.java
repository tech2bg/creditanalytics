
package org.drip.product.ois;

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
 * OvernightIndexFloatingStream contains an implementation of the Floating leg cash flow stream backed by an
 * 	Overnight Stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndexFloatingStream extends org.drip.product.rates.FloatingStream {

	private org.drip.analytics.output.PeriodCouponMeasures compoundArithmetic (
		final double dblAccrualEndDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
				csqs.fixings();

		if (null == mapFixings || 0 == mapFixings.size()) return null;

		double dblValueDate = valParams.valueDate();

		double dblPrevDate = currentPeriod.start();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

		java.lang.String strFRIFullName = fri.fullyQualifiedName();

		double dblNominalAccrued = 0.;
		double dblConvexityAdjustedAccrued = 0.;
		double dblLastCoupon = java.lang.Double.NaN;
		org.drip.analytics.date.JulianDate dt = null;

		try {
			dt = new org.drip.analytics.date.JulianDate (dblPrevDate + 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblDate = dt.julian();

		java.lang.String strCurrency = couponCurrency()[0];

		while (dblDate <= dblAccrualEndDate) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRIFixing = null;

			try {
				mapFRIFixing = mapFixings.get (new org.drip.analytics.date.JulianDate (dblDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (null != mapFRIFixing && mapFRIFixing.containsKey (strFRIFullName)) {
				java.lang.Double dblFixing = mapFRIFixing.get (strFRIFullName);

				if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing)) {
					double dblAccrualConvexityAdjustment = 1.;
					double dblIncrementalAccrued = java.lang.Double.NaN;

					if (dblValueDate < dblDate) {
						try {
							dblAccrualConvexityAdjustment = java.lang.Math.exp
								(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
									(csqs.fundingCurveVolSurface (strCurrency), csqs.forwardCurveVolSurface
										(fri), csqs.forwardFundingCorrSurface (fri, strCurrency),
											dblValueDate, dblDate));
						} catch (java.lang.Exception e) {
							e.printStackTrace();

							return null;
						}
					}

					try {
						dblIncrementalAccrued = org.drip.analytics.daycount.Convention.YearFraction
							(dblPrevDate, dblDate, strAccrualDC, false, java.lang.Double.NaN, null,
								strCalendar) * (dblLastCoupon = dblFixing);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}

					dblPrevDate = dblDate;
					dblNominalAccrued += dblIncrementalAccrued;
					dblConvexityAdjustedAccrued += dblIncrementalAccrued * dblAccrualConvexityAdjustment;
				}
			}

			dblDate = (dt = dt.addBusDays (1, strCalendar)).julian();
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblLastCoupon)) return null;

		try {
			double dblGapAccrued = org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
				dblAccrualEndDate, currentPeriod.accrualDC(), false, java.lang.Double.NaN, null, strCalendar)
					* dblLastCoupon;

			double dblDCFNormalizer = org.drip.analytics.daycount.Convention.YearFraction
				(currentPeriod.start(), dblAccrualEndDate, strAccrualDC, false, java.lang.Double.NaN, null,
					strCalendar);

			return new org.drip.analytics.output.PeriodCouponMeasures ((dblNominalAccrued + dblGapAccrued) /
				dblDCFNormalizer, (dblConvexityAdjustedAccrued + dblGapAccrued) / dblDCFNormalizer);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.output.PeriodCouponMeasures compoundGeometric (
		final double dblAccrualEndDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
				csqs.fixings();

		if (null == mapFixings || 0 == mapFixings.size()) return null;

		double dblPrevDate = currentPeriod.start();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

		java.lang.String strFRIFullName = fri.fullyQualifiedName();

		double dblAccruedAccount = 1.;
		double dblLastCoupon = java.lang.Double.NaN;
		org.drip.analytics.date.JulianDate dt = null;

		try {
			dt = new org.drip.analytics.date.JulianDate (dblPrevDate + 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblDate = dt.julian();

		while (dblDate <= dblAccrualEndDate) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRIFixing = null;

			try {
				mapFRIFixing = mapFixings.get (new org.drip.analytics.date.JulianDate (dblDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (null != mapFRIFixing && mapFRIFixing.containsKey (strFRIFullName)) {
				java.lang.Double dblFixing = mapFRIFixing.get (strFRIFullName);

				if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing)) {
					try {
						dblAccruedAccount *= (1. + org.drip.analytics.daycount.Convention.YearFraction
							(dblPrevDate, dblDate, strAccrualDC, false, java.lang.Double.NaN, null,
								strCalendar) * (dblLastCoupon = dblFixing));
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}

					dblPrevDate = dblDate;
				}
			}

			dblDate = (dt = dt.addBusDays (1, strCalendar)).julian();
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblLastCoupon)) return null;

		try {
			return org.drip.analytics.output.PeriodCouponMeasures.Nominal ((dblAccruedAccount * (1. +
				org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate, dblAccrualEndDate,
					currentPeriod.accrualDC(), false, java.lang.Double.NaN, null, strCalendar) *
						dblLastCoupon) - 1.) / org.drip.analytics.daycount.Convention.YearFraction
							(currentPeriod.start(), dblAccrualEndDate, strAccrualDC, false,
								java.lang.Double.NaN, null, strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected org.drip.analytics.output.PeriodCouponMeasures compoundFixing (
		final double dblAccrualEndDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.analytics.output.PeriodCouponMeasures pcm = super.compoundFixing (dblAccrualEndDate,
			org.drip.product.params.FloatingRateIndex.Create (fri.currency(), fri.index(), "1D"),
				currentPeriod, valParams, csqs);

		if (null != pcm) return pcm;

		return fri.isArithmeticCompounding() ? compoundArithmetic (dblAccrualEndDate, fri, currentPeriod,
			valParams, csqs) : compoundGeometric (dblAccrualEndDate, fri, currentPeriod, csqs);
	}

	/**
	 * OvernightIndexFloatingStream constructor
	 * 
	 * @param strCurrency Cash Flow Currency
	 * @param dblSpread Spread
	 * @param dblNotional Initial Notional Amount
	 * @param notlSchedule Notional Schedule
	 * @param lsCouponPeriod List of the Coupon Periods
	 * @param fri Floating Rate Index
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public OvernightIndexFloatingStream (
		final java.lang.String strCurrency,
		final double dblSpread,
		final double dblNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod,
		final org.drip.product.params.FloatingRateIndex fri,
		final boolean bIsReference)
		throws java.lang.Exception
	{
		super (strCurrency, null, dblSpread, dblNotional, notlSchedule, lsCouponPeriod, fri, bIsReference);
	}
}
