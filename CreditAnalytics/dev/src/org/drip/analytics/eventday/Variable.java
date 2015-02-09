
package org.drip.analytics.eventday;

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
 * Variable class contains the rule characterizing the variable holiday’s month, day in week, week in month,
 * 	and the weekend days. Specific holidays in the given year are generated using these rules.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Variable extends Base {
	private int _iMonth = 0;
	private int _iWeekDay = 0;
	private int _iWeekInMonth = 0;
	private boolean _bFromFront = true;
	private org.drip.analytics.eventday.Weekend _wkend = null;

	/**
	 * Construct the object from the week, day, month, from front/back, week end, and description
	 * 
	 * @param iWeekInMonth Week of the Month
	 * @param iWeekDay Day of the Week
	 * @param iMonth Month
	 * @param bFromFront From Front (true), Back (false)
	 * @param wkend Weekend
	 * @param strDescription Description
	 */

	public Variable (
		final int iWeekInMonth,
		final int iWeekDay,
		final int iMonth,
		final boolean bFromFront,
		final Weekend wkend,
		final java.lang.String strDescription)
	{
		super (strDescription);

		_wkend = wkend;
		_iMonth = iMonth;
		_iWeekDay = iWeekDay;
		_bFromFront = bFromFront;
		_iWeekInMonth = iWeekInMonth;
	}

	@Override public double dateInYear (
		final int iYear,
		final boolean bAdjustForWeekend)
	{
		double dblDate = java.lang.Double.NaN;

		try {
			if (_bFromFront)
				dblDate = (org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, _iMonth,
					_iWeekDay)).julian() + (7 * (_iWeekInMonth - 1));
			else {
				dblDate = (org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, _iMonth,
					org.drip.analytics.date.DateUtil.DaysInMonth (_iMonth, iYear))).julian() - (7 *
						(_iWeekInMonth - 1));

				while (_iWeekDay != (dblDate % 7)) --dblDate;
			}

			if (bAdjustForWeekend) return Base.rollHoliday (dblDate, true, _wkend);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dblDate;
	}
}
