
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
 * ComposableFixedPeriod contains the fixed cash flow periods' composable sub period details. Currently it
 *  holds the accrual start date, the accrual end date, the fixed coupon, the basis spread, coupon and
 *  accrual day counts, and the EOM adjustment flags.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableFixedPeriod extends org.drip.service.stream.Serializer {

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
	private double _dblBasis = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblFixedCoupon = java.lang.Double.NaN;
	private double _dblFullCouponDCF = java.lang.Double.NaN;
	private double _dblAccrualEndDate = java.lang.Double.NaN;
	private double _dblAccrualStartDate = java.lang.Double.NaN;

	/**
	 * The ComposableFixedPeriod constructor
	 * 
	 * @param dblAccrualStartDate Accrual Start Date
	 * @param dblAccrualEndDate Accrual End Date
	 * @param strCouponDC Coupon Day Count
	 * @param bCouponEOMAdjustment Coupon EOM Adjustment Flag
	 * @param strAccrualDC Accrual Day Count
	 * @param bAccrualEOMAdjustment Accrual EOM Adjustment Flag
	 * @param strCalendar Calendar
	 * @param dblFullCouponDCF The Period's Full Coupon DCF
	 * @param dblFixedCoupon Fixed Coupon (Annualized)
	 * @param dblBasis Basis over the Fixed Coupon in the same units
	 * @param dblNotional The Period Notional
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ComposableFixedPeriod (
		final double dblAccrualStartDate,
		final double dblAccrualEndDate,
		final java.lang.String strCouponDC,
		final boolean bCouponEOMAdjustment,
		final java.lang.String strAccrualDC,
		final boolean bAccrualEOMAdjustment,
		final java.lang.String strCalendar,
		final double dblFullCouponDCF,
		final double dblFixedCoupon,
		final double dblBasis,
		final double dblNotional)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStartDate = dblAccrualStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEndDate = dblAccrualEndDate) ||
				_dblAccrualStartDate >= _dblAccrualEndDate || null == (_strCouponDC = strCouponDC) ||
					_strCouponDC.isEmpty() || null == (_strAccrualDC = strAccrualDC) ||
						_strAccrualDC.isEmpty() || null == (_strCalendar = strCalendar) ||
							_strCalendar.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
								(_dblFullCouponDCF = dblFullCouponDCF) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblFixedCoupon =
										dblFixedCoupon) || !org.drip.quant.common.NumberUtil.IsValid
											(_dblBasis = dblBasis) ||
												!org.drip.quant.common.NumberUtil.IsValid (_dblNotional =
													dblNotional))
			throw new java.lang.Exception ("ComposableFixedPeriod ctr: Invalid Inputs");

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
	 * Retrieve the Fixed Coupon
	 * 
	 * @return The Fixed Coupon
	 */

	public double fixedCoupon()
	{
		return _dblFixedCoupon;
	}

	/**
	 * Retrieve the Basis
	 * 
	 * @return The Basis
	 */

	public double basis()
	{
		return _dblBasis;
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
			throw new java.lang.Exception ("ComposableFixedPeriod::dateLocation => Invalid Date Node");

		if (dblDateNode < _dblAccrualStartDate) return NODE_LEFT_OF_SEGMENT;

		if (dblDateNode > _dblAccrualEndDate) return NODE_RIGHT_OF_SEGMENT;

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
	 * 
	 * @return The Accrued
	 * 
	 * @exception Thrown if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrued (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		return _dblNotional * (_dblFixedCoupon + _dblBasis) * accrualDCF (dblAccrualEnd);
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
	 * @return The Period Full Coupon Accrued
	 */

	public double fullCouponAccrued()
	{
		return _dblNotional * (_dblFixedCoupon + _dblBasis) * _dblFullCouponDCF;
	}

	/**
	 * De-serialization of ComposableFixedPeriod from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize ComposableFixedPeriod
	 */

	public ComposableFixedPeriod (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ComposableFixedPeriod de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ComposableFixedPeriod de-serializer: Empty state");

		java.lang.String strComposableFixedPeriod = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strComposableFixedPeriod || strComposableFixedPeriod.isEmpty())
			throw new java.lang.Exception ("ComposableFixedPeriod de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strComposableFixedPeriod,
			fieldDelimiter());

		if (null == astrField || 12 > astrField.length)
			throw new java.lang.Exception ("ComposableFixedPeriod de-serialize: Invalid number of fields");

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Accrual Start Date");

		_dblAccrualStartDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Accrual End Date");

		_dblAccrualEndDate = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Coupon Day Count");

		_strCouponDC = astrField[3];

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Coupon EOM Adjustment Flag");

		_bCouponEOMAdjustment = new java.lang.Boolean (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Accrual Day Count");

		_strAccrualDC = astrField[5];

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Accrual EOM Adjustment Flag");

		_bAccrualEOMAdjustment = new java.lang.Boolean (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("ComposableFixedPeriod de-serializer: Cannot locate Calendar");

		_strCalendar = astrField[7];

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Period Full Coupon DCF");

		_dblFullCouponDCF = new java.lang.Double (astrField[8]);

		if (null == astrField[9] || astrField[9].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Fixed Coupon");

		_dblFixedCoupon = new java.lang.Double (astrField[9]);

		if (null == astrField[10] || astrField[10].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[10]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Fixed Coupon Basis");

		_dblBasis = new java.lang.Double (astrField[10]);

		if (null == astrField[11] || astrField[11].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[11]))
			throw new java.lang.Exception
				("ComposableFixedPeriod de-serializer: Cannot locate Period Notional");

		_dblNotional = new java.lang.Double (astrField[11]);
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "'";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "_";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _dblAccrualStartDate +
			fieldDelimiter() + _dblAccrualEndDate + fieldDelimiter() + _strCouponDC + fieldDelimiter() +
				_bCouponEOMAdjustment + fieldDelimiter() + _strAccrualDC + fieldDelimiter() +
					_bAccrualEOMAdjustment + fieldDelimiter() + _strCalendar + fieldDelimiter() +
						_dblFullCouponDCF + fieldDelimiter() + _dblFixedCoupon + fieldDelimiter() +
							_dblBasis + fieldDelimiter() + _dblNotional);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ComposableFixedPeriod (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
