
package org.drip.market.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * ShortTermFuturesContainer holds the short term futures contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ShortTermFuturesContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.definition.ShortTermFutures>
		_mapFutures = new
			java.util.TreeMap<java.lang.String, org.drip.market.definition.ShortTermFutures>();

	/**
	 * Initialize the Overnight Index Container with the Overnight Indexes
	 * 
	 * @return TRUE => The Overnight Index Container successfully initialized with the indexes
	 */

	public static final boolean Init()
	{
		try {
			_mapFutures.put ("CAD-CDOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"MX"}, 1000000.));

			_mapFutures.put ("CHF-LIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"LIFFE"}, 1000000.));

			_mapFutures.put ("DKK-CIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"OMX"}, 1000000.));

			_mapFutures.put ("EUR-EURIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"EUREX", "LIFFE", "NLX"}, 1000000.));

			_mapFutures.put ("GBP-LIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"LIFFE", "NLX"}, 500000.));

			_mapFutures.put ("JPY-LIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"SGX"}, 100000000.));

			_mapFutures.put ("JPY-TIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"CME", "SGX"}, 100000000.));

			_mapFutures.put ("USD-LIBOR-1M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"CME"}, 3000000.));

			_mapFutures.put ("USD-LIBOR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"CME", "SGX"}, 1000000.));

			_mapFutures.put ("ZAR-JIBAR-3M", new org.drip.market.definition.ShortTermFutures (new
				java.lang.String[] {"SAFEX"}, 1000000.));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Short Term Futures Exchange Info From the Corresponding Forward Label
	 * 
	 * @param forwardLabel The Forward Label
	 * 
	 * @return The Short Term Futures Exchange Info
	 */

	public static final org.drip.market.definition.ShortTermFutures ExchangeInfo (
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == forwardLabel) return null;

		java.lang.String strFullyQualifiedName = forwardLabel.fullyQualifiedName();

		return _mapFutures.containsKey (strFullyQualifiedName) ? _mapFutures.get (strFullyQualifiedName) :
			null;
	}

	/**
	 * Retrieve the Short Term Futures Exchange Info From the Corresponding Forward Label
	 * 
	 * @param forwardLabel The Forward Label
	 * 
	 * @return The Short Term Futures Exchange Info
	 */

	public static final org.drip.market.definition.ShortTermFutures ExchangeInfo (
		final java.lang.String strFullyQualifiedName)
	{
		return null != strFullyQualifiedName && _mapFutures.containsKey (strFullyQualifiedName) ?
			_mapFutures.get (strFullyQualifiedName) : null;
	}
}
