
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
	 * @param cps Composite Period Setting Instance
	 * @param lsCUP List of Composable Unit Fixed Periods
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public CompositeFixedPeriod (
		final org.drip.param.period.CompositePeriodSetting cps,
		final java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP)
		throws java.lang.Exception
	{
		super (cps, lsCUP);
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
				fsqs.coupon() : cup.baseRate (csqs), fsqs.containsCouponBasis() ? fsqs.couponBasis() :
					basis());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double basisQuote (
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblBasis = basis();

		if (null == pqs || !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)) return dblBasis;

		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsCouponBasis()) return fsqs.couponBasis();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dblBasis;
	}
}
