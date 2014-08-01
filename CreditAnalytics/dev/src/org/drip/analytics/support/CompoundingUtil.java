
package org.drip.analytics.support;

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
 * CompoundingUtil performs a sub-period roll-over compounding onto the corresponding pay period. Currently
 * 	it supports arithmetic and geometric compounding. It outputs both the nominal and convexity adjusted
 * 	coupons.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CompoundingUtil {

	/**
	 * Compounds the sub period flows arithmetically onto the pay period
	 * 
	 * @param dblAccrualEndDate The Accrual End Date
	 * @param fri The Floating Rate Index
	 * @param currentPeriod The Period Enclosing the Accrual Date
	 * @param valParams The Valuation Parameters
	 * @param csqs The Market Parameters Surface
	 * 
	 * @return Arithmetically rolled over Nominal/Convexity Adjusted Coupon Amounts
	 */

	public static final org.drip.analytics.output.PeriodCouponMeasures Arithmetic (
		final double dblAccrualEndDate,
		final org.drip.state.identifier.ForwardLabel fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == currentPeriod || null == fri || null == valParams || null == csqs) return null;

		try {
			if (!currentPeriod.contains (dblAccrualEndDate)) return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblValueDate = valParams.valueDate();

		double dblPrevDate = currentPeriod.start();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

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

		java.lang.String strCurrency = fri.currency();

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(strCurrency);

		while (dblDate <= dblAccrualEndDate) {
			if (csqs.available (dblDate, fri)) {
				double dblAccrualConvexityAdjustment = 1.;
				double dblIncrementalAccrued = java.lang.Double.NaN;

				if (dblValueDate < dblDate) {
					try {
						dblAccrualConvexityAdjustment = java.lang.Math.exp
							(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
								(csqs.fundingCurveVolSurface (fundingLabel), csqs.forwardCurveVolSurface
									(fri), csqs.forwardFundingCorrSurface (fri, fundingLabel), dblValueDate,
										dblDate));
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				}

				try {
					dblIncrementalAccrued = org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
						dblDate, strAccrualDC, false, java.lang.Double.NaN, null, strCalendar) *
							(dblLastCoupon = csqs.getFixing (dblDate, fri));
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}

				dblPrevDate = dblDate;
				dblNominalAccrued += dblIncrementalAccrued;
				dblConvexityAdjustedAccrued += dblIncrementalAccrued * dblAccrualConvexityAdjustment;
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

	/**
	 * Compounds the sub period flows geometrically onto the pay period
	 * 
	 * @param dblAccrualEndDate The Accrual End Date
	 * @param fri The Floating Rate Index
	 * @param currentPeriod The Period Enclosing the Accrual Date
	 * @param csqs The Market Parameters Surface
	 * 
	 * @return Geometrically rolled over Nominal/Convexity Adjusted Coupon Amounts
	 */

	public static final org.drip.analytics.output.PeriodCouponMeasures Geometric (
		final double dblAccrualEndDate,
		final org.drip.state.identifier.ForwardLabel fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == currentPeriod || null == fri || null == csqs) return null;

		try {
			if (!currentPeriod.contains (dblAccrualEndDate)) return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblPrevDate = currentPeriod.start();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

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
			if (csqs.available (dblDate, fri)) {
				try {
					dblAccruedAccount *= (1. + org.drip.analytics.daycount.Convention.YearFraction
						(dblPrevDate, dblDate, strAccrualDC, false, java.lang.Double.NaN, null, strCalendar)
							* (dblLastCoupon = csqs.getFixing (dblDate, fri)));
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}

				dblPrevDate = dblDate;
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
}
