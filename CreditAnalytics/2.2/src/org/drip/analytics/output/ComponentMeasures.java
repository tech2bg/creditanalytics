
package org.drip.analytics.output;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * ComponentMeasures is the place holder for analytical single component output measures, optionally across
 * 	scenarios. It contains measure maps for the following scenarios:
 * 	- Unadjusted Base IR/credit curves
 *	- Flat delta/gamma bump measure maps for IR/credit bump curves
 *	- Tenor bump double maps for IR/credit curves
 *	- Flat/recovery bumped measure maps for recovery bumped credit curves
 *	- Measure Maps generated for Custom Scenarios
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentMeasures extends org.drip.service.stream.Serializer {

	/**
	 * Calculation Time
	 */

	public double _dblCalcTime = java.lang.Double.NaN;

	/**
	 * Map of the base measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mBase = null;

	/**
	 * Map of the parallel RR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mRRDelta = null;

	/**
	 * Map of the parallel RR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mRRGamma = null;

	/**
	 * Map of the parallel IR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRDelta = null;

	/**
	 * Map of the parallel IR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRGamma = null;

	/**
	 * Map of the parallel credit delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditDelta = null;

	/**
	 * Map of the parallel credit gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditGamma = null;

	/**
	 * Map of the tenor IR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorIRDelta = null;

	/**
	 * Map of the tenor IR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorIRGamma = null;

	/**
	 * Map of the tenor credit delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorCreditDelta = null;

	/**
	 * Map of the tenor credit gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorCreditGamma = null;

	/**
	 * Map of the custom scenario measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCustom = null;

	/**
	 * Empty constructor - all members initialized to NaN or null
	 */

	public ComponentMeasures()
	{
	}

	/**
	 * ComponentMeasures de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ComponentMeasures cannot be properly de-serialized
	 */

	public ComponentMeasures (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ComponentMeasures de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ComponentMeasures de-serializer: Empty state");

		java.lang.String strSerializedComponentOutput = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedComponentOutput || strSerializedComponentOutput.isEmpty())
			throw new java.lang.Exception ("ComponentMeasures de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split (strSerializedComponentOutput,
			getFieldDelimiter());

		if (null == astrField || 14 > astrField.length)
			throw new java.lang.Exception ("ComponentMeasures de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("ComponentMeasures de-serializer: Cannot locate calc time");

		_dblCalcTime = new java.lang.Double (astrField[1]).doubleValue();

		if (null == astrField[2] || astrField[2].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[2]))
			_mBase = null;
		else
			_mBase = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[2],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[3] || astrField[3].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[3]))
			_mRRDelta = null;
		else
			_mRRDelta = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[3],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[4] || astrField[4].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
				(astrField[4]))
			_mRRGamma = null;
		else
			_mRRGamma = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[4],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[5] || astrField[5].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[5]))
			_mFlatIRDelta = null;
		else
			_mFlatIRDelta = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[5],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[6] || astrField[6].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
				(astrField[6]))
			_mFlatIRGamma = null;
		else
			_mFlatIRGamma = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[6],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[7] || astrField[7].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[7]))
			_mFlatCreditDelta = null;
		else
			_mFlatCreditDelta = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[7],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[8] || astrField[8].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[8]))
			_mFlatCreditGamma = null;
		else
			_mFlatCreditGamma = org.drip.math.common.MapUtil.FlatStringTo2DSDMap (astrField[8],
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[9] || astrField[9].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[9]))
			_mmTenorIRDelta = null;
		else
			_mmTenorIRDelta = org.drip.math.common.MapUtil.FlatStringTo3DSDMap (astrField[9],
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[10] || astrField[10].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[10]))
			_mmTenorIRGamma = null;
		else
			_mmTenorIRGamma = org.drip.math.common.MapUtil.FlatStringTo3DSDMap (astrField[10],
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[11] || astrField[11].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[11]))
			_mmTenorCreditDelta = null;
		else
			_mmTenorCreditDelta = org.drip.math.common.MapUtil.FlatStringTo3DSDMap (astrField[11],
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[12] || astrField[12].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[12]))
			_mmTenorCreditGamma = null;
		else
			_mmTenorCreditGamma = org.drip.math.common.MapUtil.FlatStringTo3DSDMap (astrField[12],
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[13] || astrField[13].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[13]))
			_mmCustom = null;
		else
			_mmCustom = org.drip.math.common.MapUtil.FlatStringTo3DSDMap (astrField[13],
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter(), true, NULL_SER_STRING);
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + getFieldDelimiter() + _dblCalcTime + getFieldDelimiter());

		if (null == _mBase || 0 == _mBase.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mBase,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mRRDelta || 0 == _mRRDelta.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mRRDelta,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mRRGamma || 0 == _mRRGamma.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mRRGamma,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mFlatIRDelta || 0 == _mFlatIRDelta.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mFlatIRDelta,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mFlatIRGamma || 0 == _mFlatIRGamma.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mFlatIRGamma,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mFlatCreditDelta || 0 == _mFlatCreditDelta.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mFlatCreditDelta,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mFlatCreditGamma || 0 == _mFlatCreditGamma.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.TwoDSDMapToFlatString (_mFlatCreditGamma,
				getCollectionKeyValueDelimiter(), getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mmTenorIRDelta || 0 == _mmTenorIRDelta.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.ThreeDSDMapToFlatString (_mmTenorIRDelta,
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mmTenorIRGamma || 0 == _mmTenorIRGamma.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.ThreeDSDMapToFlatString (_mmTenorIRGamma,
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mmTenorCreditDelta || 0 == _mmTenorCreditDelta.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.ThreeDSDMapToFlatString (_mmTenorCreditDelta,
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mmTenorCreditGamma || 0 == _mmTenorCreditGamma.size())
			sb.append (NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (org.drip.math.common.MapUtil.ThreeDSDMapToFlatString (_mmTenorCreditGamma,
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter()) + getFieldDelimiter());

		if (null == _mmCustom || 0 == _mmCustom.size())
			sb.append (NULL_SER_STRING);
		else
			sb.append (org.drip.math.common.MapUtil.ThreeDSDMapToFlatString (_mmCustom,
				getCollectionMultiLevelKeyDelimiter(), getCollectionKeyValueDelimiter(),
					getCollectionRecordDelimiter()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ComponentMeasures (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
