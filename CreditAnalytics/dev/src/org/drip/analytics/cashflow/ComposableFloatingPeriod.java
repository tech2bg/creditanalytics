
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
 * ComposableFloatingPeriod contains the cash flow periods' composable sub period details. Currently it holds
 * 	the accrual start date, the accrual end date, the fixing date, the spread over the index, and the
 * 	corresponding reference index period.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableFloatingPeriod {

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

	private java.lang.String _strCalendar = "";
	private java.lang.String _strCouponDC = "";
	private java.lang.String _strAccrualDC = "";
	private boolean _bCouponEOMAdjustment = false;
	private boolean _bAccrualEOMAdjustment = false;
	private double _dblSpread = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblFullCouponDCF = java.lang.Double.NaN;
	private double _dblAccrualEndDate = java.lang.Double.NaN;
	private double _dblAccrualStartDate = java.lang.Double.NaN;
	private org.drip.analytics.cashflow.ReferenceIndexPeriod _refIndexPeriod = null;

	/**
	 * The ComposableFloatingPeriod constructor
	 * 
	 * @param dblAccrualStartDate Accrual Start Date
	 * @param dblAccrualEndDate Accrual End Date
	 * @param strCouponDC Coupon Day Count
	 * @param bCouponEOMAdjustment Coupon EOM Adjustment Flag
	 * @param strAccrualDC Accrual Day Count
	 * @param bAccrualEOMAdjustment Accrual EOM Adjustment Flag
	 * @param strCalendar Calendar
	 * @param dblFullCouponDCF The Period's Full Coupon DCF
	 * @param refIndexPeriod The Reference Index Period
	 * @param dblSpread The Floater Spread
	 * @param dblNotional The Period Notional
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ComposableFloatingPeriod (
		final double dblAccrualStartDate,
		final double dblAccrualEndDate,
		final java.lang.String strCouponDC,
		final boolean bCouponEOMAdjustment,
		final java.lang.String strAccrualDC,
		final boolean bAccrualEOMAdjustment,
		final java.lang.String strCalendar,
		final double dblFullCouponDCF,
		final org.drip.analytics.cashflow.ReferenceIndexPeriod refIndexPeriod,
		final double dblSpread,
		final double dblNotional)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStartDate = dblAccrualStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEndDate = dblAccrualEndDate) ||
				_dblAccrualStartDate >= _dblAccrualEndDate || null == (_strCouponDC = strCouponDC) ||
					_strCouponDC.isEmpty() || null == (_strAccrualDC = strAccrualDC) ||
						_strAccrualDC.isEmpty() || null == (_strCalendar = strCalendar) ||
							_strCalendar.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
								(_dblFullCouponDCF = dblFullCouponDCF) || null == (_refIndexPeriod =
									refIndexPeriod) || !org.drip.quant.common.NumberUtil.IsValid (_dblSpread
										= dblSpread) || !org.drip.quant.common.NumberUtil.IsValid
											(_dblNotional = dblNotional))
			throw new java.lang.Exception ("ComposableFloatingPeriod ctr: Invalid Inputs");

		_bCouponEOMAdjustment = bCouponEOMAdjustment;
		_bAccrualEOMAdjustment = bAccrualEOMAdjustment;
	}

	/**
	 * Retrieve the Accrual Start Date
	 * 
	 * @return The Accrual Start Date
	 */

	public double accrualStartDate()
	{
		return _dblAccrualStartDate;
	}

	/**
	 * Retrieve the Accrual End Date
	 * 
	 * @return The Accrual End Date
	 */

	public double accrualEndDate()
	{
		return _dblAccrualEndDate;
	}

	/**
	 * Retrieve the Coupon Day Count
	 * 
	 * @return The Coupon Day Count
	 */

	public java.lang.String couponDC()
	{
		return _strCouponDC;
	}

	/**
	 * Retrieve the Coupon EOM Adjustment Flag
	 * 
	 * @return The Coupon EOM Adjustment Flag
	 */

	public boolean couponEOMAdjustment()
	{
		return _bCouponEOMAdjustment;
	}

	/**
	 * Retrieve the Accrual Day Count
	 * 
	 * @return The Accrual Day Count
	 */

	public java.lang.String accrualDC()
	{
		return _strAccrualDC;
	}

	/**
	 * Retrieve the Accrual EOM Adjustment Flag
	 * 
	 * @return The Accrual EOM Adjustment Flag
	 */

	public boolean accrualEOMAdjustment()
	{
		return _bAccrualEOMAdjustment;
	}

	/**
	 * Retrieve the Calendar
	 * 
	 * @return The Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
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
	 * Retrieve the Spread
	 * 
	 * @return The Spread
	 */

	public double spread()
	{
		return _dblSpread;
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

	/**
	 * Retrieve the Period Notional
	 * 
	 * @return The Period Notional
	 */

	public double notional()
	{
		return _dblNotional;
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
			throw new java.lang.Exception ("ComposableFloatingPeriod::dateLocation => Invalid Date Node");

		if (dblDateNode < _dblAccrualStartDate) return NODE_LEFT_OF_SEGMENT;

		if (dblDateNode > _dblAccrualEndDate) return NODE_RIGHT_OF_SEGMENT;

		return NODE_INSIDE_SEGMENT;
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

	public double referenceRate (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == csqs) return java.lang.Double.NaN;

		double dblFixingDate = _refIndexPeriod.fixingDate();

		org.drip.state.identifier.ForwardLabel forwardLabel = _refIndexPeriod.forwardLabel();

		if (csqs.available (dblFixingDate, forwardLabel))
			return csqs.getFixing (dblFixingDate, forwardLabel);

		double dblReferencePeriodEndDate = _refIndexPeriod.endDate();

		org.drip.analytics.rates.ForwardRateEstimator fre = csqs.forwardCurve (forwardLabel);

		if (null != fre) return fre.forward (dblReferencePeriodEndDate);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (forwardLabel.currency()));

		if (null == dcFunding)
			throw new java.lang.Exception
				("ComposableFloatingPeriod::referenceRate => Cannot locate Funding Curve");

		double dblReferencePeriodStartDate = _refIndexPeriod.startDate();

		double dblEpochDate = dcFunding.epoch().julian();

		if (dblEpochDate > dblReferencePeriodStartDate)
			dblReferencePeriodEndDate = new org.drip.analytics.date.JulianDate (dblReferencePeriodStartDate =
				dblEpochDate).addTenor (forwardLabel.tenor()).julian();

		return dcFunding.libor (dblReferencePeriodStartDate, dblReferencePeriodEndDate, _dblFullCouponDCF);
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
				("ComposableFixedPeriod::accrualDCF => Invalid in-period accrual date!");

		return org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate, dblAccrualEnd,
			_strAccrualDC, _bAccrualEOMAdjustment, null, _strCalendar) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate,
					_dblAccrualEndDate, _strAccrualDC, _bAccrualEOMAdjustment, null, _strCalendar) *
						_dblFullCouponDCF;
	}

	/**
	 * Get the Accrued01 to an accrual end date
	 * 
	 * @param dblAccrualEnd Accrual End Date
	 * 
	 * @return The Accrued01
	 * 
	 * @exception Thrown if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrued01 (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		return 0.0001 * _dblNotional * accrualDCF (dblAccrualEnd);
	}

	/**
	 * Get the Accrued to an accrual end date
	 * 
	 * @param dblAccrualEnd Accrual End Date
	 * @param csqs The Market Curve and Surface
	 * 
	 * @return The Accrued
	 * 
	 * @exception Thrown if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrued (
		final double dblAccrualEnd,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		return _dblNotional * (referenceRate (csqs) + _dblSpread) * accrualDCF (dblAccrualEnd);
	}

	/**
	 * Get the Period Full Coupon 01
	 * 
	 * @return The Period Full Coupon 01
	 */

	public double fullCoupon01()
	{
		return 0.0001 * _dblNotional * _dblFullCouponDCF;
	}

	/**
	 * Get the Period Full Coupon Accrued
	 * 
	 * @param csqs The Market Curve and Surface
	 * 
	 * @return The Period Full Coupon Accrued
	 * 
	 * @exception Thrown if inputs are invalid, or if the date does not lie within the period
	 */

	public double fullCouponAccrued (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		return _dblNotional * (referenceRate (csqs) + _dblSpread) * _dblFullCouponDCF;
	}
}
