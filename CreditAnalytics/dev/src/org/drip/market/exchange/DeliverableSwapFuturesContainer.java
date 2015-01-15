
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
 * DeliverableSwapFuturesContainer holds the Deliverable Swap Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DeliverableSwapFuturesContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.exchange.DeliverableSwapFutures>
		_mapFutures = new
			java.util.TreeMap<java.lang.String, org.drip.market.exchange.DeliverableSwapFutures>();


	/**
	 * Initialize the Deliverable Swap Futures Container with the pre-set Deliverable Swap Futures Contract
	 * 
	 * @return TRUE => The Deliverable Swap Futures Container successfully initialized with the pre-set
	 *  Deliverable Swap Futures Contract
	 */

	public static final boolean Init()
	{
		try {
			_mapFutures.put ("USD-2Y", new org.drip.market.exchange.DeliverableSwapFutures ("USD", "2Y",
				100000., 0.0025, new org.drip.product.params.LastTradingDateSetting
					(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION, "2D",
						java.lang.Double.NaN)));

			_mapFutures.put ("USD-5Y", new org.drip.market.exchange.DeliverableSwapFutures ("USD", "5Y",
				100000., 0.0025, new org.drip.product.params.LastTradingDateSetting
					(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION, "2D",
						java.lang.Double.NaN)));

			_mapFutures.put ("USD-10Y", new org.drip.market.exchange.DeliverableSwapFutures ("USD", "10Y",
				100000., 0.0025, new org.drip.product.params.LastTradingDateSetting
					(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION, "2D",
						java.lang.Double.NaN)));

			_mapFutures.put ("USD-30Y", new org.drip.market.exchange.DeliverableSwapFutures ("USD", "30Y",
				100000., 0.0025, new org.drip.product.params.LastTradingDateSetting
					(org.drip.product.params.LastTradingDateSetting.MID_CURVE_OPTION, "2D",
						java.lang.Double.NaN)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Deliverable Swap Futures Info from the Currency and the Tenor
	 * 
	 * @param strCurrency The Currency
	 * @param strTenor The Tenor
	 * 
	 * @return The Deliverable Swap Futures Instance
	 */

	public static final org.drip.market.exchange.DeliverableSwapFutures ProductInfo (
		final java.lang.String strCurrency,
		final java.lang.String strTenor)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == strTenor || strTenor.isEmpty())
			return null;

		java.lang.String strKey = strCurrency + "-" + strTenor;

		return _mapFutures.containsKey (strKey) ? _mapFutures.get (strKey) : null;
	}
}
