
package org.drip.product.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * BondBasketBuilder contains the suite of helper functions for creating the bond Basket Product from different
 *  kinds of inputs and byte streams.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BondBasketBuilder {

	/**
	 * BondBasket constructor
	 * 
	 * @param strName BondBasket Name
	 * @param aBond Component bonds
	 * @param adblWeights Component Bond weights
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final org.drip.product.definition.BasketProduct CreateBondBasket (
		final java.lang.String strName,
		final org.drip.product.definition.Bond[] aBond,
		final double[] adblWeights)
	{
		try {
			return new org.drip.product.credit.BondBasket (strName, aBond, adblWeights);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
