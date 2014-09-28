
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
 * FactorSchedule the contains array of dates and factors. It provides methods to create/access different
 * 	varieties of factor schedule creation It exports serialization into and de-serialization out of byte
 *  arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FactorSchedule {
	private double _adblDate[] = null;
	private double _adblFactor[] = null;

	/**
	 * Create the factor schedule from a matched string array of dates and factors
	 * 
	 * @param strDates String array of dates
	 * @param strFactors String array of Factors
	 * 
	 * @return FactorSchedule object
	 */

	public static final FactorSchedule CreateFromDateFactorSet (
		final java.lang.String strDates,
		final java.lang.String strFactors)
	{
		if (null == strDates || strDates.isEmpty() || null == strFactors || strFactors.isEmpty())
			return null;

		try {
			return new FactorSchedule (org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer
				(new java.util.StringTokenizer (strDates, ";")),
					org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer (new
						java.util.StringTokenizer (strFactors, ";")));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create the factor schedule from a matched array of dates and factors
	 * 
	 * @param adblDate Array of dates
	 * @param adblFactor Array of Factors
	 * 
	 * @return FactorSchedule object
	 */

	public static final FactorSchedule CreateFromDateFactorArray (
		final double[] adblDate,
		final double[] adblFactor)
	{
		try {
			return new FactorSchedule (adblDate, adblFactor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create the factor schedule from a matched array of dates and factor deltas
	 * 
	 * @param adblDate Array of dates
	 * @param adblFactorDelta Array of Factor Deltas
	 * 
	 * @return FactorSchedule object
	 */

	public static final FactorSchedule CreateFromDateFactorDeltaArray (
		final double[] adblDate,
		final double[] adblFactorDelta)
	{
		if (null == adblDate || 0 == adblDate.length || null == adblFactorDelta || 0 ==
			adblFactorDelta.length || adblDate.length != adblFactorDelta.length)
			return null;

		double[] adblFactor = new double[adblFactorDelta.length];

		int i = 0;
		adblFactor[0] = 1.;

		for (double dblFactorDelta : adblFactorDelta) {
			if (i < adblFactorDelta.length - 1) adblFactor[i + 1] = adblFactor[i] - dblFactorDelta;

			++i;
		}

		try {
			return new FactorSchedule (adblDate, adblFactor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create factor schedule of flat unit notional
	 * 
	 * @return FactorSchedule object
	 */

	public static final FactorSchedule CreateBulletSchedule()
	{
		double[] adblDate = new double[1];
		double[] adblFactor = new double[1];
		adblFactor[0] = 1.;

		adblDate[0] = org.drip.analytics.date.JulianDate.CreateFromYMD (1900, 1, 1).julian();

		try {
			return new FactorSchedule (adblDate, adblFactor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private FactorSchedule (
		final double[] adblDate,
		final double[] adblFactor)
		throws java.lang.Exception
	{
		if (null == adblDate || 0 == adblDate.length || null == adblFactor || 0 == adblFactor.length ||
			adblDate.length != adblFactor.length)
			throw new java.lang.Exception ("FactorSchedule ctr => Invalid params");

		_adblDate = new double[adblDate.length];
		_adblFactor = new double[adblFactor.length];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = adblDate[i];
			_adblFactor[i] = adblFactor[i];
		}
	}

	/**
	 * Retrieve the notional factor for a given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Notional factor 
	 * 
	 * @throws java.lang.Exception Thrown if the notional cannot be computed
	 */

	public double getFactor (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("FactorSchedule::getFactor => Invalid Input");

		if (dblDate <= _adblDate[0]) return _adblFactor[0];

		for (int i = 1; i < _adblDate.length; ++i) {
			if (dblDate > _adblDate[i - 1] && dblDate <= _adblDate[i]) return _adblFactor[i];
		}

		return _adblFactor[_adblDate.length - 1];
	}

	/**
	 * Retrieve the index that corresponds to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Index 
	 * 
	 * @throws java.lang.Exception Thrown if the index cannot be computed
	 */

	public int getIndex (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("FactorSchedule::getIndex => Invalid Input/State");

		if (dblDate <= _adblDate[0]) return 0;

		for (int i = 1; i < _adblDate.length; ++i) {
			if (dblDate <= _adblDate[i]) return i;
		}

		return _adblDate.length - 1;
	}

	/**
	 * Retrieve the time-weighted notional factor between 2 dates
	 * 
	 * @param dblStartDate Start Date
	 * @param dblEndDate End Date
	 * 
	 * @return Notional factor 
	 * 
	 * @throws java.lang.Exception Thrown if the notional cannot be computed
	 */

	public double getFactor (
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate))
			throw new java.lang.Exception ("FactorSchedule::getFactor => Invalid Inputs");

		int iEndIndex = getIndex (dblEndDate);

		int iStartIndex = getIndex (dblStartDate);

		if (iStartIndex == iEndIndex) return _adblFactor[iStartIndex];

		double dblWeightedFactor = _adblFactor[iStartIndex] * (_adblDate[iStartIndex] - dblStartDate);

		for (int i = iStartIndex + 1; i <= iEndIndex; ++i)
			dblWeightedFactor += _adblFactor[i] * (_adblDate[i] - _adblDate[i - 1]);

		return (dblWeightedFactor + _adblFactor[iEndIndex] * (dblEndDate - _adblDate[iEndIndex])) /
			(dblEndDate - dblStartDate);
	}

	/**
	 * Retrieve the array of dates
	 * 
	 * @return Double array of JulianDate
	 */

	public double[] getDates()
	{
		return _adblDate;
	}

	/**
	 * Retrieve the array of notional factors
	 * 
	 * @return Double array of notional factors
	 */

	public double[] getFactors()
	{
		return _adblFactor;
	}

	/**
	 * Indicate if this Factor Schedule matches the "other" Entry-by-Entry
	 * 
	 * @param fsOther The "Other" Factor Schedule Instance
	 * 
	 * @return TRUE => The Factor Schedules match Entry-by-Entry
	 */

	public boolean match (
		final FactorSchedule fsOther)
	{
		if (null == fsOther) return false;

		double[] adblOtherDate = fsOther._adblDate;
		double[] adblOtherFactor = fsOther._adblFactor;
		int iNumOtherDate = adblOtherDate.length;
		int iNumOtherFactor = adblOtherFactor.length;

		if (iNumOtherDate != _adblDate.length || iNumOtherFactor != _adblFactor.length) return false;

		for (int i = 0; i < iNumOtherDate; ++i) {
			if (adblOtherDate[i] != _adblDate[i]) return false;
		}

		for (int i = 0; i < iNumOtherFactor; ++i) {
			if (adblOtherFactor[i] != _adblFactor[i]) return false;
		}

		return true;
	}
}
