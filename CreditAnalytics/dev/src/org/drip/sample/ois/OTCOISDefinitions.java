
package org.drip.sample.ois;

import org.drip.market.otc.OvernightFixedFloatContainer;
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
 * OTCOISDefinitions contains all the pre-fixed definitions of the OTC OIS Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCOISDefinitions {
	private static final void DisplayOvernightOTCInfo (
		String strCurrency)
	{
		System.out.println (
			"\t" + strCurrency + " => " +
			OvernightFixedFloatContainer.ConventionFromJurisdiction (
				strCurrency
			)
		);
	}

	public static final void main (
		final String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------\n");

		DisplayOvernightOTCInfo ("AUD");

		DisplayOvernightOTCInfo ("CAD");

		DisplayOvernightOTCInfo ("GBP");

		DisplayOvernightOTCInfo ("EUR");

		DisplayOvernightOTCInfo ("INR");

		DisplayOvernightOTCInfo ("JPY");

		DisplayOvernightOTCInfo ("SGD");

		DisplayOvernightOTCInfo ("USD");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------\n");
	}
}
