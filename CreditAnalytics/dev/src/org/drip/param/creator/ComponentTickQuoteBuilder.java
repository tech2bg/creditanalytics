
package org.drip.param.creator;

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
 * ComponentTickQuoteBuilder implements the component tick quote builder object. It contains static functions
 * 	that build component quotes from the inputs, as well as from byte array.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentTickQuoteBuilder {

	/**
	 * Constructor: Constructs an Empty Component Quote instance.
	 * 
	 * @return ComponentQuote Instance
	 */

	public static final org.drip.param.market.ComponentTickQuote CreateComponentTickQuote()
	{
		return new org.drip.param.market.ComponentTickQuote();
	}

	/**
	 * Create a Component Quote Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return ComponentQuote Instance
	 */

	public static final org.drip.param.market.ComponentTickQuote FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.param.market.ComponentTickQuote (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
