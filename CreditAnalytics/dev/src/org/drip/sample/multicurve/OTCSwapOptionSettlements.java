
package org.drip.sample.multicurve;

import org.drip.market.otc.SwapOptionSettlementContainer;
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
 * OTCSwapOptionSettlements contains all the pre-fixed Definitions of the OTC Swap Option Settlements.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCSwapOptionSettlements {
	private static final void DisplayOTCSwapOptionSettlement (
		final String strCurrency)
	{
		System.out.println ("\t\t" + strCurrency + " => " +
			SwapOptionSettlementContainer.ConventionFromJurisdiction (strCurrency)
		);
	}

	public static final void main (
		final String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\tL -> R:");

		System.out.println ("\t\t\tReference Currency");

		System.out.println ("\t\t\tSettlement Type");

		System.out.println ("\t\t\tSettlement Quote Valuation (for Cash Settled Options)");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");

		DisplayOTCSwapOptionSettlement ("AUD");

		DisplayOTCSwapOptionSettlement ("CHF");

		DisplayOTCSwapOptionSettlement ("DKK");

		DisplayOTCSwapOptionSettlement ("EUR");

		DisplayOTCSwapOptionSettlement ("GBP");

		DisplayOTCSwapOptionSettlement ("JPY");

		DisplayOTCSwapOptionSettlement ("NOK");

		DisplayOTCSwapOptionSettlement ("SEK");

		DisplayOTCSwapOptionSettlement ("USD");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");
	}
}
