
package org.drip.analytics.daycount;

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
 * This class contains parameters to represent Act/Act day count. It exports the following functionality:
 * 	- Frequency/Start/End Date Fields
 *  - Serialization/De-serialization to and from Byte Arrays
 *
 * @author Lakshmi Krishnamurthy
 */

public class ActActDCParams extends org.drip.service.stream.Serializer {
	private int _iFreq = 0;
	private double _dblEnd = java.lang.Double.NaN;
	private double _dblStart = java.lang.Double.NaN;

	/**
	 * De-serialization of ActActDCParams from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize
	 */

	public ActActDCParams (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ActActDCParams de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ActActDCParams de-serializer: Empty state");

		java.lang.String strAAP = strRawString.substring (0, strRawString.indexOf (getObjectTrailer()));

		if (null == strAAP || strAAP.isEmpty())
			throw new java.lang.Exception ("ActActDCParams de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strAAP, getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ActActDCParams de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("ActActDCParams de-serializer: Cannot locate frequency");

		_iFreq = new java.lang.Integer (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("ActActDCParams de-serializer: Cannot locate start date");

		_dblStart = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("ActActDCParams de-serializer: Cannot locate end date");

		_dblEnd = new java.lang.Double (astrField[3]);
	}

	/**
	 * Constructs an ActActDCParams instance from the corresponding parameters
	 * 
	 * @param iFreq Frequency
	 * @param dblStart Period start date
	 * @param dblEnd Period end date
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ActActDCParams (
		final int iFreq,
		final double dblStart,
		final double dblEnd)
		throws java.lang.Exception
	{
		_iFreq = iFreq;

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEnd = dblEnd) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblStart = dblStart))
			throw new java.lang.Exception ("ActActDCParams ctr: Invalid inputs");
	}

	/**
	 * Retrieve the Frequency
	 * 
	 * @return The Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Retrieve the Start Date
	 * 
	 * @return The Start Date
	 */

	public double start()
	{
		return _dblStart;
	}

	/**
	 * Retrieve the End Date
	 * 
	 * @return The End Date
	 */

	public double end()
	{
		return _dblEnd;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter() + _iFreq +
			getFieldDelimiter() + _dblStart + getFieldDelimiter() + _dblEnd);

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab) {
		try {
			return new ActActDCParams (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().getJulian();

		ActActDCParams aap = new ActActDCParams (2, dblStart, dblStart + 180);

		byte[] abAAP = aap.serialize();

		System.out.println ("Input: " + new java.lang.String (abAAP));

		ActActDCParams aapDeser = new ActActDCParams (abAAP);

		System.out.println ("Output: " + new java.lang.String (aapDeser.serialize()));
	}
}
