
package org.drip.analytics.daycount;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * This class contains flags that indicate where the holidays are loaded from, as well as the holiday types
 * 	and load rules. It exports the following date related functionality:
 *  - Add business days according to the specified calendar
 *  - The Year Fraction between any 2 days given the day count type and the holiday calendar
 *  - Adjust/roll to the next working day according to the adjustment rule
 *  - Holiday Functions - is the given day a holiday/business day, the number and the set of
 *  	holidays/business days between 2 days.
 *  - Calendars and Day counts - Available set of day count conventions and calendars, and the weekend days
 *  	corresponding to a given calendar.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Convention {

	/**
	 * Date Roll Actual
	 */

	public static final int DATE_ROLL_ACTUAL = 0;

	/**
	 * Date Roll Following
	 */

	public static final int DATE_ROLL_FOLLOWING = 1;

	/**
	 * Date Roll Modified Following
	 */

	public static final int DATE_ROLL_MODIFIED_FOLLOWING = 2;

	/**
	 * Date Roll Modified Following Bi-monthly
	 */

	public static final int DATE_ROLL_MODIFIED_FOLLOWING_BIMONTHLY = 4;

	/**
	 * Date Roll Previous
	 */

	public static final int DATE_ROLL_PREVIOUS = 8;

	/**
	 * Date Roll Modified Previous
	 */

	public static final int DATE_ROLL_MODIFIED_PREVIOUS = 16;

	/**
	 * Week Day Holiday
	 */

	public static final int WEEKDAY_HOLS = 1;

	/**
	 * Week End Holiday
	 */

	public static final int WEEKEND_HOLS = 2;

	private static final int INIT_FROM_HOLS_DB = 1;
	private static final int INIT_FROM_HOLS_XML = 2;
	private static final int INIT_FROM_HOLS_SOURCE = 4;

	private static int s_iInitHols = INIT_FROM_HOLS_SOURCE;
	private static org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.eventday.Locale>
		s_mapLocHols = null;

	private static
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.daycount.DCFCalculator>
			s_mapDCCalc = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.daycount.DCFCalculator>();

	private static final boolean UpdateDCCalcMap (
		final org.drip.analytics.daycount.DCFCalculator dcfCalc)
	{
		for (java.lang.String strDC : dcfCalc.alternateNames())
			s_mapDCCalc.put (strDC, dcfCalc);

		return true;
	}

	private static final boolean SetDCCalc()
	{
		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC1_1())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC28_360())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30_360())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30_365())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30_Act())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30E_360())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30E_360_ISDA())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DC30EPLUS_360_ISDA())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_360())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_364())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_365())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_365L())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_Act())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_Act_ISDA())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCAct_Act_UST())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCNL_360())) return false;

		if (!UpdateDCCalcMap (new org.drip.analytics.daycount.DCNL_365())) return false;

		return UpdateDCCalcMap (new org.drip.analytics.daycount.DCNL_Act());
	}

	private static final boolean AddLH (
		final org.drip.analytics.holset.LocationHoliday lh,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.eventday.Locale> mapHols)
	{
		if (null == lh || null == mapHols) return false;

		java.lang.String strLocation = lh.getHolidayLoc();

		org.drip.analytics.eventday.Locale locHols = lh.getHolidaySet();

		if (null == locHols || null == strLocation || strLocation.isEmpty()) return false;

		mapHols.put (strLocation, locHols);

		return true;
	}

	private static final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.eventday.Locale>
		SetHolsFromSource()
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.eventday.Locale> mapHols = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.eventday.Locale>();

		AddLH (new org.drip.analytics.holset.AEDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ANGHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ARAHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ARFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ARNHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ARPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ARSHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ATSHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.AUDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.AZMHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BAKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BBDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BEFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BGLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BHDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BMDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BRCHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BRLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.BSDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CADHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CAEHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CERHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CFFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CHFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CLFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CLUHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CNYHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.COFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CONHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.COPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CRCHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CYPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.CZKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.DEMHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.DKKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.DOPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.DTFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ECSHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.EEKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.EGPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ESBHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ESPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ESTHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.EUBHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.EURHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.GBPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.GELHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.GFRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.GRDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.HKDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.HRKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.HUFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.IBRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.IDRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.IEPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.IGPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ILSHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.INRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.IPCHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ITLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.JMDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.JPYHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.KPWHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.KRWHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.KWDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.KYDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.KZTHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.LKRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.LTLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.LUFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.LUXHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.LVLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MDLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MIXHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MKDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MXCHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MXNHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MXPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MXVHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.MYRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.NLGHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.NOKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.NZDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PABHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PEFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PENHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PESHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PHPHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PLNHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PLZHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.PTEHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.QEFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.RUBHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.RURHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SARHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SEKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SGDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SITHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SKKHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.SVCHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.TABHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.TGTHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.THBHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.TRLHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.TRYHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.TWDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.UAHHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.USDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.USVHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.UVRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.UYUHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.UYUHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.VACHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.VEBHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.VEFHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.VNDHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.XDRHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.XEUHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ZALHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ZARHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ZUSHoliday(), mapHols);

		AddLH (new org.drip.analytics.holset.ZWDHoliday(), mapHols);

		return mapHols;
	}

	private static final boolean LocationHoliday (
		final java.lang.String strCalendarSet,
		final double dblDate,
		final int iHolType)
	{
		if (null == strCalendarSet || strCalendarSet.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate))
			return false;

		java.lang.String[] astrCalendars = strCalendarSet.split (",");

		for (java.lang.String strCalendar : astrCalendars) {
			if (null != strCalendar && null != s_mapLocHols.get (strCalendar)) {
				org.drip.analytics.eventday.Locale lh = s_mapLocHols.get (strCalendar);

				if (null == lh) continue;

				if (0 != (WEEKEND_HOLS & iHolType) && null != lh.weekendDays() && lh.weekendDays().isWeekend
					(dblDate))
					return true;

				if (null == lh.holidays() || 0 == (WEEKDAY_HOLS & iHolType)) continue;

				for (org.drip.analytics.eventday.Base hol : lh.holidays()) {
					try {
						if (null != hol && (int) dblDate == (int) hol.dateInYear
							(org.drip.analytics.date.DateUtil.Year (dblDate), true))
							return true;
					} catch (java.lang.Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return false;
	}

	/**
	 * Initialize the day count basis object from the calendar set
	 * 
	 * @param strCalendarSetLoc The calendar set
	 * 
	 * @return Success (true) Failure (false)
	 */

	public static final boolean Init (
		final java.lang.String strCalendarSetLoc)
	{
		if (!SetDCCalc()) return false;

		if (INIT_FROM_HOLS_SOURCE == s_iInitHols) {
			if (null == (s_mapLocHols = SetHolsFromSource())) return false;

			return true;
		}

		try {
			if (INIT_FROM_HOLS_XML == s_iInitHols)
				s_mapLocHols = org.drip.param.config.ConfigLoader.LoadHolidayCalendars
					(strCalendarSetLoc);
			else if (INIT_FROM_HOLS_DB == s_iInitHols)
				s_mapLocHols = org.drip.param.config.ConfigLoader.LoadHolidayCalendarsFromDB
					(strCalendarSetLoc);

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (null == s_mapLocHols && null == (s_mapLocHols = SetHolsFromSource()))  return false;

		return false;
	}

	/**
	 * Retrieve the set of holiday locations
	 * 
	 * @return Set of holiday locations
	 */

	public static final java.util.Set<java.lang.String> HolidayLocations()
	{
		return s_mapLocHols.keySet();
	}

	/**
	 * Get the week end days for the given holiday calendar set
	 * 
	 * @param strCalendarSet Holiday calendar set
	 * 
	 * @return Array of days indicating the week day union
	 */

	public static final int[] WeekendDays (
		final java.lang.String strCalendarSet)
	{
		if (null == strCalendarSet || strCalendarSet.isEmpty()) return null;

		java.lang.String[] astrCalendars = strCalendarSet.split (",");

		java.util.Set<java.lang.Integer> si = new java.util.HashSet<java.lang.Integer>();

		for (java.lang.String strCalendar : astrCalendars) {
			if (null != strCalendar && null != s_mapLocHols.get (strCalendar)) {
				org.drip.analytics.eventday.Locale lh = s_mapLocHols.get (strCalendar);

				if (null == lh || null == lh.weekendDays() || null == lh.weekendDays().days()) continue;

				for (int i : lh.weekendDays().days())
					si.add (i);
			}
		}

		int j = 0;

		int[] aiWkend = new int[si.size()];

		for (int iHol : si)
			aiWkend[j++] = iHol;

		return aiWkend;
	}

	/**
	 * Get all available DRIP day count conventions
	 * 
	 * @return Available DRIP day count conventions
	 */

	public static final java.lang.String AvailableDC()
	{
		java.lang.StringBuffer sbDCSet = new java.lang.StringBuffer();

		for (java.lang.String strDC : s_mapDCCalc.keySet())
			sbDCSet.append (strDC + " | ");

		return sbDCSet.toString();
	}

	/**
	 * Calculate the accrual fraction in years between 2 given days for the given day count convention and
	 * 	the other parameters
	 * 
	 * @param dblStart Start Date
	 * @param dblEnd End Date
	 * @param strDayCount Day count convention
	 * @param bApplyEOMAdj Apply end-of-month adjustment (true)
	 * @param actactParams ActActParams
	 * @param strCalendar Holiday Calendar
	 * 
	 * @return Accrual Fraction in years
	 * 
	 * @throws java.lang.Exception Thrown if the accrual fraction cannot be calculated
	 */

	public static final double YearFraction (
		final double dblStart,
		final double dblEnd,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if ("BUS252".equalsIgnoreCase (strDayCount) || "BUS DAYS252".equalsIgnoreCase (strDayCount) ||
			"BUS/252".equalsIgnoreCase (strDayCount))
			return BusinessDays (dblStart, dblEnd, strCalendar) / 252.;

		org.drip.analytics.daycount.DCFCalculator dfcCalc = s_mapDCCalc.get (strDayCount);

		if (null != dfcCalc)
			return dfcCalc.yearFraction (dblStart, dblEnd, bApplyEOMAdj, actactParams, strCalendar);

		System.out.println ("Convention::YearFraction => Unknown DC: " + strDayCount +
			"; defaulting to Actual/365.25");

		return (dblEnd - dblStart) / 365.25;
	}

	/**
	 * Calculate the days accrued between 2 given days for the given day count convention and the other
	 *  parameters
	 * 
	 * @param dblStart Start Date
	 * @param dblEnd End Date
	 * @param strDayCount Day count convention
	 * @param bApplyEOMAdj Apply end-of-month adjustment (true)
	 * @param actactParams ActActParams
	 * @param strCalendar Holiday Calendar
	 * 
	 * @return Number of Days Accrued
	 * 
	 * @throws java.lang.Exception Thrown if the accrual fraction cannot be calculated
	 */

	public static final int DaysAccrued (
		final double dblStart,
		final double dblEnd,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if ("BUS252".equalsIgnoreCase (strDayCount) || "BUS DAYS252".equalsIgnoreCase (strDayCount) ||
			"BUS/252".equalsIgnoreCase (strDayCount))
			return BusinessDays (dblStart, dblEnd, strCalendar);

		org.drip.analytics.daycount.DCFCalculator dfcCalc = s_mapDCCalc.get (strDayCount);

		if (null != dfcCalc)
			return dfcCalc.daysAccrued (dblStart, dblEnd, bApplyEOMAdj, actactParams, strCalendar);

		return (int) (dblEnd - dblStart);
	}

	/**
	 * Roll the given date in accordance with the roll mode and the calendar set
	 * 
	 * @param dblDate Date to be rolled
	 * @param iRollMode Roll Mode (one of DR_ACT, DR_FOLL, DR_MOD_FOLL, DR_PREV, or DR_MOD_PREV)
	 * @param strCalendarSet Calendar Set to calculate the holidays by
	 * @param iNumDaysToRoll The Number of Days to Roll
	 * 
	 * @return The Rolled Date
	 * 
	 * @throws java.lang.Exception Thrown if the date cannot be rolled
	 */

	public static final double RollDate (
		final double dblDate,
		final int iRollMode,
		final java.lang.String strCalendarSet,
		int iNumDaysToRoll)
		throws java.lang.Exception
	{
		if (0 > iNumDaysToRoll || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Convention::RollDate => Invalid Inputs");

		if (null == strCalendarSet || strCalendarSet.isEmpty() || DATE_ROLL_ACTUAL == iRollMode)
			return dblDate + iNumDaysToRoll;

		double dblRolledDate = dblDate;

		while (0 != iNumDaysToRoll) {
			if (DATE_ROLL_FOLLOWING == iRollMode || DATE_ROLL_MODIFIED_FOLLOWING == iRollMode ||
				DATE_ROLL_MODIFIED_FOLLOWING_BIMONTHLY == iRollMode) {
				while (IsHoliday (dblRolledDate, strCalendarSet))
					++dblRolledDate;
			}

			if (DATE_ROLL_PREVIOUS == iRollMode || DATE_ROLL_MODIFIED_PREVIOUS == iRollMode) {
				while (IsHoliday (dblRolledDate, strCalendarSet))
					--dblRolledDate;
			}

			--iNumDaysToRoll;
		}

		if (DATE_ROLL_MODIFIED_FOLLOWING == iRollMode) {
			if (org.drip.analytics.date.DateUtil.Month (dblDate) != org.drip.analytics.date.DateUtil.Month
				(dblRolledDate)) {
				while (IsHoliday (dblRolledDate, strCalendarSet))
					--dblRolledDate;
			}
		}

		if (DATE_ROLL_MODIFIED_FOLLOWING_BIMONTHLY == iRollMode) {
			int iOriginalDay = org.drip.analytics.date.DateUtil.Day (dblDate);

			int iRolledDay = org.drip.analytics.date.DateUtil.Day (dblRolledDate);

			if ((15 < iOriginalDay && 15 > iRolledDay) || (15 > iOriginalDay && 15 < iRolledDay)) {
				while (IsHoliday (dblRolledDate, strCalendarSet))
					--dblRolledDate;
			}
		}

		if (DATE_ROLL_MODIFIED_PREVIOUS == iRollMode) {
			if (org.drip.analytics.date.DateUtil.Month (dblDate) != org.drip.analytics.date.DateUtil.Month
				(dblRolledDate)) {
				while (IsHoliday (dblRolledDate, strCalendarSet))
					++dblRolledDate;
			}
		}

		return dblRolledDate;
	}

	/**
	 * Indicate whether the given date is a holiday in the specified location(s)
	 * 
	 * @param dblDate Date
	 * @param strCalendar Location Calendar set
	 * @param iHolType WEEKDAY_HOLS or WEEKEND_HOLS
	 * 
	 * @return True (it is a holiday) or false
	 * 
	 * @throws java.lang.Exception Thrown if it cannot be evaluated
	 */

	public static final boolean IsHoliday (
		final double dblDate,
		final java.lang.String strCalendar,
		final int iHolType)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Convention::IsHoliday => Cannot a NaN date for holiday!");

		return LocationHoliday ((null == strCalendar || strCalendar.isEmpty() || "".equalsIgnoreCase
			(strCalendar)) ? "USD" : strCalendar, dblDate, iHolType);
	}

	/**
	 * Indicates whether the given date is a holiday in the specified location(s)
	 * 
	 * @param dblDate Date
	 * @param strCalendar Location Calendar set
	 * 
	 * @return True (it is a holiday) or false
	 * 
	 * @throws java.lang.Exception Thrown if it cannot be evaluated
	 */

	public static final boolean IsHoliday (
		final double dblDate,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		return IsHoliday (dblDate, strCalendar, WEEKDAY_HOLS | WEEKEND_HOLS);
	}

	/**
	 * Calculate the number of business days between the start and the end dates
	 * 
	 * @param dblStart Start Date
	 * @param dblFinish End Date
	 * @param strCalendar Holiday Calendar set
	 * 
	 * @return The number of business days
	 * 
	 * @throws java.lang.Exception Thrown if it cannot be evaluated
	 */

	public static final int BusinessDays (
		final double dblStart,
		final double dblFinish,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblFinish) || dblStart > dblFinish)
			throw new java.lang.Exception ("Convention::BusinessDays => Invalid Inputs");

		if (dblStart == dblFinish) return 0;

		int iNumBusDays = 0;
		double dblDate = dblStart + 1;

		while (dblDate <= dblFinish) {
			if (!IsHoliday (dblDate, strCalendar)) ++iNumBusDays;

			++dblDate;
		}

		return iNumBusDays;
	}

	/**
	 * Calculate the set of holidays between the start and the end dates
	 * 
	 * @param dblStart Start Date
	 * @param dblFinish End Date
	 * @param strCalendar Holiday Calendar set
	 * 
	 * @return The set of holidays
	 * 
	 * @throws java.lang.Exception Thrown if it cannot be evaluated
	 */

	public static final java.util.List<java.lang.Double> HolidaySet (
		final double dblStart,
		final double dblFinish,
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblFinish))
			return null;

		java.util.List<java.lang.Double> lsHolidays = new java.util.ArrayList<java.lang.Double>();

		double dblEnd = dblFinish;
		double dblBegin = dblStart;

		if (dblBegin > dblEnd) {
			dblEnd = dblStart;
			dblBegin = dblFinish;
		}

		while (dblBegin != dblEnd) {
			try {
				if (IsHoliday (dblBegin++, strCalendar)) lsHolidays.add (dblBegin - 1);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return lsHolidays;
	}

	/**
	 * Calculate the number of holidays between the start and the end dates
	 * 
	 * @param dblStart Start Date
	 * @param dblFinish End Date
	 * @param strCalendar Holiday Calendar set
	 * 
	 * @return The number of holidays
	 * 
	 * @throws java.lang.Exception Thrown if it cannot be evaluated
	 */

	public static final int Holidays (
		final double dblStart,
		final double dblFinish,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblFinish))
			throw new java.lang.Exception ("Convention::Holidays => Cannot have a NaN date for a holiday!");

		int iNumHolidays = 0;
		double dblEnd = dblFinish;
		double dblBegin = dblStart;

		if (dblBegin > dblEnd) {
			dblEnd = dblStart;
			dblBegin = dblFinish;
		}

		while (dblBegin != dblEnd) {
			if (IsHoliday (dblBegin++, strCalendar)) ++iNumHolidays;
		}

		return dblBegin > dblEnd ? -1 * iNumHolidays : iNumHolidays;
	}

	/**
	 * Adjust the given date in accordance with the adjustment mode and the calendar set
	 * 
	 * @param dblDate Date to be rolled
	 * @param strCalendar Calendar Set to calculate the holidays by
	 * @param iAdjustMode Adjustment Mode (one of DR_ACT, DR_FOLL, DR_MOD_FOLL, DR_PREV, or DR_MOD_PREV
	 * 
	 * @return The Adjusted Date
	 * 
	 * @throws java.lang.Exception Thrown if the date cannot be adjusted
	 */

	public static final double Adjust (
		final double dblDate,
		final java.lang.String strCalendar,
		final int iAdjustMode)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Convention::Adjust => Cannot adjust a NaN date!");

		double dblDateAdjusted = dblDate;

		while (IsHoliday (dblDateAdjusted, strCalendar)) ++dblDateAdjusted;

		return dblDateAdjusted;
	}

	/**
	 * Add the specified number of business days and adjusts it according to the calendar set
	 * 
	 * @param dblDate Date to be rolled
	 * @param iNumDays Number of days to add
	 * @param strCalendar Calendar Set to calculate the holidays by
	 * 
	 * @return The adjusted date
	 * 
	 * @throws java.lang.Exception Propogated if exception encountered
	 */

	public static final double AddBusinessDays (
		final double dblDate,
        final int iNumDays,
        final java.lang.String strCalendar)
        throws java.lang.Exception
    {
        return Adjust (dblDate + iNumDays, strCalendar, DATE_ROLL_FOLLOWING);
    }

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		Convention.Init ("c:\\DRIP\\CreditProduct\\Config.xml");

		double dblDate = org.drip.analytics.date.DateUtil.CreateFromYMD (2011, 5, 5).julian();

		org.drip.analytics.eventday.Locale lh = s_mapLocHols.get ("HKD");

		System.out.println (lh.weekendDays());

		for (org.drip.analytics.eventday.Base hol : lh.holidays()) {
			double dblHoliday = hol.dateInYear (org.drip.analytics.date.DateUtil.Year (dblDate), true);

			System.out.println (dblHoliday + "=" + org.drip.analytics.date.DateUtil.FromJulian
				(dblHoliday));
		}
	}
}
