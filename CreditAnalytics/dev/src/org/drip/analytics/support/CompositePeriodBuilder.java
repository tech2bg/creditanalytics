
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

		java.util.List<java.lang.Double> lsDate = new java.util.ArrayList<java.lang.Double>();

		lsDate.add (dtEffective.julian());

		org.drip.analytics.date.JulianDate dtPeriodEnd = dtEffective.addTenor (strTenor);

		while (dtPeriodEnd.julian() < dtMaturity.julian()) {
			lsDate.add (dtPeriodEnd.julian());

			dtPeriodEnd = dtPeriodEnd.addTenor (strTenor);
		}

		if (dtPeriodEnd.julian() > dtMaturity.julian()) {
			if (NO_ADJUSTMENT == iPSEC)
				lsDate.add (dtEffective.julian());
			else if (LONG_BACK_STUB == iPSEC) {
				lsDate.remove (lsDate.size() - 1);

				lsDate.add (dtMaturity.julian());
			}
		} else if (dtPeriodEnd.julian() == dtMaturity.julian())
			lsDate.add (dtMaturity.julian());

		int iNumDate = lsDate.size();

		if (null == dap) return lsDate;

		java.util.List<java.lang.Double> lsDateAdjusted = new java.util.ArrayList<java.lang.Double>();

		lsDateAdjusted.add (lsDate.get (0));

		for (int i = 1; i < iNumDate - 1; ++i)
			lsDateAdjusted.add (DAPAdjust (lsDate.get (i), dap));

		lsDateAdjusted.add (lsDate.get (iNumDate - 1));

		return lsDateAdjusted;
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

		java.util.List<java.lang.Double> lsDate = new java.util.ArrayList<java.lang.Double>();

		lsDate.add (dtMaturity.julian());

		org.drip.analytics.date.JulianDate dtPeriodStart = dtMaturity.subtractTenor (strTenor);

		while (dtPeriodStart.julian() > dtEffective.julian()) {
			lsDate.add (0, dtPeriodStart.julian());

			dtPeriodStart = dtPeriodStart.subtractTenor (strTenor);
		}

		if (dtPeriodStart.julian() < dtEffective.julian()) {
			if (NO_ADJUSTMENT == iPSEC)
				lsDate.add (0, dtEffective.julian());
			else if (FULL_FRONT_PERIOD == iPSEC)
				lsDate.add (0, dtPeriodStart.julian());
			else if (LONG_FRONT_STUB == iPSEC) {
				lsDate.remove (0);

				lsDate.add (0, dtEffective.julian());
			}
		} else if (dtPeriodStart.julian() == dtEffective.julian())
			lsDate.add (0, dtEffective.julian());

		int iNumDate = lsDate.size();

		if (null == dap) return lsDate;

		java.util.List<java.lang.Double> lsDateAdjusted = new java.util.ArrayList<java.lang.Double>();

		lsDateAdjusted.add (lsDate.get (0));

		for (int i = 1; i < iNumDate - 1; ++i)
			lsDateAdjusted.add (DAPAdjust (lsDate.get (i), dap));

		lsDateAdjusted.add (lsDate.get (iNumDate - 1));

		return lsDateAdjusted;
	}

	/**
	 * Generate a list of regular period edge dates forward from the start.
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity date
	 * @param strPeriodTenor Period Tenor
	 * @param strMaturityTenor Period Tenor
	 * @param dap Inner Date Adjustment Parameters
	 * 
	 * @return List of Period Edge Dates
	 */

	public static final java.util.List<java.lang.Double> RegularEdgeDates (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strPeriodTenor,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap)
	{
		if (null == dtEffective || null == dtMaturity || dtEffective.julian() >= dtMaturity.julian() || null
			== strPeriodTenor || strPeriodTenor.isEmpty() || null == strMaturityTenor ||
				strMaturityTenor.isEmpty())
			return null;

		int iPeriodTenorMonths = -1;
		int iMaturityTenorMonths = -1;

		try {
			iPeriodTenorMonths = org.drip.analytics.support.AnalyticsHelper.TenorToMonths (strPeriodTenor);

			iMaturityTenorMonths = org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(strMaturityTenor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (iPeriodTenorMonths > iMaturityTenorMonths) return null;

		java.util.List<java.lang.Double> lsDate = new java.util.ArrayList<java.lang.Double>();

		lsDate.add (dtEffective.julian());

		org.drip.analytics.date.JulianDate dtEnd = dtEffective;
		int iNumPeriod = iMaturityTenorMonths / iPeriodTenorMonths;

		for (int i = 0; i < iNumPeriod - 1; ++i) {
			dtEnd = dtEnd.addTenor (strPeriodTenor);

			lsDate.add (DAPAdjust (dtEnd.julian(), dap));
		}

		lsDate.add (dtEnd.addTenor (strPeriodTenor).julian());

		return lsDate;
	}
}
