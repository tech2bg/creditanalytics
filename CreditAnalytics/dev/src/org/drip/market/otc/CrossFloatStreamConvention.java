
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
 * CrossFloatStreamConvention contains the Details of the Single Currency Floating Stream of an OTC Contact.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CrossFloatStreamConvention {
	private boolean _bApplySpread = false;
	private java.lang.String _strTenor = "";
	private java.lang.String _strCurrency = "";

	/**
	 * CrossFloatStreamConvention Constructor
	 * 
	 * @param strCurrency Floating Stream Currency
	 * @param strTenor Floating Stream Tenor
	 * @param bApplySpread TRUE => Apply Spread to this Stream
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public CrossFloatStreamConvention (
		final java.lang.String strCurrency,
		final java.lang.String strTenor,
		final boolean bApplySpread)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_strTenor = strTenor)
			|| _strTenor.isEmpty())
			throw new java.lang.Exception ("CrossFloatStreamConvention ctr: Invalid Inputs");

		_bApplySpread = bApplySpread;
	}

	/**
	 * Retrieve the Currency
	 * 
	 * @return The Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Tenor
	 * 
	 * @return The Tenor
	 */

	public java.lang.String tenor()
	{
		return _strTenor;
	}

	/**
	 * Retrieve the "Apply Spread" Flag
	 * 
	 * @return The "Apply Spread" Flag
	 */

	public boolean applySpread()
	{
		return _bApplySpread;
	}

	@Override public java.lang.String toString()
	{
		return "[" + _strCurrency + " | " + _strTenor + " | " + _bApplySpread + "]";
	}
}
