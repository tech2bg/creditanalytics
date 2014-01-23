
package org.drip.analytics.rates;

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
 * Turn implements rate spread at discrete time spans. It contains the turn amount and the start/end turn
 * 	spans.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Turn {
	private double _dblSpread = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFinishDate = java.lang.Double.NaN;

	/**
	 * Turn Constructor
	 * 
	 * @param dblStartDate Turn Period Start Date
	 * @param dblFinishDate Turn Period Finish Date
	 * @param dblSpread Turn Period Spread
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public Turn (
		final double dblStartDate,
		final double dblFinishDate,
		final double dblSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSpread = dblSpread) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFinishDate = dblFinishDate) || _dblFinishDate
					<= _dblStartDate)
			throw new java.lang.Exception ("Turn Ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Start Date
	 * 
	 * @return The Start Date
	 */

	public double start()
	{
		return _dblStartDate;
	}

	/**
	 * Retrieve the Finish Date
	 * 
	 * @return The Finish Date
	 */

	public double finish()
	{
		return _dblFinishDate;
	}

	/**
	 * Retrieve the Spread
	 * 
	 * @return The Spread
	 */

	public double spread()
	{
		return _dblSpread;
	}
}
