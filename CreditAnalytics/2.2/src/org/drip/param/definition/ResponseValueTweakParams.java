
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * ResponseValueTweakParams contains the place holder for the scenario tweak parameters, for either a specific curve
 *  node, or the entire curve (flat). Parameter bumps can be parallel or proportional.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResponseValueTweakParams extends org.drip.service.stream.Serializer {

	/**
	 * Flat Manifest Measure Tweak Mode
	 */

	public static final int MANIFEST_MEASURE_FLAT_TWEAK = -1;

	/**
	 * Index of the Instrument whose Manifest Measure is to be Tweaked
	 */

	public int _iTweakNode = MANIFEST_MEASURE_FLAT_TWEAK;

	/**
	 * Is the tweak parallel or proportional
	 */

	public boolean _bIsTweakProportional = false;

	/**
	 * Node tweak amount
	 */

	public double _dblTweakAmount = java.lang.Double.NaN;

	/**
	 * ResponseValueTweakParams constructor
	 * 
	 * @param iTweakNode Node to be tweaked - Set to NODE_FLAT_TWEAK for flat curve tweak
	 * @param bIsTweakProportional True => Tweak is proportional, False => parallel
	 * @param dblTweakAmount Amount to be tweaked - proportional tweaks are represented as percent, parallel
	 * 			tweaks are absolute numbers
	 */

	public ResponseValueTweakParams (
		final int iTweakNode,
		final boolean bIsTweakProportional,
		final double dblTweakAmount)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblTweakAmount = dblTweakAmount))
			throw new java.lang.Exception ("ResponseValueTweakParams ctr => Invalid Inputs");

		_iTweakNode = iTweakNode;
		_bIsTweakProportional = bIsTweakProportional;
	}

	/**
	 * ResponseValueTweakParams de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ResponseValueTweakParams cannot be properly de-serialized
	 */

	public ResponseValueTweakParams (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("ResponseValueTweakParams de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ResponseValueTweakParams de-serializer: Empty state");

		java.lang.String strSerializedNodeTweakParams = strRawString.substring (0, strRawString.indexOf
			(super.getObjectTrailer()));

		if (null == strSerializedNodeTweakParams || strSerializedNodeTweakParams.isEmpty())
			throw new java.lang.Exception ("ResponseValueTweakParams de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedNodeTweakParams,
			super.getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ResponseValueTweakParams de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("ResponseValueTweakParams de-serializer: Cannot locate tweak node");

		_iTweakNode = new java.lang.Integer (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("ResponseValueTweakParams de-serializer: Cannot locate tweak amount");

		_dblTweakAmount = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("ResponseValueTweakParams de-serializer: Cannot locate is proportional tweak flag");

		_bIsTweakProportional = new java.lang.Boolean (astrField[3]);
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + super.getFieldDelimiter() + _iTweakNode +  super.getFieldDelimiter() +
			_dblTweakAmount +  super.getFieldDelimiter() + _bIsTweakProportional);

		return sb.append (super.getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ResponseValueTweakParams (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
