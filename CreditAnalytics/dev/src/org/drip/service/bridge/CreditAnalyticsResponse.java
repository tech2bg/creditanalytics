
package org.drip.service.bridge;

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
 * CreditAnalyticsResponse contains the response from the Credit Analytics server to the client. It contains
 * 	the following parameters:
 * 	- Compose Specific Response Messages - STATUS, FAILURE and SUCCESS messages.
 * 	- The GUID and of the request.
 * 	- The type and time-stamp of the response.
 * 	- The string and byte array version of the response body.
 * 	- Serlization into and de-serialization out of byte arrays.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsResponse extends org.drip.service.stream.Serializer {

	/**
	 * Status Message
	 */

	public static final java.lang.String CAR_STATUS = "Status";

	/**
	 * Failure Message
	 */

	public static final java.lang.String CAR_FAILURE = "Failure";

	/**
	 * Success Message
	 */

	public static final java.lang.String CAR_SUCCESS = "Success";

	private byte[] _abMeasure = null;
	private java.lang.String _strTime = "";
	private java.lang.String _strType = "";
	private java.lang.String _strBaseMsg = "";
	private java.lang.String _strRequestID = "";

	/**
	 * CreditAnalyticsResponse de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CreditAnalyticsResponse cannot be properly de-serialized
	 */

	public CreditAnalyticsResponse (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CreditAnalyticsResponse de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsResponse de-serializer: Empty state");

		java.lang.String strSerializedCreditAnalyticsResponse = strRawString.substring (0,
			strRawString.indexOf (getObjectTrailer()));

		if (null == strSerializedCreditAnalyticsResponse || strSerializedCreditAnalyticsResponse.isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsResponse de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedCreditAnalyticsResponse, getFieldDelimiter());

		if (null == astrField || 6 > astrField.length)
			throw new java.lang.Exception ("CreditAnalyticsResponse de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsResponse de-serializer: Cannot locate Request ID");

		_strRequestID = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsResponse de-serializer: Cannot locate type");

		_strType = astrField[2];

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsResponse de-serializer: Cannot locate valuation params");

		_strBaseMsg = astrField[3];

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsResponse de-serializer: Cannot locate pricer params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_abMeasure = null;
		else
			_abMeasure = astrField[4].getBytes();

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsResponse de-serializer: Cannot locate time stamp");

		_strTime = astrField[5];
	}

	/**
	 * CreditAnalyticsResponse constructor
	 * 
	 * @param strRequestID The corresponding Request ID
	 * @param strType Type
	 * @param strBaseMsg Base Message
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CreditAnalyticsResponse (
		final java.lang.String strRequestID,
		final java.lang.String strType,
		final java.lang.String strBaseMsg)
		throws java.lang.Exception
	{
		if (null == (_strRequestID = strRequestID) || _strRequestID.isEmpty() || null == (_strType = strType)
			|| (!CAR_STATUS.equalsIgnoreCase (_strType) && !CAR_FAILURE.equalsIgnoreCase (_strType) &&
				!CAR_SUCCESS.equalsIgnoreCase (_strType)) || null == (_strBaseMsg = strBaseMsg) ||
					_strBaseMsg.isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsResponse ctr: Invalid inputs");

		_strTime = new java.util.Date().toString();
	}

	/**
	 * Retrieve the Request ID
	 * 
	 * @return The Type
	 */

	public java.lang.String getRequestID()
	{
		return _strRequestID;
	}

	/**
	 * Retrieve the Time Snap
	 * 
	 * @return The Time Snap
	 */

	public java.lang.String getTimeSnap()
	{
		return _strTime;
	}

	/**
	 * Retrieve the Type
	 * 
	 * @return The Type
	 */

	public java.lang.String getType()
	{
		return _strType;
	}

	/**
	 * Retrieve the Base Message
	 * 
	 * @return The Base Message
	 */

	public java.lang.String getBaseMsg()
	{
		return _strBaseMsg;
	}

	/**
	 * Set the Measure Bytes
	 * 
	 * @param abMeasure The Measure Bytes
	 * 
	 * @return TRUE => Message Properly set
	 */

	public boolean setSerializedMsg (
		final byte[] abMeasure)
	{
		return null != (_abMeasure = abMeasure);
	}

	/**
	 * Retrieve the Measure Bytes
	 * 
	 * @return The Measure Bytes
	 */

	public byte[] getSerializedMsg()
	{
		return _abMeasure;
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "(";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return ")";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_strRequestID + getFieldDelimiter());

		sb.append (_strType + getFieldDelimiter());

		sb.append (_strBaseMsg + getFieldDelimiter());

		if (null == _abMeasure)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_abMeasure) + getFieldDelimiter());

		sb.append (_strTime);

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab) {
		try {
			return new CreditAnalyticsResponse (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.support.Logger.Init ("c:\\DRIP\\Config.xml");

		org.drip.analytics.daycount.Convention.Init ("c:\\DRIP\\Config.xml");

		CreditAnalyticsResponse cre = new CreditAnalyticsResponse ("1", CAR_STATUS, "OK");

		byte[] abCRE = cre.serialize();

		java.lang.String strCRE = new java.lang.String (abCRE);

		System.out.println (strCRE);

		CreditAnalyticsResponse creDeser = new CreditAnalyticsResponse (abCRE);

		System.out.println (new java.lang.String (creDeser.serialize()));
	}
}
