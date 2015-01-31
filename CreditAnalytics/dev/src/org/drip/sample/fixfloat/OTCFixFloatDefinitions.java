
package org.drip.sample.fixfloat;

import org.drip.market.otc.IBORFixedFloatContainer;
import org.drip.service.api.CreditAnalytics;

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
 * OTCFixFloatDefinitions contains all the pre-fixed definitions of the OTC Fix-Float IRS contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCFixFloatDefinitions {
	private static final void DisplayIRSOTCInfo (
		String strCurrency,
		String strLocation,
		String strMaturityTenor,
		String strIndex)
	{
		System.out.println (
			"\t" + strCurrency + "-" + strLocation + "-" + strMaturityTenor + "-" + strIndex + " => " +
			IBORFixedFloatContainer.ConventionFromJurisdiction (
				strCurrency,
				strLocation,
				strMaturityTenor,
				strIndex
			)
		);
	}

	public static final void main (
		final String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------\n");

		DisplayIRSOTCInfo ("AUD", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("AUD", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("CAD", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("CAD", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("CHF", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("CHF", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("CNY", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("CNY", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("DKK", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("DKK", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("EUR", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("EUR", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("GBP", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("GBP", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("HKD", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("HKD", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("INR", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("INR", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("JPY", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("JPY", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("JPY", "ALL", "1Y", "TIBOR");

		DisplayIRSOTCInfo ("JPY", "ALL", "5Y", "TIBOR");

		DisplayIRSOTCInfo ("NOK", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("NOK", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("NZD", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("NZD", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("PLN", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("PLN", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("SEK", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("SEK", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("SGD", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("SGD", "ALL", "5Y", "MAIN");

		DisplayIRSOTCInfo ("USD", "LON", "1Y", "MAIN");

		DisplayIRSOTCInfo ("USD", "LON", "5Y", "MAIN");

		DisplayIRSOTCInfo ("USD", "NYC", "1Y", "MAIN");

		DisplayIRSOTCInfo ("USD", "NYC", "5Y", "MAIN");

		DisplayIRSOTCInfo ("ZAR", "ALL", "1Y", "MAIN");

		DisplayIRSOTCInfo ("ZAR", "ALL", "5Y", "MAIN");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------\n");
	}
}
