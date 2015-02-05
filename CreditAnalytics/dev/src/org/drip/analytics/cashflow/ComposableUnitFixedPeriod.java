
package org.drip.analytics.cashflow;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ComposableUnitFixedPeriod contains the fixed cash flow periods' composable sub period details. Currently
 *  it holds the accrual start date, the accrual end date, the fixed coupon, the basis spread, coupon and
 *  accrual day counts, and the EOM adjustment flags.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableUnitFixedPeriod extends org.drip.analytics.cashflow.ComposableUnitPeriod {
	private org.drip.param.period.ComposableFixedUnitSetting _cufs = null;

	/**
	 * The ComposableUnitFixedPeriod constructor
	 * 
	 * @param dblStartDate Accrual Start Date
	 * @param dblEndDate Accrual End Date
	 * @param ucas Unit Coupon/Accrual Setting
	 * @param cufs Composable Unit Fixed Setting
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ComposableUnitFixedPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final org.drip.param.period.UnitCouponAccrualSetting ucas,
		final org.drip.param.period.ComposableFixedUnitSetting cufs)
		throws java.lang.Exception
	{
		super (dblStartDate, dblEndDate, cufs.tenor(), ucas);

		if (null == (_cufs = cufs))
			throw new java.lang.Exception ("ComposableUnitFixedPeriod ctr: Invalid Inputs");
	}

	@Override public double baseRate (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		return _cufs.fixedCoupon();
	}

	@Override public double basis()
	{
		return _cufs.basis();
	}

	@Override public java.lang.String couponCurrency()
	{
		return _cufs.couponCurrency();
	}
}
