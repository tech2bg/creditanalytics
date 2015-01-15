
package org.drip.market.exchange;

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
 * ShortTermFutures contains the details of the exchange-traded Short-Term Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ShortTermFutures {
	private java.lang.String[] _astrExchange = null;
	private double _dblNotional = java.lang.Double.NaN;

	/**
	 * ShortTermFutures constructor
	 * 
	 * @param astrExchange Array of Exchanges
	 * @param dblNotional Notional
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ShortTermFutures (
		final java.lang.String[] astrExchange,
		final double dblNotional)
		throws java.lang.Exception
	{
		if (null == (_astrExchange = astrExchange) || 0 == _astrExchange.length ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional))
			throw new java.lang.Exception ("ShortTermFutures ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the List of Exchanges
	 * 
	 * @return The List of Exchanges
	 */

	public java.lang.String[] exchanges()
	{
		return _astrExchange;
	}

	/**
	 * Retrieve the Traded Notional
	 * 
	 * @return The Exchange Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}
}
