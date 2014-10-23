
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
 * PeriodBuilder exposes several period construction functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PeriodBuilder {
	private static final double DAPAdjust (
		final double dblDate,
		final org.drip.analytics.daycount.DateAdjustParams dap)
	{
		if (null == dap) return dblDate;

		try {
			return dap.roll (dblDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dblDate;
	}

	/**
	 * Fully customized Regular Period List Generation
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblFXFixingDate FX Fixing Date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapFixing Fixing Date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param forwardLabel The Forward Label
	 * @param creditlabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> RegularPeriodSingleReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final double dblFXFixingDate,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapFixing,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) || null == strMaturityTenor)
			return null;

		boolean bLoopOn = true;
		java.lang.String strPeriodTenor = (12 / iFreq) + "M";
		org.drip.analytics.date.JulianDate dtPeriodStart = null;

		try {
			dtPeriodStart = new org.drip.analytics.date.JulianDate (dblEffective);

		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.date.JulianDate dtMaturity = dtPeriodStart.addTenor (strMaturityTenor);

		if (null == dtMaturity) return null;

		double dblMaturityDate = dtMaturity.julian();

		org.drip.analytics.date.JulianDate dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor);

		if (null == dtPeriodEnd) return null;

		double dblPeriodEndDate = dtPeriodEnd.julian();

		if (dblPeriodEndDate > dblMaturityDate) dblPeriodEndDate = dblMaturityDate;

		java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.cashflow.GenericCouponPeriod>();

		while (dblPeriodEndDate <= dblMaturityDate && bLoopOn) {
			if (dblPeriodEndDate >= dblMaturityDate) {
				bLoopOn = false;
				dblPeriodEndDate = dblMaturityDate;
			}

			double dblPeriodStartDate = dtPeriodStart.julian();

			double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

			double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodEnd);

			double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

			double dblAccrualEnd = dblPeriodEndDate == dblMaturityDate ? dblPeriodEndDate : DAPAdjust
				(dblPeriodEndDate, dapAccrualStart);

			try {
				org.drip.analytics.cashflow.ResetPeriodContainer rpc = null;

				if (null != forwardLabel && !(rpc = new org.drip.analytics.cashflow.ResetPeriodContainer
					(org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC)).appendResetPeriod
					(new org.drip.analytics.cashflow.GenericComposablePeriod (dblAdjustedStartDate,
						dblAdjustedEndDate, DAPAdjust (dblPeriodStartDate, dapFixing), forwardLabel)))
					return null;

				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
						strAccrualDC, bApplyAccEOMAdj, new org.drip.analytics.daycount.ActActDCParams (iFreq,
							dblAccrualStart, dblAccrualEnd), strCalendar);

				lsCashflowPeriod.add (new org.drip.analytics.cashflow.GenericCouponPeriod (dblAdjustedStartDate,
					dblAdjustedEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay),
						rpc, dblFXFixingDate, iFreq, dblDCF, strCouponDC, strAccrualDC, bApplyCpnEOMAdj,
							bApplyAccEOMAdj, strCalendar, dblBaseNotional, notlSchedule,
								dblFixedCouponFloatSpread, strPayCurrency, strCouponCurrency, forwardLabel,
									creditLabel));

				dtPeriodStart = dtPeriodEnd;

				if (null == (dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor))) return null;

				dblPeriodEndDate = dtPeriodEnd.julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Regular Period List Generation
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblFXFixingDate FX Fixing Date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param forwardLabel The Forward Label
	 * @param creditlabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> RegularPeriodDailyReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final double dblFXFixingDate,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final int iAccrualCompoundingRule,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) || null == strMaturityTenor)
			return null;

		boolean bLoopOn = true;
		java.lang.String strPeriodTenor = (12 / iFreq) + "M";
		org.drip.analytics.date.JulianDate dtPeriodStart = null;

		try {
			dtPeriodStart = new org.drip.analytics.date.JulianDate (dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.date.JulianDate dtMaturity = dtPeriodStart.addTenor (strMaturityTenor);

		if (null == dtMaturity) return null;

		double dblMaturityDate = dtMaturity.julian();

		org.drip.analytics.date.JulianDate dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor);

		if (null == dtPeriodEnd) return null;

		java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.cashflow.GenericCouponPeriod>();

		double dblPeriodEndDate = dtPeriodEnd.julian();

		if (dblPeriodEndDate > dblMaturityDate) dblPeriodEndDate = dblMaturityDate;

		while (dblPeriodEndDate <= dblMaturityDate && bLoopOn) {
			if (dblPeriodEndDate >= dblMaturityDate) {
				bLoopOn = false;
				dblPeriodEndDate = dblMaturityDate;
			}

			double dblPeriodStartDate = dtPeriodStart.julian();

			double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

			double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodEnd);

			double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

			double dblAccrualEnd = dblPeriodEndDate == dblMaturityDate ? dblPeriodEndDate : DAPAdjust
				(dblPeriodEndDate, dapAccrualStart);

			try {
				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
						strAccrualDC, bApplyAccEOMAdj, new org.drip.analytics.daycount.ActActDCParams (iFreq,
							dblAccrualStart, dblAccrualEnd), strCalendar);

				lsCashflowPeriod.add (new org.drip.analytics.cashflow.GenericCouponPeriod (dblAdjustedStartDate,
					dblAdjustedEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay),
						null != forwardLabel ? org.drip.analytics.support.CompositePeriodUtil.DailyResetPeriod
							(dblAdjustedStartDate, dblAdjustedEndDate, forwardLabel, iAccrualCompoundingRule,
								strCalendar) : null, dblFXFixingDate, iFreq, dblDCF, strCouponDC,
									strAccrualDC, bApplyCpnEOMAdj, bApplyAccEOMAdj, strCalendar,
										dblBaseNotional, notlSchedule, dblFixedCouponFloatSpread,
											strPayCurrency, strCouponCurrency, forwardLabel, creditLabel));

				dtPeriodStart = dtPeriodEnd;

				if (null == (dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor))) return null;

				dblPeriodEndDate = dtPeriodEnd.julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}

	/**
	 * Generate a single Cash Flow period between the effective and the maturity dates
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dblFXFixingDate FX Fixing Date
	 * @param strDayCount Day Count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCouponCurrency Pay Currency
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List containing the single Cash Flow period
	 */

	public static final java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> SinglePeriodSingleReset (
		final double dblEffective,
		final double dblMaturity,
		final double dblFXFixingDate,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.cashflow.GenericCouponPeriod>();

		try {
			org.drip.analytics.cashflow.ResetPeriodContainer rpc = null;

			if (null != forwardLabel && !(rpc = new org.drip.analytics.cashflow.ResetPeriodContainer
				(org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC)).appendResetPeriod
					(new org.drip.analytics.cashflow.GenericComposablePeriod (dblEffective, dblMaturity,
						dblEffective, forwardLabel)))
				return null;

			lsCashflowPeriod.add (0, new org.drip.analytics.cashflow.GenericCouponPeriod (dblEffective, dblMaturity,
				dblEffective, dblMaturity, dblMaturity, rpc, dblFXFixingDate, 1,
					org.drip.analytics.daycount.Convention.YearFraction (dblEffective, dblMaturity,
						strDayCount, false, null, strCalendar), strDayCount, strDayCount, false, false,
							strCalendar, dblBaseNotional, notlSchedule, dblFixedCouponFloatSpread,
								strPayCurrency, strPayCurrency, forwardLabel, creditLabel));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return lsCashflowPeriod;
	}

	/**
	 * Generate the daily period list starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dblFXFixingDate FX Fixing Date
	 * @param dapReset Reset Date Adjust Parameters
	 * @param dapPay Pay Date Adjust Parameters
	 * @param strDC Accrual/Coupon day count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> DailyPeriodDailyReset (
		final double dblEffective,
		final double dblMaturity,
		final double dblFXFixingDate,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final java.lang.String strDC,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final int iAccrualCompoundingRule,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		boolean bTerminationReached = false;
		double dblPeriodStartDate = dblEffective;

		java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.cashflow.GenericCouponPeriod>();

		while (!bTerminationReached) {
			try {
				double dblAdjustedStartDate = org.drip.analytics.daycount.Convention.RollDate
					(dblPeriodStartDate, org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar);

				double dblAdjustedEndDate = org.drip.analytics.daycount.Convention.RollDate
					(dblAdjustedStartDate + 1, org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar);

				if (dblAdjustedStartDate >= dblMaturity) {
					dblAdjustedStartDate = dblPeriodStartDate;
					bTerminationReached = true;
				}

				if (dblAdjustedEndDate >= dblMaturity) {
					dblAdjustedEndDate = dblMaturity;
					bTerminationReached = true;
				}

				org.drip.analytics.cashflow.ResetPeriodContainer rpc = null;

				if (null != forwardLabel && !(rpc = new org.drip.analytics.cashflow.ResetPeriodContainer
					(iAccrualCompoundingRule)).appendResetPeriod (new
						org.drip.analytics.cashflow.GenericComposablePeriod (dblAdjustedStartDate,
							dblAdjustedEndDate, dblAdjustedStartDate, forwardLabel)))
					return null;

				if (dblAdjustedStartDate < dblAdjustedEndDate)
					lsCashflowPeriod.add (new org.drip.analytics.cashflow.GenericCouponPeriod (dblAdjustedStartDate,
						dblAdjustedEndDate, dblAdjustedStartDate, dblAdjustedEndDate, DAPAdjust
							(dblAdjustedEndDate, dapPay), rpc, dblFXFixingDate, 360,
								org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedStartDate,
									dblAdjustedEndDate, strDC, false, null, strCalendar), strDC, strDC,
										false, false, strCalendar, dblBaseNotional, notlSchedule,
											dblFixedCouponFloatSpread, strPayCurrency, strCouponCurrency,
												forwardLabel, creditLabel));

				dblPeriodStartDate = dblAdjustedEndDate;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}
}
