
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
 * Fixed contains the fixed holiday’s date and month. Holidays are generated on a per-year basis by applying
 * 	the year, and by adjusting the date generated.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Fixed extends Base {
	private int _iDay = 0;
	private int _iMonth = 0;
	private Weekend _wkend = null;

	/**
	 * Construct the object from the day, month, weekend, and description
	 * 
	 * @param iDay Day
	 * @param iMonth Month
	 * @param wkend Weekend Object
	 * @param strDescription Description
	 */

	public Fixed (
		final int iDay,
		final int iMonth,
		final Weekend wkend,
		final java.lang.String strDescription)
	{
		super (strDescription);

		_iDay = iDay;
		_wkend = wkend;
		_iMonth = iMonth;
	}

	@Override public double dateInYear (
		final int iYear,
		final boolean bAdjust)
	{
		double dblDate = java.lang.Double.NaN;

		try {
			dblDate = org.drip.analytics.date.DateUtil.CreateFromYMD (iYear, _iMonth, _iDay).julian();

			if (bAdjust) return Base.rollHoliday (dblDate, true, _wkend);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dblDate;
	}
}
