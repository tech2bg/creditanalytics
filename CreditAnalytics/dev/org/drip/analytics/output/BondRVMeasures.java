
package org.drip.analytics.output;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * BondRVMeasures encapsulates the comprehensive set of RV measures calculated for the bond to the
 *  appropriate exercise:
 * 	- Workout Information
 * 	- Price, Yield, and Yield01
 * 	- Spread Measures: Asset Swap/Credit/G/I/OAS/PECS/TSY/Z
 * 	- Basis Measures: Bond Basis, Credit Basis, Yield Basis
 * 	- Duration Measures: Macaulay/Modified Duration, Convexity
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondRVMeasures extends org.drip.service.stream.Serializer {

	/**
	 * Price
	 */

	public double _dblPrice = java.lang.Double.NaN;

	/**
	 * Bond Basis
	 */

	public double _dblBondBasis = java.lang.Double.NaN;

	/**
	 * Z Spread
	 */

	public double _dblZSpread = java.lang.Double.NaN;

	/**
	 * G Spread
	 */

	public double _dblGSpread = java.lang.Double.NaN;

	/**
	 * I Spread
	 */

	public double _dblISpread = java.lang.Double.NaN;

	/**
	 * Option Adjusted Spread
	 */

	public double _dblOASpread = java.lang.Double.NaN;

	/**
	 * Treasury Spread
	 */

	public double _dblTSYSpread = java.lang.Double.NaN;

	/**
	 * Discount Margin
	 */

	public double _dblDiscountMargin = java.lang.Double.NaN;

	/**
	 * Asset swap spread
	 */

	public double _dblAssetSwapSpread = java.lang.Double.NaN;

	/**
	 * Credit Basis
	 */

	public double _dblCreditBasis = java.lang.Double.NaN;

	/**
	 * PECS
	 */

	public double _dblPECS = java.lang.Double.NaN;

	/**
	 * Yield 01
	 */

	public double _dblYield01 = java.lang.Double.NaN;

	/**
	 * Macaulay Duration
	 */

	public double _dblMacaulayDuration = java.lang.Double.NaN;

	/**
	 * Modified Duration
	 */

	public double _dblModifiedDuration = java.lang.Double.NaN;

	/**
	 * Convexity
	 */

	public double _dblConvexity = java.lang.Double.NaN;

	/**
	 * Work-out info
	 */

	public org.drip.param.valuation.WorkoutInfo _wi = null;

	/**
	 * BondRVMeasures de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if BondRVMeasures cannot be properly de-serialized
	 */

	public BondRVMeasures (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Empty state");

		java.lang.String strSerializedBondRVMeasures = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedBondRVMeasures || strSerializedBondRVMeasures.isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split (strSerializedBondRVMeasures,
			getFieldDelimiter());

		if (null == astrField || 17 > astrField.length)
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Price");

		_dblPrice = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Z Spread");

		_dblZSpread = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate G Spread");

		_dblGSpread = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate I Spread");

		_dblISpread = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("BondRVMeasures de-serializer: Cannot locate Option Adjusted Spread");

		_dblOASpread = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate TSY Spread");

		_dblTSYSpread = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Discount Margin");

		_dblDiscountMargin = new java.lang.Double (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Asset Swap Spread");

		_dblAssetSwapSpread = new java.lang.Double (astrField[8]);

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Credit Basis");

		_dblCreditBasis = new java.lang.Double (astrField[9]);

		if (null == astrField[10] || astrField[10].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate PECS");

		_dblPECS = new java.lang.Double (astrField[10]);

		if (null == astrField[11] || astrField[11].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Yield01");

		_dblYield01 = new java.lang.Double (astrField[11]);

		if (null == astrField[12] || astrField[12].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Mac Dur");

		_dblMacaulayDuration = new java.lang.Double (astrField[12]);

		if (null == astrField[13] || astrField[13].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Mod Dur");

		_dblModifiedDuration = new java.lang.Double (astrField[13]);

		if (null == astrField[14] || astrField[14].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Convexity");

		_dblConvexity = new java.lang.Double (astrField[14]);

		if (null == astrField[15] || astrField[15].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Bond Basis");

		_dblBondBasis = new java.lang.Double (astrField[15]);

		if (null == astrField[16] || astrField[16].isEmpty())
			throw new java.lang.Exception ("BondRVMeasures de-serializer: Cannot locate Work-out info");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[16]))
			_wi = null;
		else
			_wi = new org.drip.param.valuation.WorkoutInfo (astrField[16].getBytes());
	}

	/**
	 * BondRVMeasures ctr
	 * 
	 * @param dblPrice BondRV Clean Price
	 * @param dblBondBasis BondRV Bond Basis
	 * @param dblZSpread BondRV Z Spread
	 * @param dblGSpread BondRV G Spread
	 * @param dblISpread BondRV I Spread
	 * @param dblOASpread BondRV OAS
	 * @param dblTSYSpread BondRV TSY Spread
	 * @param dblDiscountMargin BondRV Asset Swap Spread
	 * @param dblAssetSwapSpread BondRV Asset Swap Spread
	 * @param dblCreditBasis BondRV Credit Basis
	 * @param dblPECS BondRV PECS
	 * @param dblYield01 BondRV Yield01
	 * @param dblModifiedDuration BondRV Modified Duration
	 * @param dblMacaulayDuration BondRV Macaulay Duration
	 * @param dblConvexity BondRV Convexity
	 * @param wi BondRV work-out info
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public BondRVMeasures (
		final double dblPrice,
		final double dblBondBasis,
		final double dblZSpread,
		final double dblGSpread,
		final double dblISpread,
		final double dblOASpread,
		final double dblTSYSpread,
		final double dblDiscountMargin,
		final double dblAssetSwapSpread,
		final double dblCreditBasis,
		final double dblPECS,
		final double dblYield01,
		final double dblModifiedDuration,
		final double dblMacaulayDuration,
		final double dblConvexity,
		final org.drip.param.valuation.WorkoutInfo wi)
		throws java.lang.Exception
	{
		if (null == (_wi = wi)) throw new java.lang.Exception ("BondRVMeasures ctr: Invalid inputs!");

		_dblPECS = dblPECS;
		_dblPrice = dblPrice;
		_dblGSpread = dblGSpread;
		_dblISpread = dblISpread;
		_dblYield01 = dblYield01;
		_dblZSpread = dblZSpread;
		_dblOASpread = dblOASpread;
		_dblBondBasis = dblBondBasis;
		_dblConvexity = dblConvexity;
		_dblTSYSpread = dblTSYSpread;
		_dblCreditBasis = dblCreditBasis;
		_dblDiscountMargin = dblDiscountMargin;
		_dblAssetSwapSpread = dblAssetSwapSpread;
		_dblMacaulayDuration = dblMacaulayDuration;
		_dblModifiedDuration = dblModifiedDuration;
	}

	/**
	 * Returns the state as a measure map
	 * 
	 * @param strPrefix RV Measure name prefix
	 * 
	 * @return Map of the RV measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> toMap (
		final java.lang.String strPrefix)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRVMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapRVMeasures.put (strPrefix + "AssetSwapSpread", _dblAssetSwapSpread);

		mapRVMeasures.put (strPrefix + "ASW", _dblAssetSwapSpread);

		mapRVMeasures.put (strPrefix + "BondBasis", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "Convexity", _dblConvexity);

		mapRVMeasures.put (strPrefix + "CreditBasis", _dblCreditBasis);

		mapRVMeasures.put (strPrefix + "DiscountMargin", _dblDiscountMargin);

		mapRVMeasures.put (strPrefix + "Duration", _dblModifiedDuration);

		mapRVMeasures.put (strPrefix + "GSpread", _dblGSpread);

		mapRVMeasures.put (strPrefix + "ISpread", _dblISpread);

		mapRVMeasures.put (strPrefix + "MacaulayDuration", _dblMacaulayDuration);

		mapRVMeasures.put (strPrefix + "ModifiedDuration", _dblModifiedDuration);

		mapRVMeasures.put (strPrefix + "OAS", _dblOASpread);

		mapRVMeasures.put (strPrefix + "OASpread", _dblOASpread);

		mapRVMeasures.put (strPrefix + "OptionAdjustedSpread", _dblOASpread);

		mapRVMeasures.put (strPrefix + "PECS", _dblPECS);

		mapRVMeasures.put (strPrefix + "Price", _dblPrice);

		mapRVMeasures.put (strPrefix + "TSYSpread", _dblTSYSpread);

		mapRVMeasures.put (strPrefix + "WorkoutDate", _wi._dblDate);

		mapRVMeasures.put (strPrefix + "WorkoutFactor", _wi._dblExerciseFactor);

		mapRVMeasures.put (strPrefix + "WorkoutType", (double) _wi._iWOType);

		mapRVMeasures.put (strPrefix + "WorkoutYield", _wi._dblYield);

		mapRVMeasures.put (strPrefix + "Yield", _wi._dblYield);

		mapRVMeasures.put (strPrefix + "Yield01", _dblYield01);

		mapRVMeasures.put (strPrefix + "YieldBasis", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "YieldSpread", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "ZSpread", _dblZSpread);

		return mapRVMeasures;
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "@";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "!";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter() + _dblPrice +
			getFieldDelimiter() + _dblZSpread + getFieldDelimiter() + _dblGSpread + getFieldDelimiter() +
				_dblISpread + getFieldDelimiter() + _dblOASpread + getFieldDelimiter() + _dblTSYSpread +
					getFieldDelimiter() + _dblDiscountMargin + getFieldDelimiter() + _dblAssetSwapSpread +
						getFieldDelimiter() + _dblCreditBasis + getFieldDelimiter() + _dblPECS +
							getFieldDelimiter() + _dblYield01 + getFieldDelimiter() + _dblMacaulayDuration +
								getFieldDelimiter() + _dblModifiedDuration + getFieldDelimiter() +
									_dblConvexity + getFieldDelimiter() + _dblBondBasis +
										getFieldDelimiter());

		if (null == _wi)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else
			sb.append (new java.lang.String (_wi.serialize()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new BondRVMeasures (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
