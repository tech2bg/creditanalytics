
package org.drip.analytics.holiday;

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
 * Base is an abstraction around holiday and description. Abstract function generates an optional
 * 	adjustment for weekends in a given year.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class Base {
	private java.lang.String _strDescription = "";

	/**
	 * Constructs the Base instance from the description
	 * 
	 * @param strDescription Holiday Description
	 */

	public Base (
		final java.lang.String strDescription)
	{
		_strDescription = strDescription;
	}

	/**
	 * Roll the date to a non-holiday according to the rule specified
	 * 
	 * @param dblDate Date to be rolled
	 * @param bBalkOnYearShift Throw an exception if the year change happens
	 * @param wkend Object representing the weekend days
	 * 
	 * @return The adjusted date
	 * 
	 * @throws java.lang.Exception Thrown if the holiday cannot be rolled
	 */

	public static final double rollHoliday (
		final double dblDate,
		final boolean bBalkOnYearShift,
		final Weekend wkend)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Base::rollHoliday => Cannot Roll NaN date!");

		double dblRolledDate = dblDate;

		if (null != wkend && wkend.isLeftWeekend (dblDate)) dblRolledDate = dblDate - 1;

		if (null != wkend && wkend.isRightWeekend (dblDate)) dblRolledDate = dblDate + 1;

		if (bBalkOnYearShift & org.drip.analytics.date.DateUtil.Year (dblDate) !=
			org.drip.analytics.date.DateUtil.Year (dblRolledDate))
			return -1.;

		return dblRolledDate;
	}

	/**
	 * Return the description
	 * 
	 * @return Description
	 */

	public java.lang.String description()
	{
		return _strDescription;
	}

	/**
	 * Generate the full date specific to the input year
	 * 
	 * @param iYear Input Year
	 * @param bAdjusted Whether adjustment is desired
	 * 
	 * @return The full date
	 */

	public abstract double dateInYear (
		final int iYear,
		final boolean bAdjusted);
}
