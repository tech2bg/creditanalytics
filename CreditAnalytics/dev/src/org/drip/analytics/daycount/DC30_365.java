
package org.drip.analytics.daycount;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * This class implements the 30/365 day count convention.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DC30_365 implements org.drip.analytics.daycount.DCFCalculator {

	/**
	 * Empty DC30_365 constructor
	 */

	public DC30_365()
	{
	}

	@Override public java.lang.String baseCalculationType()
	{
		return "DC30_365";
	}

	@Override public java.lang.String[] alternateNames()
	{
		return new java.lang.String[] {"30/365", "ISMA 30/365", "ISDA SWAPS:30/365", "ISDA30/365",
			"ISDA 30E/365", "DC30_365"};
	}

	@Override public double yearFraction (
		final double dblStart,
		final double dblEnd,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		DateEOMAdjustment dm = DateEOMAdjustment.MakeDEOMA30_365 (dblStart, dblEnd, bApplyEOMAdj);

		if (null == dm)
			throw new java.lang.Exception ("DC30_365.yearFraction: Cannot create DateEOMAdjustment!");

		return (365.* (org.drip.analytics.date.DateUtil.Year (dblEnd) - org.drip.analytics.date.DateUtil.Year
			(dblStart)) + 30. * (org.drip.analytics.date.DateUtil.Month (dblEnd) -
				org.drip.analytics.date.DateUtil.Month (dblStart) + dm.posterior() - dm.anterior()) +
					(org.drip.analytics.date.DateUtil.Day (dblEnd) - org.drip.analytics.date.DateUtil.Day
						(dblStart))) / 365.;
	}

	@Override public int daysAccrued (
		final double dblStart,
		final double dblEnd,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		DateEOMAdjustment dm = DateEOMAdjustment.MakeDEOMA30_365 (dblStart, dblEnd, bApplyEOMAdj);

		if (null == dm)
			throw new java.lang.Exception ("DC30_365.daysAccrued: Cannot create DateEOMAdjustment!");

		return 365 * (org.drip.analytics.date.DateUtil.Year (dblEnd) - org.drip.analytics.date.DateUtil.Year
			(dblStart)) + 30 * (org.drip.analytics.date.DateUtil.Month (dblEnd) -
				org.drip.analytics.date.DateUtil.Month (dblStart) + dm.posterior() - dm.anterior()) +
					(org.drip.analytics.date.DateUtil.Day (dblEnd) - org.drip.analytics.date.DateUtil.Day
						(dblStart));
	}
}
