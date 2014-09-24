
package org.drip.analytics.cashflow;

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
 * ComposedFixedPeriodContainer holds the Fixed Coupon Period's Composable Settings. Currently it holds the
 *  Composable Fixed Coupon Period List and the accrual compounding rules.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposedFixedPeriodContainer {
	private int _iAccrualCompoundingRule = -1;
	private java.util.List<org.drip.analytics.cashflow.ComposableFixedPeriod> _lsComposableFixedPeriod =
		null;

	/**
	 * ComposedFixedPeriodContainer Constructor
	 * 
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public ComposedFixedPeriodContainer (
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule (_iAccrualCompoundingRule =
			iAccrualCompoundingRule))
			throw new java.lang.Exception
				("ComposedFixedPeriodContainer ctr: Invalid Accrual Compounding Rule");
	}

	/**
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _iAccrualCompoundingRule;
	}

	/**
	 * Append the specified Composable Fixed Period
	 * 
	 * @param cfp The Composable Fixed Period
	 * 
	 * @return TRUE => The Composable Fixed Period Successfully Appended
	 */

	public boolean appendPeriod (
		final org.drip.analytics.cashflow.ComposableFixedPeriod cfp)
	{
		if (null == cfp) return false;

		if (null == _lsComposableFixedPeriod)
			_lsComposableFixedPeriod = new
				java.util.ArrayList<org.drip.analytics.cashflow.ComposableFixedPeriod>();

		_lsComposableFixedPeriod.add (cfp);

		return true;
	}

	/**
	 * Retrieve the Composable Fixed Periods
	 * 
	 * @return The Composable Fixed Periods
	 */

	public java.util.List<org.drip.analytics.cashflow.ComposableFixedPeriod> periods()
	{
		return _lsComposableFixedPeriod;
	}
}
