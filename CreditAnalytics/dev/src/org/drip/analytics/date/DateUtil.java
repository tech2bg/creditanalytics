
package org.drip.analytics.date;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * DateUtil contains the various date utilities.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DateUtil {

	/**
	 * HALF_SECOND Constant for Julian Date Construction
	 */

	public static double HALFSECOND = 0.5;

	/**
	 * JGREG Constant for Julian Date Construction
	 */

  	public static int JGREG = 15 + 31 * (10 + 12 * 1582);

	/**
	 * LEFT_INCLUDE includes the start date in the Feb29 check
	 */

	public static final int LEFT_INCLUDE = 1;

	/**
	 * RIGHT_INCLUDE includes the end date in the Feb29 check
	 */

	public static final int RIGHT_INCLUDE = 2;

	/**
	 * Days of the week - Monday
	 */

	public static final int MONDAY = 0;

	/**
	 * Days of the week - Tuesday
	 */

	public static final int TUESDAY = 1;

	/**
	 * Days of the week - Wednesday
	 */

	public static final int WEDNESDAY = 2;

	/**
	 * Days of the week - Thursday
	 */

	public static final int THURSDAY = 3;

	/**
	 * Days of the week - Friday
	 */

	public static final int FRIDAY = 4;

	/**
	 * Days of the week - Saturday
	 */

	public static final int SATURDAY = 5;

	/**
	 * Days of the week - Sunday
	 */

	public static final int SUNDAY = 6;

	/**
	 * Integer Month - January
	 */

	public static final int JANUARY = 1;

	/**
	 * Integer Month - February
	 */

	public static final int FEBRUARY = 2;

	/**
	 * Integer Month - March
	 */

	public static final int MARCH = 3;

	/**
	 * Integer Month - April
	 */

	public static final int APRIL = 4;

	/**
	 * Integer Month - May
	 */

	public static final int MAY = 5;

	/**
	 * Integer Month - June
	 */

	public static final int JUNE = 6;

	/**
	 * Integer Month - July
	 */

	public static final int JULY = 7;

	/**
	 * Integer Month - August
	 */

	public static final int AUGUST = 8;

	/**
	 * Integer Month - September
	 */

	public static final int SEPTEMBER = 9;

	/**
	 * Integer Month - October
	 */

	public static final int OCTOBER = 10;

	/**
	 * Integer Month - November
	 */

	public static final int NOVEMBER = 11;

	/**
	 * Integer Month - December
	 */

	public static final int DECEMBER = 12;

	/**
	 * Convert YMD to a Julian double.
	 * 
	 * @param iYear Year
	 * @param iMonth Month
	 * @param iDay Day
	 * 
	 * @return double representing the Julian date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public static double ToJulian (
		final int iYear,
		final int iMonth,
		final int iDay)
		throws java.lang.Exception
	{
		if (0 > iYear || 0 > iMonth || 0 > iDay)
			throw new java.lang.Exception ("DateUtil::ToJulian => Invalid Inputs");

		int iJulianYear = iYear;
		int iJulianMonth = iMonth;

		if (iYear < 0) ++iJulianYear;

		if (iMonth > 2)
			++iJulianMonth;
		else {
			--iJulianYear;
			iJulianMonth += 13;
		}

		double dblJulian = (java.lang.Math.floor (365.25 * iJulianYear) + java.lang.Math.floor (30.6001 *
			iJulianMonth) + iDay + 1720995.0);

		if (iDay + 31 * (iMonth + 12 * iYear) >= JGREG) {
 			int iJA = (int)(0.01 * iJulianYear);
 			dblJulian += 2 - iJA + (0.25 * iJA);
   		}

   		return java.lang.Math.floor (dblJulian);
	}

	/**
	 * Create a MM/DD/YYYY string from the input Julian double
	 * 
	 * @param dblJulianIn double representing Julian date
	 * 
	 * @return MM/DD/YYYY date string
	 */

	public static java.lang.String FromJulian (
		final double dblJulianIn)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblJulianIn)) return null;

		int iJA = (int) (dblJulianIn + HALFSECOND / 86400.0);

		if (iJA >= JGREG) {
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

		return org.drip.quant.common.FormatUtil.PrePad (iMonth) + "/" +
			org.drip.quant.common.FormatUtil.PrePad (iDay) + "/" + iYear;
	}

	/**
	 * Return the Year corresponding to the Julian double
	 * 
	 * @param dblJulianIn double representing the Julian date
	 * 
	 * @return integer representing the month
	 * 
	 * @throws java.lang.Exception thrown if the input date in invalid
	 */

	public static int Year (
		final double dblJulianIn)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblJulianIn))
			throw new java.lang.Exception ("DateUtil::Year => Invalid Input!");

		int iJA = (int) (dblJulianIn + HALFSECOND / 86400.0);

		if (iJA >= JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680.0 + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		int iMonth = iJE - 1;
		int iYear = iJC - 4715;

		if (iMonth > 12) iMonth -= 12;

		if (iMonth > 2) --iYear;

		if (iYear <= 0) --iYear;

		return iYear;
	}

	/**
	 * Return the month given the date represented by the Julian double.
	 * 
	 * @param dblJulianIn double representing the Julian date
	 * 
	 * @return integer representing the month
	 * 
	 * @throws java.lang.Exception thrown if input date is invalid
	 */

	public static int Month (
		final double dblJulianIn)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblJulianIn))
			throw new java.lang.Exception ("DateUtil::Month => Invalid Input!");

		int iJA = (int) (dblJulianIn + HALFSECOND / 86400.0);

		if (iJA >= JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680.0 + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iMonth = (int) ((iJB - iJD) / 30.6001) - 1;

		if (iMonth > 12) iMonth -= 12;

		return iMonth;
	}

	/**
	 * Return the day corresponding to the Julian double
	 *  
	 * @param dblJulianIn double representing the Julian date
	 * 
	 * @return integer representing the day
	 * 
	 * @throws java.lang.Exception thrown if input date is invalid
	 */

	public static int Day (
		final double dblJulianIn)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblJulianIn))
			throw new java.lang.Exception ("DateUtil::Day => Invalid Input!");

		int iJA = (int) (dblJulianIn + HALFSECOND / 86400.0);

		if (iJA >= JGREG) {
			int iJAlpha = (int) (((iJA - 1867216) - 0.25) / 36524.25);
			iJA = iJA + 1 + iJAlpha - iJAlpha / 4;
		}

		int iJB = iJA + 1524;
		int iJC = (int) (6680.0 + ((iJB - 2439870) - 122.1) / 365.25);
		int iJD = 365 * iJC + iJC / 4;
		int iJE = (int) ((iJB - iJD) / 30.6001);
   		return iJB - iJD - (int) (30.6001 * iJE);
	}

	/**
	 * Number of days elapsed in the year represented by the given Julian date
	 * 
	 * @param dblDate Double representing the Julian date
	 * 
	 * @return Double representing the number of days in the current year
	 * 
	 * @throws java.lang.Exception Thrown if the input date is invalid
	 */

	public static final int DaysElapsed (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DateUtil::DaysElapsed => Invalid Input!");

		return (int) (dblDate - ToJulian (Year (dblDate), JANUARY, 1));
	}

	/**
	 * Number of days remaining in the year represented by the given Julian year
	 * 
	 * @param dblDate Double representing the Julian date
	 * 
	 * @return Double representing the number of days remaining
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int DaysRemaining (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DateUtil::DaysRemaining => Invalid Input!");

		return (int) (ToJulian (Year (dblDate), DECEMBER, 31) - dblDate);
	}

	/**
	 * Indicate if the year in the given Julian date is a leap year
	 * 
	 * @param dblDate Double representing the input Julian date
	 * 
	 * @return True indicates leap year
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final boolean IsLeapYear (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DateUtil::IsLeapYear => Invalid Input!");

		return 0 == (Year (dblDate) % 4);
	}

	/**
	 * Indicate whether there is at least one leap day between 2 given Julian dates
	 *  
	 * @param dblStart Double representing the starting Julian date
	 * @param dblEnd Double representing the ending Julian date
	 * @param iIncludeSide INCLUDE_LEFT or INCLUDE_RIGHT indicating whether the starting date, the ending
	 * 	date, or both dates are to be included
	 *  
	 * @return True indicates there is at least one Feb29 between the dates
	 * 
	 * @throws java.lang.Exception If inputs are invalid
	 */

	public static final boolean ContainsFeb29 (
		final double dblStart,
		final double dblEnd,
		final int iIncludeSide)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("DateUtil::ContainsFeb29 => Invalid Input!");

		if (dblStart >= dblEnd) return false;

		double dblLeft = dblStart;
		double dblRight = dblEnd;

		if (0 == (iIncludeSide & LEFT_INCLUDE)) ++dblLeft;

		if (0 == (iIncludeSide & RIGHT_INCLUDE)) --dblRight;

		for (double dblDate = dblLeft; dblDate <= dblRight; ++dblDate) {
			if (FEBRUARY == Month (dblDate) && 29 == Day (dblDate)) return true;
		}

		return false;
	}

	/**
	 * Calculate how many leap days exist between the 2 given Julian days
	 * 
	 * @param dblStart Double representing the starting Julian date
	 * @param dblEnd Double representing the ending Julian date
	 * @param iIncludeSide INCLUDE_LEFT or INCLUDE_RIGHT indicating whether the starting date, the ending
	 * 	date, or both dates are to be included
	 * 
	 * @return Integer representing the number of leap days
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public static final int NumFeb29 (
		final double dblStart,
		final double dblEnd,
		final int iIncludeSide)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("DateUtil::NumFeb29 => Invalid Input!");

		int iNumFeb29 = 0;
		boolean bLoop = true;
		double dblDate = dblStart;

		while (bLoop) {
			double dblEndDate = dblDate + 365;

			if (dblEndDate > dblEnd) {
				bLoop = false;
				dblEndDate = dblEnd;
			}

			if (ContainsFeb29 (dblDate, dblEndDate, iIncludeSide)) ++iNumFeb29;

			dblDate = dblEndDate;
		}

		return iNumFeb29;
	}

	/**
	 * Return the English word corresponding to the input integer month
	 *  
	 * @param iMonth Integer representing the month
	 * 
	 * @return String of the English word
	 */

	public static final java.lang.String MonthChar (
		final int iMonth)
	{
		if (JANUARY == iMonth) return "January";

		if (FEBRUARY == iMonth) return "February";

		if (MARCH == iMonth) return "March";

		if (APRIL == iMonth) return "April";

		if (MAY == iMonth) return "May";

		if (JUNE == iMonth) return "June";

		if (JULY == iMonth) return "July";

		if (AUGUST == iMonth) return "August";

		if (SEPTEMBER == iMonth) return "September";

		if (OCTOBER == iMonth) return "October";

		if (NOVEMBER == iMonth) return "November";

		if (DECEMBER == iMonth) return "December";

		return null;
	}

	/**
	 * Return the Oracle DB trigram corresponding to the input integer month
	 * 
	 * @param iMonth Integer representing the month
	 * 
	 * @return String representing the Oracle DB trigram
	 */

	public static java.lang.String MonthOracleChar (
		final int iMonth)
	{
		if (JANUARY == iMonth) return "JAN";

		if (FEBRUARY == iMonth) return "FEB";

		if (MARCH == iMonth) return "MAR";

		if (APRIL == iMonth) return "APR";

		if (MAY == iMonth) return "MAY";

		if (JUNE == iMonth) return "JUN";

		if (JULY == iMonth) return "JUL";

		if (AUGUST == iMonth) return "AUG";

		if (SEPTEMBER == iMonth) return "SEP";

		if (OCTOBER == iMonth) return "OCT";

		if (NOVEMBER == iMonth) return "NOV";

		if (DECEMBER == iMonth) return "DEC";

		return null;
	}

	/**
	 * Convert the month trigram/word to the corresponding month integer
	 * 
	 * @param strMonth Month trigram or English Word
	 * 
	 * @return Integer representing the Month
	 * 
	 * @throws java.lang.Exception Thrown on Invalid Input Month
	 */

	public static final int MonthFromMonthChars (
		final java.lang.String strMonth)
		throws java.lang.Exception
	{
		if (null == strMonth || strMonth.isEmpty())
			throw new java.lang.Exception ("DateUtil::MonthFromMonthChars => Invalid Month!");

		if (strMonth.equalsIgnoreCase ("JAN") || strMonth.equalsIgnoreCase ("JANUARY")) return JANUARY;

		if (strMonth.equalsIgnoreCase ("FEB") || strMonth.equalsIgnoreCase ("FEBRUARY")) return FEBRUARY;

		if (strMonth.equalsIgnoreCase ("MAR") || strMonth.equalsIgnoreCase ("MARCH")) return MARCH;

		if (strMonth.equalsIgnoreCase ("APR") || strMonth.equalsIgnoreCase ("APRIL")) return APRIL;

		if (strMonth.equalsIgnoreCase ("MAY")) return MAY;

		if (strMonth.equalsIgnoreCase ("JUN") || strMonth.equalsIgnoreCase ("JUNE")) return JUNE;

		if (strMonth.equalsIgnoreCase ("JUL") || strMonth.equalsIgnoreCase ("JULY")) return JULY;

		if (strMonth.equalsIgnoreCase ("AUG") || strMonth.equalsIgnoreCase ("AUGUST")) return AUGUST;

		if (strMonth.equalsIgnoreCase ("SEP") || strMonth.equalsIgnoreCase ("SEPTEMBER") ||
			strMonth.equalsIgnoreCase ("SEPT"))
			return SEPTEMBER;

		if (strMonth.equalsIgnoreCase ("OCT") || strMonth.equalsIgnoreCase ("OCTOBER")) return OCTOBER;

		if (strMonth.equalsIgnoreCase ("NOV") || strMonth.equalsIgnoreCase ("NOVEMBER")) return NOVEMBER;

		if (strMonth.equalsIgnoreCase ("DEC") || strMonth.equalsIgnoreCase ("DECEMBER")) return DECEMBER;

		throw new java.lang.Exception ("DateUtil::MonthFromMonthChars => Invalid Month: " + strMonth);
	}

	/**
	 * Get the English word for day corresponding to the input integer
	 * 
	 * @param iDay Integer representing the day
	 * 
	 * @return String representing the English word for the day
	 * 
	 * @throws java.lang.Exception Thrown if the input day is invalid
	 */

	public static java.lang.String DayChars (
		final int iDay)
	{
		if (MONDAY == iDay) return "Monday";

		if (TUESDAY == iDay) return "Tuesday";

		if (WEDNESDAY == iDay) return "Wednesday";

		if (THURSDAY == iDay) return "Thursday";

		if (FRIDAY == iDay) return "Friday";

		if (SATURDAY == iDay) return "Saturday";

		if (SUNDAY == iDay) return "Sunday";

		return "";
	}

	/**
	 * Get the maximum number of days in the given month and year
	 * 
	 * @param iMonth Integer representing the month
	 * @param iYear Integer representing the year
	 * 
	 * @return Integer representing the maximum days
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final int DaysInMonth (
		final int iMonth,
		final int iYear)
		throws java.lang.Exception
	{
		if (JANUARY == iMonth) return 31;

		if (FEBRUARY == iMonth) return 0 == (iYear % 4) ? 29 : 28;

		if (MARCH == iMonth) return 31;

		if (APRIL == iMonth) return 30;

		if (MAY == iMonth) return 31;

		if (JUNE == iMonth) return 30;

		if (JULY == iMonth) return 31;

		if (AUGUST == iMonth) return 31;

		if (SEPTEMBER == iMonth) return 30;

		if (OCTOBER == iMonth) return 31;

		if (NOVEMBER == iMonth) return 30;

		if (DECEMBER == iMonth) return 31;

		throw new java.lang.Exception ("DateUtil::DaysInMonth => Invalid Month: " + iMonth);
	}

	/**
	 * Indicate if the given Julian double corresponds to an end of month day
	 * 
	 * @param dblDate Double representing the Julain date
	 * 
	 * @return True indicates EOM is true
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final boolean IsEOM (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DateUtil::IsEOM => Invalid Date: " + dblDate);

		return Day (dblDate) == DaysInMonth (Month (dblDate), Year (dblDate)) ? true : false;
	}

	/**
	 * Create a JulianDate from year, month, and date
	 *  
	 * @param iYear Integer year
	 * @param iMonth Integer month
	 * @param iDay Integer day
	 * 
	 * @return Julian Date corresponding to the specified year, month, and day
	 */

	public static final org.drip.analytics.date.JulianDate CreateFromYMD (
		final int iYear,
		final int iMonth,
		final int iDay)
	{
		try {
			return new org.drip.analytics.date.JulianDate (ToJulian (iYear, iMonth, iDay));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return a Julian Date corresponding to today
	 *  
	 * @return JulianDate corresponding to today
	 */

	public static final org.drip.analytics.date.JulianDate Today()
	{
		java.util.Date dtNow = new java.util.Date();

		try {
			return CreateFromYMD (Year (dtNow), Month (dtNow), Day (dtNow));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a JulianDate from a string containing date in the DDMMYYYY format
	 * 
	 * @param strDate String containing date in the DDMMYYYY format
	 * 
	 * @return JulianDate
	 */

	public static final JulianDate CreateFromDDMMMYYYY (
		final java.lang.String strDate)
	{
		if (null == strDate || strDate.isEmpty()) return null;

		java.lang.String[] astrParts = strDate.split ("-");

		if (3 != astrParts.length) return null;

		try {
			return CreateFromYMD (new java.lang.Integer (astrParts[2]), MonthFromMonthChars (astrParts[1]),
				new java.lang.Integer (astrParts[0]));
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	/**
	 * Create a JulianDate from a string containing date in the DDMMYYYY format
	 * 
	 * @param strMDY String containing date in the MM/DD/YYYY format
	 * @param strDelim String Delimiter
	 * 
	 * @return JulianDate
	 */

	public static final JulianDate CreateFromMDY (
		final java.lang.String strMDY,
		final java.lang.String strDelim)
	{
		if (null == strMDY || strMDY.isEmpty() || null == strDelim || strDelim.isEmpty()) return null;

		java.lang.String[] astrParts = strMDY.split (strDelim);

		if (3 != astrParts.length) return null;

		try {
			return CreateFromYMD (new java.lang.Integer (astrParts[2]), new java.lang.Integer (astrParts[1]),
				new java.lang.Integer (astrParts[2]));
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	/**
	 * Return the Day of the Week corresponding to the input java.util.Date
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return The Day Of The Week
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int DayOfTheWeek (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::DayOfTheWeek => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.DAY_OF_WEEK);
	}

	/**
	 * Return the Day corresponding to the input java.util.Date
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return The Day
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int Day (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::Day => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.DATE);
	}

	/**
	 * Return the Month corresponding to the input java.util.Date. 1 => January, and 12 => December
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return The Month
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int Month (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::Month => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.MONTH) + 1;
	}

	/**
	 * Return the Year corresponding to the input java.util.Date.
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return The Year
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int Year (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::Year => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.YEAR);
	}

	/**
	 * Create an Oracle Date Trigram from a YYYYMMDD string
	 * 
	 * @param strYYYYMMDD Date String in the YYYYMMDD format.
	 * 
	 * @return Oracle Date Trigram String
	 */

	public static java.lang.String MakeOracleDateFromYYYYMMDD (
		final java.lang.String strYYYYMMDD)
	{
		if (null == strYYYYMMDD || strYYYYMMDD.isEmpty()) return null;

		try {
			return strYYYYMMDD.substring (6) + "-" + MonthOracleChar ((new java.lang.Integer
				(strYYYYMMDD.substring (4, 6))).intValue()) + "-" + strYYYYMMDD.substring (0, 4);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Oracle date trigram from a Bloomberg date string
	 * 
	 * @param strBBGDate Bloomberg date string
	 * 
	 * @return Oracle date trigram string
	 */

	public static java.lang.String MakeOracleDateFromBBGDate (
		final java.lang.String strBBGDate)
	{
		if (null == strBBGDate || strBBGDate.isEmpty()) return null;

		java.util.StringTokenizer st = new java.util.StringTokenizer (strBBGDate, "/");

		try {
			java.lang.String strMonth = MonthOracleChar ((new java.lang.Integer
				(st.nextToken())).intValue());

			if (null == strMonth) return null;

			return st.nextToken() + "-" + strMonth + "-" + st.nextToken();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a JulianDate from the java Date
	 * 
	 * @param dt Java Date input
	 * 
	 * @return JulianDate output
	 */

	public static final org.drip.analytics.date.JulianDate MakeJulianFromRSEntry (
		final java.util.Date dt)
	{
		if (null == dt) return null;

		try {
			return CreateFromYMD (Year (dt), Month (dt), Day (dt));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve a Java Date Instance from the Julian Date Instance
	 * 
	 * @param dt Julian Date Instance
	 * 
	 * @return The Java Date Instance
	 */

	public static final java.util.Date JavaDateFromJulianDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		java.util.Calendar cal = java.util.Calendar.getInstance();

		double dblDate = dt.julian();

		try {
			cal.set (Year (dblDate), Month (dblDate) - 1, Day (dblDate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return cal.getTime();
	}

	/**
	 * Create a JulianDate from the DD MMM YY
	 * 
	 * @param strDDMMMYY Java Date input as delimited DD MMM YY
	 * @param strDelim Delimiter
	 * 
	 * @return JulianDate output
	 */

	public static final org.drip.analytics.date.JulianDate MakeJulianFromDDMMMYY (
		final java.lang.String strDDMMMYY,
		final java.lang.String strDelim)
	{
		if (null == strDDMMMYY || strDDMMMYY.isEmpty() || null == strDelim || strDelim.isEmpty())
			return null;

		java.lang.String[] astrDMY = strDDMMMYY.split (strDelim);

		if (null == astrDMY || 3 != astrDMY.length) return null;

		try {
			return CreateFromYMD (2000 + new java.lang.Integer (astrDMY[2].trim()), MonthFromMonthChars
				(astrDMY[1].trim()), new java.lang.Integer (astrDMY[0].trim()));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a JulianDate from the YYYY MM DD
	 * 
	 * @param strYYYYMMDD Java Date input as delimited YYYY MM DD
	 * @param strDelim Delimiter
	 * 
	 * @return JulianDate output
	 */

	public static final org.drip.analytics.date.JulianDate MakeJulianFromYYYYMMDD (
		final java.lang.String strYYYYMMDD,
		final java.lang.String strDelim)
	{
		if (null == strYYYYMMDD || strYYYYMMDD.isEmpty() || null == strDelim || strDelim.isEmpty())
			return null;

		java.lang.String[] astrYYYYMMDD = strYYYYMMDD.split (strDelim);

		if (null == astrYYYYMMDD || 3 != astrYYYYMMDD.length) return null;

		try {
			return CreateFromYMD (new java.lang.Integer (astrYYYYMMDD[0].trim()), new java.lang.Integer
				(astrYYYYMMDD[1].trim()), new java.lang.Integer (astrYYYYMMDD[2].trim()));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a JulianDate from Bloomberg date string
	 * 
	 * @param strBBGDate Bloomberg date string
	 * 
	 * @return The new JulianDate
	 */

	public static final org.drip.analytics.date.JulianDate MakeJulianDateFromBBGDate (
		final java.lang.String strBBGDate)
	{
		if (null == strBBGDate || strBBGDate.isEmpty()) return null;

		java.lang.String[] astrFields = strBBGDate.split ("/");

		if (3 != astrFields.length) return null;

		try {
			return CreateFromYMD ((int) new java.lang.Double (astrFields[2].trim()).doubleValue(), (int) new
				java.lang.Double (astrFields[0].trim()).doubleValue(), (int) new java.lang.Double
					(astrFields[1].trim()).doubleValue());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Month corresponding to the Month Digit Code
	 * 
	 * @param ch The Month Digit Code
	 * 
	 * @return The Month corresponding to the Month Digit Code
	 * 
	 * @throws java.lang.Exception Thrown if the Digit Code is Invalid
	 */

	public static final int MonthFromCode (
		final char ch)
		throws java.lang.Exception
	{
		if ('F' == ch) return JANUARY;

		if ('G' == ch) return FEBRUARY;

		if ('H' == ch) return MARCH;

		if ('J' == ch) return APRIL;

		if ('K' == ch) return MAY;

		if ('M' == ch) return JUNE;

		if ('N' == ch) return JULY;

		if ('Q' == ch) return AUGUST;

		if ('U' == ch) return SEPTEMBER;

		if ('V' == ch) return OCTOBER;

		if ('X' == ch) return NOVEMBER;

		if ('Z' == ch) return DECEMBER;

		throw new java.lang.Exception ("DateUtil::MonthFromCode => Invalid Character: " + ch);
	}

	/**
	 * Retrieve the Digit Code corresponding to the Month
	 * 
	 * @param iMonth The Month
	 * 
	 * @return The Digit Code corresponding to the Month
	 * 
	 * @throws java.lang.Exception Thrown if the Digit Code cannot be computed
	 */

	public static final char CodeFromMonth (
		final int iMonth)
		throws java.lang.Exception
	{
		if (JANUARY == iMonth) return 'F';

		if (FEBRUARY == iMonth) return 'G';

		if (MARCH == iMonth) return 'H';

		if (APRIL == iMonth) return 'J';

		if (MAY == iMonth) return 'K';

		if (JUNE == iMonth) return 'M';

		if (JULY == iMonth) return 'N';

		if (AUGUST == iMonth) return 'Q';

		if (SEPTEMBER == iMonth) return 'U';

		if (OCTOBER == iMonth) return 'V';

		if (NOVEMBER == iMonth) return 'X';

		if (DECEMBER == iMonth) return 'Z';

		throw new java.lang.Exception ("DateUtil::CodeFromMonth => Invalid Month: " + iMonth);
	}
}
