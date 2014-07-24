
package org.drip.analytics.holiday;

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
 * Weekend holds the left and the right weekend days. It provides functionality to retrieve them, check if
 *  the given day is a weekend, and serialize/de-serialize weekend days.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Weekend extends org.drip.service.stream.Serializer {
	private int[] _aiDay = null;

	/**
	 * Create a Weekend Instance with SATURDAY and SUNDAY
	 * 
	 * @return Weekend object
	 */

	public static final Weekend StandardWeekend()
	{
		try {
			return new Weekend (new int[] {org.drip.analytics.date.JulianDate.SUNDAY,
				org.drip.analytics.date.JulianDate.SATURDAY});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * De-serialization of Weekend from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize Weekend
	 */

	public Weekend (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("Weekend de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("Weekend de-serializer: Empty state");

		java.lang.String strWH = strRawString.substring (0, strRawString.indexOf (objectTrailer()));

		if (null == strWH || strWH.isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (strWH))
			throw new java.lang.Exception ("Weekend de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strWH, fieldDelimiter());

		if (null == astrField || 2 > astrField.length)
			throw new java.lang.Exception ("Weekend de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		java.util.List<java.lang.Integer> lsi = new java.util.ArrayList<java.lang.Integer>();

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]) ||
			!org.drip.quant.common.StringUtil.IntegerListFromString (lsi, astrField[1],
				collectionRecordDelimiter()))
			throw new java.lang.Exception ("Weekend de-serializer: Cannot decode state");

		_aiDay = new int[lsi.size()];

		for (int i = 0; i < _aiDay.length; ++i)
			_aiDay[i] = lsi.get (i);
	}

	/**
	 * Create the weekend instance object from the array of the weekend days
	 * 
	 * @param aiDay Array of the weekend days
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize Weekend
	 */

	public Weekend (
		final int[] aiDay)
		throws java.lang.Exception
	{
		if (null == aiDay) throw new java.lang.Exception ("Weekend ctr: Invalid Inputs");

		int iNumWeekendDays = aiDay.length;;

		if (0 == iNumWeekendDays) throw new java.lang.Exception ("Weekend ctr: Invalid Inputs");

		_aiDay = new int[iNumWeekendDays];

		for (int i = 0; i < iNumWeekendDays; ++i)
			_aiDay[i] = aiDay[i];
	}

	/**
	 * Retrieve the weekend days
	 * 
	 * @return Array of the weekend days
	 */

	public int[] days()
	{
		return _aiDay;
	}

	/**
	 * Is the given date a left weekend day
	 * 
	 * @param dblDate Date
	 * 
	 * @return True (Left weekend day)
	 */

	public boolean isLeftWeekend (
		final double dblDate)
	{
		if (null == _aiDay || 0 == _aiDay.length) return false;

		if (_aiDay[0] == (dblDate % 7)) return true;

		return false;
	}

	/**
	 * Is the given date a right weekend day
	 * 
	 * @param dblDate Date
	 * 
	 * @return True (Right weekend day)
	 */

	public boolean isRightWeekend (
		final double dblDate)
	{
		if (null == _aiDay || 1 >= _aiDay.length) return false;

		if (_aiDay[1] == (dblDate % 7)) return true;

		return false;
	}

	/**
	 * Is the given date a weekend day
	 * 
	 * @param dblDate Date
	 * 
	 * @return True (Weekend day)
	 */

	public boolean isWeekend (
		final double dblDate)
	{
		return isLeftWeekend (dblDate) || isRightWeekend (dblDate);
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		for (int i = 0; i < _aiDay.length; ++i) {
			if (0 != i) sb.append (collectionRecordDelimiter());

			sb.append (_aiDay[i]);
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new Weekend (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		Weekend wh = Weekend.StandardWeekend();

		byte[] abWH = wh.serialize();

		System.out.println ("Input: " + new java.lang.String (abWH));

		Weekend whDeser = new Weekend (abWH);

		System.out.println ("Output: " + new java.lang.String (whDeser.serialize()));
	}
}
