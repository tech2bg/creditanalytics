
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
 * SwapOptionSettlementContainer holds the Settlement Settings of the standard Option on an OTC Fix-Float
 *  Swap Contract.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SwapOptionSettlementContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.otc.SwapOptionSettlement>
		_mapConvention = new java.util.TreeMap<java.lang.String, org.drip.market.otc.SwapOptionSettlement>();

	/**
	 * Initialize the Swap Option Settlement Conventions Container with the pre-set Swap Option Settlement
	 *  Conventions
	 * 
	 * @return TRUE => The Swap Option Settlement Conventions Container successfully initialized with the
	 *  pre-set Swap Option Settlement Conventions
	 */

	public static final boolean Init()
	{
		try {
			_mapConvention.put ("AUD", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_PHYSICAL_DELIVERY, 0));

			_mapConvention.put ("CHF", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));

			_mapConvention.put ("DKK", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));

			_mapConvention.put ("EUR", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));

			_mapConvention.put ("GBP", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));

			_mapConvention.put ("JPY", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_PHYSICAL_DELIVERY, 0));

			_mapConvention.put ("NOK", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));

			_mapConvention.put ("USD", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_EXACT_CURVE));

			_mapConvention.put ("SEK", new org.drip.market.otc.SwapOptionSettlement
				(org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED,
					org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Swap Option Settlement Convention for the specified Jurisdiction
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * 
	 * @return The Swap Option Settlement Convention
	 */

	public static final org.drip.market.otc.SwapOptionSettlement ConventionFromJurisdiction (
		final java.lang.String strJurisdictionName)
	{
		return null == strJurisdictionName || strJurisdictionName.isEmpty() || !_mapConvention.containsKey
			(strJurisdictionName) ? null : _mapConvention.get (strJurisdictionName);
	}
}
