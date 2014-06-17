
package org.drip.param.market;

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
 * MultiSidedQuote implements the Quote interface, which contains the stubs corresponding to a product
 *  quote. It contains the quote value, quote instant for the different quote sides (bid/ask/mid).
 *   
 * @author Lakshmi Krishnamurthy
 */

public class MultiSidedQuote extends org.drip.param.definition.Quote {
	class SidedQuote extends org.drip.service.stream.Serializer {
		double _dblSize = java.lang.Double.NaN;
		double _dblQuote = java.lang.Double.NaN;
		org.drip.analytics.date.DateTime _dt = null;

		SidedQuote (
			final double dblQuote,
			final double dblSize)
			throws java.lang.Exception
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblQuote = dblQuote))
				throw new java.lang.Exception ("MultiSidedQuote::SidedQuote ctr: Invalid Inputs!");

			_dblSize = dblSize;

			_dt = new org.drip.analytics.date.DateTime();
		}

		SidedQuote (
			final byte[] ab)
			throws java.lang.Exception
		{
			if (null == ab || 0 == ab.length)
				throw new java.lang.Exception ("MultiSidedQuote::SidedQuote de-serialize: Invalid input");

			java.lang.String strRawString = new java.lang.String (ab);

			if (null == strRawString || strRawString.isEmpty())
				throw new java.lang.Exception ("MultiSidedQuote::SidedQuote de-serializer: Empty state");

			java.lang.String strSidedQuote = strRawString.substring (0, strRawString.indexOf
				(objectTrailer()));

			if (null == strSidedQuote || strSidedQuote.isEmpty())
				throw new java.lang.Exception ("MultiSidedQuote::SidedQuote de-serializer: Invalid state");

			java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSidedQuote,
				fieldDelimiter());

			if (null == astrField || 4 > astrField.length)
				throw new java.lang.Exception
					("MultiSidedQuote::SidedQuote de-serialize: Invalid number of fields");

			if (null == astrField[1] || astrField[1].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
				throw new java.lang.Exception
					("MultiSidedQuote::SidedQuote de-serializer: Cannot locate DateTime");

			_dt = new org.drip.analytics.date.DateTime (astrField[1].getBytes());

			if (null == astrField[2] || astrField[2].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
				throw new java.lang.Exception
					("MultiSidedQuote::SidedQuote de-serializer: Cannot locate Quote");

			_dblQuote = new java.lang.Double (astrField[2]);

			if (null == astrField[3] || astrField[3].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
				throw new java.lang.Exception
					("MultiSidedQuote::SidedQuote de-serializer: Cannot locate Size");

			_dblSize = new java.lang.Double (astrField[3]);
		}

		double getQuote()
		{
			return _dblQuote;
		}

		double getSize()
		{
			return _dblSize;
		}

		org.drip.analytics.date.DateTime getQuoteTime()
		{
			return _dt;
		}

		boolean setQuote (
			final double dblQuote)
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (dblQuote)) return false;

			_dblQuote = dblQuote;
			return true;
		}

		boolean setSize (
			final double dblSize)
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (dblSize)) return false;

			_dblSize = dblSize;
			return true;
		}

		@Override public java.lang.String fieldDelimiter()
		{
			return "%";
		}

		@Override public java.lang.String objectTrailer()
		{
			return "!";
		}

		@Override public byte[] serialize()
		{
			java.lang.StringBuffer sb = new java.lang.StringBuffer();

			sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + new java.lang.String
				(_dt.serialize()) + fieldDelimiter() + _dblQuote + fieldDelimiter() + _dblSize);

			return sb.append (objectTrailer()).toString().getBytes();
		}

		@Override public org.drip.service.stream.Serializer deserialize (
			final byte[] ab) {
			try {
				return new SidedQuote (ab);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	};

	private org.drip.analytics.support.CaseInsensitiveTreeMap<SidedQuote> _mapSidedQuote = new
		org.drip.analytics.support.CaseInsensitiveTreeMap<SidedQuote>();

	/**
	 * MultiSidedQuote Constructor: Constructs a Quote object from the quote value and the side string.
	 * 
	 * @param strSide bid/ask/mid
	 * @param dblQuote Quote Value
	 * 
	 * @throws java.lang.Exception Thrown on invalid inputs
	 */

	public MultiSidedQuote (
		final java.lang.String strSide,
		final double dblQuote)
		throws java.lang.Exception
	{
		if (null == strSide || strSide.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			throw new java.lang.Exception ("MultiSidedQuote ctr: Invalid Side/Quote/Size!");

		_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, java.lang.Double.NaN));
	}

	/**
	 * MultiSidedQuote Constructor: Constructs a Quote object from the quote size/value and the side string.
	 * 
	 * @param strSide bid/ask/mid
	 * @param dblQuote Quote Value
	 * @param dblSize Size
	 * 
	 * @throws java.lang.Exception Thrown on invalid inputs
	 */

	public MultiSidedQuote (
		final java.lang.String strSide,
		final double dblQuote,
		final double dblSize)
		throws java.lang.Exception
	{
		if (null == strSide || strSide.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			throw new java.lang.Exception ("MultiSidedQuote ctr: Invalid Side/Quote/Size!");

		_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, dblSize));
	}

	/**
	 * MultiSidedQuote de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if MultiSidedQuote cannot be properly de-serialized
	 */

	public MultiSidedQuote (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("MultiSidedQuote de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("MultiSidedQuote de-serializer: Empty state");

		java.lang.String strSerializedQuote = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedQuote || strSerializedQuote.isEmpty())
			throw new java.lang.Exception ("MultiSidedQuote de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedQuote,
			fieldDelimiter());

		if (null == astrField || 2 > astrField.length)
			throw new java.lang.Exception ("MultiSidedQuote de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]).doubleValue();

		if (null != astrField[1] && !astrField[1].isEmpty() &&
			!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[1],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				if (null == _mapSidedQuote)
					_mapSidedQuote = new org.drip.analytics.support.CaseInsensitiveTreeMap<SidedQuote>();

				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty()) continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						collectionKeyValueDelimiter());
					
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() || null == astrKVPair[1] || astrKVPair[1].isEmpty())
						continue;

					_mapSidedQuote.put (astrKVPair[0], new SidedQuote (astrKVPair[1].getBytes()));
				}
			}
		}
	}

	@Override public double getQuote (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return java.lang.Double.NaN;

		return _mapSidedQuote.get (strSide).getQuote();
	}

	@Override public double getSize (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return java.lang.Double.NaN;

		return _mapSidedQuote.get (strSide).getSize();
	}

	@Override public org.drip.analytics.date.DateTime getQuoteTime (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return null;

		return _mapSidedQuote.get (strSide).getQuoteTime();
	}

	@Override public boolean setSide (
		final java.lang.String strSide,
		final double dblQuote,
		final double dblSize)
	{
		if (null != strSide && !strSide.isEmpty() && !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			return false;

		try {
			_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, dblSize));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "#";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "^";
	}

	@Override public java.lang.String collectionKeyValueDelimiter()
	{
		return "-";
	}

	@Override public java.lang.String collectionRecordDelimiter()
	{
		return "+";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _mapSidedQuote || 0 == _mapSidedQuote.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbSQMap = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, SidedQuote> me : _mapSidedQuote.entrySet()) {
				if (null == me) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbSQMap.append (collectionRecordDelimiter());

				sbSQMap.append (me.getKey() + collectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (sbSQMap.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbSQMap.toString());
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new MultiSidedQuote (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
