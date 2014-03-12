
package org.drip.param.valuation;

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
 * CollateralizationParams holds the parameters needed to carry out a collateralized valuation of the
 * 	product. Currently it holds the collateral currency and the collateral type.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CollateralizationParams {
	private java.lang.String _strType = "";
	private java.lang.String _strCurrency = "";

	/**
	 * CollateralizationParams Constructor
	 * 
	 * @param strType Collateral Type
	 * @param strCurrency Collateral Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralizationParams (
		final java.lang.String strType,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (null == (_strType = strType) || _strType.isEmpty() || null == (_strCurrency = strCurrency) ||
			_strCurrency.isEmpty())
			throw new java.lang.Exception ("CollateralizationParams ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Collateral Currency
	 * 
	 * @return The Collateral Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Collateral Type
	 * 
	 * @return The Collateral Type
	 */

	public java.lang.String type()
	{
		return _strType;
	}

	/**
	 * Synthesize and Retrieve the Collateral Key
	 * 
	 * @return The Collateral Key
	 */

	public java.lang.String key()
	{
		return _strCurrency + "_" + _strType;
	}
}
