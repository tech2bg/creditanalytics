
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
 * ComposableFixedUnitSetting contains the fixed unit details. Currently it holds the coupon currency, the
 * 	fixed coupon, and the basis.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableFixedUnitSetting {
	private java.lang.String _strTenor = "";
	private int _iEdgeDateSequenceScheme = -1;
	private double _dblBasis = java.lang.Double.NaN;
	private java.lang.String _strCouponCurrency = "";
	private double _dblFixedCoupon = java.lang.Double.NaN;
	private org.drip.analytics.daycount.DateAdjustParams _dapEdge = null;

	/**
	 * ComposableFixedUnitSetting constructor
	 * 
	 * @param strTenor Unit Tenor
	 * @param iEdgeDateSequenceScheme Edge Date Generation Scheme
	 * @param dapEdge Date Adjust Parameter Settings for the Edge Dates
	 * @param dblFixedCoupon Fixed Coupon (Annualized)
	 * @param dblBasis Basis over the Fixed Coupon in the same units
	 * @param strCouponCurrency Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public ComposableFixedUnitSetting (
		final java.lang.String strTenor,
		final int iEdgeDateSequenceScheme,
		final org.drip.analytics.daycount.DateAdjustParams dapEdge,
		final double dblFixedCoupon,
		final double dblBasis,
		final java.lang.String strCouponCurrency)
		throws java.lang.Exception
	{
		if (null == (_strTenor = strTenor) || _strTenor.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblFixedCoupon = dblFixedCoupon) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblBasis = dblBasis) || null ==
					(_strCouponCurrency = strCouponCurrency) || _strCouponCurrency.isEmpty())
			throw new java.lang.Exception ("ComposableFixedUnitSetting ctr: Invalid Inputs");

		_dapEdge = dapEdge;
		_iEdgeDateSequenceScheme = iEdgeDateSequenceScheme;
	}

	/**
	 * Retrieve the Tenor
	 * 
	 * @return The Tenor
	 */

	public java.lang.String tenor()
	{
		return _strTenor;
	}

	/**
	 * Retrieve the Edge Date Generation Scheme
	 * 
	 * @return The Edge Date Generation Scheme
	 */

	public int edgeDateSequenceScheme()
	{
		return _iEdgeDateSequenceScheme;
	}

	/**
	 * Retrieve the Edge Date Adjust Parameters
	 * 
	 * @return The Edge Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapEdge()
	{
		return _dapEdge;
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
	 * Retrieve the Fixed Coupon Basis
	 * 
	 * @return The Fixed Coupon Basis
	 */

	public double basis()
	{
		return _dblBasis;
	}

	/**
	 * Retrieve the Fixed Coupon Currency
	 * 
	 * @return The Fixed Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}
}