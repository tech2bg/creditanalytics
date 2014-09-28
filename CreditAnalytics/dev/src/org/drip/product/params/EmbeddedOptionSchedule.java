
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
 * EmbeddedOptionSchedule is a place holder for the embedded option schedule for the component. It contains
 *  the schedule of exercise dates and factors, the exercise notice period, and the option is to call or put.
 *  Further, if the option is of the type fix-to-float on exercise, contains the post-exercise floater index
 *  and floating spread. If the exercise is not discrete (American option), the exercise dates/factors are
 *  discretized according to a pre-specified discretization grid. It exports serialization into and
 *  de-serialization out of byte arrays.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EmbeddedOptionSchedule {
	public static final int CALL_NOTICE_PERIOD_DEFAULT = 30;

	private boolean _bIsPut = false;
	private double _adblDate[] = null;
	private double _adblFactor[] = null;
	private java.lang.String _strFloatIndex = "";
	private boolean _bFixToFloatOnExercise = false;
	private int _iNoticePeriod = CALL_NOTICE_PERIOD_DEFAULT;
	private double _dblFixToFloatSpread = java.lang.Double.NaN;
	private double _dblFixToFloatExerciseDate = java.lang.Double.NaN;

	/**
	 * Create the EOS from the dates/factors string arrays
	 * 
	 * @param strDates String representing the date array
	 * @param strFactors String representing the factor array
	 * @param iNoticePeriod Exercise Notice Period
	 * @param bIsPut True (Put), False (Call)
	 * @param bIsDiscrete True (Discrete), False (Continuous)
	 * @param dblScheduleStart Schedule start Date
	 * @param bFixToFloatOnExercise True => component becomes a floater on call
	 * @param dblFixToFloatExerciseDate Date at which the fix to float conversion happens
	 * @param strFloatIndex Floater Rate Index
	 * @param dblFixToFloatSpread Floater Spread
	 * 
	 * @return EOS object
	 */

	public static final EmbeddedOptionSchedule CreateFromDateFactorSet (
		final java.lang.String strDates,
		final java.lang.String strFactors,
		final int iNoticePeriod,
		final boolean bIsPut,
		final boolean bIsDiscrete,
		final double dblScheduleStart,
		final boolean bFixToFloatOnExercise,
		final double dblFixToFloatExerciseDate,
		final java.lang.String strFloatIndex,
		final double dblFixToFloatSpread)
	{
		if (null == strDates || strDates.isEmpty() || null == strFactors || strFactors.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblScheduleStart))
			return null;

		if (bIsDiscrete) {
			try {
				return new EmbeddedOptionSchedule
					(org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer (new
						java.util.StringTokenizer (strDates, ";")),
							org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer (new
								java.util.StringTokenizer (strFactors, ";")), bIsPut, iNoticePeriod,
									bFixToFloatOnExercise, dblFixToFloatExerciseDate, strFloatIndex,
										dblFixToFloatSpread);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		return fromAmerican (dblScheduleStart,
			org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer (new
				java.util.StringTokenizer (strDates, ";")),
					org.drip.quant.common.StringUtil.MakeDoubleArrayFromStringTokenizer (new
						java.util.StringTokenizer (strFactors, ";")), bIsPut, iNoticePeriod,
							bFixToFloatOnExercise, dblFixToFloatExerciseDate, strFloatIndex,
								dblFixToFloatSpread);
	}

	/**
	 * Create the discretized American EOS schedule from the array of dates and factors
	 * 
	 * @param dblValDate Valuation Date - date to which the component is assumed to not have been exercised
	 * @param adblDate Array of dates
	 * @param adblFactor Matched Array of Factors
	 * @param bIsPut True (Put), False (Call)
	 * @param iNoticePeriod Exercise Notice Period
	 * @param bFixToFloatOnExercise True => component becomes a floater on call
	 * @param dblFixToFloatExerciseDate Date at which the fix to float conversion happens
	 * @param strFloatIndex Floater Rate Index
	 * @param dblFixToFloatSpread Floater Spread
	 * 
	 * @return Discretized EOS
	 */

	public static final EmbeddedOptionSchedule fromAmerican (
		final double dblValDate,
		final double adblDate[],
		final double adblFactor[],
		final boolean bIsPut,
		final int iNoticePeriod,
		final boolean bFixToFloatOnExercise,
		final double dblFixToFloatExerciseDate,
		final java.lang.String strFloatIndex,
		final double dblFixToFloatSpread)
	{
		if (null == adblDate || adblDate.length == 0 || null == adblFactor || adblFactor.length == 0 ||
			adblDate.length != adblFactor.length)
			return null;

		int i = 0;
		int iCallDiscretization = 30;
		double dblScheduleStart = dblValDate;

		if (dblValDate < adblDate[0]) dblScheduleStart = adblDate[0];

		java.util.ArrayList<java.lang.Double> ldblCallDates = new java.util.ArrayList<java.lang.Double>();

		java.util.ArrayList<java.lang.Double> ldblCallFactors = new java.util.ArrayList<java.lang.Double>();

		for (; i < adblDate.length; ++i) {
			double dblCallDate = dblScheduleStart;

			if (0 != i) dblCallDate = adblDate[i - 1];

			while (dblCallDate <= adblDate[i]) {
				ldblCallDates.add (dblCallDate);

				ldblCallFactors.add (adblFactor[i]);

				dblCallDate += iCallDiscretization;
			}
		}

		double[] adblEOSDate = new double[ldblCallDates.size()];

		i = 0;

		for (double dblCallDate : ldblCallDates)
			adblEOSDate[i++] = dblCallDate;

		double[] adblEOSFactor = new double[ldblCallFactors.size()];

		i = 0;

		for (double dblCallFactor : ldblCallFactors)
			adblEOSFactor[i++] = dblCallFactor;

		try {
			return new EmbeddedOptionSchedule (adblEOSDate, adblEOSFactor, bIsPut, iNoticePeriod,
				bFixToFloatOnExercise, dblFixToFloatExerciseDate, strFloatIndex, dblFixToFloatSpread);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Construct the EOS from the array of dates and factors
	 * 
	 * @param adblDate Array of dates
	 * @param adblFactor Matched Array of Factors
	 * @param bIsPut True (Put), False (Call)
	 * @param iNoticePeriod Exercise Notice Period
	 * @param bFixToFloatOnExercise True => component becomes a floater on call
	 * @param dblFixToFloatExerciseDate Date at which the fix to float conversion happens
	 * @param strFloatIndex Floater Rate Index
	 * @param dblFixToFloatSpread Floater Spread
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public EmbeddedOptionSchedule (
		final double[] adblDate,
		final double[] adblFactor,
		final boolean bIsPut,
		final int iNoticePeriod,
		final boolean bFixToFloatOnExercise,
		final double dblFixToFloatExerciseDate,
		final java.lang.String strFloatIndex,
		final double dblFixToFloatSpread)
		throws java.lang.Exception
	{
		if (null == adblDate || null == adblFactor || adblDate.length != adblFactor.length)
			throw new java.lang.Exception ("EmbeddedOptionSchedule ctr => Invalid params");

		_adblDate = new double[adblDate.length];
		_adblFactor = new double[adblFactor.length];

		for (int i = 0; i < _adblDate.length; ++i)
			_adblDate[i] = adblDate[i];

		for (int i = 0; i < _adblFactor.length; ++i)
			_adblFactor[i] = adblFactor[i];

		_bIsPut = bIsPut;
		_iNoticePeriod = iNoticePeriod;
		_strFloatIndex = strFloatIndex;
		_dblFixToFloatSpread = dblFixToFloatSpread;
		_bFixToFloatOnExercise = bFixToFloatOnExercise;
		_dblFixToFloatExerciseDate = dblFixToFloatExerciseDate;
	}

	/**
	 * Construct a Deep Copy EOS from another EOS
	 * 
	 * @param eosOther The Other EOS
	 */

	public EmbeddedOptionSchedule (
		final EmbeddedOptionSchedule eosOther)
	{
		_adblDate = new double[eosOther._adblDate.length];
		_adblFactor = new double[eosOther._adblFactor.length];

		for (int i = 0; i < _adblDate.length; ++i)
			_adblDate[i] = eosOther._adblDate[i];

		for (int i = 0; i < _adblFactor.length; ++i)
			_adblFactor[i] = eosOther._adblFactor[i];

		_bIsPut = eosOther._bIsPut;
		_iNoticePeriod = eosOther._iNoticePeriod;
		_strFloatIndex = eosOther._strFloatIndex;
		_dblFixToFloatSpread = eosOther._dblFixToFloatSpread;
		_bFixToFloatOnExercise = eosOther._bFixToFloatOnExercise;
		_dblFixToFloatExerciseDate = eosOther._dblFixToFloatExerciseDate;
	}

	/**
	 * Whether the component is putable or callable
	 * 
	 * @return True (Put), False (Call)
	 */

	public boolean isPut()
	{
		return _bIsPut;
	}

	/**
	 * Get the array of dates
	 * 
	 * @return The array of dates
	 */

	public double[] getDates()
	{
		return _adblDate;
	}

	/**
	 * Get the array of factors
	 * 
	 * @return The array of factors
	 */

	public double[] getFactors()
	{
		return _adblFactor;
	}

	/**
	 * Get the specific indexed factor
	 * 
	 * @param iIndex Factor index
	 * 
	 * @return Factor corresponding to the index
	 */

	public double getFactor (
		final int iIndex)
	{
		return _adblFactor[iIndex];
	}

	/**
	 * Retrieve the exercise notice period
	 * 
	 * @return Minimum Exercise Notice Period in Days
	 */

	public int getExerciseNoticePeriod()
	{
		return _iNoticePeriod;
	}

	/**
	 * Return whether the component is fix to float on exercise
	 * 
	 * @return True (component becomes a floater on call), False (component does not change)
	 */

	public boolean isFixToFloatOnExercise()
	{
		return _bFixToFloatOnExercise;
	}
}
