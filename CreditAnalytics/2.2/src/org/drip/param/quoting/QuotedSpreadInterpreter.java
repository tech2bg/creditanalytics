
package org.drip.param.quoting;

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
 * QuotedSpreadInterpreter holds the fields needed to interpret a Quoted Spread Quote. It contains the
 * 	contract type and the coupon.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class QuotedSpreadInterpreter extends org.drip.param.quoting.MeasureInterpreter {

	/**
	 * SNAC CDS Contract
	 */

	public static final java.lang.String SNAC_CDS = "SNAC";

	/**
	 * Conventional CDS Contract
	 */

	public static final java.lang.String CONV_CDS = "CONV";

	/**
	 * STEM CDS Contract
	 */

	public static final java.lang.String STEM_CDS = "CONV";

	private java.lang.String _strCDSContractType = "";
	private double _dblCouponStrike = java.lang.Double.NaN;

	/**
	 * QuotedSpreadInterpreter constructor
	 * 
	 * @param strCDSContractType The CDS Contract Type
	 * @param dblCouponStrike The Coupon Strike
	 * 
	 * @throws java.lang.Exception
	 */

	public QuotedSpreadInterpreter (
		final java.lang.String strCDSContractType,
		final double dblCouponStrike)
		throws java.lang.Exception
	{
		if (null == (_strCDSContractType = strCDSContractType) || (!CONV_CDS.equalsIgnoreCase
			(_strCDSContractType) && !SNAC_CDS.equalsIgnoreCase (_strCDSContractType) &&
				!STEM_CDS.equalsIgnoreCase (_strCDSContractType)))
			throw new java.lang.Exception ("QuotedSpreadInterpreter ctr: Invalid Inputs");

		_dblCouponStrike = dblCouponStrike;
	}

	/**
	 * QuotedSpreadInterpreter de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if QuotedSpreadInterpreter cannot be properly de-serialized
	 */

	public QuotedSpreadInterpreter (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("QuotedSpreadInterpreter de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("QuotedSpreadInterpreter de-serializer: Empty state");

		java.lang.String strSerializedQuotedSpreadInterpreter = strRawString.substring (0,
			strRawString.indexOf (getObjectTrailer()));

		if (null == strSerializedQuotedSpreadInterpreter || strSerializedQuotedSpreadInterpreter.isEmpty())
			throw new java.lang.Exception ("QuotedSpreadInterpreter de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split
			(strSerializedQuotedSpreadInterpreter, getFieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("QuotedSpreadInterpreter de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("QuotedSpreadInterpreter de-serializer: Cannot locate yield DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strCDSContractType = "";
		else
			_strCDSContractType = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("QuotedSpreadInterpreter de-serializer: Cannot locate Yield Frequency");

		_dblCouponStrike = new java.lang.Double (astrField[2]);
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "~";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "`";
	}

	/**
	 * Retrieve the CDS Contract Type
	 * 
	 * @return The CDS Contract Type
	 */

	public java.lang.String getCDSContractType()
	{
		return _strCDSContractType;
	}

	/**
	 * Retrieve the Coupon Strike
	 * 
	 * @return The Coupon Strike
	 */

	public double getCouponStrike()
	{
		return _dblCouponStrike;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_strCDSContractType + getFieldDelimiter());

		sb.append (_dblCouponStrike + getFieldDelimiter());

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new QuotedSpreadInterpreter (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		QuotedSpreadInterpreter qsi = new QuotedSpreadInterpreter (SNAC_CDS, 100.);

		byte[] abQSI = qsi.serialize();

		System.out.println (new java.lang.String (abQSI));

		QuotedSpreadInterpreter qsiDeser = new QuotedSpreadInterpreter (abQSI);

		System.out.println (new java.lang.String (qsiDeser.serialize()));
	}
}
