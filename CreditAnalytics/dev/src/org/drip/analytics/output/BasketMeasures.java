
package org.drip.analytics.output;

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
 * BasketMeasures is the place holder for the analytical basket measures, optionally across scenarios. It
 * 	contains the following scenario measure maps:
 * 	- Unadjusted Base Measures
 *	- Flat delta/gamma bump measure maps for IR/credit/RR bump curves
 *	- Component/tenor bump double maps for IR/credit/RR curves
 *	- Flat/component recovery bumped measure maps for recovery bumped credit curves
 *	- Custom scenario measure map
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BasketMeasures extends org.drip.service.stream.Serializer {

	/**
	 * Basket output calculation time
	 */

	public double _dblCalcTime = java.lang.Double.NaN;

	/**
	 * Map of the base measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mBase = null;

	/**
	 * Map of the parallel IR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRDelta = null;

	/**
	 * Map of the parallel IR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRGamma = null;

	/**
	 * Map of the parallel RR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatRRDelta = null;

	/**
	 * Map of the parallel RR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatRRGamma = null;

	/**
	 * Map of the parallel credit delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditDelta = null;

	/**
	 * Map of the parallel credit gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditGamma = null;

	/**
	 * Map of the component IR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmIRDelta = null;

	/**
	 * Map of the component IR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmIRGamma = null;

	/**
	 * Map of the component credit delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCreditDelta = null;

	/**
	 * Map of the component credit gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCreditGamma = null;

	/**
	 * Map of the component RR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmRRDelta = null;

	/**
	 * Map of the component RR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmRRGamma = null;

	/**
	 * Triple Map of the component, IR tenor, measure, and delta value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmIRTenorDelta = null;

	/**
	 * Triple Map of the component, IR tenor, measure, and gamma value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmIRTenorGamma = null;

	/**
	 * Triple Map of the component, credit tenor, measure, and delta value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmCreditTenorDelta = null;

	/**
	 * Triple Map of the component, credit tenor, measure, and gamma value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmCreditTenorGamma = null;

	/**
	 * Map of the custom scenario measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCustom = null;

	/**
	 * Empty constructor - all members initialized to NaN or null
	 */

	public BasketMeasures()
	{
	}

	/**
	 * BasketMeasures de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if BasketMeasures cannot be properly de-serialized
	 */

	public BasketMeasures (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("BasketMeasures de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("BasketMeasures de-serializer: Empty state");

		java.lang.String strSerializedBasketOutput = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedBasketOutput || strSerializedBasketOutput.isEmpty())
			throw new java.lang.Exception ("BasketMeasures de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedBasketOutput,
			fieldDelimiter());

		if (null == astrField || 20 > astrField.length)
			throw new java.lang.Exception ("BasketMeasures de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("BasketMeasures de-serializer: Cannot locate calc time");

		_dblCalcTime = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[2]))
			_mBase = null;
		else
			_mBase = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[2],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[3] || astrField[3].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[3]))
			_mFlatRRDelta = null;
		else
			_mFlatRRDelta = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[3],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[4] || astrField[4].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[4]))
			_mFlatRRGamma = null;
		else
			_mFlatRRGamma = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[4],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[5] || astrField[5].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[5]))
			_mFlatIRDelta = null;
		else
			_mFlatIRDelta = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[5],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[6] || astrField[6].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[6]))
			_mFlatIRGamma = null;
		else
			_mFlatIRGamma = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[6],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[7] || astrField[7].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[7]))
			_mFlatCreditDelta = null;
		else
			_mFlatCreditDelta = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[7],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[8] || astrField[8].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[8]))
			_mFlatCreditGamma = null;
		else
			_mFlatCreditGamma = org.drip.quant.common.CollectionUtil.FlatStringTo2DSDMap (astrField[8],
				collectionKeyValueDelimiter(), collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[9] || astrField[9].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[9]))
			_mmIRDelta = null;
		else
			_mmIRDelta = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[9],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[10] || astrField[10].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[10]))
			_mmIRGamma = null;
		else
			_mmIRGamma = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[10],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[11] || astrField[11].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[11]))
			_mmCreditDelta = null;
		else
			_mmCreditDelta = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[11],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[12] || astrField[12].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[12]))
			_mmCreditGamma = null;
		else
			_mmCreditGamma = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[12],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[13] || astrField[13].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[13]))
			_mmRRDelta = null;
		else
			_mmRRDelta = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[13],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[14] || astrField[14].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[14]))
			_mmRRGamma = null;
		else
			_mmRRGamma = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[14],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[15] || astrField[15].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[15]))
			_mmmIRTenorDelta = null;
		else
			_mmmIRTenorDelta = org.drip.quant.common.CollectionUtil.FlatStringTo4DSDMap (astrField[15],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[16] || astrField[16].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[16]))
			_mmmIRTenorGamma = null;
		else
			_mmmIRTenorGamma = org.drip.quant.common.CollectionUtil.FlatStringTo4DSDMap (astrField[16],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[17] || astrField[17].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[17]))
			_mmmCreditTenorDelta = null;
		else
			_mmmCreditTenorDelta = org.drip.quant.common.CollectionUtil.FlatStringTo4DSDMap (astrField[17],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[18] || astrField[18].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[18]))
			_mmmCreditTenorGamma = null;
		else
			_mmmCreditTenorGamma = org.drip.quant.common.CollectionUtil.FlatStringTo4DSDMap (astrField[18],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);

		if (null == astrField[19] || astrField[19].isEmpty() || NULL_SER_STRING.equalsIgnoreCase
			(astrField[19]))
			_mmCustom = null;
		else
			_mmCustom = org.drip.quant.common.CollectionUtil.FlatStringTo3DSDMap (astrField[19],
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter(), true, NULL_SER_STRING);
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + fieldDelimiter() + _dblCalcTime + fieldDelimiter());

		if (null == _mBase || 0 == _mBase.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mBase,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatRRDelta || 0 == _mFlatRRDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatRRDelta,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatRRGamma || 0 == _mFlatRRGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatRRGamma,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatIRDelta || 0 == _mFlatIRDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatIRDelta,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatIRGamma || 0 == _mFlatIRGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatIRGamma,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatCreditDelta || 0 == _mFlatCreditDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatCreditDelta,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mFlatCreditGamma || 0 == _mFlatCreditGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.TwoDSDMapToFlatString (_mFlatCreditGamma,
				collectionKeyValueDelimiter(), collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmIRDelta || 0 == _mmIRDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmIRDelta,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmIRGamma || 0 == _mmIRGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmIRGamma,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmCreditDelta || 0 == _mmCreditDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmCreditDelta,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmCreditGamma || 0 == _mmCreditGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmCreditGamma,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmRRDelta || 0 == _mmRRDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmRRDelta,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmRRGamma || 0 == _mmRRGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmRRGamma,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmmIRTenorDelta || 0 == _mmmIRTenorDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.FourDSDMapToFlatString (_mmmIRTenorDelta,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmmIRTenorGamma || 0 == _mmmIRTenorGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.FourDSDMapToFlatString (_mmmIRTenorGamma,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmmCreditTenorDelta || 0 == _mmmCreditTenorDelta.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.FourDSDMapToFlatString (_mmmCreditTenorDelta,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmmCreditTenorGamma || 0 == _mmmCreditTenorGamma.size())
			sb.append (NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (org.drip.quant.common.CollectionUtil.FourDSDMapToFlatString (_mmmCreditTenorGamma,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()) + fieldDelimiter());

		if (null == _mmCustom || 0 == _mmCustom.size())
			sb.append (NULL_SER_STRING);
		else
			sb.append (org.drip.quant.common.CollectionUtil.ThreeDSDMapToFlatString (_mmCustom,
				collectionMultiLevelKeyDelimiter(), collectionKeyValueDelimiter(),
					collectionRecordDelimiter()));

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new BasketMeasures (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
