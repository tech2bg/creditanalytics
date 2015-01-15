
package org.drip.sample.futures;

import org.drip.market.exchange.*;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * FuturesOptionsDefinition demonstrates the functionality to retrieve the Futures Options Definitions for
 *  the various Jurisdictions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FuturesOptionsDefinition {
	private static final void DisplayExchangeInfo (
		final String strFullyQualifiedName,
		final String strTradingMode)
	{
		FuturesOptions fo = FuturesOptionsContainer.ExchangeInfo (
			strFullyQualifiedName,
			strTradingMode
		);

		String strExchangeLTDS = "";

		for (String strExchange : fo.exchanges()) {
			strExchangeLTDS += "\n\t[" + strExchange + "=>";

			for (int i = 0; i < fo.ltdsArray (strExchange).length; ++i) {
				if (0 != i) strExchangeLTDS += "; ";

				strExchangeLTDS += fo.ltdsArray (strExchange)[i];
			}

			strExchangeLTDS += "]";
		}

		System.out.println (
			fo.fullyQualifiedName() + " | " +
			fo.tradingMode() +
			strExchangeLTDS
		);
	}

	public static final void main (
		final String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayExchangeInfo (
			"CHF-LIBOR-3M",
			"MARGIN"
		);

		DisplayExchangeInfo (
			"GBP-LIBOR-3M",
			"MARGIN"
		);

		DisplayExchangeInfo (
			"EUR-EURIBOR-3M",
			"MARGIN"
		);

		DisplayExchangeInfo (
			"JPY-LIBOR-3M",
			"PREMIUM"
		);

		DisplayExchangeInfo (
			"JPY-TIBOR-3M",
			"PREMIUM"
		);

		DisplayExchangeInfo (
			"JPY-LIBOR-3M",
			"PREMIUM"
		);

		DisplayExchangeInfo (
			"USD-LIBOR-1M",
			"PREMIUM"
		);

		DisplayExchangeInfo (
			"USD-LIBOR-3M",
			"MARGIN"
		);

		DisplayExchangeInfo (
			"USD-LIBOR-3M",
			"PREMIUM"
		);
	}
}
