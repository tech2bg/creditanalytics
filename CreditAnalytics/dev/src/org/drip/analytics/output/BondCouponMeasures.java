
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
 * This class encapsulates the parsimonius but complete set of the cash-flow oriented coupon measures
 * 	generated out of a full bond analytics run to a given work-out. These are:
 * 	- DV01
 * 	- PV Measures (Coupon PV, Index Coupon PV, PV)
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondCouponMeasures extends org.drip.service.stream.Serializer {

	/**
	 * Coupon DV01
	 */

	public double _dblDV01 = java.lang.Double.NaN;

	/**
	 * Index Coupon PV
	 */

	public double _dblIndexCouponPV = java.lang.Double.NaN;

	/**
	 * Coupon PV
	 */

	public double _dblCouponPV = java.lang.Double.NaN;

	/**
	 * PV
	 */

	public double _dblPV = java.lang.Double.NaN;

	/**
	 * BondCouponMeasures constructor
	 * 
	 * @param dblDV01 DV01
	 * @param dblIndexCouponPV Index Coupon PV
	 * @param dblCouponPV Coupon PV
	 * @param dblPV PV
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public BondCouponMeasures (
		final double dblDV01,
		final double dblIndexCouponPV,
		final double dblCouponPV,
		final double dblPV)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDV01 = dblDV01) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblCouponPV = dblCouponPV) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblPV = dblPV))
			throw new java.lang.Exception ("BondCouponMeasures ctr: Invalid Inputs!");

		_dblIndexCouponPV = dblIndexCouponPV;
	}

	/**
	 * BondCouponMeasures de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if BondCouponMeasures cannot be properly de-serialized
	 */

	public BondCouponMeasures (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Empty state");

		java.lang.String strSerializedBondCouponMeasures = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedBondCouponMeasures || strSerializedBondCouponMeasures.isEmpty())
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedBondCouponMeasures, getFieldDelimiter());

		if (null == astrField || 5 > astrField.length)
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Cannot locate DV01");

		_dblDV01 = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("BondCouponMeasures de-serializer: Cannot locate Index Coupon PV");

		_dblIndexCouponPV = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Cannot locate Coupon PV");

		_dblCouponPV = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("BondCouponMeasures de-serializer: Cannot locate PV");

		_dblPV = new java.lang.Double (astrField[4]);
	}

	/**
	 * Adjusts the bond coupon measures by a cash settlement discount factor
	 * 
	 * @param dblCashPayDF Cash Pay discount factor
	 * 
	 * @return TRUE => if the adjustment has been successfully applied
	 */

	public boolean adjustForSettlement (
		final double dblCashPayDF)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCashPayDF)) return false;

		_dblDV01 /= dblCashPayDF;
		_dblIndexCouponPV /= dblCashPayDF;
		_dblCouponPV /= dblCashPayDF;
		_dblPV /= dblCashPayDF;
		return true;
	}

	/**
	 * Adjust Measures for accrued
	 * 
	 * @param dblAccrued01 Accrued 01
	 * @param dblCoupon Coupon during the accrued phase
	 * @param dblIndex Index Rate during the accrued phase
	 * @param bDirtyFromClean True => Change measures from Clean to Dirty
	 * 
	 * @return True => if the adjustment has been successfully applied
	 */

	public boolean adjustForAccrual (
		final double dblAccrued01,
		final double dblCoupon,
		final double dblIndex,
		final boolean bDirtyFromClean)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrued01) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblCoupon))
			return false;

		if (bDirtyFromClean)
			_dblDV01 -= dblAccrued01;
		else
			_dblDV01 += dblAccrued01;

		if (bDirtyFromClean)
			_dblIndexCouponPV -= dblAccrued01 * dblIndex;
		else
			_dblIndexCouponPV += dblAccrued01 * dblIndex;

		if (bDirtyFromClean)
			_dblCouponPV -= dblAccrued01 * dblCoupon;
		else
			_dblCouponPV += dblAccrued01 * dblCoupon;

		if (bDirtyFromClean)
			_dblPV -= dblAccrued01 * dblCoupon;
		else
			_dblPV += dblAccrued01 * dblCoupon;

		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + getFieldDelimiter() + _dblDV01 + getFieldDelimiter() + _dblIndexCouponPV +
			getFieldDelimiter() + _dblCouponPV + getFieldDelimiter() + _dblPV);

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	/**
	 * Returns the state as a named measure map
	 * 
	 * @param strPrefix Measure name prefix
	 * 
	 * @return Map of the measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> toMap (
		final java.lang.String strPrefix)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapMeasures.put (strPrefix + "DV01", _dblDV01);

		mapMeasures.put (strPrefix + "IndexCouponPV", _dblIndexCouponPV);

		mapMeasures.put (strPrefix + "CouponPV", _dblCouponPV);

		mapMeasures.put (strPrefix + "PV", _dblPV);

		return mapMeasures;
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new BondCouponMeasures (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
