
package org.drip.product.rates;

import org.drip.product.credit.CDSBasket;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * Implements the Basket of Rates Component legs.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesBasket extends org.drip.product.definition.BasketProduct {
	private java.lang.String _strName = "";
	private org.drip.product.definition.RatesComponent[] _aComp = null;

	/**
	 * RatesBasket constructor
	 * 
	 * @param strName Basket Name
	 * @param aComp Basket Rates Components
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public RatesBasket (
		final java.lang.String strName,
		final org.drip.product.definition.RatesComponent[] aComp)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_aComp = aComp) || 0 ==
			_aComp.length)
			throw new java.lang.Exception ("RatesBasket ctr => Invalid Inputs");
	}

	/**
	 * RatesBasket de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if RatesBasket cannot be properly de-serialized
	 */

	public RatesBasket (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("RatesBasket de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("RatesBasket de-serializer: Empty state");

		java.lang.String strSerializedRatesBasket = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedRatesBasket || strSerializedRatesBasket.isEmpty())
			throw new java.lang.Exception ("RatesBasket de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.analytics.support.GenericUtil.Split
			(strSerializedRatesBasket, getFieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("RatesBasket de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equals (astrField[1]))
			_strName = "";
		else
			_strName = astrField[1];

		java.lang.String[] astrComp = org.drip.analytics.support.GenericUtil.Split (astrField[2],
			getCollectionRecordDelimiter());

		if (null == astrComp || 0 == astrComp.length)
			throw new java.lang.Exception ("RatesBasket de-serializer: Cannot locate component array");

		_aComp = new org.drip.product.definition.RatesComponent[astrComp.length];

		for (int i = 0; i < astrComp.length; ++i) {
			if (null == astrComp[i] || astrComp[i].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrComp[i]))
				throw new java.lang.Exception ("RatesBasket de-serializer: Cannot locate component #" + i);

			// _aComp[i] = new CDSComponent (astrComp[i].getBytes());
		}
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		if (null == _strName || _strName.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strName + getFieldDelimiter());

		if (null == _aComp || 0 == _aComp.length)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbComp = new java.lang.StringBuffer();

			for (org.drip.product.definition.Component comp : _aComp) {
				if (null == comp || !(comp instanceof org.drip.product.definition.RatesComponent)) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbComp.append (getCollectionRecordDelimiter());

				sbComp.append (new java.lang.String (comp.serialize()));
			}

			if (sbComp.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbComp.toString() + getFieldDelimiter());
		}

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab) {
		try {
			return new CDSBasket (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String getName()
	{
		return _strName;
	}

	@Override public org.drip.product.definition.Component[] getComponents()
	{
		return _aComp;
	}

	@Override public java.util.Set<java.lang.String> getComponentIRCurveNames()
	{
		java.util.Set<java.lang.String> sIR = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < _aComp.length; ++i)
			sIR.add (_aComp[i].getIRCurveName());

		return sIR;
	}

	@Override public java.util.Set<java.lang.String> getComponentCreditCurveNames()
	{
		java.util.Set<java.lang.String> sCC = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < _aComp.length; ++i)
			sCC.add (_aComp[i].getCreditCurveName());

		return sCC;
	}
}
