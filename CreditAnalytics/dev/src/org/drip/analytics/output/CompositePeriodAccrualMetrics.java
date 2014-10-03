
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
 * CompositePeriodAccrualMetrics holds the results of the compounded Composed period Accrual Metrics Estimate
 *  Output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CompositePeriodAccrualMetrics extends org.drip.analytics.output.CompositePeriodCouponMetrics {
	private double _dblResetDate = java.lang.Double.NaN;

	/**
	 * CompositePeriodAccrualMetrics Instance from the list of the composite period metrics
	 * 
	 * @param dblResetDate Reset Date
	 * @param lsUPM List of Unit Period Metrics
	 * 
	 * @return Instance of CompositePeriodAccrualMetrics
	 */

	public static final CompositePeriodAccrualMetrics Create (
		final double dblResetDate,
		final java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM)
	{
		try {
			CompositePeriodAccrualMetrics cpam = new CompositePeriodAccrualMetrics (dblResetDate, lsUPM);

			return cpam.initialize() ? cpam : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected CompositePeriodAccrualMetrics (
		final double dblResetDate,
		final java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM)
		throws java.lang.Exception
	{
		super (lsUPM);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblResetDate = dblResetDate))
			throw new java.lang.Exception ("CompositePeriodAccrualMetrics ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Reset Date
	 * 
	 * @return The Reset Date
	 */

	public double resetDate()
	{
		return _dblResetDate;
	}
}
