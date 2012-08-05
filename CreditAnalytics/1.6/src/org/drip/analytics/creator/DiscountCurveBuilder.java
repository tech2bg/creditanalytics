
package org.drip.analytics.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * This class contains the baseline discount curve builder object. It contains static functions that build
 * 		bootstrapped and other types of discount curve from differing types of inputs.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscountCurveBuilder {
	/**
	 * Builds a Discount Curve from an array of discount factors
	 * 
	 * @param dtStart Start Date
	 * @param strCurrency Currency
	 * @param adblDate Array of dates
	 * @param adblDF array of discount factors
	 * 
	 * @return Discount Curve
	 */

	public static final org.drip.analytics.definition.DiscountCurve BuildFromDF (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double adblDate[],
		final double adblDF[])
	{
		if (null == adblDate || 0 == adblDate.length || null == adblDF || adblDate.length != adblDF.length ||
			null == dtStart || null == strCurrency || strCurrency.isEmpty())
			return null;

		double dblDFBegin = 1.;
		double[] adblRate = new double[adblDate.length];

		double dblPeriodBegin = dtStart.getJulian();

		for (int i = 0; i < adblDate.length; ++i) {
			if (adblDate[i] <= dblPeriodBegin) return null;

			adblRate[i] = 365.25 / (adblDate[i] - dblPeriodBegin) * java.lang.Math.log (dblDFBegin /
				adblDF[i]);

			dblDFBegin = adblDF[i];
			dblPeriodBegin = adblDate[i];
		}

		try {
			return new org.drip.analytics.curve.CalibratedDiscountCurve (dtStart, strCurrency, adblDate,
				adblRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a discount curve from the flat rate
	 * 
	 * @param dtStart Start Date
	 * @param strCurrency Currency
	 * @param dblRate Date
	 * 
	 * @return Discount Curve
	 */

	public static final org.drip.analytics.definition.DiscountCurve CreateFromFlatRate (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double dblRate)
	{
		if (null == dtStart || java.lang.Double.isNaN (dblRate)) return null;

		try {
			return new org.drip.analytics.curve.CalibratedDiscountCurve (dtStart, strCurrency, new double[]
				{dtStart.getJulian()}, new double[] {dblRate});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a discount curve
	 * 
	 * @param dtStart Start Date
	 * @param strCurrency Currency
	 * @param adblDate array of dates
	 * @param adblRate array of rates
	 * 
	 * @return Creates the discount curve
	 */

	public static final org.drip.analytics.definition.DiscountCurve CreateDC (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double[] adblDate,
		final double[] adblRate)
	{
		try {
			return new org.drip.analytics.curve.CalibratedDiscountCurve (dtStart, strCurrency, adblDate,
				adblRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a discount curve instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Discount Curve Instance
	 */

	public static final org.drip.analytics.definition.DiscountCurve FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.analytics.curve.CalibratedDiscountCurve (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
