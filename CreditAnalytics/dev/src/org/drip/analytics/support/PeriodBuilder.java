
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
 * 
 * PeriodBuilder exposes several period construction functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PeriodBuilder {

	/**
	 * Period Set Generation Customization - No adjustment on either end
	 */

	public static final int NO_ADJUSTMENT = 0;

	/**
	 * Period Set Generation Customization - Merge the front periods to produce a long front
	 */

	public static final int FULL_FRONT_PERIOD = 1;

	/**
	 * Period Set Generation Customization - Stub (if present) belongs to the front end
	 */

	public static final int LONG_FRONT_STUB = 2;

	/**
	 * Period Set Generation Customization - Stub (if present) belongs to the back end
	 */

	public static final int LONG_BACK_STUB = 4;

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
	 * Merge the left and right Cash Flow periods onto a bigger Cash Flow period
	 * 
	 * @param periodLeft Left Cash Flow Period
	 * @param periodRight Right Cash Flow Period
	 * 
	 * @return Merged Cash Flow Period
	 */

	public static final org.drip.analytics.period.CashflowPeriod MergeCashFlowPeriods (
		final org.drip.analytics.period.CashflowPeriod periodLeft,
		final org.drip.analytics.period.CashflowPeriod periodRight)
	{
		if (null == periodLeft || null == periodRight || periodLeft.end() != periodRight.start())
			return null;

		java.lang.String strCurrency = periodLeft.currency();

		if (!strCurrency.equalsIgnoreCase (periodRight.currency())) return null;

		try {
			double dblLeftDCF = org.drip.analytics.daycount.Convention.YearFraction
				(periodLeft.accrualEnd(), periodLeft.accrualStart(), periodLeft.accrualDC(),
					periodLeft.accrualEODAdjustment(), java.lang.Double.NaN, null, periodLeft.calendar());

			if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftDCF)) return null;

			return new org.drip.analytics.period.CashflowPeriod (periodLeft.start(), periodRight.end(),
				periodLeft.accrualStart(), periodRight.accrualEnd(), periodRight.pay(), periodLeft.reset(),
					periodRight.freq(), dblLeftDCF + 1. / periodRight.freq(), periodRight.couponDC(),
						periodRight.couponEODAdjustment(), periodRight.accrualDC(),
							periodRight.accrualEODAdjustment(), java.lang.Double.NaN, periodRight.calendar(),
								strCurrency);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Fully Customized Generation of the period list backward starting from the end.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturityUnadjusted Unadjusted Maturity date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsBackward (
		final double dblEffective,
		final double dblMaturityUnadjusted,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturityUnadjusted) || dblEffective >=
				dblMaturityUnadjusted || 0 == iFreq)
			return null;

		double dblMaturity = DAPAdjust (dblMaturityUnadjusted, dapMaturity);

		boolean bFinalPeriod = true;
		boolean bGenerationDone = false;
		double dblPeriodEndDate = dblMaturity;
		java.lang.String strTenor = (12 / iFreq) + "M";
		double dblPeriodStartDate = java.lang.Double.NaN;
		org.drip.analytics.period.CashflowPeriod periodFirst = null;
		org.drip.analytics.period.CashflowPeriod periodSecond = null;

		try {
			dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
				(strTenor).julian();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

		while (!bGenerationDone) {
			if (dblPeriodStartDate <= dblEffective) {
				if (FULL_FRONT_PERIOD == iPSEC) dblPeriodStartDate = dblEffective;

				bGenerationDone = true;
			}

			try {
				periodSecond = periodFirst;

				double dblAdjustedAccrualStartDate = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

				double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

				double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodStart);

				double dblAdjustedPayDate = DAPAdjust (dblPeriodEndDate, dapPay);

				double dblAdjustedResetDate = DAPAdjust (dblPeriodStartDate, dapReset);

				double dblAdjustedAccrualEndDate = bFinalPeriod ? dblPeriodEndDate : DAPAdjust
					(dblPeriodEndDate, dapAccrualEnd);

				if (bFinalPeriod) bFinalPeriod = false;

				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedAccrualStartDate,
						dblAdjustedAccrualEndDate, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
							org.drip.analytics.daycount.ActActDCParams (iFreq, dblAdjustedAccrualStartDate,
								dblAdjustedAccrualEndDate), strCalendar);

				if (dblAdjustedStartDate < dblAdjustedEndDate && dblAdjustedAccrualStartDate <
					dblAdjustedAccrualEndDate)
					lsCashflowPeriod.add (0, periodFirst = new org.drip.analytics.period.CashflowPeriod
						(dblAdjustedStartDate, dblAdjustedEndDate, dblAdjustedAccrualStartDate,
							dblAdjustedAccrualEndDate, dblAdjustedPayDate, dblAdjustedResetDate, iFreq,
								dblDCF, strCouponDC, bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj,
									dblMaturity, strCalendar, strCurrency));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;

			try {
				dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
					(strTenor).julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (LONG_FRONT_STUB != iPSEC || null == periodFirst || null == periodSecond) return lsCashflowPeriod;

		org.drip.analytics.period.CashflowPeriod periodMerged = MergeCashFlowPeriods (periodFirst,
			periodSecond);

		if (null == periodMerged) return lsCashflowPeriod;

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.add (0, periodMerged);

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Generation of the period list forward starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturityUnadjusted Unadjusted Maturity date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsForward (
		final double dblEffective,
		final double dblMaturityUnadjusted,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturityUnadjusted) || dblEffective >=
				dblMaturityUnadjusted|| 0 == iFreq)
			return null;

		double dblMaturity = DAPAdjust (dblMaturityUnadjusted, dapMaturity);

		boolean bFinalPeriod = false;
		double dblPeriodDays = 365.25 / iFreq;
		double dblPeriodStartDate = dblEffective;
		org.drip.analytics.period.CashflowPeriod periodFinal = null;
		double dblPeriodEndDate = dblPeriodStartDate + dblPeriodDays;
		org.drip.analytics.period.CashflowPeriod periodPenultimate = null;

		if (dblPeriodEndDate > dblMaturity) dblPeriodEndDate = dblMaturity;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

		while (!bFinalPeriod) {
			if (dblPeriodEndDate >= dblMaturity) {
				bFinalPeriod = true;
				dblPeriodEndDate = dblMaturity;
			}

			try {
				if (!bFinalPeriod) {
					double dblAdjustedAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAdjustedAccrualEnd = DAPAdjust (dblPeriodEndDate, dapAccrualEnd);

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedAccrualStart,
							dblAdjustedAccrualEnd, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAdjustedAccrualStart,
									dblAdjustedAccrualEnd), strCalendar);

					lsCashflowPeriod.add (periodPenultimate = new org.drip.analytics.period.CashflowPeriod
						(DAPAdjust (dblPeriodStartDate, dapPeriodStart), DAPAdjust (dblPeriodEndDate,
							dapPeriodEnd), dblAdjustedAccrualStart, dblAdjustedAccrualEnd, DAPAdjust
								(dblPeriodEndDate, dapPay), DAPAdjust (dblPeriodStartDate, dapReset), iFreq,
									dblDCF, strCouponDC, bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj,
										dblMaturity, strCalendar, strCurrency));
				} else {
					double dblAdjustedAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAdjustedAccrualEnd = dblPeriodEndDate;

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedAccrualStart,
							dblAdjustedAccrualEnd, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAdjustedAccrualStart,
									dblAdjustedAccrualEnd), strCalendar);

					lsCashflowPeriod.add (periodFinal = new org.drip.analytics.period.CashflowPeriod
						(DAPAdjust (dblPeriodStartDate, dapPeriodStart), dblPeriodEndDate,
							dblAdjustedAccrualStart, dblAdjustedAccrualEnd, DAPAdjust (dblPeriodEndDate,
								dapPay), DAPAdjust (dblPeriodStartDate, dapReset), iFreq, dblDCF,
									strCouponDC, bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj, dblMaturity,
										strCalendar, strCurrency));
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;
			dblPeriodStartDate = dblPeriodEndDate - dblPeriodDays;
		}

		if (LONG_BACK_STUB != iPSEC || null == periodFinal || null == periodPenultimate)
			return lsCashflowPeriod;

		org.drip.analytics.period.CashflowPeriod periodMerged = MergeCashFlowPeriods (periodFinal,
			periodPenultimate);

		if (null == periodMerged) return lsCashflowPeriod;

		lsCashflowPeriod.remove (lsCashflowPeriod.size() - 1);

		lsCashflowPeriod.remove (lsCashflowPeriod.size() - 1);

		lsCashflowPeriod.add (periodMerged);

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Regular Period List Generation
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsRegular (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
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

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

		double dblPeriodEndDate = dtPeriodEnd.julian();

		if (dblPeriodEndDate > dblMaturityDate) dblPeriodEndDate = dblMaturityDate;

		while (dblPeriodEndDate <= dblMaturityDate && bLoopOn) {
			if (dblPeriodEndDate >= dblMaturityDate) {
				bLoopOn = false;
				dblPeriodEndDate = dblMaturityDate;
			}

			double dblPeriodStartDate = dtPeriodStart.julian();

			double dblAdjustedAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

			double dblAdjustedAccrualEnd = dblPeriodEndDate == dblMaturityDate ? dblPeriodEndDate :
				DAPAdjust (dblPeriodEndDate, dapAccrualStart);

			try {
				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedAccrualStart,
						dblAdjustedAccrualEnd, strAccrualDC, bApplyAccEOMAdj, dblMaturityDate, new
							org.drip.analytics.daycount.ActActDCParams (iFreq, dblAdjustedAccrualStart,
								dblAdjustedAccrualEnd), strCalendar);

				lsCashflowPeriod.add (new org.drip.analytics.period.CashflowPeriod (DAPAdjust
					(dblPeriodStartDate, dapPeriodStart), dblPeriodEndDate, dblAdjustedAccrualStart,
						dblAdjustedAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay), DAPAdjust
							(dblPeriodStartDate, dapReset), iFreq, dblDCF, strCouponDC, bApplyCpnEOMAdj,
								strAccrualDC, bApplyAccEOMAdj, dblMaturityDate, strCalendar, strCurrency));

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

	/** Simplified Generation of the period list backward starting from the end.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bMergeLeadingPeriods - TRUE - Merge the Front 2 coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsBackward (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		return GeneratePeriodsBackward (dblEffective, dblMaturity, dap, dap, dap, dap, dap, dap, dap, dap,
			iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, iPSEC, bCouponDCFOffOfFreq,
				strCalendar, strCurrency);
	}

	/**
	 * Simplified Generation of the period list forward starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count Convention
	 * @param bApplyEOMAdj End-of-month adjustment
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsForward (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		return GeneratePeriodsForward (dblEffective, dblMaturity, dap, dap, dap, dap, dap, dap, dap, dap,
			iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, iPSEC, bCouponDCFOffOfFreq,
				strCalendar, strCurrency);
	}

	/**
	 * Simplified Generation of the regular period lists.
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count Convention
	 * @param bApplyEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 	determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GeneratePeriodsRegular (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		return GeneratePeriodsRegular (dblEffective, strMaturityTenor, dap, dap, dap, dap, dap, dap, dap,
			dap, iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, bCouponDCFOffOfFreq,
				strCalendar, strCurrency);
	}

	/**
	 * Generate a single Cash Flow period between the effective and the maturity dates
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param strDayCount Day Count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List containing the single Cash Flow period
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GenerateSinglePeriod (
		final double dblEffective,
		final double dblMaturity,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

		try {
			lsCashflowPeriod.add (0, new org.drip.analytics.period.CashflowPeriod (dblEffective, dblMaturity,
				dblEffective, dblMaturity, dblMaturity, dblEffective, 1,
					org.drip.analytics.daycount.Convention.YearFraction (dblEffective, dblMaturity,
						strDayCount, false, dblMaturity, null, strCalendar), strDayCount, false, strDayCount,
							false, dblMaturity, strCalendar, strCurrency));
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
	 * @param dapReset Reset Date Adjust Parameters
	 * @param dapPay Pay Date Adjust Parameters
	 * @param strDC Accrual/Coupon day count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CashflowPeriod> GenerateDailyPeriod (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final java.lang.String strDC,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		boolean bTerminationReached = false;
		double dblPeriodStartDate = dblEffective;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

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

				if (dblAdjustedStartDate < dblAdjustedEndDate)
					lsCashflowPeriod.add (new org.drip.analytics.period.CashflowPeriod (dblAdjustedStartDate,
						dblAdjustedEndDate, dblAdjustedStartDate, dblAdjustedEndDate, DAPAdjust
							(dblAdjustedEndDate, dapPay), DAPAdjust (dblPeriodStartDate, dapReset), 360,
								(dblAdjustedEndDate - dblAdjustedStartDate) / 360., strDC, false, strDC,
									false, dblMaturity, strCalendar, strCurrency));

				dblPeriodStartDate = dblAdjustedEndDate;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}
}
