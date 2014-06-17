
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

public class CurrencySet extends org.drip.service.stream.Serializer implements
	org.drip.product.params.Validatable {
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

	/**
	 * CurrencySet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CurrencySet cannot be properly de-serialized
	 */

	public CurrencySet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CurrencySet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CurrencySet de-serializer: Empty state");

		java.lang.String strSerializedCurrencySet = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedCurrencySet || strSerializedCurrencySet.isEmpty())
			throw new java.lang.Exception ("CurrencySet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedCurrencySet,
			fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("CurrencySet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		java.lang.String strCollectionRecordDelimiter = collectionRecordDelimiter();

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("CurrencySet de-serializer: Cannot locate Coupon Currency Array");

		_astrCouponCurrency = org.drip.quant.common.StringUtil.Split (astrField[1],
			strCollectionRecordDelimiter);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("CurrencySet de-serializer: Cannot locate Principal Currency");

		_astrPrincipalCurrency = org.drip.quant.common.StringUtil.Split (astrField[2],
			strCollectionRecordDelimiter);

		if (!validate()) throw new java.lang.Exception ("CurrencySet de-serializer: Cannot validate!");
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

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		java.lang.String strCollectionRecordDelimiter = collectionRecordDelimiter();

		java.lang.String strCouponCurrencyArray = org.drip.quant.common.StringUtil.StringArrayToString
			(_astrCouponCurrency, strCollectionRecordDelimiter,
				org.drip.service.stream.Serializer.NULL_SER_STRING);

		strCouponCurrencyArray = null == strCouponCurrencyArray || strCouponCurrencyArray.isEmpty() ?
			org.drip.service.stream.Serializer.NULL_SER_STRING : strCouponCurrencyArray;

		java.lang.String strPrincipalCurrencyArray = org.drip.quant.common.StringUtil.StringArrayToString
			(_astrPrincipalCurrency, strCollectionRecordDelimiter,
				org.drip.service.stream.Serializer.NULL_SER_STRING);

		strPrincipalCurrencyArray = null == strPrincipalCurrencyArray || strPrincipalCurrencyArray.isEmpty()
			? org.drip.service.stream.Serializer.NULL_SER_STRING : strPrincipalCurrencyArray;

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + strCouponCurrencyArray +
			fieldDelimiter() + strPrincipalCurrencyArray);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CurrencySet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		CurrencySet bcp = new CurrencySet (new java.lang.String[] {"ABC"}, new java.lang.String[] {"GHI"});

		byte[] abBCP = bcp.serialize();

		System.out.println (new java.lang.String (abBCP));

		CurrencySet bcpDeser = new CurrencySet (abBCP);

		System.out.println (new java.lang.String (bcpDeser.serialize()));
	}
}
