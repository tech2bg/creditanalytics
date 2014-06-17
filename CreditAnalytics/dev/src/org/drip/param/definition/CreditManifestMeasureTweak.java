
package org.drip.param.definition;


/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * CreditManifestMeasureTweak contains the place holder for the credit curve scenario tweak parameters: in
 *  addition to the ResponseValueTweakParams fields, this exposes the calibration manifest measure, the curve
 *  node, and the nodal calibration type (entire curve/flat or a given tenor point).
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditManifestMeasureTweak extends ResponseValueTweakParams {

	/**
	 * Tweak Parameter Type of Quote
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_PARAM_QUOTE = "Quote";

	/**
	 * Tweak Parameter Type of Recovery
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_PARAM_RECOVERY = "Recovery";

	/**
	 * Tweak Measure Type of Quote
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_MEASURE_QUOTE = "Quote";

	/**
	 * Tweak Measure Type of Hazard
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_MEASURE_HAZARD = "Hazard";

	/**
	 * Tweak Parameter Type
	 */

	public java.lang.String _strTweakParamType = "";

	/**
	 * Tweak Measure Type
	 */

	public java.lang.String _strTweakMeasureType = "";

	/**
	 * Flag indicating if the calibration occurs over a single node
	 */

	public boolean _bSingleNodeCalib = false;

	/**
	 * CreditManifestMeasureTweak constructor
	 * 
	 * @param strTweakParamType Node Tweak Parameter Type
	 * @param strTweakMeasureType Node Tweak Measure Type
	 * @param iTweakNode Node to be tweaked - Set to NODE_FLAT_TWEAK for flat curve tweak
	 * @param bIsTweakProportional True => Tweak is proportional, False => parallel
	 * @param dblTweakAmount Amount to be tweaked - proportional tweaks are represented as percent, parallel
	 * 			tweaks are absolute numbers
	 * @param bSingleNodeCalib Flat Calibration using a single node?
	 */

	public CreditManifestMeasureTweak (
		final java.lang.String strTweakParamType,
		final java.lang.String strTweakMeasureType,
		final int iTweakNode,
		final boolean bIsTweakProportional,
		final double dblTweakAmount,
		final boolean bSingleNodeCalib)
		throws java.lang.Exception
	{
		super (iTweakNode, bIsTweakProportional, dblTweakAmount);

		if (null == (_strTweakParamType = strTweakParamType) ||
			!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakParamType) ||
				!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakParamType))
			throw new java.lang.Exception
				("CreditManifestMeasureTweak ctr => Invalid Tweak Parameter Type!");

		if (null == (_strTweakMeasureType = strTweakMeasureType) ||
			!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakMeasureType) ||
				!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakMeasureType))
			throw new java.lang.Exception ("CreditManifestMeasureTweak ctr => Invalid Tweak Measure Type!");

		_bSingleNodeCalib = bSingleNodeCalib;
	}

	/**
	 * CreditManifestMeasureTweak de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CreditManifestMeasureTweak cannot be properly de-serialized
	 */

	public CreditManifestMeasureTweak (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (ab);

		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("CreditManifestMeasureTweak de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CreditManifestMeasureTweak de-serializer: Empty state");

		java.lang.String strSerializedCreditNodeTweakParams = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedCreditNodeTweakParams || strSerializedCreditNodeTweakParams.isEmpty())
			throw new java.lang.Exception ("CreditManifestMeasureTweak de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedCreditNodeTweakParams, fieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception
				("CreditManifestMeasureTweak de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("CreditManifestMeasureTweak de-serializer: Cannot locate Tweak Parameter Type");

		_strTweakParamType = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("CreditManifestMeasureTweak de-serializer: Cannot locate Tweak Measure Type");

		_strTweakMeasureType = astrField[2];

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("CreditManifestMeasureTweak de-serializer: Cannot locate Tweak Measure Type");

		_bSingleNodeCalib = new java.lang.Boolean (astrField[3]);
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "#";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "@";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (new java.lang.String (super.serialize()) + fieldDelimiter() + _strTweakParamType +
			fieldDelimiter() + _strTweakMeasureType + fieldDelimiter() + _bSingleNodeCalib);

		return sb.append (objectTrailer()).toString().getBytes();
	}
}
