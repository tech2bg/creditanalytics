
package org.drip.market.otc;

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
 * FloatFloatConventionContainer holds the settings of the standard OTC float-float swap contract
 *  conventions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatConventionContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.otc.FloatFloatConvention>
		_mapConvention = new java.util.TreeMap<java.lang.String, org.drip.market.otc.FloatFloatConvention>();

	/**
	 * Initialize the Float-Float Conventions Container with the pre-set Float-Float Contracts
	 * 
	 * @return TRUE => The Float-Float Conventions Container successfully initialized with the pre-set
	 *  Float-Float Contracts
	 */

	public static final boolean Init()
	{
		try {
			_mapConvention.put ("AUD", new org.drip.market.otc.FloatFloatConvention ("AUD", "6M", true, true,
				false, 1));

			_mapConvention.put ("CAD", new org.drip.market.otc.FloatFloatConvention ("CAD", "6M", true, true,
				false, 0));

			_mapConvention.put ("CHF", new org.drip.market.otc.FloatFloatConvention ("CHF", "6M", true, true,
				false, 2));

			_mapConvention.put ("CNY", new org.drip.market.otc.FloatFloatConvention ("CNY", "6M", true, true,
				false, 2));

			_mapConvention.put ("DKK", new org.drip.market.otc.FloatFloatConvention ("DKK", "6M", true, true,
				false, 2));

			_mapConvention.put ("EUR", new org.drip.market.otc.FloatFloatConvention ("EUR", "6M", true, true,
				true, 2));

			_mapConvention.put ("GBP", new org.drip.market.otc.FloatFloatConvention ("GBP", "6M", true, true,
				false, 0));

			_mapConvention.put ("HKD", new org.drip.market.otc.FloatFloatConvention ("HKD", "6M", true, true,
				false, 0));

			_mapConvention.put ("INR", new org.drip.market.otc.FloatFloatConvention ("INR", "6M", true, true,
				false, 2));

			_mapConvention.put ("JPY", new org.drip.market.otc.FloatFloatConvention ("JPY", "6M", true, true,
				false, 2));

			_mapConvention.put ("NOK", new org.drip.market.otc.FloatFloatConvention ("NOK", "6M", true, true,
				false, 2));

			_mapConvention.put ("NZD", new org.drip.market.otc.FloatFloatConvention ("NZD", "6M", true, true,
				false, 0));

			_mapConvention.put ("PLN", new org.drip.market.otc.FloatFloatConvention ("PLN", "6M", true, true,
				false, 2));

			_mapConvention.put ("SEK", new org.drip.market.otc.FloatFloatConvention ("SEK", "6M", true, true,
				false, 2));

			_mapConvention.put ("SGD", new org.drip.market.otc.FloatFloatConvention ("SGD", "6M", true, true,
				false, 2));

			_mapConvention.put ("USD", new org.drip.market.otc.FloatFloatConvention ("USD", "6M", true, true,
				false, 2));

			_mapConvention.put ("ZAR", new org.drip.market.otc.FloatFloatConvention ("ZAR", "6M", true, true,
				false, 0));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Float-Float Convention Instance from the Jurisdiction Name
	 * 
	 * @param strCurrency The Jurisdiction Name
	 * 
	 * @return The Float-Float Convention Instance
	 */

	public static final org.drip.market.otc.FloatFloatConvention ConventionFromJurisdiction (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() || !_mapConvention.containsKey (strCurrency) ?
			null : _mapConvention.get (strCurrency);
	}
}
