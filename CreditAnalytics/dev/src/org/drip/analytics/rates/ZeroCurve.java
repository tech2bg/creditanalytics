
package org.drip.analytics.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * ZeroCurve exposes the node set containing the zero curve node points. In addition to the discount curve
 * 	functionality that it automatically provides by extension, it provides the functionality to calculate the
 *  zero rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ZeroCurve extends org.drip.analytics.rates.DiscountCurve {

	protected ZeroCurve (
		final double dblEpochDate,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams)
		throws java.lang.Exception
	{
		super (dblEpochDate, strCurrency, collatParams, null);
	}

	/**
	 * Retrieve the zero rate corresponding to the given date
	 * 
	 * @param dblDate Date for which the zero rate is requested
	 * 
	 * @return Zero Rate
	 * 
	 * @throws java.lang.Exception Thrown if the date is not represented in the map
	 */

	public abstract double zeroRate (
		final double dblDate)
		throws java.lang.Exception;
}
