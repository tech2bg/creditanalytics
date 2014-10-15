
package org.drip.state.identifier;

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
 * CollateralLabel contains the Identifier Parameters referencing the Latent State of the named Collateral
 *  Discount Curve. Currently it only contains the collateral currency.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class CollateralLabel implements org.drip.state.identifier.LatentStateLabel {
	private java.lang.String _strCurrency = "";

	/**
	 * Make a Standard Collateral Label from the Collateral Currency
	 * 
	 * @param strCurrency The Collateral Currency
	 * 
	 * @return The Collateral Label
	 */

	public static final CollateralLabel Standard (
		final java.lang.String strCurrency)
	{
		try {
			return new CollateralLabel (strCurrency);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CollateralLabel constructor
	 * 
	 * @param strCurrency Collateral Currency
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	private CollateralLabel (
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty())
			throw new java.lang.Exception ("CollateralLabel ctr: Invalid Inputs");
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _strCurrency;
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.CollateralLabel) ? false :
			_strCurrency.equalsIgnoreCase (lslOther.fullyQualifiedName());
	}
}
