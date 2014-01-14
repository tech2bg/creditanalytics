
package org.drip.service.api;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * CDXCOB contains the Name and the COB Price for a given CDX.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CDXCOB {
	private java.lang.String _strCDXName = "";
	private double _dblPrice = java.lang.Double.NaN;

	/**
	 * CDXCOB constructor
	 * 
	 * @param strCDXName The CDX Name
	 * @param dblPrice The COB Price
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public CDXCOB (
		final java.lang.String strCDXName,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == (_strCDXName = strCDXName) || _strCDXName.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblPrice = dblPrice))
			throw new java.lang.Exception ("CDXCOB ctr: Invalid Inputs");
	}

	/**
	 * The CDX Name
	 * 
	 * @return The CDX Name
	 */

	public java.lang.String name()
	{
		return _strCDXName;
	}

	/**
	 * The COB Price
	 * 
	 * @return The COB Price
	 */

	public double price()
	{
		return _dblPrice;
	}

	/**
	 * Display the CDXCOB Content
	 * 
	 * @return The CDXCOB Content
	 */

	public java.lang.String display()
	{
		return _strCDXName + " => " + _dblPrice;
	}
}
