
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
 * CurrencySet contains the component's coupon, and the principal currency arrays. It exports serialization
 *  into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurrencySet implements org.drip.product.params.Validatable {
	private java.lang.String[] _astrCouponCurrency = null;
	private java.lang.String[] _astrPrincipalCurrency = null;

	/**
	 * Create a Single Currency CurrencySet Instance
	 * 
	 * @param strCurrency The Currency
	 * 
	 * @return The CurrencySet Instance
	 */

	public static final CurrencySet Create (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty()) return null;

		java.lang.String[] astrCurrency = new java.lang.String[] {strCurrency};

		CurrencySet cs = new CurrencySet (astrCurrency, astrCurrency);

		return cs.validate() ? cs : null;
	}

	/**
	 * Construct the CurrencySet object from the coupon and the principal currencies.
	 * 
	 * @param astrCouponCurrency Array of Coupon Currencies
	 * @param astrPrincipalCurrency Array of Principal Currencies
	 */

	public CurrencySet (
		final java.lang.String[] astrCouponCurrency,
		final java.lang.String[] astrPrincipalCurrency)
	{
		_astrCouponCurrency = astrCouponCurrency;
		_astrPrincipalCurrency = astrPrincipalCurrency;
	}

	@Override public boolean validate()
	{
		if (null == _astrCouponCurrency && null == _astrPrincipalCurrency) return true;

		int iNumCouponCurrency = null == _astrCouponCurrency ? 0 : _astrCouponCurrency.length;
		int iNumPrincipalCurrency = null == _astrPrincipalCurrency ? 0 : _astrPrincipalCurrency.length;

		for (int i = 0; i < iNumCouponCurrency; ++i) {
			if (null == _astrCouponCurrency[i] || _astrCouponCurrency[i].isEmpty()) return false;
		}

		for (int i = 0; i < iNumPrincipalCurrency; ++i) {
			if (null == _astrPrincipalCurrency[i] || _astrPrincipalCurrency[i].isEmpty()) return false;
		}

		return true;
	}

	/**
	 * Retrieve the Array of Coupon Currencies
	 * 
	 * @return The Array of Coupon Currencies
	 */

	public java.lang.String[] couponCurrency()
	{
		return _astrCouponCurrency;
	}

	/**
	 * Retrieve the Array of Principal Currencies
	 * 
	 * @return The Array of Principal Currencies
	 */

	public java.lang.String[] principalCurrency()
	{
		return _astrPrincipalCurrency;
	}
}
