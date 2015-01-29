
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
 * CrossCurrencyFloatConvention contains the Details of the Cross-Currency Floating Swap of an OTC contact.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CrossCurrencyFloatConvention {
	private int _iFixingType = -1;
	private org.drip.market.otc.CrossFloatStreamConvention _crossStreamDerived = null;
	private org.drip.market.otc.CrossFloatStreamConvention _crossStreamReference = null;

	/**
	 * CrossCurrencyFloatConvention Constructor
	 * 
	 * @param crossStreamReference Reference Cross Float Stream
	 * @param crossStreamDerived Derived Cross Float Stream
	 * @param iFixingType Fixing Type
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public CrossCurrencyFloatConvention (
		final org.drip.market.otc.CrossFloatStreamConvention crossStreamReference,
		final org.drip.market.otc.CrossFloatStreamConvention crossStreamDerived,
		final int iFixingType)
		throws java.lang.Exception
	{
		if (null == (_crossStreamReference = crossStreamReference) || null == (_crossStreamDerived =
			crossStreamDerived) || !org.drip.param.period.FixingSetting.ValidateType (_iFixingType =
				iFixingType))
			throw new java.lang.Exception ("CrossCurrencyFloatConvention ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Reference Convention
	 * 
	 * @return The Reference Convention
	 */

	public org.drip.market.otc.CrossFloatStreamConvention referenceConvention()
	{
		return _crossStreamReference;
	}

	/**
	 * Retrieve the Derived Convention
	 * 
	 * @return The Derived Convention
	 */

	public org.drip.market.otc.CrossFloatStreamConvention derivedConvention()
	{
		return _crossStreamDerived;
	}

	/**
	 * Retrieve the Fixing Setting Type
	 * 
	 * @return The Fixing Setting Type
	 */

	public int fixingType()
	{
		return _iFixingType;
	}

	@Override public java.lang.String toString()
	{
		return _crossStreamReference + " " + _crossStreamDerived + " " + _iFixingType;
	}
}
