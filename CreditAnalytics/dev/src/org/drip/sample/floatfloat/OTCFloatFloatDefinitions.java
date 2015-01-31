
package org.drip.sample.floatfloat;

import org.drip.market.otc.*;
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
 * OTCFloatFloatDefinitions contains all the pre-fixed Definitions of the OTC Float-Float Swap Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCFloatFloatDefinitions {
	private static final void DisplayOTCInfo (
		String strCurrency)
	{
		IBORFloatFloatConvention ffConv = IBORFloatFloatContainer.ConventionFromJurisdiction (strCurrency);

		System.out.println (
			"\t\t" + strCurrency + " => " +
			ffConv.referenceTenor() + " | " +
			ffConv.spotLag() + " | " +
			ffConv.basisOnDerivedStream() + " | " +
			ffConv.basisOnDerivedComponent() + " | " +
			ffConv.derivedCompoundedToReference() + " | " +
			ffConv.componentPair()
		);
	}

	public static final void main (
		String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\tL -> R:");

		System.out.println ("\t\t\tCurrency");

		System.out.println ("\t\t\tReference Tenor");

		System.out.println ("\t\t\tSpot Lag");

		System.out.println ("\t\t\tBasis on Derived Stream");

		System.out.println ("\t\t\tBasis on Derived Component");

		System.out.println ("\t\t\tDerived Stream Compounded To Reference Stream");

		System.out.println ("\t\t\tComponent Pair");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");

		DisplayOTCInfo ("AUD");

		DisplayOTCInfo ("CAD");

		DisplayOTCInfo ("CHF");

		DisplayOTCInfo ("CNY");

		DisplayOTCInfo ("DKK");

		DisplayOTCInfo ("EUR");

		DisplayOTCInfo ("GBP");

		DisplayOTCInfo ("HKD");

		DisplayOTCInfo ("INR");

		DisplayOTCInfo ("JPY");

		DisplayOTCInfo ("NOK");

		DisplayOTCInfo ("NZD");

		DisplayOTCInfo ("PLN");

		DisplayOTCInfo ("SEK");

		DisplayOTCInfo ("SGD");

		DisplayOTCInfo ("USD");

		DisplayOTCInfo ("ZAR");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");
	}
}
