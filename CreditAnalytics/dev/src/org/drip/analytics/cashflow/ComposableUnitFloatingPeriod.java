
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
 * ComposableUnitFloatingPeriod contains the cash flow periods' composable sub period details. Currently it
 * 	holds the accrual start date, the accrual end date, the fixing date, the spread over the index, and the
 * 	corresponding reference index period.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableUnitFloatingPeriod extends org.drip.analytics.cashflow.ComposableUnitPeriod {
	private double _dblSpread = java.lang.Double.NaN;
	private org.drip.analytics.cashflow.ReferenceIndexPeriod _refIndexPeriod = null;

	/**
	 * The ComposableUnitFloatingPeriod constructor
	 * 
	 * @param dblStartDate Accrual Start Date
	 * @param dblEndDate Accrual End Date
	 * @param refIndexPeriod The Reference Index Period
	 * @param dblSpread The Floater Spread
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ComposableUnitFloatingPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final org.drip.analytics.cashflow.ReferenceIndexPeriod refIndexPeriod,
		final double dblSpread)
		throws java.lang.Exception
	{
		super (dblStartDate, dblEndDate, refIndexPeriod.forwardLabel().ucas());

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSpread = dblSpread))
			throw new java.lang.Exception ("ComposableUnitFloatingPeriod ctr: Invalid Inputs");

		_refIndexPeriod = refIndexPeriod;
	}

	/**
	 * Retrieve the Reference Rate for the Floating Period
	 * 
	 * @param csqs The Market Curve and Surface
	 * 
	 * @return The Reference Rate for the Floating Period
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	@Override public double baseRate (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == csqs) return java.lang.Double.NaN;

		double dblFixingDate = _refIndexPeriod.fixingDate();

		org.drip.state.identifier.ForwardLabel forwardLabel = _refIndexPeriod.forwardLabel();

		if (csqs.available (dblFixingDate, forwardLabel)) return csqs.fixing (dblFixingDate, forwardLabel);

		double dblReferencePeriodEndDate = _refIndexPeriod.endDate();

		org.drip.analytics.rates.ForwardRateEstimator fre = csqs.forwardCurve (forwardLabel);

		if (null != fre) return fre.forward (dblReferencePeriodEndDate);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (forwardLabel.currency()));

		if (null == dcFunding)
			throw new java.lang.Exception
				("ComposableUnitFloatingPeriod::referenceRate => Cannot locate Funding Curve " +
					forwardLabel.currency());

		double dblReferencePeriodStartDate = _refIndexPeriod.startDate();

		double dblEpochDate = dcFunding.epoch().julian();

		if (dblEpochDate > dblReferencePeriodStartDate)
			dblReferencePeriodEndDate = new org.drip.analytics.date.JulianDate (dblReferencePeriodStartDate =
				dblEpochDate).addTenor (forwardLabel.tenor()).julian();

		return dcFunding.libor (dblReferencePeriodStartDate, dblReferencePeriodEndDate, fullCouponDCF());
	}

	@Override public double basis()
	{
		return _dblSpread;
	}

	@Override public java.lang.String couponCurrency()
	{
		return _refIndexPeriod.forwardLabel().currency();
	}

	/**
	 * Retrieve the Reference Index Period
	 * 
	 * @return The Reference Index Period
	 */

	public org.drip.analytics.cashflow.ReferenceIndexPeriod referenceIndexPeriod()
	{
		return _refIndexPeriod;
	}
}
