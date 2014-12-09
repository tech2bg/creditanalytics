
package org.drip.product.fx;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * FXSpotContract contains the FX spot contract parameters - the spot date and the currency pair. Additional
 *  functions serialize into and de-serialize out of byte arrays.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class FXSpotContract extends org.drip.product.definition.FXSpot {
	private double _dblSpotDate = java.lang.Double.NaN;
	private org.drip.product.params.CurrencyPair _ccyPair = null;

	/**
	 * Constructor: Create the FX spot object from the spot date and the currency pair.
	 * 
	 * @param dtSpot Spot date
	 * @param ccyPair CurrencyPair
	 * 
	 * @throws java.lang.Exception Thrown on invalid parameters
	 */

	public FXSpotContract (
		final org.drip.analytics.date.JulianDate dtSpot,
		final org.drip.product.params.CurrencyPair ccyPair)
		throws java.lang.Exception
	{
		if (null == dtSpot || null == ccyPair)
			throw new java.lang.Exception ("FXSpotContract ctr: Invalid params");

		_ccyPair = ccyPair;

		_dblSpotDate = dtSpot.julian();
	}

	@Override public double spotDate()
	{
		return _dblSpotDate;
	}

	@Override public org.drip.product.params.CurrencyPair currencyPair()
	{
		return _ccyPair;
	}
}
