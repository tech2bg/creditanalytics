
package org.drip.sample.misc;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.service.api.CreditAnalytics;

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
 * DateRoll demonstrates Date Roll Functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DateRoll {
	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		JulianDate dt = JulianDate.CreateFromYMD (2012, JulianDate.FEBRUARY, 5);

		System.out.println ("\tDATE_ROLL_ACTUAL                      : " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_ACTUAL,
					"USD"
				)
			)
		);

		System.out.println ("\tDATE_ROLL_FOLLOWING                   : " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_FOLLOWING,
					"USD"
				)
			)
		);

		System.out.println ("\tDATE_ROLL_MODIFIED_FOLLOWING          : " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_MODIFIED_FOLLOWING,
					"USD"
				)
			)
		);

		System.out.println ("\tDATE_ROLL_MODIFIED_FOLLOWING_BIMONTHLY: " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_MODIFIED_FOLLOWING_BIMONTHLY,
					"USD"
				)
			)
		);

		System.out.println ("\tDATE_ROLL_PREVIOUS                    : " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_PREVIOUS,
					"USD"
				)
			)
		);

		System.out.println ("\tDATE_ROLL_MODIFIED_PREVIOUS           : " + dt + " -> " + new JulianDate (
			Convention.RollDate (
					dt.julian(),
					Convention.DATE_ROLL_MODIFIED_PREVIOUS,
					"USD"
				)
			)
		);
	}
}
