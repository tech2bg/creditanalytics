
package org.drip.analytics.period;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * CashflowPeriod extends the period class with the cash-flow specific fields. It exposes the following
 * 	functionality:
 * 
 * 	- Frequency, reset date, and accrual day-count convention
 * 	- Static methods to construct cash-flow period sets starting backwards/forwards, generate single period
 * 	 sets, as well as merge cash-flow periods.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CashflowPeriod extends Period {
	private static final boolean s_bLog = false;

	private int _iFreq = 2;
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCalendar = "";
	private double _dblReset = java.lang.Double.NaN;
	private java.lang.String _strCouponDC = "30/360";
	private java.lang.String _strAccrualDC = "30/360";
	private double _dblMaturity = java.lang.Double.NaN;

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

	public static final CashflowPeriod MergeCashFlowPeriods (
		final CashflowPeriod periodLeft,
		final CashflowPeriod periodRight)
	{
		if (null == periodLeft || null == periodRight || periodLeft._dblEnd != periodRight._dblStart)
			return null;

		try {
			double dblLeftDCF = org.drip.analytics.daycount.Convention.YearFraction
				(periodLeft._dblAccrualStart, periodLeft._dblAccrualEnd, periodLeft._strAccrualDC,
					periodLeft._bApplyAccEOMAdj, periodLeft._dblMaturity, null, periodLeft._strCalendar);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftDCF)) return null;

			return new CashflowPeriod (periodLeft._dblStart, periodRight._dblEnd,
				periodLeft._dblAccrualStart, periodRight._dblAccrualEnd, periodRight._dblPay,
					periodLeft._dblReset, periodRight._iFreq, dblLeftDCF + 1. / periodRight._iFreq,
						periodRight._strCouponDC, periodRight._bApplyCpnEOMAdj, periodRight._strAccrualDC,
							periodRight._bApplyAccEOMAdj, periodRight._dblMaturity,
								periodRight._strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Generate the period list backward starting from the end.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturityIn Maturity date
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
	 * @param bFullStub TRUE - generates full first stub
	 * @param bMergeLeadingPeriods - TRUE - Merge the Front 2 coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<CashflowPeriod> GeneratePeriodsBackward (
		final double dblEffective,
		final double dblMaturityIn,
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
		final boolean bFullStub,
		final boolean bMergeLeadingPeriods,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturityIn) || dblEffective >= dblMaturityIn || 0 ==
				iFreq)
			return null;

		double dblMaturity = DAPAdjust (dblMaturityIn, dapMaturity);

		boolean bFinalPeriod = true;
		boolean bGenerationDone = false;
		CashflowPeriod periodFirst = null;
		CashflowPeriod periodSecond = null;
		double dblPeriodEndDate = dblMaturity;
		java.lang.String strTenor = (12 / iFreq) + "M";
		double dblPeriodStartDate = java.lang.Double.NaN;

		try {
			dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
				(strTenor).getJulian();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<CashflowPeriod> lsCashflowPeriod = new java.util.ArrayList<CashflowPeriod>();

		while (!bGenerationDone) {
			if (dblPeriodStartDate <= dblEffective) {
				if (!bFullStub) dblPeriodStartDate = dblEffective;

				bGenerationDone = true;
			}

			try {
				periodSecond = periodFirst;

				if (bFinalPeriod) {
					double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAccrualEnd = dblPeriodEndDate;

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
							strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
									dblAccrualEnd), strCalendar);

					lsCashflowPeriod.add (0, periodFirst = new CashflowPeriod (DAPAdjust (dblPeriodStartDate,
						dapPeriodStart), dblPeriodEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust
							(dblPeriodEndDate, dapPay), DAPAdjust (dblPeriodStartDate, dapReset), iFreq,
								dblDCF, strCouponDC, bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj,
									dblMaturity, strCalendar));

					bFinalPeriod = false;
				} else {
					double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAccrualEnd = DAPAdjust (dblPeriodEndDate, dapAccrualEnd);

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
							strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
									dblAccrualEnd), strCalendar);

					lsCashflowPeriod.add (0, periodFirst = new CashflowPeriod (DAPAdjust (dblPeriodStartDate,
						dapPeriodStart), DAPAdjust (dblPeriodEndDate, dapPeriodEnd), dblAccrualStart,
							dblAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay), DAPAdjust
								(dblPeriodStartDate, dapReset), iFreq, dblDCF, strCouponDC, bApplyCpnEOMAdj,
									strAccrualDC, bApplyAccEOMAdj, dblMaturity, strCalendar));
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;

			try {
				dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
					(strTenor).getJulian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!bMergeLeadingPeriods || null == periodFirst || null == periodSecond) return lsCashflowPeriod;

		CashflowPeriod periodMerged = MergeCashFlowPeriods (periodFirst, periodSecond);

		if (null == periodMerged) return lsCashflowPeriod;

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.add (0, periodMerged);

		return lsCashflowPeriod;
	}

	/**
	 * Generate the period list forward starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
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
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<CashflowPeriod> GeneratePeriodsForward (
		final double dblEffective,
		final double dblMaturity,
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
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity || 0 ==
				iFreq)
			return null;

		boolean bFinalPeriod = false;
		double dblPeriodDays = 365.25 / iFreq;
		double dblPeriodStartDate = dblEffective;
		double dblPeriodEndDate = dblPeriodStartDate + dblPeriodDays;

		java.util.List<CashflowPeriod> lsCashflowPeriod = new java.util.ArrayList<CashflowPeriod>();

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

					lsCashflowPeriod.add (0, new CashflowPeriod (DAPAdjust (dblPeriodStartDate,
						dapPeriodStart), DAPAdjust (dblPeriodEndDate, dapPeriodEnd), dblAdjustedAccrualStart,
							dblAdjustedAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay), DAPAdjust
								(dblPeriodStartDate, dapReset), iFreq, dblDCF, strCouponDC,
									bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj, dblMaturity,
										strCalendar));
				} else {
					double dblAdjustedAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAdjustedAccrualEnd = dblPeriodEndDate;

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedAccrualStart,
							dblAdjustedAccrualEnd, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAdjustedAccrualStart,
									dblAdjustedAccrualEnd), strCalendar);

					lsCashflowPeriod.add (0, new CashflowPeriod (DAPAdjust (dblPeriodStartDate,
						dapPeriodStart), dblPeriodEndDate, dblAdjustedAccrualStart, dblAdjustedAccrualEnd,
							DAPAdjust (dblPeriodEndDate, dapPay), DAPAdjust (dblPeriodStartDate, dapReset),
								iFreq, dblDCF, strCouponDC, bApplyCpnEOMAdj, strAccrualDC, bApplyAccEOMAdj,
									dblMaturity, strCalendar));
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;
			dblPeriodStartDate = dblPeriodEndDate - dblPeriodDays;
		}

		return lsCashflowPeriod;
	}

	/**
	 * Generate a single Cash Flow period between the effective and the maturity dates
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * 
	 * @return List containing the single Cash Flow period
	 */

	public static final java.util.List<CashflowPeriod> GetSinglePeriod (
		final double dblEffective,
		final double dblMaturity,
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		java.util.List<CashflowPeriod> lsCashflowPeriod = new java.util.ArrayList<CashflowPeriod>();

		try {
			lsCashflowPeriod.add (0, new CashflowPeriod (dblEffective, dblMaturity, dblEffective,
				dblMaturity, dblMaturity, dblEffective, 2, 0.5, "30/360", true, "30/360", true, dblMaturity,
					strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return lsCashflowPeriod;
	}

	/**
	 * Construct a CashflowPeriod instance from the specified dates
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblAccrualStart Period Accrual Start Date
	 * @param dblAccrualEnd Period Accrual End Date
	 * @param dblPay Period Pay Date
	 * @param dblReset Period Reset Date
	 * @param iFreq Frequency
	 * @param dblDCF Full Period Day Count Fraction
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual Day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param dblMaturity Maturity date
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CashflowPeriod (
		final double dblStart,
		final double dblEnd,
		final double dblAccrualStart,
		final double dblAccrualEnd,
		final double dblPay,
		final double dblReset,
		final int iFreq,
		final double dblDCF,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final double dblMaturity,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		super (dblStart, dblEnd, dblAccrualStart, dblAccrualEnd, dblPay, dblDCF);

		if (s_bLog)
			System.out.println (org.drip.analytics.date.JulianDate.fromJulian (dblStart) + "=>" +
				org.drip.analytics.date.JulianDate.fromJulian (dblEnd) + " | " +
					org.drip.analytics.date.JulianDate.fromJulian (dblPay));

		_iFreq = iFreq;
		_dblReset = dblReset;
		_dblMaturity= dblMaturity;
		_strCalendar = strCalendar;
		_strCouponDC = strCouponDC;
		_strAccrualDC = strAccrualDC;
		_bApplyAccEOMAdj = bApplyAccEOMAdj;
		_bApplyCpnEOMAdj = bApplyCpnEOMAdj;
	}

	/**
	 * De-serialization of CashflowPeriod from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize CashflowPeriod
	 */

	public CashflowPeriod (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (ab);
	}

	@Override public double getResetDate()
	{
		return _dblReset;
	}

	@Override public double getAccrualDCF (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEnd))
			throw new java.lang.Exception ("CashflowPeriod::getAccrualDCF => Accrual end is NaN!");

		if (_dblAccrualStart > dblAccrualEnd && dblAccrualEnd > _dblAccrualEnd)
			throw new java.lang.Exception
				("CashflowPeriod::getAccrualDCF => Invalid in-period accrual date!");

		return org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStart, dblAccrualEnd,
			_strAccrualDC, _bApplyAccEOMAdj, _dblMaturity, new org.drip.analytics.daycount.ActActDCParams
				(_iFreq, _dblAccrualStart, _dblAccrualEnd), _strCalendar);
	}
}
