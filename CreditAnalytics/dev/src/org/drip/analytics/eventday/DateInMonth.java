
package org.drip.analytics.eventday;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * DateInMonth exports Functionality that generates the specific Event Date inside of the specified
 * 	Month/Year.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DateInMonth {

	/**
	 * Instance Date Generation Rules - Generate from Lag from Front/Back
	 */

	public static final int INSTANCE_GENERATOR_RULE_EDGE_LAG = 1;

	/**
	 * Instance Date Generation Rule - Generate from Specified Day in Week/Week in Month
	 */

	public static final int INSTANCE_GENERATOR_RULE_WEEK_DAY = 2;

	private int _iLag = -1;
	private int _iDayOfWeek = -1;
	private int _iWeekInMonth = -1;
	private boolean _bFromBack = false;
	private int _iInstanceGeneratorRule = -1;

	/**
	 * DateInMonth Constructor
	 * 
	 * @param iInstanceGeneratorRule Instance Generation Rule
	 * @param bFromBack TRUE => Apply Rules from Back of EOM
	 * @param iLag The Lag
	 * @param iDayOfWeek Day of Week
	 * @param iWeekInMonth Week in the Month
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public DateInMonth (
		final int iInstanceGeneratorRule,
		final boolean bFromBack,
		final int iLag,
		final int iDayOfWeek,
		final int iWeekInMonth)
		throws java.lang.Exception
	{
		_bFromBack = bFromBack;

		if (INSTANCE_GENERATOR_RULE_EDGE_LAG == (_iInstanceGeneratorRule = iInstanceGeneratorRule)) {
			if (0 > (_iLag = iLag)) throw new java.lang.Exception ("DateInMonth ctr: Invalid Inputs");
		} else {
			_iDayOfWeek = iDayOfWeek;
			_iWeekInMonth = iWeekInMonth;
		}
	}

	/**
	 * Retrieve the Instance Generation Rule
	 * 
	 * @return The Instance Generation Rule
	 */

	public int instanceGenerator()
	{
		return _iInstanceGeneratorRule;
	}

	/**
	 * Retrieve the Flag indicating whether the Lag is from the Front/Back
	 * 
	 * @return TRUE => The Lag is from the Back.
	 */

	public boolean fromBack()
	{
		return _bFromBack;
	}

	/**
	 * Retrieve the Date Lag
	 * 
	 * @return The Date Lag
	 */

	public int lag()
	{
		return _iLag;
	}

	/**
	 * Retrieve the Week In Month
	 * 
	 * @return The Week In Month
	 */

	public int weekInMonth()
	{
		return _iWeekInMonth;
	}

	/**
	 * Retrieve the Day Of Week
	 * 
	 * @return The Day Of Week
	 */

	public int dayOfWeek()
	{
		return _iDayOfWeek;
	}

	public double instanceDay (
		final int iYear,
		final int iMonth,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (INSTANCE_GENERATOR_RULE_EDGE_LAG == _iInstanceGeneratorRule)
			return _bFromBack ? org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, iMonth,
				org.drip.analytics.date.DateUtil.DaysInMonth (iYear, iMonth)).subtractBusDays (_iLag,
					strCalendar).julian() : org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, iMonth,
						1).addBusDays (_iLag, strCalendar).julian();

		if (_bFromBack) {
			org.drip.analytics.date.JulianDate dtEOM = org.drip.analytics.date.DateUtil.CreateFromYMD (iYear,
				iMonth, org.drip.analytics.date.DateUtil.DaysInMonth (iYear, iMonth));

			while (_iDayOfWeek != org.drip.analytics.date.DateUtil.Day
				(org.drip.analytics.date.DateUtil.JavaDateFromJulianDate (dtEOM)))
				dtEOM = dtEOM.subtractDays (1);

			return dtEOM.subtractDays (_iWeekInMonth * 7).julian();
		}

		org.drip.analytics.date.JulianDate dtSOM = org.drip.analytics.date.DateUtil.CreateFromYMD (iYear,
			iMonth, 1);

		while (_iDayOfWeek != org.drip.analytics.date.DateUtil.Day
			(org.drip.analytics.date.DateUtil.JavaDateFromJulianDate (dtSOM)))
			dtSOM = dtSOM.addDays (1);

		return dtSOM.addDays (_iWeekInMonth * 7).julian();
	}

	@Override public java.lang.String toString()
	{
		return "[DateInMonth => Instance Generator Rule: " + _iInstanceGeneratorRule + " | From Back Flag: "
			+ _bFromBack + " | Day Of Week: " + _iDayOfWeek + " | Week In Month: " + _iWeekInMonth +
				" | Lag: " + _iLag + "]";
	}
}
