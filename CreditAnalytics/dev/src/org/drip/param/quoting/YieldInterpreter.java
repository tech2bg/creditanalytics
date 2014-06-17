
package org.drip.param.quoting;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * YieldInterpreter holds the fields needed to interpret a Yield Quote. It contains the quote day count,
 *  quote frequency, quote EOM Adjustment, quote Act/Act parameters, and quote Calendar.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class YieldInterpreter extends org.drip.param.quoting.MeasureInterpreter {

	/*
	 * Quote Day Count
	 */

	private java.lang.String _strDC = "";

	/*
	 * Quote Frequency
	 */

	private int _iFreq = 0;

	/*
	 * Quote Apply EOM Adjustment?
	 */

	private boolean _bApplyEOMAdj = false;

	/*
	 * Quote Act Act DC Params
	 */

	private org.drip.analytics.daycount.ActActDCParams _aap = null;

	/*
	 * Quote Calendar
	 */

	private java.lang.String _strCalendar = "";

	/**
	 * YieldInterpreter de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if YieldInterpreter cannot be properly de-serialized
	 */

	public YieldInterpreter (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Empty state");

		java.lang.String strSerializedYieldInterpreter = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedYieldInterpreter || strSerializedYieldInterpreter.isEmpty())
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedYieldInterpreter,
			fieldDelimiter());

		if (null == astrField || 6 > astrField.length)
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Cannot locate yield DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strDC = "";
		else
			_strDC = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Cannot locate Yield Frequency");

		_iFreq = new java.lang.Integer (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Cannot locate yield DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCalendar = "";
		else
			_strCalendar = astrField[3];

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("YieldInterpreter de-serializer: Cannot locate apply EOM flag");

		_bApplyEOMAdj = new java.lang.Boolean (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("YieldInterpreter de-serializer: Cannot locate optional yield ActAct Params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			_aap = null;
		else
			_aap = new org.drip.analytics.daycount.ActActDCParams (astrField[5].getBytes());
	}

	/**
	 * Construct YieldInterpreter from the Day Count and the Frequency parameters
	 * 
	 * @param strDC Quoting Day Count
	 * @param iFreq Quoting Frequency
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public YieldInterpreter (
		final java.lang.String strDC,
		final int iFreq,
		final boolean bApplyEOMAdj,
		final org.drip.analytics.daycount.ActActDCParams aap,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == (_strDC = strDC) || _strDC.isEmpty() || 0 == (_iFreq = iFreq))
			throw new java.lang.Exception ("YieldInterpreter ctr: Invalid quoting params!");

		_aap = aap;
		_strCalendar = strCalendar;
		_bApplyEOMAdj = bApplyEOMAdj;
	}

	public java.lang.String getDC()
	{
		return _strDC;
	}

	public int getFrequency()
	{
		return _iFreq;
	}

	public boolean getApplyEOMAdj()
	{
		return _bApplyEOMAdj;
	}

	public org.drip.analytics.daycount.ActActDCParams getAAP()
	{
		return _aap;
	}

	public java.lang.String getCalendar()
	{
		return _strCalendar;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "~";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "`";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _strDC || _strDC.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strDC + fieldDelimiter());

		sb.append (_iFreq + fieldDelimiter());

		if (null == _strCalendar || _strCalendar.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCalendar + fieldDelimiter());

		sb.append (_bApplyEOMAdj + fieldDelimiter());

		if (null == _aap)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_aap.serialize()) + fieldDelimiter());

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new YieldInterpreter (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		YieldInterpreter yqi = new YieldInterpreter ("30/360", 2, true, null, "DKK");

		byte[] abYQI = yqi.serialize();

		System.out.println (new java.lang.String (abYQI));

		YieldInterpreter yqiDeser = new YieldInterpreter (abYQI);

		System.out.println (new java.lang.String (yqiDeser.serialize()));
	}
}
