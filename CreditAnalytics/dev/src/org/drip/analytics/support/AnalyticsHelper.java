
package org.drip.analytics.support;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * AnalyticsHelper contains the collection of the analytics related utility functions used by the modules.
 * 	The following are the functionality that it exposes:
 * 	- Yield to Discount Factor, and vice versa.
 * 	- Map Bloomberg Day Count Codes to Credit Analytics Day Count Codes
 * 	- Generate rule-based curve node manifest measure bumps
 * 	- Generate loss periods using a variety of different schemes
 * 	- Aggregate/disaggregate/merge coupon period lists
 * 	- Create fixings objects, rate index from currency/coupon/frequency
 * 	- Construct JulianDate from BBG dates and other formats, off of RS Entries
 * 	- String Tenor/Month Code/Work-out
 * 	- Standard Treasury Bench-mark off of Maturity
 * 
 * @author Lakshmi Krishnamurthy
 */

public class AnalyticsHelper {
	private static final boolean s_bBlog = false;

	private static final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> s_mapIRSwitch =
		new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String>();

	private static final java.util.Map<java.lang.Integer, java.lang.String> s_mapDCBBGCode = new
		java.util.HashMap<java.lang.Integer, java.lang.String>();

	/**
	 * Initialize IR switcher and Bloomberg day count maps
	 */

	public static final void Init()
	{
		s_mapDCBBGCode.put (1, "ACT/ACT");

		s_mapDCBBGCode.put (2, "ACT/360");

		s_mapDCBBGCode.put (3, "ACT/365");

		s_mapDCBBGCode.put (4, "30/ACT");

		s_mapDCBBGCode.put (5, "30/360");

		s_mapDCBBGCode.put (6, "30/365");

		s_mapDCBBGCode.put (7, "NL/ACT");

		s_mapDCBBGCode.put (8, "NL/360");

		s_mapDCBBGCode.put (9, "NL/365");

		s_mapDCBBGCode.put (10, "ACT/ACT NON-EOM");

		s_mapDCBBGCode.put (11, "ACT/360 NON-EOM");

		s_mapDCBBGCode.put (12, "ACT/365 NON-EOM");

		s_mapDCBBGCode.put (13, "30/ACT NON-EOM");

		s_mapDCBBGCode.put (14, "30/360 NON-EOM");

		s_mapDCBBGCode.put (15, "30/365 NON-EOM");

		s_mapDCBBGCode.put (16, "NL/ACT NON-EOM");

		s_mapDCBBGCode.put (17, "NL/360 NON-EOM");

		s_mapDCBBGCode.put (18, "NL/365 NON-EOM");

		s_mapDCBBGCode.put (19, "ISMA 30/ACT");

		s_mapDCBBGCode.put (20, "ISMA 30/360");

		s_mapDCBBGCode.put (21, "ISMA 30/365");

		s_mapDCBBGCode.put (22, "ISMA 30/ACT NON-EOM");

		s_mapDCBBGCode.put (23, "ISMA 30/360 NON-EOM");

		s_mapDCBBGCode.put (24, "ISMA 30/365 NON-EOM");

		s_mapDCBBGCode.put (27, "ACT/364");

		s_mapDCBBGCode.put (29, "US MUNI: 30/360");

		s_mapDCBBGCode.put (30, "ACT/364 NON-EOM");

		s_mapDCBBGCode.put (32, "MUNI30/360 NON-EOM");

		s_mapDCBBGCode.put (33, "BUS DAYS/252");

		s_mapDCBBGCode.put (35, "GERMAN:30/360");

		s_mapDCBBGCode.put (36, "BUS DAY/252 NON-EOM");

		s_mapDCBBGCode.put (38, "GER:30/360 NON-EOM");

		s_mapDCBBGCode.put (40, "US:WIT ACT/ACT");

		s_mapDCBBGCode.put (41, "US:WIB ACT/360");

		s_mapDCBBGCode.put (44, "ISDA SWAPS:30/360");

		s_mapDCBBGCode.put (45, "ISDA SWAPS:30/365");

		s_mapDCBBGCode.put (46, "ISDA SWAPS:30/ACT");

		s_mapDCBBGCode.put (47, "ISDA30/360 NON-EOM");

		s_mapDCBBGCode.put (48, "ISDA30/365 NON-EOM");

		s_mapDCBBGCode.put (49, "ISDA30/ACT NON-EOM");

		s_mapDCBBGCode.put (50, "ISDA 30E/360");

		s_mapDCBBGCode.put (51, "ISDA 30E/365");

		s_mapDCBBGCode.put (52, "ISDA 30E/ACT");

		s_mapDCBBGCode.put (53, "ISDA 30E/360 N-EOM");

		s_mapDCBBGCode.put (54, "ISDA 30E/365 N-EOM");

		s_mapDCBBGCode.put (55, "ISDA 30E/ACT N-EOM");

		s_mapDCBBGCode.put (101, "ACT/ACT");

		s_mapDCBBGCode.put (102, "ACT/360");

		s_mapDCBBGCode.put (103, "ACT/365");

		s_mapDCBBGCode.put (104, "30/360");

		s_mapDCBBGCode.put (105, "ACT/ACT NON-EOM");

		s_mapDCBBGCode.put (106, "ACT/360 NON-EOM");

		s_mapDCBBGCode.put (107, "ACT/365 NON-EOM");

		s_mapDCBBGCode.put (108, "ACT/360");

		s_mapDCBBGCode.put (131, "ISMA 30/360");

		s_mapDCBBGCode.put (201, "ISDA ACT/ACT");

		s_mapDCBBGCode.put (202, "AFB ACT/ACT");

		s_mapDCBBGCode.put (203, "ISDA ACT/ACT NOM");

		s_mapDCBBGCode.put (204, "AFB ACT/ACT NOM");

		s_mapDCBBGCode.put (206, "ISMA ACT/ACT");

		s_mapDCBBGCode.put (207, "ISMA ACT/ACT NON-EOM");

		s_mapIRSwitch.put ("ITL", "EUR");

		s_mapIRSwitch.put ("FRF", "EUR");

		s_mapIRSwitch.put ("CZK", "EUR");

		s_mapIRSwitch.put ("BEF", "EUR");

		s_mapIRSwitch.put ("ATS", "EUR");

		s_mapIRSwitch.put ("SKK", "EUR");
	}

