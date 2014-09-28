
package org.drip.state.identifier;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * ForwardLabel contains the Index Parameters referencing a payment on a Forward Index. It provides the
 *  following functionality:
 *  - Indicate if the Index is an Overnight Index
 *  - Retrieve Index, Tenor, Currency, and Fully Qualified Name.
 *  - Serialization into and de-serialization out of byte arrays.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class ForwardLabel implements org.drip.product.params.Validatable,
	org.drip.state.identifier.LatentStateLabel {
	private java.lang.String _strIndex = "";
	private java.lang.String _strTenor = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strFullyQualifiedName = "";

	/**
	 * Create from the Currency, the Index, and the Tenor
	 * 
	 * @param strCurrency Currency
	 * @param strIndex Index
	 * @param strTenor Tenor
	 * 
	 * @return ForwardLabel Instance
	 */

	public static final ForwardLabel Create (
		final java.lang.String strCurrency,
		final java.lang.String strIndex,
		final java.lang.String strTenor)
	{
		try {
			return new ForwardLabel (strCurrency, strIndex, strTenor, strCurrency + "-" + strIndex + "-" +
				strTenor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a ForwardLabel from the corresponding Fully Qualified Name
	 * 
	 * @param strFullyQualifiedName The Fully Qualified Name
	 * 
	 * @return ForwardLabel Instance
	 */

	public static final ForwardLabel Standard (
		final java.lang.String strFullyQualifiedName)
	{
		if (null == strFullyQualifiedName|| strFullyQualifiedName.isEmpty()) return null;

		java.lang.String[] astr = strFullyQualifiedName.split ("-");

		if (null == astr || 3 != astr.length) return null;

		try {
			return new ForwardLabel (astr[0], astr[1], astr[2], strFullyQualifiedName);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ForwardLabel constructor
	 * 
	 * @param strCurrency Currency
	 * @param strIndex Index
	 * @param strTenor Tenor
	 * @param strFullyQualifiedName The Fully Qualified Name
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	private ForwardLabel (
		final java.lang.String strCurrency,
		final java.lang.String strIndex,
		final java.lang.String strTenor,
		final java.lang.String strFullyQualifiedName)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_strIndex = strIndex)
			|| _strIndex.isEmpty() || null == (_strTenor = strTenor) || _strTenor.isEmpty() || null ==
				(_strFullyQualifiedName = strFullyQualifiedName) || _strFullyQualifiedName.isEmpty())
			throw new java.lang.Exception ("ForwardLabel ctr: Invalid Inputs");
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
	 * Retrieve the Index
	 * 
	 * @return The Index
	 */

	public java.lang.String index()
	{
		return _strIndex;
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
	 * Indicate if the Index is an Overnight Index
	 * 
	 * @return TRUE => Overnight Index
	 */

	public boolean overnight()
	{
		return "ON".equalsIgnoreCase (_strTenor) || "1D".equalsIgnoreCase (_strTenor);
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _strFullyQualifiedName;
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.ForwardLabel) ? false :
			_strFullyQualifiedName.equalsIgnoreCase (lslOther.fullyQualifiedName());
	}

	@Override public boolean validate()
	{
		return null != _strCurrency && !_strCurrency.isEmpty() && null != _strIndex && !_strIndex.isEmpty()
			&& null != _strTenor && !_strTenor.isEmpty() && null != _strFullyQualifiedName &&
				!_strFullyQualifiedName.isEmpty();
	}
}
