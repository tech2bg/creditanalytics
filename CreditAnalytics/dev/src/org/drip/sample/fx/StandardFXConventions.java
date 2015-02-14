
package org.drip.sample.fx;

import org.drip.market.definition.FXSettingsContainer;
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
 * StandardFXConventions demonstrates the accessing of the Standard FX Currency Order and Currency Pair
 *  Conventions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class StandardFXConventions {

	private static final void CurrencyOrder (
		final String strCurrency)
		throws Exception
	{
		System.out.println ("\t|     " + strCurrency + "   " +
			FXSettingsContainer.CurrencyOrder (
				strCurrency
			) + "    |"
		);
	}

	private static final void CurrencyPairInfo (
		final String strCurrency1,
		final String strCurrency2)
		throws Exception
	{
		System.out.println ("\t|  " + strCurrency1 + "/" + strCurrency2 + " => " +
			FXSettingsContainer.CurrencyPair (
				strCurrency1,
				strCurrency2
			)
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		System.out.println ("\t|----------------|");

		System.out.println ("\t| CURRENCY ORDER |");

		System.out.println ("\t|----------------|");

		CurrencyOrder ("AUD");

		CurrencyOrder ("CAD");

		CurrencyOrder ("CHF");

		CurrencyOrder ("EUR");

		CurrencyOrder ("GBP");

		CurrencyOrder ("JPY");

		CurrencyOrder ("NZD");

		CurrencyOrder ("USD");

		CurrencyOrder ("ZAR");

		System.out.println ("\t|----------------|\n\n");

		System.out.println ("\t|---------------------------------------|");

		System.out.println ("\t|     PAIR    NUM  DENOM  BASE   FACTOR |");

		System.out.println ("\t|---------------------------------------|");

		CurrencyPairInfo ("AUD", "EUR");

		CurrencyPairInfo ("AUD", "USD");

		CurrencyPairInfo ("EUR", "GBP");

		CurrencyPairInfo ("EUR", "JPY");

		CurrencyPairInfo ("EUR", "USD");

		CurrencyPairInfo ("GBP", "JPY");

		CurrencyPairInfo ("GBP", "USD");

		CurrencyPairInfo ("USD", "BRL");

		CurrencyPairInfo ("USD", "CAD");

		CurrencyPairInfo ("USD", "CHF");

		CurrencyPairInfo ("USD", "CNY");

		CurrencyPairInfo ("USD", "EGP");

		CurrencyPairInfo ("USD", "HUF");

		CurrencyPairInfo ("USD", "INR");

		CurrencyPairInfo ("USD", "JPY");

		CurrencyPairInfo ("USD", "KRW");

		CurrencyPairInfo ("USD", "MXN");

		CurrencyPairInfo ("USD", "PLN");

		CurrencyPairInfo ("USD", "TRY");

		CurrencyPairInfo ("USD", "TWD");

		CurrencyPairInfo ("USD", "ZAR");

		System.out.println ("\t|---------------------------------------|");
	}
}
