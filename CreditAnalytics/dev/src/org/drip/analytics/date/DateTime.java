
package org.drip.analytics.date;

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
 * This class provides the representation of the instantiation-time date and time objects. It provides the
 *  following functionality:
 *  - Instantiation-time and Explicit Date/Time Construction
 *  - Retrieval of Date/Time Fields
 *  - Serialization/De-serialization to and from Byte Arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DateTime extends org.drip.service.stream.Serializer {
	private long _lTime = 0L;
	private double _dblDate = java.lang.Double.NaN;

	/**
	 * Default constructor initializes the time and date to the current time and current date.
	 */

	public DateTime()
		throws java.lang.Exception
	{
		_lTime = System.nanoTime();

		java.util.Date dtNow = new java.util.Date();

		_dblDate = JulianDate.toJulian (org.drip.quant.common.DateUtil.GetYear (dtNow),
			org.drip.quant.common.DateUtil.GetMonth (dtNow), org.drip.quant.common.DateUtil.GetDate (dtNow));
	}

	/**
	 * Constructs DateTime from separate date and time inputs 
	 * 
	 * @param dblDate Date
	 * @param lTime Time
	 * 
	 * @throws java.lang.Exception Thrown on Invalid Inputs
	 */

	public DateTime (
		final double dblDate,
		final long lTime)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DateTime ctr: Invalid Inputs!");

		_lTime = lTime;
		_dblDate = dblDate;
	}

	/**
	 * DateTime de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if DateTime cannot be properly de-serialized
	 */

	public DateTime (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("DateTime de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("DateTime de-serializer: Empty state");

		java.lang.String strSerializedDateTime = strRawString.substring (0, strRawString.indexOf
			(super.objectTrailer()));

		if (null == strSerializedDateTime || strSerializedDateTime.isEmpty())
			throw new java.lang.Exception ("DateTime de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedDateTime,
			super.fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("DateTime de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("DateTime de-serializer: Cannot locate long time");

		_lTime = new java.lang.Long (astrField[1]).longValue();

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("DateTime de-serializer: Cannot locate date");

		_dblDate = new java.lang.Double (astrField[2]).doubleValue();
	}

	/**
	 * Retrieve the Date
	 * 
	 * @return date
	 */

	public double date()
	{
		return _dblDate;
	}

	/**
	 * Retrieve the time
	 * 
	 * @return time
	 */

	public long time()
	{
		return _lTime;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + super.fieldDelimiter() + _lTime +
			super.fieldDelimiter() + _dblDate);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new DateTime (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
