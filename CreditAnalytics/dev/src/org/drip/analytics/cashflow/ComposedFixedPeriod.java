
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
 * ComposedFoxedPeriod implements the composed fixed coupon period functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposedFixedPeriod extends org.drip.analytics.cashflow.ComposedPeriod{
	private java.lang.String _strCouponCurrency = "";

	/**
	 * ComposedFixedPeriod Constructor
	 * 
	 * @param iFreq Frequency
	 * @param dblPayDate Period Pay Date
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param creditLabel The Credit Label
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * @param strCouponCurrency Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public ComposedFixedPeriod (
		final int iFreq,
		final double dblPayDate,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final double dblFXFixingDate,
		final java.lang.String strCouponCurrency)
		throws java.lang.Exception
	{
		super (iFreq, dblPayDate, strPayCurrency, iAccrualCompoundingRule, dblBaseNotional, notlSchedule,
			creditLabel, dblFXFixingDate);

		if (null == (_strCouponCurrency = strCouponCurrency) || _strCouponCurrency.isEmpty())
			throw new java.lang.Exception ("ComposedFixedPeriod ctr: Invalid Inputs");
	}

	@Override public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}

	@Override public org.drip.analytics.cashflow.ComposedPeriodQuoteSet periodQuoteSet (
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs || !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)) return null;

		double dblBaseRate = java.lang.Double.NaN;

		org.drip.analytics.cashflow.ComposablePeriod cpFirst = periods().get (0);

		try {
			dblBaseRate = cpFirst.baseRate (null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblBasis = cpFirst.basis();

		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsCoupon()) dblBaseRate = fsqs.coupon();

			if (fsqs.containsCouponBasis()) dblBasis = fsqs.couponBasis();

			return new org.drip.analytics.cashflow.ComposedPeriodQuoteSet (dblBaseRate, dblBasis);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
