
package org.drip.quant.common;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * DateUtil implements date utility functions those are extraneous to the JulianDate implementation. It
 * 	exposes the following functionality:
 * 	- Retrieve Day, Month, and Year From Java Date
 * 	- Switch between multiple date formats (Oracle Date, BBG Date, different string representations etc).
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DateUtil {

	/**
	 * Returns the date corresponding to the input java.util.Date
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return Date
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int GetDate (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::GetDate => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.DATE);
	}

	/**
	 * Returns the month corresponding to the input java.util.Date. 1 => January, and 12 => December
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return Month
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int GetMonth (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::GetMonth => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.MONTH) + 1;
	}

	/**
	 * Returns the year corresponding to the input java.util.Date.
	 * 
	 * @param dt java.util.Date Input
	 * 
	 * @return Year
	 * 
	 * @throws java.lang.Exception Thrown if input date is invalid
	 */

	public static final int GetYear (
		final java.util.Date dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DateUtil::GetYear => Invalid Date");

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime (dt);

		return cal.get (java.util.Calendar.YEAR);
	}

	/**
	 * Creates an Oracle date trigram from a YYYYMMDD string
	 * 
	 * @param strYYYYMMDD Date string in the YYYYMMDD format.
	 * 
	 * @return Oracle date trigram string
	 */

	public static java.lang.String MakeOracleDateFromYYYYMMDD (
		final java.lang.String strYYYYMMDD)
	{
		if (null == strYYYYMMDD || strYYYYMMDD.isEmpty()) return null;

		try {
			return strYYYYMMDD.substring (6) + "-" + org.drip.analytics.date.JulianDate.getMonthOracleChar
				((new java.lang.Integer (strYYYYMMDD.substring (4, 6))).intValue()) + "-" +
					strYYYYMMDD.substring (0, 4);
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
			java.lang.String strMonth = org.drip.analytics.date.JulianDate.getMonthOracleChar ((new
				java.lang.Integer (st.nextToken())).intValue());

			if (null == strMonth) return null;

			return st.nextToken() + "-" + strMonth + "-" + st.nextToken();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
