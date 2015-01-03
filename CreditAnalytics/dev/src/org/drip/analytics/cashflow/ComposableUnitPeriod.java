
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
 * ComposableUnitPeriod contains the cash flow periods' composable unit period details. Currently it
 *  holds the accrual start date, the accrual end date, the fixed coupon, the basis spread, coupon and
 *  accrual day counts, and the EOM adjustment flags.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ComposableUnitPeriod {

	/**
	 * Node is to the Left of the Period
	 */

	public static final int NODE_LEFT_OF_SEGMENT = 1;

	/**
	 * Node is Inside the Period
	 */

	public static final int NODE_INSIDE_SEGMENT = 2;

	/**
	 * Node is to the Right of the Period
	 */

	public static final int NODE_RIGHT_OF_SEGMENT = 4;

	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFullCouponDCF = java.lang.Double.NaN;
	private org.drip.param.period.UnitCouponAccrualSetting _ucas = null;

	protected ComposableUnitPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final org.drip.param.period.UnitCouponAccrualSetting ucas)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) || _dblStartDate >=
				_dblEndDate || null == (_ucas = ucas))
			throw new java.lang.Exception ("ComposableUnitPeriod ctr: Invalid Inputs");

		_dblFullCouponDCF = _ucas.couponDCFOffOfFreq() ? 1. / _ucas.freq() :
			org.drip.analytics.daycount.Convention.YearFraction (_dblStartDate, _dblEndDate,
				_ucas.couponDC(), _ucas.couponEOMAdjustment(), null, _ucas.calendar());
		
	}

	/**
	 * Retrieve the Accrual Start Date
	 * 
	 * @return The Accrual Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Retrieve the Accrual End Date
	 * 
	 * @return The Accrual End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _ucas.freq();
	}

	/**
	 * Retrieve the Coupon Day Count
	 * 
	 * @return The Coupon Day Count
	 */

	public java.lang.String couponDC()
	{
		return _ucas.couponDC();
	}

	/**
	 * Retrieve the Coupon EOM Adjustment Flag
	 * 
	 * @return The Coupon EOM Adjustment Flag
	 */

	public boolean couponEOMAdjustment()
	{
		return _ucas.couponEOMAdjustment();
	}

	/**
	 * Retrieve the Accrual Day Count
	 * 
	 * @return The Accrual Day Count
	 */

	public java.lang.String accrualDC()
	{
		return _ucas.accrualDC();
	}

	/**
	 * Retrieve the Accrual EOM Adjustment Flag
	 * 
	 * @return The Accrual EOM Adjustment Flag
	 */

	public boolean accrualEOMAdjustment()
	{
		return _ucas.accrualEOMAdjustment();
	}

	/**
	 * Retrieve the Flag indicating whether Coupon DCF is computed off of the DCF Flag
	 * 
	 * @return TRUE => The Flag indicating whether Coupon DCF is computed off of the DCF Flag
	 */

	public boolean couponDCFOffOfFreq()
	{
		return _ucas.couponDCFOffOfFreq();
	}

	/**
	 * Retrieve the Calendar
	 * 
	 * @return The Calendar
	 */

	public java.lang.String calendar()
	{
		return _ucas.calendar();
	}

	/**
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _ucas.accrualCompoundingRule();
	}

	/**
	 * Retrieve the Period Full Coupon DCF
	 * 
	 * @return The Period Full Coupon DCF
	 */

	public double fullCouponDCF()
	{
		return _dblFullCouponDCF;
	}

	/**
	 * Convert the Coupon Frequency into a Tenor
	 * 
	 * @return The Coupon Frequency converted into a Tenor
	 */

	public java.lang.String tenor()
	{
		int iTenorInMonths = 12 / freq() ;

		return 1 == iTenorInMonths || 2 == iTenorInMonths || 3 == iTenorInMonths || 6 == iTenorInMonths || 12
			== iTenorInMonths ? iTenorInMonths + "M" : "ON";
	}

	/**
	 * Place the Date Node Location in relation to the segment Location
	 * 
	 * @param dblDateNode The Node Ordinate
	 * 
	 * @return One of NODE_LEFT_OF_SEGMENT, NODE_RIGHT_OF_SEGMENT, or NODE_INSIDE_SEGMENT
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public int dateLocation (
		final double dblDateNode)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDateNode))
			throw new java.lang.Exception ("ComposableUnitPeriod::dateLocation => Invalid Date Node");

		if (dblDateNode < _dblStartDate) return NODE_LEFT_OF_SEGMENT;

		if (dblDateNode > _dblEndDate) return NODE_RIGHT_OF_SEGMENT;

		return NODE_INSIDE_SEGMENT;
	}

	/**
	 * Get the period Accrual Day Count Fraction to an accrual end date
	 * 
	 * @param dblAccrualEnd Accrual End Date
	 * 
	 * @return The Accrual DCF
	 * 
	 * @exception Thrown if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrualDCF (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		if (NODE_INSIDE_SEGMENT != dateLocation (dblAccrualEnd))
			throw new java.lang.Exception
				("ComposableUnitPeriod::accrualDCF => Invalid in-period accrual date!");

		return org.drip.analytics.daycount.Convention.YearFraction (_dblStartDate, dblAccrualEnd,
			accrualDC(), accrualEOMAdjustment(), null, calendar()) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblStartDate, _dblEndDate,
					accrualDC(), accrualEOMAdjustment(), null, calendar()) * _dblFullCouponDCF;
	}

	/**
	 * Get the Period Full Coupon Rate
	 * 
	 * @param csqs The Market Curve and Surface
	 * 
	 * @return The Period Full Coupon Rate
	 * 
	 * @exception Thrown if the full Coupon Rate cannot be calculated
	 */

	public double fullCouponRate (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		return baseRate (csqs) + basis();
	}

	/**
	 * Get the Period Base Coupon Rate
	 * 
	 * @param csqs The Market Curve and Surface
	 * 
	 * @return The Period Base Coupon Rate
	 * 
	 * @exception Thrown if the base Coupon Rate cannot be calculated
	 */

	public abstract double baseRate (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception;

	/**
	 * Get the Period Coupon Basis
	 * 
	 * @return The Period Coupon Basis
	 */

	public abstract double basis();

	/**
	 * Get the Period Coupon Currency
	 * 
	 * @return The Period Coupon Currency
	 */

	public abstract java.lang.String couponCurrency();
}
