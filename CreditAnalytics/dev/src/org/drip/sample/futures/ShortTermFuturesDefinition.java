
package org.drip.sample.futures;

import org.drip.market.definition.*;
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
 * ShortTermFuturesDefinition illustrates the Construction and Usage of the Short Term Futures Exchange
 *  Details.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ShortTermFuturesDefinition {
	private static final void DisplayExchangeInfo (
		final String strFullyQualifiedNameName)
	{
		ShortTermFutures stf = ShortTermFuturesContainer.ExchangeInfo (strFullyQualifiedNameName);

		String strExchange = "";

		for (int i = 0; i < stf.exchanges().length; ++i) {
			strExchange += stf.exchanges()[i];

			if (0 != i) strExchange += " | ";
		}

		System.out.println ("\t[" +
			strFullyQualifiedNameName + "] => " +
			stf.notional() + " || " +
			strExchange
		);
	}

	public static final void main (
		String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayExchangeInfo ("CAD-CDOR-3M");

		DisplayExchangeInfo ("CHF-LIBOR-3M");

		DisplayExchangeInfo ("DKK-CIBOR-3M");

		DisplayExchangeInfo ("EUR-EURIBOR-3M");

		DisplayExchangeInfo ("GBP-LIBOR-3M");

		DisplayExchangeInfo ("JPY-LIBOR-3M");

		DisplayExchangeInfo ("JPY-TIBOR-3M");

		DisplayExchangeInfo ("USD-LIBOR-1M");

		DisplayExchangeInfo ("USD-LIBOR-3M");

		DisplayExchangeInfo ("ZAR-JIBAR-3M");
	}
}
