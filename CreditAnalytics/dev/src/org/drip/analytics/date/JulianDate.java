
package org.drip.analytics.date;

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
 * Class provides a comprehensive representation of Julian date and date manipulation functionality. It
 * 	exports the following functionality:
 * 	- Explicit date construction, as well as date construction from several input string formats/today
 *  - Date Addition/Adjustment/Elapsed/Difference, add/subtract days/weeks/months/years and tenor codes
 *  - Leap Year Functionality (number of leap days in the given interval, is the given year a leap year etc.)
 *  - Generate the subsequent IMM date (CME IMM date, CDS/Credit ISDA IMM date etc)
 *  - Year/Month/Day in numbers/characters
 *  - Days Elapsed/Remaining, is EOM
 *  - Comparison with the Other, equals/hash-code/comparator
 *  - Export the date to a variety of date formats (Oracle, Julian, Bloomberg)
 *  - Serialization/De-serialization to and from Byte Arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class JulianDate implements java.lang.Comparable<JulianDate> {
	private double _dblJulian = java.lang.Double.NaN;

	/**
	 * Create JulianDate from a double Julian
	 * 
	 * @param dblJulian Double representing the JulianDate
	 * 
	 * @throws java.lang.Exception Thrown if the input date is invalid
	 */

	public JulianDate (
		final double dblJulian)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblJulian))
			throw new java.lang.Exception ("JulianDate ctr => Invalid Input!");

		_dblJulian = dblJulian;
	}

	/**
	 * Return the double Julian
	 * 
	 * @return The double Julian
	 */

	public double julian()
	{
		return _dblJulian;
	}

	/**
	 * Add the given number of days and returns a new JulianDate
	 * 
	 * @param iDays Integer representing the number of days to be added
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate addDays (
		final int iDays)
	{
		try {
			return new JulianDate (_dblJulian + iDays);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Subtract the given number of days and returns a new JulianDate
	 * 
	 * @param iDays Integer representing the number of days to be subtracted
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate subtractDays (
		final int iDays)
	{
		try {
			return new JulianDate (_dblJulian - iDays);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add the given number of business days and returns a new JulianDate
	 * 
	 * @param iDays Integer representing the number of days to be subtracted
	 * 
	 * @param strCalendarSet String representing the calendar set containing the business days
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate addBusDays (
		final int iDays,
		final java.lang.String strCalendarSet)
	{
		int iNumDaysToAdd = iDays;
		double dblAdjusted = _dblJulian;

		try {
			while (0 < iNumDaysToAdd--) {
				++dblAdjusted;

				while (org.drip.analytics.daycount.Convention.IsHoliday (dblAdjusted, strCalendarSet))
					++dblAdjusted;
			}

			while (org.drip.analytics.daycount.Convention.IsHoliday (dblAdjusted, strCalendarSet))
				++dblAdjusted;

			return new JulianDate (dblAdjusted);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Subtract the given number of business days and returns a new JulianDate
	 * 
	 * @param iDays Integer representing the number of days to be subtracted
	 * 
	 * @param strCalendarSet String representing the calendar set containing the business days
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate subtractBusDays (
		final int iDays,
		final java.lang.String strCalendarSet)
	{
		int iNumDaysToAdd = iDays;
		double dblAdjusted = _dblJulian;

		try {
			while (0 < iNumDaysToAdd--) {
				--dblAdjusted;

				while (org.drip.analytics.daycount.Convention.IsHoliday (dblAdjusted, strCalendarSet))
					--dblAdjusted;
			}

			while (org.drip.analytics.daycount.Convention.IsHoliday (dblAdjusted, strCalendarSet))
				--dblAdjusted;

			return new JulianDate (dblAdjusted);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add the given number of years and returns a new JulianDate
	 * 
	 * @param iNumYears Integer representing the number of years to be added
	 *  
	 * @return The new JulianDate
	 */

	public JulianDate addYears (
		final int iNumYears)
	{
		int iJA = (int) (_dblJulian + org.drip.analytics.date.DateUtil.HALFSECOND / 86400.);

		if (iJA >= org.drip.analytics.date.DateUtil.JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680.0 + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		int iDay = iJB - iJD - (int) (30.6001 * iJE);
   		int iMonth = iJE - 1;
		int iYear = iJC - 4715;

		if (iMonth > 12) iMonth -= 12;

		if (iMonth > 2) --iYear;

		if (iYear <= 0) --iYear;

		try {
			return org.drip.analytics.date.DateUtil.CreateFromYMD (iYear + iNumYears, iMonth, iDay);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add the given number of months and returns a new JulianDate
	 * 
	 * @param iNumMonths Integer representing the number of months to be added
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate addMonths (
		final int iNumMonths)
	{
		int iJA = (int) (_dblJulian + org.drip.analytics.date.DateUtil.HALFSECOND / 86400.);

		if (iJA >= org.drip.analytics.date.DateUtil.JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680.0 + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		int iDay = iJB - iJD - (int) (30.6001 * iJE);
   		int iMonth = iJE - 1;
		int iYear = iJC - 4715;

		if (iMonth > 12) iMonth -= 12;

		if (iMonth > 2) --iYear;

		if (iYear <= 0) --iYear;

		if (12 < (iMonth += iNumMonths)) {
			while (12 < iMonth) {
				++iYear;
				iMonth -= 12;
			}
		} else if (0 >= iMonth) {
			--iYear;
			iMonth += 12;
		}

		try {
			while (iDay > org.drip.analytics.date.DateUtil.DaysInMonth (iMonth, iYear))
				--iDay;

			return org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, iMonth, iDay);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the First IMM Date from this JulianDate
	 * 
	 * @param iNumRollMonths Integer representing number of months to roll
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate firstIMMDate (
		final int iNumRollMonths)
	{
		int iJA = (int) (_dblJulian + org.drip.analytics.date.DateUtil.HALFSECOND / 86400.);

		if (iJA >= org.drip.analytics.date.DateUtil.JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680. + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		int iDay = iJB - iJD - (int) (30.6001 * iJE);
   		int iMonth = iJE - 1;
		int iYear = iJC - 4715;

		if (iMonth > 12) iMonth -= 12;

		if (iMonth > 2) --iYear;

		if (iYear <= 0) --iYear;

		if (15 <= iDay) {
			if (12 < ++iMonth) {
				++iYear;
				iMonth -= 12;
			}
		}

		while (0 != iMonth % iNumRollMonths) ++iMonth;

		try {
			return org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, iMonth, 15);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the First Credit IMM roll date from this JulianDate
	 * 
	 * @param iNumRollMonths Integer representing number of months to roll
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate firstCreditIMMDate (
		final int iNumRollMonths)
	{
		int iJA = (int) (_dblJulian + org.drip.analytics.date.DateUtil.HALFSECOND / 86400.);

		if (iJA >= org.drip.analytics.date.DateUtil.JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680. + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		int iDay = iJB - iJD - (int) (30.6001 * iJE);
   		int iMonth = iJE - 1;
		int iYear = iJC - 4715;

		if (iMonth > 12) iMonth -= 12;

		if (iMonth > 2) --iYear;

		if (iYear <= 0) --iYear;

		if (15 <= iDay) {
			if (12 < ++iMonth) {
				++iYear;
				iMonth -= 12;
			}
		}

		while (0 != iMonth % iNumRollMonths) ++iMonth;

		try {
			return org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, iMonth, 20);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add the tenor to the JulianDate to create a new date
	 * 
	 * @param strTenorIn String representing the Input Tenor to add
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate addTenor (
		final java.lang.String strTenorIn)
	{
		if (null == strTenorIn || strTenorIn.isEmpty()) return null;

		java.lang.String strTenor = "ON".equalsIgnoreCase (strTenorIn) ? "1D" : strTenorIn;

		int iNumChar = strTenor.length();

		char chTenor = strTenor.charAt (iNumChar - 1);

		int iTimeUnit = -1;

		try {
			iTimeUnit = new java.lang.Integer (strTenor.substring (0, iNumChar - 1));
		} catch (java.lang.Exception e) {
			System.out.println ("Bad time unit " + iTimeUnit + " in tenor " + strTenor);

			return null;
		}

		if ('d' == chTenor || 'D' == chTenor) return addDays (iTimeUnit);

		if ('w' == chTenor || 'W' == chTenor) return addDays (iTimeUnit * 7);

		if ('l' == chTenor || 'L' == chTenor) return addDays (iTimeUnit * 28);

		if ('m' == chTenor || 'M' == chTenor) return addMonths (iTimeUnit);

		if ('y' == chTenor || 'Y' == chTenor) return addYears (iTimeUnit);

		System.out.println ("Unknown tenor format " + strTenor);

		return null;
	}

	/**
	 * Add the tenor to the JulianDate to create a new business date
	 * 
	 * @param strTenor The Tenor
	 * @param strCalendarSet The Holiday Calendar Set
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate addTenorAndAdjust (
		final java.lang.String strTenor,
		final java.lang.String strCalendarSet)
	{
		JulianDate dtNew = addTenor (strTenor);

		if (null == dtNew) return null;

		try {
			return new JulianDate (org.drip.analytics.daycount.Convention.RollDate (dtNew.julian(),
				org.drip.analytics.daycount.Convention.DATE_ROLL_FOLLOWING, strCalendarSet, 1));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Subtract the tenor to the JulianDate to create a new date
	 * 
	 * @param strTenorIn String representing the tenor to add
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate subtractTenor (
		final java.lang.String strTenorIn)
	{
		if (null == strTenorIn || strTenorIn.isEmpty()) return null;

		java.lang.String strTenor = "ON".equalsIgnoreCase (strTenorIn) ? "1D" : strTenorIn;

		int iNumChar = strTenor.length();

		char chTenor = strTenor.charAt (iNumChar - 1);

		int iTimeUnit = -1;

		try {
			iTimeUnit = new java.lang.Integer (strTenor.substring (0, iNumChar - 1));
		} catch (java.lang.Exception e) {
			System.out.println ("Bad time unit " + iTimeUnit + " in tenor " + strTenor);

			return null;
		}

		if ('d' == chTenor || 'D' == chTenor) return addDays (-iTimeUnit);

		if ('w' == chTenor || 'W' == chTenor) return addDays (-iTimeUnit * 7);

		if ('l' == chTenor || 'L' == chTenor) return addDays (-iTimeUnit * 28);

		if ('m' == chTenor || 'M' == chTenor) return addMonths (-iTimeUnit);

		if ('y' == chTenor || 'Y' == chTenor) return addYears (-iTimeUnit);

		return null;
	}

	/**
	 * Subtract the tenor to the JulianDate to create a new business date
	 * 
	 * @param strTenor The Tenor
	 * @param strCalendarSet The Holiday Calendar Set
	 * 
	 * @return The new JulianDate
	 */

	public JulianDate subtractTenorAndAdjust (
		final java.lang.String strTenor,
		final java.lang.String strCalendarSet)
	{
		JulianDate dtNew = subtractTenor (strTenor);

		if (null == dtNew) return null;

		try {
			return new JulianDate (org.drip.analytics.daycount.Convention.RollDate (dtNew.julian(),
				org.drip.analytics.daycount.Convention.DATE_ROLL_FOLLOWING, strCalendarSet, 1));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Difference in days between the current and the input date
	 * 
	 * @param dt JulianDate representing the input date
	 * 
	 * @return Integer representing the difference in days
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public int daysDiff (
		final JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("JulianDate::daysDiff => Invalid Input!");

		return (int) (_dblJulian - dt.julian());
	}

	/**
	 * Return a trigram representation of date
	 * 
	 * @return String representing the trigram representation of date
	 */

	public java.lang.String toOracleDate()
	{
		try {
			return DateUtil.Day (_dblJulian) + "-" + org.drip.analytics.date.DateUtil.MonthOracleChar
				(org.drip.analytics.date.DateUtil.Month (_dblJulian)) + "-" +
					org.drip.analytics.date.DateUtil.Year (_dblJulian);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return a representation of date as YYYYMMDD
	 * 
	 * @param strDelimIn Field delimiter
	 * 
	 * @return String of the YYYYMMDD representation of date
	 */

	public java.lang.String toYYYYMMDD (
		final java.lang.String strDelimIn)
	{
		java.lang.String strDelim = null == strDelimIn ? "" : strDelimIn;

		try {
			return org.drip.quant.common.FormatUtil.FormatDouble (DateUtil.Year (_dblJulian), 4, 0, 1.) +
				strDelim + org.drip.quant.common.FormatUtil.FormatDouble
					(org.drip.analytics.date.DateUtil.Month (_dblJulian), 2, 0, 1.) + strDelim +
						org.drip.quant.common.FormatUtil.FormatDouble (DateUtil.Day (_dblJulian), 2, 0, 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public boolean equals (
		final java.lang.Object o)
	{
		if (!(o instanceof JulianDate)) return false;

		return (int) _dblJulian == (int) ((JulianDate) o)._dblJulian;
	}

	@Override public int hashCode()
	{
		long lBits = java.lang.Double.doubleToLongBits ((int) _dblJulian);

		return (int) (lBits ^ (lBits >>> 32));
	}

	@Override public java.lang.String toString()
	{
		return org.drip.analytics.date.DateUtil.FromJulian (_dblJulian);
	}

	public int compareTo (
		final JulianDate dtOther)
	{
		if ((int) _dblJulian > (int) (dtOther._dblJulian)) return 1;

		if ((int) _dblJulian < (int) (dtOther._dblJulian)) return -1;

		return 0;
	}
}
