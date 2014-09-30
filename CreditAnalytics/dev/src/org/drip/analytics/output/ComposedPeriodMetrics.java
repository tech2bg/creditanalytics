
package org.drip.analytics.output;

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
 * ComposedPeriodMetrics holds the results of the compounded Composed period metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposedPeriodMetrics {
	private java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> _lsCPM = null;

	/**
	 * Composed Period Metrics Instance from the list of composable period metrics
	 * 
	 * @param lsCPM List of Composable Period Metrics
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public ComposedPeriodMetrics (
		final java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM)
		throws java.lang.Exception
	{
		if (null == (_lsCPM = lsCPM) || 0 == _lsCPM.size())
			throw new java.lang.Exception ("ComposedPeriodMetrics ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the List of Composable Period Metrics
	 * 
	 * @return The List of Composable Period Metrics
	 */

	public java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> composableMetrics()
	{
		return _lsCPM;
	}
}
