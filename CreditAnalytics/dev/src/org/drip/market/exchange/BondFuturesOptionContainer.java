
package org.drip.market.exchange;

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
 * BondFuturesOptionContainer holds the Details of the Bond Futures Options Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesOptionContainer {
	private static final java.util.Map<java.lang.String,
		org.drip.market.exchange.BondFuturesOptionConvention> _mapFuturesOptions = new
			java.util.TreeMap<java.lang.String, org.drip.market.exchange.BondFuturesOptionConvention>();

	/**
	 * Initialize the Bond Futures Options Convention Container with the Conventions
	 * 
	 * @return TRUE => The Bond Futures Options Convention Container successfully initialized
	 */

	public static final boolean Init()
	{
		try {
			org.drip.product.params.LastTradingDateSetting[] aLTDS = new
				org.drip.product.params.LastTradingDateSetting[] {new
					org.drip.product.params.LastTradingDateSetting
						(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION, "2D",
							java.lang.Double.NaN), new org.drip.product.params.LastTradingDateSetting
								(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION_QUARTERLY,
									"", java.lang.Double.NaN)};

			_mapFuturesOptions.put ("USD-TREASURY-BOND-ULTRA", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"OUB", "OUL"},
					"USD-TREASURY-BOND-ULTRA", 100000., true, aLTDS));

			_mapFuturesOptions.put ("USD-TREASURY-BOND-30Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"OZB",
					"CG-PG"}, "USD-TREASURY-BOND-30Y", 100000., true, aLTDS));

			_mapFuturesOptions.put ("USD-TREASURY-NOTE-10Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"OZN",
					"TC-TP"}, "USD-TREASURY-NOTE-10Y", 100000., true, aLTDS));

			_mapFuturesOptions.put ("USD-TREASURY-NOTE-5Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"OZF",
					"FL-FP"}, "USD-TREASURY-NOTE-5Y", 100000., true, aLTDS));

			_mapFuturesOptions.put ("USD-TREASURY-NOTE-2Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"OZT",
					"TUC-TUP"}, "USD-TREASURY-NOTE-2Y", 200000., true, aLTDS));

			_mapFuturesOptions.put ("EUR-EURO-BUXL-30Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"BUXL"},
					"EUR-EURO-BUXL-30Y", 100000., false, aLTDS));

			_mapFuturesOptions.put ("EUR-EURO-BUND-10Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"BUND"},
					"EUR-EURO-BUND-10Y", 100000., false, aLTDS));

			_mapFuturesOptions.put ("EUR-EURO-BOBL-5Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"BOBL"},
					"EUR-EURO-BOBL-10Y", 100000., false, aLTDS));

			_mapFuturesOptions.put ("EUR-EURO-SCHATZ-2Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"SCHATZ"},
					"EUR-EURO-SCHATZ-10Y", 100000., false, aLTDS));

			_mapFuturesOptions.put ("EUR-TREASURY-BONO-10Y", new
				org.drip.market.exchange.BondFuturesOptionConvention (new java.lang.String[] {"BONO"},
					"EUR-TREASURY-BONO-10Y", 100000., false, aLTDS));

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Retrieve the Bond Futures Option Convention from the Contract Name
	 * 
	 * @param strContractName The Options Contract Name
	 * 
	 * @return The Bond Futures Option Convention
	 */

	public static final org.drip.market.exchange.BondFuturesOptionConvention FromContract (
		final java.lang.String strContractName)
	{
		return null == strContractName || strContractName.isEmpty() || !_mapFuturesOptions.containsKey
			(strContractName) ? null : _mapFuturesOptions.get (strContractName);
	}
}
