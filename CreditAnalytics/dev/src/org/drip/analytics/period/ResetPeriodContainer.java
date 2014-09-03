
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
 * ResetPeriodContainer holds the Coupon Period's Reset Settings. Currently it contains the Reset Period List
 *  and the accrual compounding rules..
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResetPeriodContainer extends org.drip.service.stream.Serializer {
	private int _iAccrualCompoundingRule = -1;
	private java.util.List<org.drip.analytics.period.ResetPeriod> _lsResetPeriod = null;

	/**
	 * ResetPeriodContainer Constructor
	 * 
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public ResetPeriodContainer (
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule (_iAccrualCompoundingRule =
			iAccrualCompoundingRule))
			throw new java.lang.Exception ("ResetPeriodContainer ctr: Invalid Accrual Compounding Rule");
	}

	/**
	 * De-serialize the ResetPeriodContainer Instance from the Stream Byte Array
	 * 
	 * @param ab The Stream Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ResetPeriodContainer cannot be de-serialized
	 */

	public ResetPeriodContainer (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ResetPeriodContainer de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ResetPeriodContainer de-serializer: Empty state");

		java.lang.String strResetPeriodContainer = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strResetPeriodContainer || strResetPeriodContainer.isEmpty())
			throw new java.lang.Exception ("ResetPeriodContainer de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strResetPeriodContainer,
			fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("ResetPeriodContainer de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("ResetPeriodContainer de-serializer: Cannot locate the Accrual Compounding Rule");

		_iAccrualCompoundingRule = new java.lang.Integer (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("ResetPeriodContainer de-serializer: Cannot locate Reset Period List");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_lsResetPeriod = new java.util.ArrayList<org.drip.analytics.period.ResetPeriod>();
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[2],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsResetPeriod)
						_lsResetPeriod = new java.util.ArrayList<org.drip.analytics.period.ResetPeriod>();

					_lsResetPeriod.add (new org.drip.analytics.period.ResetPeriod
						(astrRecord[i].getBytes()));
				}
			}
		}
	}

	/**
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _iAccrualCompoundingRule;
	}

	/**
	 * Append the Reset Period
	 * 
	 * @param rp The Reset Period
	 * 
	 * @return TRUE => The Reset Period Successfully Appended
	 */

	public boolean appendResetPeriod (
		final org.drip.analytics.period.ResetPeriod rp)
	{
		if (null == rp) return false;

		if (null == _lsResetPeriod)
			_lsResetPeriod = new java.util.ArrayList<org.drip.analytics.period.ResetPeriod>();

		_lsResetPeriod.add (rp);

		return true;
	}

	/**
	 * Retrieve the Reset Periods
	 * 
	 * @return The Reset Periods
	 */

	public java.util.List<org.drip.analytics.period.ResetPeriod> resetPeriods()
	{
		return _lsResetPeriod;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "]";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "}";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_iAccrualCompoundingRule + fieldDelimiter());

		if (null == _lsResetPeriod || 0 == _lsResetPeriod.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.ResetPeriod rp : _lsResetPeriod) {
				if (null == rp) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (collectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (rp.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		sb.append (fieldDelimiter());

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ResetPeriodContainer (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
