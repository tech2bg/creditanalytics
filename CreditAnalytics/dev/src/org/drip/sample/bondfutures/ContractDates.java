
package org.drip.sample.bondfutures;

import org.drip.analytics.date.*;
import org.drip.market.exchange.*;
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
 * ContractDates illustrates Generation of Event Dates from the Expiry Month/Year of the Bond Futures
 *  Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContractDates {

	private static final void DisplayEventDateInfo (
		String strCurrency,
		String strUnderlierType,
		String strUnderlierSubtype,
		String strMaturityTenor,
		JulianDate dtSettle)
		throws Exception
	{
		BondFuturesConvention bfc = BondFuturesConventionContainer.FromJuristictionTypeMaturity (
			strCurrency,
			strUnderlierType,
			strUnderlierSubtype,
			strMaturityTenor
		);

		System.out.println ("\t| " +
			bfc.eventDates (
				DateUtil.Year (dtSettle.julian()),
				DateUtil.Month (dtSettle.julian())
			) + " | [" +
			strCurrency + "-" +
			strUnderlierType + "-" +
			strUnderlierSubtype + "-" +
			strMaturityTenor + "]"
		);
	}

	public static final void main (
		final String[] args)
		throws Exception
	{
		CreditAnalytics.Init ("");

		System.out.println();

		java.lang.String strForwardTenor = "3M";

		JulianDate dtToday = DateUtil.Today().addTenor (strForwardTenor);

		System.out.println ("\t|------------------------------------------------------------------------------------------------|");

		System.out.println ("\t|   EXPIRY   | DELIV START |  DELIV END | DELIV NOTICE | LAST TRADE |           FUTURE           |");

		System.out.println ("\t|------------------------------------------------------------------------------------------------|");

		DisplayEventDateInfo ("AUD", "BANK", "BILLS", "3M", dtToday);

		DisplayEventDateInfo ("AUD", "TREASURY", "BOND", "3Y", dtToday);

		DisplayEventDateInfo ("AUD", "TREASURY", "BOND", "10Y", dtToday);

		DisplayEventDateInfo ("EUR", "EURO", "SCHATZ", "2Y", dtToday);

		DisplayEventDateInfo ("EUR", "EURO", "BOBL", "5Y", dtToday);

		DisplayEventDateInfo ("EUR", "EURO", "BUND", "10Y", dtToday);

		DisplayEventDateInfo ("EUR", "EURO", "BUXL", "30Y", dtToday);

		DisplayEventDateInfo ("EUR", "TREASURY", "BONO", "10Y", dtToday);

		DisplayEventDateInfo ("GBP", "SHORT", "GILT", "2Y", dtToday);

		DisplayEventDateInfo ("GBP", "MEDIUM", "GILT", "5Y", dtToday);

		DisplayEventDateInfo ("GBP", "LONG", "GILT", "10Y", dtToday);

		DisplayEventDateInfo ("JPY", "TREASURY", "JGB", "5Y", dtToday);

		DisplayEventDateInfo ("JPY", "TREASURY", "JGB", "10Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "NOTE", "2Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "NOTE", "3Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "NOTE", "5Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "NOTE", "10Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "BOND", "30Y", dtToday);

		DisplayEventDateInfo ("USD", "TREASURY", "BOND", "ULTRA", dtToday);

		System.out.println ("\t|------------------------------------------------------------------------------------------------|\n");
	}
}