	/**
	 * Calculate the discount factor from the specified frequency, yield, and accrual year fraction
	 * 
	 * @param iFreqIn Input frequency - if zero, set to semi-annual.
	 * @param dblYield Yield
	 * @param dblTime Time in DC years
	 * 
	 * @return the discount factor
	 * 
	 * @throws java.lang.Exception if input are invalid.
	 */

	public static final double Yield2DF (
		final int iFreqIn,
		final double dblYield,
		final double dblTime)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield) || !org.drip.quant.common.NumberUtil.IsValid
			(dblTime))
			throw new java.lang.Exception ("CurveProductHelper.YieldDF: Bad yield/time");

		int iFreq = (0 == iFreqIn) ? 2 : iFreqIn;

		return java.lang.Math.pow (1. + (dblYield / iFreq), -1. * dblTime * iFreq);
	}

	/**
	 * Calculate the yield from the specified discount factor to the given time.
	 * 
	 * @param iFreqIn Yield calculation frequency - defaults to semi-annual if zero.
	 * @param dblDF Discount Factor
	 * @param dblTime Time to which the yield/DF are specified
	 * 
	 * @return Implied yield
	 * 
	 * @throws java.lang.Exception Thrown if yield cannot be computed
	 */

	public static final double DF2Yield (
		final int iFreqIn,
		final double dblDF,
		final double dblTime)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDF) || !org.drip.quant.common.NumberUtil.IsValid
			(dblTime))
			throw new java.lang.Exception ("CurveProductHelper.DFYield: Bad yield/time");

		int iFreq = (0 == iFreqIn) ? 2 : iFreqIn;

		return iFreq * (java.lang.Math.pow (dblDF, -1. / (iFreq * dblTime)) - 1.);
	}

	/**
	 * Return the standard on-the-run benchmark treasury string from the valuation and the maturity dates
	 * 
	 * @param dblValue the valuation date
	 * @param dblMaturity the maturity date
	 * 
	 * @return the standard on-the-run benchmark treasury string
	 */

	public static final java.lang.String BaseTsyBmk (
		final double dblValue,
		final double dblMaturity)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue) || !org.drip.quant.common.NumberUtil.IsValid
			(dblMaturity))
			return null;

		double dblMatYears = (dblMaturity - dblValue) / 365.25;

		if (1.0 < dblMatYears && dblMatYears <= 2.5) return "2YON";

		if (2.5 < dblMatYears && dblMatYears <= 4.0) return "3YON";

		if (4.0 < dblMatYears && dblMatYears <= 6.0) return "5YON";

		if (6.0 < dblMatYears && dblMatYears <= 8.5) return "7YON";

		if (8.5 < dblMatYears && dblMatYears <= 15.) return "10YON";

		if (dblMatYears > 15.) return "30YON";

		return null;
	}

	/**
	 * Turn the work out type to string
	 * 
	 * @param iWOType One of the WO_TYPE_* fields in the WorkoutInfo class
	 * 
	 * @return String representation of the work out type field
	 */

	public static final java.lang.String WorkoutTypeToString (
		final int iWOType)
	{
		if (org.drip.param.valuation.WorkoutInfo.WO_TYPE_PUT == iWOType) return "Put";

		if (org.drip.param.valuation.WorkoutInfo.WO_TYPE_CALL == iWOType) return "Call";

		if (org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY == iWOType) return "Maturity";

		return "Unknown work out type";
	}

	/**
	 * Convert the Bloomberg day count code to DRIP day count code.
	 *  
	 * @param strBBGDCCode String representing the Bloomberg day count code.
	 * 
	 * @return String representing the DRIP day count code.
	 */

	public static final java.lang.String ParseFromBBGDCCode (
		final java.lang.String strBBGDCCode)
	{
		if (null == strBBGDCCode) return "Unknown BBG DC";

		try {
			return s_mapDCBBGCode.get ((int) new java.lang.Double (strBBGDCCode.trim()).doubleValue());
		} catch (java.lang.Exception e) {
		}

		return "Unknown BBG DC";
	}

	/**
	 * Retrieve the tenor from the frequency
	 * 
	 * @param iFreq Integer frequency
	 * 
	 * @return String representing the tenor
	 */

	public static final java.lang.String GetTenorFromFreq (
		final int iFreq)
	{
		if (1 == iFreq) return "1Y";

		if (2 == iFreq) return "6M";

		if (3 == iFreq) return "4M";

		if (4 == iFreq) return "3M";

		if (6 == iFreq) return "2M";

		if (12 == iFreq) return "1M";

		return null;
	}

	/**
	 * Retrieve the Number of Years from the Tenor
	 * 
	 * @param strTenor The Specified Tenor
	 * 
	 * @return The Number of Years
	 */

	public static final int TenorToYears (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("AnalyticsHelper::TenorToYears => Invalid Inputs");

		char chTenor = strTenor.charAt (strTenor.length() - 1);

		int iTimeUnit = (int) new java.lang.Double (strTenor.substring (0, strTenor.length() -
			1)).doubleValue();

		if ('y' == chTenor || 'Y' == chTenor) return iTimeUnit * 12;

		throw new java.lang.Exception ("AnalyticsHelper::TenorToYears => Invalid tenor format " + strTenor);
	}

	/**
	 * Retrieve the Number of Months from the Tenor
	 * 
	 * @param strTenor The Specified Tenor
	 * 
	 * @return The Number of Months
	 */

	public static final int TenorToMonths (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("AnalyticsHelper::TenorToMonths => Invalid Inputs");

		char chTenor = strTenor.charAt (strTenor.length() - 1);

		int iTimeUnit = (int) new java.lang.Double (strTenor.substring (0, strTenor.length() -
			1)).doubleValue();

		if ('l' == chTenor || 'L' == chTenor) return iTimeUnit;

		if ('m' == chTenor || 'M' == chTenor) return iTimeUnit;

		if ('y' == chTenor || 'Y' == chTenor) return iTimeUnit * 12;

		throw new java.lang.Exception ("AnalyticsHelper::TenorToMonths => Invalid tenor format " + strTenor);
	}

	/**
	 * Retrieve the Number of Days from the Tenor
	 * 
	 * @param strTenor The Specified Tenor
	 * 
	 * @return The Number of Days
	 */

	public static final int TenorToDays (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("AnalyticsHelper::TenorToDays => Invalid Inputs");

		char chTenor = strTenor.charAt (strTenor.length() - 1);

		int iTimeUnit = (int) new java.lang.Double (strTenor.substring (0, strTenor.length() -
			1)).doubleValue();

		if ('d' == chTenor || 'D' == chTenor) return iTimeUnit;

		if ('w' == chTenor || 'W' == chTenor) return iTimeUnit * 7;

		if ('l' == chTenor || 'L' == chTenor) return iTimeUnit * 28;

		if ('m' == chTenor || 'M' == chTenor) return iTimeUnit * 30;

		if ('y' == chTenor || 'Y' == chTenor) return (int) (365.25 * iTimeUnit);

		throw new java.lang.Exception ("AnalyticsHelper::TenorToDays => Unknown tenor format " + strTenor);
	}

	/**
	 * Retrieve the Number of Days from the Tenor
	 * 
	 * @param strTenor The Specified Tenor
	 * 
	 * @return The Number of Days
	 */

	public static final double TenorToYearFraction (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("AnalyticsHelper::TenorToYearFraction => Invalid Inputs");

		char chTenor = strTenor.charAt (strTenor.length() - 1);

		int iTimeUnit = (int) new java.lang.Double (strTenor.substring (0, strTenor.length() -
			1)).doubleValue();

		if ('d' == chTenor || 'D' == chTenor) return ((double) iTimeUnit) / 365.25;

		if ('w' == chTenor || 'W' == chTenor) return ((double) (7. * iTimeUnit)) / 365.25;

		if ('l' == chTenor || 'L' == chTenor) return ((double) (28. * iTimeUnit)) / 365.25;

		if ('m' == chTenor || 'M' == chTenor) return ((double) (30. * iTimeUnit)) / 365.25;

		if ('y' == chTenor || 'Y' == chTenor) return iTimeUnit;

		throw new java.lang.Exception ("AnalyticsHelper::TenorToDays => Unknown tenor format " + strTenor);
	}

	/**
	 * Retrieve the month code from input frequency
	 * 
	 * @param iFreq Integer frequency
	 * 
	 * @return String representing the month code
	 */

	public static final java.lang.String GetMonthCodeFromFreq (
		final int iFreq)
	{
		if (1 == iFreq) return "0012M";

		if (2 == iFreq) return "0006M";

		if (3 == iFreq) return "0004M";

		if (4 == iFreq) return "0003M";

		if (6 == iFreq) return "0002M";

		if (12 == iFreq) return "0001M";

		return null;
	}

	/**
	 * Calculate the rate index from the coupon currency and the frequency
	 * 
	 * @param strCouponCurrency String representing the coupon currency
	 * @param iCouponFreq Integer representing the coupon frequency
	 * 
	 * @return String representing the rate index
	 */

	public static final java.lang.String CalcRateIndex (
		final java.lang.String strCouponCurrency,
		final int iCouponFreq)
	{
		if (null == strCouponCurrency || strCouponCurrency.isEmpty()) {
			if (s_bBlog) System.out.println ("AnalyticsHelper::CalcRateIndex => Cpn ccy is null!");

			return null;
		}

		java.lang.String strFreqMonthCode = GetMonthCodeFromFreq (iCouponFreq);

		if (null == strFreqMonthCode) {
			if (s_bBlog)
				System.out.println ("AnalyticsHelper::CalcRateIndex => Cpn freq is " + iCouponFreq);

			return null;
		}

		return strCouponCurrency.substring (0, 2) + strFreqMonthCode;
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
			return org.drip.analytics.date.JulianDate.CreateFromYMD (org.drip.quant.common.DateUtil.GetYear
				(dt), org.drip.quant.common.DateUtil.GetMonth (dt), org.drip.quant.common.DateUtil.GetDate
					(dt));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
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
			return org.drip.analytics.date.JulianDate.CreateFromYMD (2000 + new java.lang.Integer
				(astrDMY[2].trim()), org.drip.analytics.date.JulianDate.MonthFromMonthChars
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
			return org.drip.analytics.date.JulianDate.CreateFromYMD (new java.lang.Integer
				(astrYYYYMMDD[0].trim()), new java.lang.Integer (astrYYYYMMDD[1].trim()), new
					java.lang.Integer (astrYYYYMMDD[2].trim()));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get the DRIP day count from the Bloomberg code
	 * 
	 * @param strBBGDC String representing the Bloomberg day count convention
	 * 
	 * @return String representing DRIP day count
	 */

	public static final java.lang.String GetDayCountFromBBGCode (
		final java.lang.String strBBGDC)
	{
		if (null == strBBGDC || strBBGDC.isEmpty()) return "30/360";

		return "30/360";
	}

	/**
	 * Calculate the rate index from currency and coupon frequency
	 * 
	 * @param strCcy String representing coupon currency
	 * @param iCouponFreq Integer representing coupon frequency
	 * 
	 * @return String representing the rate index
	 */

	public static final java.lang.String RateIndexFromCcyAndCouponFreq (
		final java.lang.String strCcy,
		final int iCouponFreq)
	{
		if (null == strCcy || strCcy.isEmpty() || 0 >= iCouponFreq) return "";

		java.lang.String strCcyPrefix = strCcy.substring (0, 2);

		if (1 == iCouponFreq)  return strCcyPrefix + "0012M";

		if (2 == iCouponFreq)  return strCcyPrefix + "0006M";

		if (3 == iCouponFreq)  return strCcyPrefix + "0004M";

		if (4 == iCouponFreq)  return strCcyPrefix + "0003M";

		if (6 == iCouponFreq)  return strCcyPrefix + "0002M";

		if (12 == iCouponFreq)  return strCcyPrefix + "0001M";

		return "";
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
			return org.drip.analytics.date.JulianDate.CreateFromYMD ((int) new java.lang.Double
				(astrFields[2].trim()).doubleValue(), (int) new java.lang.Double
					(astrFields[0].trim()).doubleValue(), (int) new java.lang.Double
						(astrFields[1].trim()).doubleValue());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Switch the given IR curve if necessary
	 * 
	 * @param strCurveIn String representing the input curve
	 * 
	 * @return String representing the switched curve
	 */

	public static final java.lang.String SwitchIRCurve (
		final java.lang.String strCurveIn)
	{
		if (null == strCurveIn) return null;

		if (!s_mapIRSwitch.containsKey (strCurveIn)) return strCurveIn;

		return s_mapIRSwitch.get (strCurveIn);
	}

	/**
	 * Create the Latent State Fixings object from the bond, the fixings date, and the fixing.
	 * 
	 * @param bond The input bond
	 * @param dtFixing The Fixings Date
	 * @param dblFixing Double representing the fixing
	 * 
	 * @return The Latent State Fixings Instance
	 */
	
	public static final org.drip.param.market.LatentStateFixingsContainer CreateFixingsObject (
		final org.drip.product.definition.Bond bond,
		final org.drip.analytics.date.JulianDate dtFixing,
		final double dblFixing)
	{
		if (!bond.isFloater()) return null;

		org.drip.param.market.LatentStateFixingsContainer lsfc = new
			org.drip.param.market.LatentStateFixingsContainer();

		return lsfc.add (dtFixing, bond.forwardLabel()[0], dblFixing) ? lsfc : null;
	}

	/**
	 * Bump the input array quotes
	 * 
	 * @param adblQuotesIn Array of the input double quotes
	 * @param dblBump Bump amount
	 * @param bIsProportional True => Bump is proportional
	 * 
	 * @return Bumped array output
	 */

	public static final double[] BumpQuotes (
		final double[] adblQuotesIn,
		final double dblBump,
		final boolean bIsProportional)
	{
		if (null == adblQuotesIn || 0 == adblQuotesIn.length || !org.drip.quant.common.NumberUtil.IsValid
			(dblBump))
			return null;

		double[] adblQuotesOut = new double[adblQuotesIn.length];

		for (int i = 0; i < adblQuotesIn.length; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblQuotesIn[i])) return null;

			if (!bIsProportional)
				adblQuotesOut[i] = adblQuotesIn[i] + dblBump;
			else
				adblQuotesOut[i] = adblQuotesIn[i] * (1. + dblBump);
		}

		return adblQuotesOut;
	}

	/**
	 * Tweak the Manifest Measures (gor the given set of nodes) in accordance with the specified tweak
	 *  parameters
	 * 
	 * @param adblQuotesIn Array of quotes to be bumped
	 * @param ntp NodeTweakParams input
	 * 
	 * @return Bumped array output
	 */

	public static final double[] TweakManifestMeasure (
		final double[] adblQuotesIn,
		final org.drip.param.definition.ResponseValueTweakParams ntp)
	{
		if (null == adblQuotesIn || 0 == adblQuotesIn.length || null == ntp) return adblQuotesIn;

		double[] adblQuotesOut = new double[adblQuotesIn.length];

		if (org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK ==
			ntp._iTweakNode) {
			for (int i = 0; i < adblQuotesIn.length; ++i) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblQuotesIn[i])) return null;

				if (!ntp._bIsTweakProportional)
					adblQuotesOut[i] = adblQuotesIn[i] + ntp._dblTweakAmount;
				else
					adblQuotesOut[i] = adblQuotesIn[i] * (1. + ntp._dblTweakAmount);
			}
		} else {
			if (ntp._iTweakNode < 0 || ntp._iTweakNode >= adblQuotesIn.length) return null;

			for (int i = 0; i < adblQuotesIn.length; ++i) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblQuotesIn[i])) return null;

				if (i == ntp._iTweakNode) {
					if (!ntp._bIsTweakProportional)
						adblQuotesOut[i] = adblQuotesIn[i] + ntp._dblTweakAmount;
					else
						adblQuotesOut[i] = adblQuotesIn[i] * (1. + ntp._dblTweakAmount);
				} else
					adblQuotesOut[i] = adblQuotesIn[i];
			}
		}

		return adblQuotesOut;
	}

	/**
	 * Merge two lists of periods
	 * 
	 * @param lsPeriod1 Period 1
	 * @param lsPeriod2 Period 2
	 * 
	 * @return The Merged Period List
	 */

	public static final java.util.List<org.drip.analytics.cashflow.CouponPeriod> MergePeriodLists (
		final java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsPeriod1,
		final java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsPeriod2)
	{
		if ((null == lsPeriod1 || 0 == lsPeriod1.size()) && (null == lsPeriod2 || 0 == lsPeriod2.size()))
			return null;

		java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsPeriodMerged = new
			java.util.ArrayList<org.drip.analytics.cashflow.CouponPeriod>();

		if (null == lsPeriod1 || 0 == lsPeriod1.size()) {
			for (org.drip.analytics.cashflow.CouponPeriod p : lsPeriod2) {
				if (null != p) lsPeriodMerged.add (p);
			}

			return lsPeriodMerged;
		}

		if (null == lsPeriod2 || 0 == lsPeriod2.size()) {
			for (org.drip.analytics.cashflow.CouponPeriod p : lsPeriod1) {
				if (null != p) lsPeriodMerged.add (p);
			}

			return lsPeriodMerged;
		}

		int iPeriod1Index = 0;
		int iPeriod2Index = 0;

		while (iPeriod1Index < lsPeriod1.size() && iPeriod2Index < lsPeriod2.size()) {
			org.drip.analytics.cashflow.CouponPeriod p1 = lsPeriod1.get (iPeriod1Index);

			org.drip.analytics.cashflow.CouponPeriod p2 = lsPeriod2.get (iPeriod2Index);

			if (p1.payDate() < p2.payDate()) {
				lsPeriodMerged.add (p1);

				++iPeriod1Index;
			} else {
				lsPeriodMerged.add (p2);

				++iPeriod2Index;
			}
		}

		if (iPeriod1Index < lsPeriod1.size() - 1) {
			for (int i = iPeriod1Index; i < lsPeriod1.size(); ++i)
				lsPeriodMerged.add (lsPeriod1.get (i));
		} else if (iPeriod2Index < lsPeriod2.size() - 1) {
			for (int i = iPeriod2Index; i < lsPeriod2.size(); ++i)
				lsPeriodMerged.add (lsPeriod2.get (i));
		}

		return lsPeriodMerged;
	}

	/**
	 * Aggregate the period lists for an array of components
	 * 
	 * @param aComp Array of Components
	 * 
	 * @return The Aggregated Period Set
	 */

	public static final java.util.Set<org.drip.analytics.cashflow.CouponPeriod> AggregateComponentPeriods (
		final org.drip.product.definition.FixedIncomeComponent[] aComp)
	{
		if (null == aComp) return null;

		int iStartIndex = 0;
		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i) {
			if (null != aComp[i]) {
				iStartIndex = i;
				break;
			}
		}

		java.util.Set<org.drip.analytics.cashflow.CouponPeriod> setAggregatedPeriod = new
			java.util.TreeSet<org.drip.analytics.cashflow.CouponPeriod>();

		for (int i = iStartIndex; i < iNumComp; ++i) {
			if (null == aComp[i]) continue;

			java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsCompPeriod =
				aComp[i].cashFlowPeriod();

			if (null == lsCompPeriod || 0 == lsCompPeriod.size()) continue;

			for (org.drip.analytics.cashflow.CouponPeriod p : lsCompPeriod) {
				if (null != p) setAggregatedPeriod.add (p);
			}
		}

		return setAggregatedPeriod;
	}

	/**
	 * Append the Prefixed Map Entries of the specified Input Map onto the Output Map
	 * 
	 * @param mapOutput The Output Map
	 * @param strPrefix The Entry Prefix
	 * @param mapInput The Input Map
	 * 
	 * @return TRUE => At least one entry appended
	 */

	public static final boolean AccumulateMeasures (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput,
		final java.lang.String strPrefix,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapInput)
	{
		if (null == mapOutput || null == strPrefix || strPrefix.isEmpty() || null == mapInput) return false;

		java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapInputESSingle =
			mapInput.entrySet();

		if (null == mapInputESSingle) return false;

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapInputESSingle) {
			if (null == me) continue;

			java.lang.String strKey = me.getKey();

			if (null == strKey || strKey.isEmpty()) continue;

			mapOutput.put (strPrefix + "@" + strKey, me.getValue());
		}

		return true;
	}

	/**
	 * Do the Left and the Right Labels Match?
	 * 
	 * @param lslLeft Left Cash Flow Period Label
	 * @param lslRight Right Cash Flow Period Label
	 * 
	 * @return TRUE => The Labels Match
	 */

	public static final boolean LabelMatch (
		final org.drip.state.identifier.LatentStateLabel lslLeft,
		final org.drip.state.identifier.LatentStateLabel lslRight)
	{
		if (null == lslLeft && null == lslRight) return true;

		if ((null == lslLeft && null != lslRight) || (null != lslLeft && null == lslRight)) return false;

		return lslLeft.match (lslRight);
	}
}
