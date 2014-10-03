
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
 * CompositePeriodBuilder exposes the composite period construction functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CompositePeriodBuilder {

	/**
	 * Period Set Generation Customization - Short Stub (i.e., No adjustment on either end)
	 */

	public static final int SHORT_STUB = 0;

	/**
	 * Period Set Generation Customization - Merge the front periods to produce a long front
	 */

	public static final int FULL_FRONT_PERIOD = 1;

	/**
	 * Period Set Generation Customization - Long Stub (if present) belongs to the front/back end depending
	 * 	upon backwards/forwards generation scheme
	 */

	public static final int LONG_STUB = 2;

	/**
	 * Reference Period Fixing is IN-ARREARS (i.e., displaced one period to the right) of the Coupon Period
	 */

	public static final int REFERENCE_PERIOD_IN_ARREARS = 0;

	/**
	 * Reference Period Fixing is IN-ADVANCE (i.e., the same as that) of the Coupon Period
	 */

	public static final int REFERENCE_PERIOD_IN_ADVANCE = 1;

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
	 * Generate a list of period edge dates forward from the start.
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity date
	 * @param strTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * @param iPSEC Period Set Edge Customizer Setting
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> ForwardEdgeDates (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iPSEC)
	{
		if (null == dtEffective || null == dtMaturity || dtEffective.julian() >= dtMaturity.julian() || null
			== strTenor || strTenor.isEmpty())
			return null;

		java.util.List<java.lang.Double> lsEdgeDate = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dtEdge = dtEffective;

		while (dtEdge.julian() < dtMaturity.julian()) {
			lsEdgeDate.add (dtEdge.julian());

			dtEdge = dtEdge.addTenor (strTenor);
		}

		if (dtEdge.julian() > dtMaturity.julian()) {
			if (SHORT_STUB == iPSEC)
				lsEdgeDate.add (dtMaturity.julian());
			else if (LONG_STUB == iPSEC) {
				lsEdgeDate.remove (lsEdgeDate.size() - 1);

				lsEdgeDate.add (dtMaturity.julian());
			}
		} else if (dtEdge.julian() == dtMaturity.julian())
			lsEdgeDate.add (dtMaturity.julian());

		if (null == dap) return lsEdgeDate;

		java.util.List<java.lang.Double> lsAdjustedEdgeDate = new java.util.ArrayList<java.lang.Double>();

		lsAdjustedEdgeDate.add (lsEdgeDate.get (0));

		int iNumDate = lsEdgeDate.size();

		for (int i = 1; i < iNumDate - 1; ++i)
			lsAdjustedEdgeDate.add (DAPAdjust (lsEdgeDate.get (i), dap));

		lsAdjustedEdgeDate.add (lsEdgeDate.get (iNumDate - 1));

		return lsAdjustedEdgeDate;
	}

	/**
	 * Generate a list of period edge dates backward from the end.
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity date
	 * @param strTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * @param iPSEC Period Set Edge Customizer Setting
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> BackwardEdgeDates (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iPSEC)
	{
		if (null == dtEffective || null == dtMaturity || dtEffective.julian() >= dtMaturity.julian() || null
			== strTenor || strTenor.isEmpty())
			return null;

		java.util.List<java.lang.Double> lsEdgeDate = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dtEdge = dtMaturity;

		while (dtEdge.julian() > dtEffective.julian()) {
			lsEdgeDate.add (0, dtEdge.julian());

			dtEdge = dtEdge.subtractTenor (strTenor);
		}

		if (dtEdge.julian() < dtEffective.julian()) {
			if (SHORT_STUB == iPSEC)
				lsEdgeDate.add (0, dtEffective.julian());
			else if (FULL_FRONT_PERIOD == iPSEC)
				lsEdgeDate.add (0, dtEdge.julian());
			else if (LONG_STUB == iPSEC) {
				lsEdgeDate.remove (0);

				lsEdgeDate.add (0, dtEffective.julian());
			}
		} else if (dtEdge.julian() == dtEffective.julian())
			lsEdgeDate.add (0, dtEffective.julian());

		if (null == dap) return lsEdgeDate;

		java.util.List<java.lang.Double> lsAdjustedEdgeDate = new java.util.ArrayList<java.lang.Double>();

		lsAdjustedEdgeDate.add (lsEdgeDate.get (0));

		int iNumDate = lsEdgeDate.size();

		for (int i = 1; i < iNumDate - 1; ++i)
			lsAdjustedEdgeDate.add (DAPAdjust (lsEdgeDate.get (i), dap));

		lsAdjustedEdgeDate.add (lsEdgeDate.get (iNumDate - 1));

		return lsAdjustedEdgeDate;
	}

	/**
	 * Generate a list of regular period edge dates forward from the start.
	 * 
	 * @param dtEffective Effective date
	 * @param strPeriodTenor Period Tenor
	 * @param strMaturityTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> RegularEdgeDates (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strPeriodTenor,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap)
	{
		if (null == dtEffective || null == strPeriodTenor || strPeriodTenor.isEmpty() || null ==
			strMaturityTenor || strMaturityTenor.isEmpty())
			return null;

		int iPeriodTenorMonth = -1;
		int iMaturityTenorMonth = -1;

		try {
			iPeriodTenorMonth = org.drip.analytics.support.AnalyticsHelper.TenorToMonths (strPeriodTenor);

			iMaturityTenorMonth = org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(strMaturityTenor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (iPeriodTenorMonth > iMaturityTenorMonth) return null;

		java.util.List<java.lang.Double> lsEdgeDate = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dtEdge = dtEffective;
		int iNumPeriod = iMaturityTenorMonth / iPeriodTenorMonth;

		lsEdgeDate.add (dtEffective.julian());

		for (int i = 0; i < iNumPeriod - 1; ++i) {
			dtEdge = dtEdge.addTenor (strPeriodTenor);

			lsEdgeDate.add (DAPAdjust (dtEdge.julian(), dap));
		}

		lsEdgeDate.add (dtEdge.addTenor (strPeriodTenor).julian());

		return lsEdgeDate;
	}

	/**
	 * Generate the List of Overnight Edge Dates between the specified dates, using the specified Calendar
	 * 
	 * @param dtStart Start Date
	 * @param dtEnd End Date
	 * @param strCalendar Calendar
	 * 
	 * @return List of Overnight Edge Dates
	 */

	public static final java.util.List<java.lang.Double> OvernightEdgeDates (
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.analytics.date.JulianDate dtEnd,
		final java.lang.String strCalendar)
	{
		if (null == dtStart || null == dtEnd || dtEnd.julian() <= dtStart.julian()) return null;

		org.drip.analytics.date.JulianDate dtEdge = dtStart;

		java.util.List<java.lang.Double> lsOvernightEdgeDate = new java.util.ArrayList<java.lang.Double>();

		while (dtEdge.julian() < dtEnd.julian()) {
			lsOvernightEdgeDate.add (dtEdge.julian());

			dtEdge = dtEdge.addBusDays (1, strCalendar);
		}

		lsOvernightEdgeDate.add (dtEnd.julian());

		return lsOvernightEdgeDate;
	}

	/**
	 * Construct a Reference Period using the Start/End Dates, Fixing DAP, Forward Label, and the Reference
	 * 	Period Arrears Type
	 * 
	 * @param dtStart Start Date
	 * @param dtEnd End Date
	 * @param dapFixing Fixing DAP
	 * @param forwardLabel Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * 
	 * @return The Reference Period
	 */

	public static final org.drip.analytics.cashflow.ReferenceIndexPeriod MakeReferencePeriod (
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.analytics.date.JulianDate dtEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapFixing,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final int iReferencePeriodArrearsType)
	{
		if (null == dtStart || null == dtEnd || null == forwardLabel) return null;

		java.lang.String strForwardTenor = forwardLabel.tenor();

		double dblStartDate = REFERENCE_PERIOD_IN_ARREARS == iReferencePeriodArrearsType ? dtStart.addTenor
			(strForwardTenor).julian() : dtStart.julian();

		try {
			return new org.drip.analytics.cashflow.ReferenceIndexPeriod (dblStartDate,
				REFERENCE_PERIOD_IN_ARREARS == iReferencePeriodArrearsType ? dtEnd.addTenor
					(strForwardTenor).julian() : dtEnd.julian(), DAPAdjust (dblStartDate, dapFixing),
						forwardLabel);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a list of period edge dates forward from the start.
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param strTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * @param iPSEC Period Set Edge Customizer Setting
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> ForwardEdgeDates (
		final double dblEffective,
		final double dblMaturity,
		final java.lang.String strTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iPSEC)
	{
		try {
			return ForwardEdgeDates (new org.drip.analytics.date.JulianDate (dblEffective), new
				org.drip.analytics.date.JulianDate (dblMaturity), strTenor, dap, iPSEC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a list of period edge dates backward from the end.
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param strTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * @param iPSEC Period Set Edge Customizer Setting
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> BackwardEdgeDates (
		final double dblEffective,
		final double dblMaturity,
		final java.lang.String strTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iPSEC)
	{
		try {
			return BackwardEdgeDates (new org.drip.analytics.date.JulianDate (dblEffective), new
				org.drip.analytics.date.JulianDate (dblMaturity), strTenor, dap, iPSEC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a list of regular period edge dates forward from the start.
	 * 
	 * @param dblEffective Effective Date
	 * @param strPeriodTenor Period Tenor
	 * @param strMaturityTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> RegularEdgeDates (
		final double dblEffective,
		final java.lang.String strPeriodTenor,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap)
	{
		try {
			return RegularEdgeDates (new org.drip.analytics.date.JulianDate (dblEffective), strPeriodTenor,
				strMaturityTenor, dap);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the List of Overnight Edge Dates between the specified dates, using the specified Calendar
	 * 
	 * @param dblStartDate Start Date
	 * @param dblEndDate End Date
	 * @param strCalendar Calendar
	 * 
	 * @return List of Overnight Edge Dates
	 */

	public static final java.util.List<java.lang.Double> OvernightEdgeDates (
		final double dblStartDate,
		final double dblEndDate,
		final java.lang.String strCalendar)
	{
		try {
			return OvernightEdgeDates (new org.drip.analytics.date.JulianDate (dblStartDate), new
				org.drip.analytics.date.JulianDate (dblEndDate), strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a Reference Period using the Start/End Dates, Fixing DAP, Forward Label, and the Reference
	 * 	Period Arrears Type
	 * 
	 * @param dblStartDate Start Date
	 * @param dblEndDate End Date
	 * @param dapFixing Fixing DAP
	 * @param forwardLabel Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * 
	 * @return The Reference Period
	 */

	public static final org.drip.analytics.cashflow.ReferenceIndexPeriod MakeReferencePeriod (
		final double dblStartDate,
		final double dblEndDate,
		final org.drip.analytics.daycount.DateAdjustParams dapFixing,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final int iReferencePeriodArrearsType)
	{
		try {
			return MakeReferencePeriod (new org.drip.analytics.date.JulianDate (dblStartDate), new
				org.drip.analytics.date.JulianDate (dblEndDate), dapFixing, forwardLabel,
					iReferencePeriodArrearsType);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct the List of Composable Floating Units from the inputs
	 * 
	 * @param lsDate The List of Dates
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param dblSpread Spread
	 * @param forwardLabel Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * @param dapFixing Fixing Date Adjust Parameters
	 * 
	 * @return The List of Composable Floating Units
	 */

	public static final java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> FloatingUnits (
		final java.util.List<java.lang.Double> lsDate,
		final org.drip.param.period.UnitCouponAccrualSetting ucas,
		final double dblSpread,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final int iReferencePeriodArrearsType,
		final org.drip.analytics.daycount.DateAdjustParams dapFixing)
	{
		if (null == lsDate || null == forwardLabel) return null;

		int iNumDate = lsDate.size();

		if (2 > iNumDate) return null;

		java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP = new
			java.util.ArrayList<org.drip.analytics.cashflow.ComposableUnitPeriod>();

		for (int i = 1; i < iNumDate; ++i) {
			double dblStartDate = lsDate.get (i - 1);

			double dblEndDate = lsDate.get (i);

			try {
				lsCUP.add (new org.drip.analytics.cashflow.ComposableUnitFloatingPeriod (dblStartDate,
					dblEndDate, ucas, MakeReferencePeriod (dblStartDate, dblEndDate, dapFixing, forwardLabel,
						iReferencePeriodArrearsType), dblSpread));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCUP;
	}

	/**
	 * Construct the List of Composite Fixed Period made from single composable Fixed Period Units
	 * 
	 * @param lsDate The Period Edge Dates
	 * @param iCompositeFreq The Composite Frequency
	 * @param strPayCurrency Pay Currency
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param cufs Composable Unit Fixed Setting
	 * @param dblBaseNotional Period Base Notional
	 * @param notlSchedule Period Notional Schedule
	 * @param creditLabel Period Credit Label
	 * @param dblFXFixingDate Period FX Fixing Date
	 * @param dapPay Pay Date Adjust Parameters
	 * 
	 * @return List of Composite Fixed Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.CompositePeriod>
		FixedCompositeSingleUnit (
			final java.util.List<java.lang.Double> lsDate,
			final java.lang.String strPayCurrency,
			final org.drip.param.period.UnitCouponAccrualSetting ucas,
			final org.drip.param.period.ComposableFixedUnitSetting cufs,
			final double dblBaseNotional,
			final org.drip.product.params.FactorSchedule notlSchedule,
			final org.drip.state.identifier.CreditLabel creditLabel,
			final double dblFXFixingDate,
			final org.drip.analytics.daycount.DateAdjustParams dapPay)
	{
		if (null == lsDate || null == ucas) return null;

		int iNumDate = lsDate.size();

		if (2 > iNumDate) return null;

		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCFP = new
			java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

		for (int i = 1; i < iNumDate; ++i) {
			double dblEndDate = lsDate.get (i);

			java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP = new
				java.util.ArrayList<org.drip.analytics.cashflow.ComposableUnitPeriod>();

			try {
				lsCUP.add (new org.drip.analytics.cashflow.ComposableUnitFixedPeriod (lsDate.get (i - 1),
					dblEndDate, ucas, cufs));

				lsCFP.add (new org.drip.analytics.cashflow.CompositeFixedPeriod (lsCUP, ucas.freq(),
					DAPAdjust (dblEndDate, dapPay), strPayCurrency,
						org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
							dblBaseNotional, notlSchedule, creditLabel, dblFXFixingDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCFP;
	}

	/**
	 * Construct the List of Composite Floating Period made from single composable Floating Period Units
	 * 
	 * @param lsDate The Period Edge Dates
	 * @param strPayCurrency Pay Currency
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param dblSpread Spread
	 * @param dblBaseNotional Period Base Notional
	 * @param notlSchedule Period Notional Schedule
	 * @param creditLabel Period Credit Label
	 * @param forwardLabel Period Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * @param dblFXFixingDate Period FX Fixing Date
	 * @param dapPay Pay Date Adjust Parameters
	 * @param dapFixing Fixing Date Adjust Parameters
	 * 
	 * @return List of Composite Floating Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.CompositePeriod>
		FloatingCompositeSingleUnit (
			final java.util.List<java.lang.Double> lsDate,
			final java.lang.String strPayCurrency,
			final org.drip.param.period.UnitCouponAccrualSetting ucas,
			final double dblSpread,
			final double dblBaseNotional,
			final org.drip.product.params.FactorSchedule notlSchedule,
			final org.drip.state.identifier.CreditLabel creditLabel,
			final org.drip.state.identifier.ForwardLabel forwardLabel,
			final int iReferencePeriodArrearsType,
			final double dblFXFixingDate,
			final org.drip.analytics.daycount.DateAdjustParams dapPay,
			final org.drip.analytics.daycount.DateAdjustParams dapFixing)
	{
		if (null == lsDate || null == ucas) return null;

		int iNumDate = lsDate.size();

		if (2 > iNumDate) return null;

		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCFP = new
			java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

		for (int i = 1; i < iNumDate; ++i) {
			double dblStartDate = lsDate.get (i - 1);

			double dblEndDate = lsDate.get (i);

			java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP = new
				java.util.ArrayList<org.drip.analytics.cashflow.ComposableUnitPeriod>();

			try {
				lsCUP.add (new org.drip.analytics.cashflow.ComposableUnitFloatingPeriod (dblStartDate,
					dblEndDate, ucas, new org.drip.analytics.cashflow.ReferenceIndexPeriod (dblStartDate,
						dblEndDate, DAPAdjust (dblStartDate, dapFixing), forwardLabel), dblSpread));

				lsCFP.add (new org.drip.analytics.cashflow.CompositeFloatingPeriod (lsCUP, ucas.freq(),
					DAPAdjust (dblEndDate, dapPay), strPayCurrency,
						org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
							dblBaseNotional, notlSchedule, creditLabel, dblFXFixingDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCFP;
	}

	/**
	 * Construct the List of Composite Floating Period made from Multiple composable Floating Period Units
	 * 
	 * @param lsDate The Period Edge Dates
	 * @param iCompositeFreq The Composite Frequency
	 * @param strPayCurrency Pay Currency
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param strCalendar Calendar
	 * @param dblSpread Spread
	 * @param dblBaseNotional Period Base Notional
	 * @param notlSchedule Period Notional Schedule
	 * @param creditLabel Period Credit Label
	 * @param forwardLabel Period Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * @param dblFXFixingDate Period FX Fixing Date
	 * @param strCompositeTenor The Composite/"Outer" Period Tenor
	 * @param strUnitTenor The Unit/"Inner" Period Tenor
	 * @param dapPay Pay Date Adjust Parameters
	 * @param dapUnit Unit Date Adjust Parameters
	 * @param dapFixing Fixing Date Adjust Parameters
	 * 
	 * @return List of Composite Floating Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.CompositePeriod>
		FloatingCompositeMultiUnit (
			final java.util.List<java.lang.Double> lsDate,
			final int iCompositeFreq,
			final java.lang.String strPayCurrency,
			final org.drip.param.period.UnitCouponAccrualSetting ucas,
			final int iAccrualCompoundingRule,
			final double dblSpread,
			final double dblBaseNotional,
			final org.drip.product.params.FactorSchedule notlSchedule,
			final org.drip.state.identifier.CreditLabel creditLabel,
			final org.drip.state.identifier.ForwardLabel forwardLabel,
			final int iReferencePeriodArrearsType,
			final double dblFXFixingDate,
			final java.lang.String strCompositeTenor,
			final java.lang.String strUnitTenor,
			final org.drip.analytics.daycount.DateAdjustParams dapPay,
			final org.drip.analytics.daycount.DateAdjustParams dapUnit,
			final org.drip.analytics.daycount.DateAdjustParams dapFixing)
	{
		if (null == lsDate) return null;

		int iNumDate = lsDate.size();

		if (2 > iNumDate) return null;

		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCFP = new
			java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

		for (int i = 1; i < iNumDate; ++i) {
			double dblStartDate = lsDate.get (i - 1);

			double dblEndDate = lsDate.get (i);

			try {
				java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP = FloatingUnits
					(RegularEdgeDates (dblStartDate, strUnitTenor, strCompositeTenor, dapUnit), ucas,
						dblSpread, forwardLabel, iReferencePeriodArrearsType, dapFixing);

				lsCFP.add (new org.drip.analytics.cashflow.CompositeFloatingPeriod (lsCUP, iCompositeFreq,
					DAPAdjust (dblEndDate, dapPay), strPayCurrency, iAccrualCompoundingRule, dblBaseNotional,
						notlSchedule, creditLabel, dblFXFixingDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCFP;
	}

	/**
	 * Construct the List of Composite Floating Period made from Daily composable Floating Period Units
	 * 
	 * @param lsDate The Period Edge Dates
	 * @param iCompositeFreq The Composite Frequency
	 * @param strPayCurrency Pay Currency
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblSpread Spread
	 * @param dblBaseNotional Period Base Notional
	 * @param notlSchedule Period Notional Schedule
	 * @param creditLabel Period Credit Label
	 * @param forwardLabel Period Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * @param dblFXFixingDate Period FX Fixing Date
	 * @param dapPay Pay Date Adjust Parameters
	 * @param dapFixing Fixing Date Adjust Parameters
	 * 
	 * @return List of Composite Floating Periods
	 */

	public static final java.util.List<org.drip.analytics.cashflow.CompositePeriod>
		FloatingCompositeDailyUnit (
			final java.util.List<java.lang.Double> lsDate,
			final int iCompositeFreq,
			final java.lang.String strPayCurrency,
			final org.drip.param.period.UnitCouponAccrualSetting ucas,
			final int iAccrualCompoundingRule,
			final double dblSpread,
			final double dblBaseNotional,
			final org.drip.product.params.FactorSchedule notlSchedule,
			final org.drip.state.identifier.CreditLabel creditLabel,
			final org.drip.state.identifier.ForwardLabel forwardLabel,
			final int iReferencePeriodArrearsType,
			final double dblFXFixingDate,
			final org.drip.analytics.daycount.DateAdjustParams dapPay,
			final org.drip.analytics.daycount.DateAdjustParams dapFixing)
	{
		if (null == lsDate || null == ucas) return null;

		int iNumDate = lsDate.size();

		if (2 > iNumDate) return null;

		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCFP = new
			java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

		for (int i = 1; i < iNumDate; ++i) {
			double dblStartDate = lsDate.get (i - 1);

			double dblEndDate = lsDate.get (i);

			try {
				java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP = FloatingUnits
					(OvernightEdgeDates (dblStartDate, dblEndDate, ucas.calendar()), ucas, dblSpread,
						forwardLabel, iReferencePeriodArrearsType, dapFixing);

				lsCFP.add (new org.drip.analytics.cashflow.CompositeFloatingPeriod (lsCUP, iCompositeFreq,
					DAPAdjust (dblEndDate, dapPay), strPayCurrency, iAccrualCompoundingRule, dblBaseNotional,
						notlSchedule, creditLabel, dblFXFixingDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCFP;
	}
}
