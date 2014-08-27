
package org.drip.analytics.period;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ResetPeriod contains the cash flow periods' reset sub period details. Currently it holds the start, the
 * 	end, and the fixing dates.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResetPeriod extends org.drip.service.stream.Serializer {

	/**
	 * Node is to the Left of the Period
	 */

	public static final int NODE_LEFT_OF_SEGMENT = 1;

	/**
	 * Node is Inside the Period
	 */

	public static final int NODE_INSIDE_SEGMENT = 2;

	/**
	 * Node is to the Right of the Period
	 */

	public static final int NODE_RIGHT_OF_SEGMENT = 4;

	private double _dblEnd = java.lang.Double.NaN;
	private double _dblStart = java.lang.Double.NaN;
	private double _dblFixing = java.lang.Double.NaN;

	/**
	 * The ResetPeriod constructor
	 * 
	 * @param dblStart Reset Period Start Date
	 * @param dblEnd Reset Period End Date
	 * @param dblFixing Reset Period Fixing Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ResetPeriod (
		final double dblStart,
		final double dblEnd,
		final double dblFixing)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStart = dblStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEnd = dblEnd) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFixing = dblFixing))
			throw new java.lang.Exception ("ResetPeriod ctr: Invalid Inputs");
	}

	/**
	 * De-serialization of ResetPeriod from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize ResetPeriod
	 */

	public ResetPeriod (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ResetPeriod de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ResetPeriod de-serializer: Empty state");

		java.lang.String strCP = strRawString.substring (0, strRawString.indexOf (objectTrailer()));

		if (null == strCP || strCP.isEmpty())
			throw new java.lang.Exception ("ResetPeriod de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strCP, fieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ResetPeriod de-serialize: Invalid number of fields");

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("ResetPeriod de-serializer: Cannot locate Start Date");

		_dblStart = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("ResetPeriod de-serializer: Cannot locate End Date");

		_dblEnd = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("ResetPeriod de-serializer: Cannot locate Fixing Date");

		_dblFixing = new java.lang.Double (astrField[3]);
	}

	/**
	 * Reset Start Date
	 * 
	 * @return The Reset Start Date
	 */

	public double start()
	{
		return _dblStart;
	}

	/**
	 * Reset End Date
	 * 
	 * @return The Reset End Date
	 */

	public double end()
	{
		return _dblEnd;
	}

	/**
	 * Reset Fixing Date
	 * 
	 * @return The Reset Fixing Date
	 */

	public double fixing()
	{
		return _dblFixing;
	}

	/**
	 * Places the Node Location in relation to the segment Location
	 * 
	 * @param dblNode The Node Ordinate
	 * 
	 * @return One of NODE_LEFT_OF_SEGMENT, NODE_RIGHT_OF_SEGMENT, or NODE_INSIDE_SEGMENT
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public int nodeLocation (
		final double dblNode)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblNode))
			throw new java.lang.Exception ("ResetPeriod::nodeLocation => Invalid Node");

		if (dblNode < _dblStart) return NODE_LEFT_OF_SEGMENT;

		if (dblNode > _dblEnd) return NODE_RIGHT_OF_SEGMENT;

		return NODE_INSIDE_SEGMENT;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "'";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "_";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _dblStart +
			fieldDelimiter() + _dblEnd + fieldDelimiter() + _dblFixing);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ResetPeriod (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
