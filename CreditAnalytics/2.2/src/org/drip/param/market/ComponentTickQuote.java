
package org.drip.param.market;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ComponentTickQuote holds the tick related component parameters - it contains the product ID, the quote
 *  composite, the source, the counter party, and whether the quote can be treated as a mark.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentTickQuote extends org.drip.service.stream.Serializer {
	private boolean _bIsMark = false;
	private java.lang.String _strSource = "";
	private java.lang.String _strProductID = "";
	private java.lang.String _strCounterParty = "";
	private org.drip.param.definition.ComponentQuote _cq = null;

	/**
	 * Empty ComponentTickQuote constructor
	 */

	public ComponentTickQuote()
	{
	}

	/**
	 * ComponentTickQuote constructor
	 * 
	 * @param strProductID Product ID
	 * @param cq Product Quote
	 * @param strCounterParty Counter Party
	 * @param strSource Quote Source
	 * @param bIsMark TRUE => This Quote may be treated as a Mark
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ComponentTickQuote (
		final java.lang.String strProductID,
		final org.drip.param.definition.ComponentQuote cq,
		final java.lang.String strCounterParty,
		final java.lang.String strSource,
		final boolean bIsMark)
		throws java.lang.Exception
	{
		if (null == (_strProductID = strProductID) || _strProductID.isEmpty() || null == (_cq = cq))
			throw new java.lang.Exception ("ComponentTickQuote ctr: Invalid Inputs");

		_bIsMark = bIsMark;
		_strSource = strSource;
		_strCounterParty = strCounterParty;
	}

	/**
	 * ComponentQuote de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ComponentQuote cannot be properly de-serialized
	 */

	public ComponentTickQuote (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ComponentTickQuote de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ComponentTickQuote de-serializer: Empty state");

		java.lang.String strSerializedComponentTickQuote = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedComponentTickQuote || strSerializedComponentTickQuote.isEmpty())
			throw new java.lang.Exception ("ComponentTickQuote de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedComponentTickQuote, getFieldDelimiter());

		if (null == astrField || 6 > astrField.length)
			throw new java.lang.Exception ("ComponentTickQuote de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("ComponentTickQuote de-serializer: Cannot locate Product ID");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strProductID = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("ComponentTickQuote de-serializer: Cannot locate Component Quote");

		_cq = new org.drip.param.market.ComponentMultiMeasureQuote (astrField[2].getBytes());

		if (null != astrField[3] && !org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
			(astrField[3]))
			_strSource = astrField[3];

		if (null != astrField[4] && !org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
			(astrField[4]))
			_strCounterParty = astrField[4];

		if (null != astrField[5] && !org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
			(astrField[5]))
			_bIsMark = new java.lang.Boolean (astrField[5]);
	}

	@Override public java.lang.String getCollectionKeyValueDelimiter()
	{
		return "]";
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "[";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "~";
	}

	/**
	 * Retrieve the Product ID
	 * 
	 * @return Product ID
	 */

	public java.lang.String getProductID()
	{
		return _strProductID;
	}

	/**
	 * Retrieve the Component Quote
	 * 
	 * @return Component Quote
	 */

	public org.drip.param.definition.ComponentQuote getComponentQuote()
	{
		return _cq;
	}

	/**
	 * Retrieve the Quote Source
	 * 
	 * @return Quote Source
	 */

	public java.lang.String getSource()
	{
		return _strSource;
	}

	/**
	 * Retrieve the Counter Party
	 * 
	 * @return Counter Party
	 */

	public java.lang.String getCounterParty()
	{
		return _strCounterParty;
	}

	/**
	 * Indicate whether the quote may be treated as a mark
	 * 
	 * @return TRUE => Treat the Quote as a Mark
	 */

	public boolean isMark()
	{
		return _bIsMark;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter() + _strProductID +
			getFieldDelimiter() + new java.lang.String (_cq.serialize()) + getFieldDelimiter());

		if (null == _strSource)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strSource + getFieldDelimiter());

		if (null == _strCounterParty)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCounterParty + getFieldDelimiter());

		return sb.append (_bIsMark + getFieldDelimiter()).append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ComponentTickQuote (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
