
package org.drip.product.params;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * RatesSetting contains the rate related valuation parameters - the discount curves to be used for
 *  discounting the coupon, the redemption, the principal, and the settle cash flows. It exports
 *  serialization into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesSetting implements org.drip.product.params.Validatable {

	/**
	 * Trade Currency Discount Curve Name
	 */

	public java.lang.String _strTradeDiscountCurve = "";

	/**
	 * Coupon Cash flow Discount Curve Name
	 */

	public java.lang.String _strCouponDiscountCurve = "";

	/**
	 * Principal Cash flow Discount Curve Name
	 */

	public java.lang.String _strPrincipalDiscountCurve = "";

	/**
	 * Redemption Cash flow Discount Curve Name
	 */

	public java.lang.String _strRedemptionDiscountCurve = "";

	/**
	 * RatesSetting constructor
	 * 
	 * @param strTradeDiscountCurve Trade Cash flow Discount Curve
	 * @param strCouponDiscountCurve Coupon Cash flow Discount Curve
	 * @param strPrincipalDiscountCurve Principal Cash flow Discount Curve
	 * @param strRedemptionDiscountCurve Redemption Cash flow Discount Curve
	 */

	public RatesSetting (
		final java.lang.String strTradeDiscountCurve,
		final java.lang.String strCouponDiscountCurve,
		final java.lang.String strPrincipalDiscountCurve,
		final java.lang.String strRedemptionDiscountCurve)
	{
		_strTradeDiscountCurve = strTradeDiscountCurve;
		_strCouponDiscountCurve = strCouponDiscountCurve;
		_strPrincipalDiscountCurve = strPrincipalDiscountCurve;
		_strRedemptionDiscountCurve = strRedemptionDiscountCurve;
	}

	@Override public boolean validate()
	{
		if (null == _strTradeDiscountCurve || _strTradeDiscountCurve.isEmpty() || null ==
			_strCouponDiscountCurve || _strCouponDiscountCurve.isEmpty() || null ==
				_strPrincipalDiscountCurve || _strPrincipalDiscountCurve.isEmpty() || null ==
					_strRedemptionDiscountCurve || _strRedemptionDiscountCurve.isEmpty())
			return false;

		return true;
	}
}
