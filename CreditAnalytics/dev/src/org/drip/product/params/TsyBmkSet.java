
package org.drip.product.params;

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
 * TsyBmkSet contains the treasury benchmark set - the primary treasury benchmark, and an array of secondary
 *  treasury benchmarks. It exports serialization into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class TsyBmkSet extends org.drip.service.stream.Serializer {
	private java.lang.String _strBmkPrimary = "";
	private java.lang.String[] _astrSecBmk = null;

	/**
	 * Construct the treasury benchmark set from the primary treasury benchmark, and an array of secondary
	 * 	treasury benchmarks
	 * 
	 * @param strBmkPrimary Primary Treasury Benchmark
	 * @param astrSecBmk Array of Secondary Treasury Benchmarks
	 */

	public TsyBmkSet (
		final java.lang.String strBmkPrimary,
		final java.lang.String[] astrSecBmk)
	{
		_astrSecBmk = astrSecBmk;
		_strBmkPrimary = strBmkPrimary;
	}

	/**
	 * TsyBmkSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if TsyBmkSet cannot be properly de-serialized
	 */

	public TsyBmkSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Empty state");

		java.lang.String strSerializedTsyBmkSet = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedTsyBmkSet || strSerializedTsyBmkSet.isEmpty())
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedTsyBmkSet,
			fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Cannot locate Primary Tsy Bmk");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strBmkPrimary = "";
		else
			_strBmkPrimary = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("TsyBmkSet de-serializer: Cannot locate CUSIP");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_astrSecBmk = null;
		else {
			_astrSecBmk = org.drip.quant.common.StringUtil.Split (astrField[2],
				collectionRecordDelimiter());

			if (null == _astrSecBmk || 0 == _astrSecBmk.length) {
				for (int i = 0; i < _astrSecBmk.length; ++i) {
					if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (_astrSecBmk[i]))
						_astrSecBmk[i] = null;
				}
			}
		}
	}

	/**
	 * Return the Primary Treasury Benchmark
	 * 
	 * @return Primary Treasury Benchmark
	 */

	public java.lang.String getPrimaryBmk()
	{
		return _strBmkPrimary;
	}

	/**
	 * Return an Array of Secondary Treasury Benchmarks
	 * 
	 * @return  Array of Secondary Treasury Benchmarks
	 */

	public java.lang.String[] getSecBmk()
	{
		return _astrSecBmk;
	}

	@Override public java.lang.String collectionRecordDelimiter()
	{
		return "!";
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

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _strBmkPrimary +
			fieldDelimiter());

		if (null == _astrSecBmk || 0 == _astrSecBmk.length)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbSB = new java.lang.StringBuffer();

			for (int i = 0; i < _astrSecBmk.length; ++i) {
				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbSB.append (collectionRecordDelimiter());

				if (null != _astrSecBmk[i] && !_astrSecBmk[i].isEmpty())
					sbSB.append (_astrSecBmk[i]);
				else
					sbSB.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			}

			if (!sbSB.toString().isEmpty())
				sb.append (sbSB.toString());
			else
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new TsyBmkSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		TsyBmkSet tss = new TsyBmkSet ("ABC", new java.lang.String[] {"DEF", "GHI", "JKL"});

		byte[] abTSS = tss.serialize();

		System.out.println (new java.lang.String (abTSS));

		TsyBmkSet tssDeser = new TsyBmkSet (abTSS);

		System.out.println (new java.lang.String (tssDeser.serialize()));
	}
}
