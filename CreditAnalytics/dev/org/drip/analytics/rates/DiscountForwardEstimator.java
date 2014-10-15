
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
 * DiscountForwardEstimator exposes the "native" forward curve associated with the specified discount curve.
 * 	It exposes functionality to extract forward rate index/tenor, as well as to compute the forward rate
 * 	implied off of the discount curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscountForwardEstimator implements org.drip.analytics.rates.ForwardRateEstimator {
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.analytics.rates.DiscountFactorEstimator _dfe = null;

	/**
	 * DiscountForwardEstimator constructor
	 * 
	 * @param dfe The Discount Factor Estimator
	 * @param fri The Floating Rate Index
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public DiscountForwardEstimator (
		final org.drip.analytics.rates.DiscountFactorEstimator dfe,
		final org.drip.state.identifier.ForwardLabel fri)
		throws java.lang.Exception
	{
		if (null == (_dfe = dfe) || null == (_fri = fri))
			throw new java.lang.Exception ("DiscountForwardEstimator ctr: Invalid Inputs");
	}

	@Override public org.drip.state.identifier.ForwardLabel index()
	{
		return _fri;
	}

	@Override public java.lang.String tenor()
	{
		return _fri.tenor();
	}

	@Override public double forward (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt)
			throw new java.lang.Exception ("DiscountForwardEstimator::forward => Invalid Inputs!");

		return _dfe.forward (dt.subtractTenor (_fri.tenor()).julian(), dt.julian());
	}

	@Override public double forward (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountForwardEstimator::forward => Invalid Inputs!");

		return forward (new org.drip.analytics.date.JulianDate (dblDate));
	}

	@Override public double forward (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountForwardEstimator::forward => Invalid Inputs!");

		return forward (_dfe.epoch().addTenor (strTenor));
	}
}
