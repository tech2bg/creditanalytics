
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
 * ResetPeriodMetrics holds the results of the reset period coupon measures estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResetPeriodMetrics {
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFixingDate = java.lang.Double.NaN;
	private double _dblNominalRate = java.lang.Double.NaN;

	/**
	 * ResetPeriodMetrics constructor
	 * 
	 * @param dblStartDate Reset Period Start Date
	 * @param dblEndDate Reset Period End Date
	 * @param dblFixingDate Reset Period Fixing Date
	 * @param dblNominalRate The Nominal Coupon Rate
	 * @param dblDCF The Period DCF
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public ResetPeriodMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblFixingDate,
		final double dblNominalRate,
		final double dblDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) || _dblStartDate >=
				_dblEndDate || !org.drip.quant.common.NumberUtil.IsValid (_dblNominalRate = dblNominalRate)
					|| !org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || 0. == _dblDCF)
			throw new java.lang.Exception ("ResetPeriodMetrics ctr: Invalid Inputs");

		_dblFixingDate = dblFixingDate;
	}

	/**
	 * Reset Start Date
	 * 
	 * @return The Reset Start Date
	 */

	public double start()
	{
		return _dblStartDate;
	}

	/**
	 * Reset End Date
	 * 
	 * @return The Reset End Date
	 */

	public double end()
	{
		return _dblEndDate;
	}

	/**
	 * Reset Fixing Date
	 * 
	 * @return The Reset Fixing Date
	 */

	public double fixing()
	{
		return _dblFixingDate;
	}

	/**
	 * Retrieve the Nominal Coupon Accrual Rate
	 * 
	 * @return The Nominal Coupon Accrual Rate
	 */

	public double nominalRate()
	{
		return _dblNominalRate;
	}

	/**
	 * Retrieve the Reset Period DCF
	 * 
	 * @return The Reset Period DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}
}
