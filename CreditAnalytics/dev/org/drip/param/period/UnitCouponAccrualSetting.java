
package org.drip.param.period;

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
 * UnitCouponAccrualSetting contains the cash flow periods' Coupon/Accrual details. Currently it holds the
 *  frequency, the calendar, the coupon/accrual day counts and the EOM adjustment flags, and flag indicating
 *  whether the coupon is computed from frequency.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnitCouponAccrualSetting {
	private int _iFreq = -1;
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCouponDC = "";
	private java.lang.String _strAccrualDC = "";
	private boolean _bCouponDCFOffOfFreq = false;
	private boolean _bCouponEOMAdjustment = false;
	private boolean _bAccrualEOMAdjustment = false;

	/**
	 * UnitCouponAccrualSetting constructor
	 * 
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon Day Count
	 * @param bCouponEOMAdjustment Coupon EOM Adjustment Flag
	 * @param strAccrualDC Accrual Day Count
	 * @param bAccrualEOMAdjustment Accrual EOM Adjustment Flag
	 * @param strCalendar Calendar
	 * @param bCouponDCFOffOfFreq Compute Full Coupon DCF from the Frequency
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public UnitCouponAccrualSetting (
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bCouponEOMAdjustment,
		final java.lang.String strAccrualDC,
		final boolean bAccrualEOMAdjustment,
		final java.lang.String strCalendar,
		final boolean bCouponDCFOffOfFreq)
		throws java.lang.Exception
	{
		if (0 >= (_iFreq = iFreq) || null == (_strCouponDC = strCouponDC) || _strCouponDC.isEmpty() || null
			== (_strAccrualDC = strAccrualDC) || _strAccrualDC.isEmpty() || null == (_strCalendar =
				strCalendar) || _strCalendar.isEmpty())
			throw new java.lang.Exception ("UnitCouponAccrualSetting ctr: Invalid Inputs");

		_bCouponDCFOffOfFreq = bCouponDCFOffOfFreq;
		_bCouponEOMAdjustment = bCouponEOMAdjustment;
		_bAccrualEOMAdjustment = bAccrualEOMAdjustment;
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _iFreq;
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
	 * Retrieve the Flag indicating whether Coupon DCF is computed off of the DCF Flag
	 * 
	 * @return TRUE => The Flag indicating whether Coupon DCF is computed off of the DCF Flag
	 */

	public boolean couponDCFOffOfFreq()
	{
		return _bCouponDCFOffOfFreq;
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
}
