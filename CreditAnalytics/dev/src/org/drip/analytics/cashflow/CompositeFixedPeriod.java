
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
 * CompositeFixedPeriod implements the composed fixed coupon period functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CompositeFixedPeriod extends org.drip.analytics.cashflow.CompositePeriod {
	/**
	 * CompositeFixedPeriod Constructor
	 * 
	 * @param lsCUP List of Composable Unit Fixed Periods
	 * @param iFreq Frequency
	 * @param dblPayDate Period Pay Date
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param creditLabel The Credit Label
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public CompositeFixedPeriod (
		final java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP,
		final int iFreq,
		final double dblPayDate,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final double dblFXFixingDate)
		throws java.lang.Exception
	{
		super (lsCUP, iFreq, dblPayDate, strPayCurrency, iAccrualCompoundingRule, dblBaseNotional,
			notlSchedule, creditLabel, dblFXFixingDate);
	}

	@Override public org.drip.analytics.cashflow.CompositePeriodQuoteSet periodQuoteSet (
		final org.drip.product.calib.ProductQuoteSet pqs,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == pqs || !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)) return null;

		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet)
			pqs;

		org.drip.analytics.cashflow.ComposableUnitPeriod cup = periods().get (0);

		try {
			return new org.drip.analytics.cashflow.CompositePeriodQuoteSet (fsqs.containsCoupon() ?
				cup.baseRate (csqs) : fsqs.coupon(), fsqs.containsCouponBasis() ? fsqs.couponBasis() :
					cup.basis());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
