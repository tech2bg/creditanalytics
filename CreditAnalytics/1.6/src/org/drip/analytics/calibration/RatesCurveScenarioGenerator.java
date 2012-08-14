
package org.drip.analytics.calibration;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * This calls contains the interest rate calibration instruments to be used with the component calibrator to
 * 		produce scenario interest rate curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesCurveScenarioGenerator {
	private java.lang.String _strCurrency = "";
	private org.drip.product.definition.CalibratableComponent[] _aCalibInst = null;

	private ComponentCalibrator _compCalib = new NewtonRaphsonCalibrator();

	/**
	 * Constructs a CreditCurveScenarioGenerator instance from the calibratable instrument array
	 * 
	 * @param strCurrency Currency
	 * @param aCalibInst Array of calibration instruments
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public RatesCurveScenarioGenerator (
		final java.lang.String strCurrency,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst)
		throws java.lang.Exception
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == aCalibInst || 0 == aCalibInst.length)
			throw new java.lang.Exception ("IRCurveScenarioGenerator.ctr: Invalid ccy/calib inst inputs");

		_aCalibInst = aCalibInst;
		_strCurrency = strCurrency;
	}

	/**
	 * Returns an array of the calibration instruments
	 * 
	 * @return Array of the calibration instruments
	 */

	public org.drip.product.definition.Component[] getInstruments()
	{
		return _aCalibInst;
	}

	/**
	 * Calibrates a discount curve
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return DiscountCurve
	 */

	public org.drip.analytics.definition.DiscountCurve createIRCurve (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == adblQuotes || null == astrCalibMeasure || adblQuotes.length != astrCalibMeasure.length ||
			_aCalibInst.length != astrCalibMeasure.length || null == valParams || java.lang.Double.isNaN
				(dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createIRCurve!");

			return null;
		}

		org.drip.analytics.definition.DiscountCurve dc = null;
		double adblDates[] = new double[adblQuotes.length];
		double adblRates[] = new double[adblQuotes.length];

		for (int i = 0; i < adblQuotes.length; ++i) {
			adblRates[i] = java.lang.Double.NaN;

			if (null == _aCalibInst[i] || null == _aCalibInst[i].getMaturityDate()) {
				System.out.println ("Param " + i + " invalid in IRCurveScenarioGenerator::createIRCurve!");

				return null;
			}

			adblDates[i] = _aCalibInst[i].getMaturityDate().getJulian();
		}

		try {
			dc = org.drip.analytics.creator.DiscountCurveBuilder.CreateDC (new
				org.drip.analytics.date.JulianDate (valParams._dblValue), _strCurrency, adblDates,
					adblRates);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < adblQuotes.length; ++i) {
			if (!_compCalib.bootstrapInterestRate (dc, dcTSY, dcEDSF, _aCalibInst[i], i, valParams,
				astrCalibMeasure[i], adblQuotes[i] + dblBump, mmFixings, quotingParams, false))
				return null;
		}

		dc.setInstrCalibInputs (valParams, _aCalibInst, adblQuotes, astrCalibMeasure, mmFixings,
			quotingParams);

		return dc;
	}

	/**
	 * Calibrates an array of tenor bumped discount curves
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Array of tenor bumped discount curves
	 */

	public org.drip.analytics.definition.DiscountCurve[] createTenorIRCurves (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == _aCalibInst || 0 == _aCalibInst.length || null == adblQuotes || null == astrCalibMeasure
			|| adblQuotes.length != astrCalibMeasure.length || _aCalibInst.length != astrCalibMeasure.length
				|| null == valParams || java.lang.Double.isNaN (dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createTenorIRCurves!");

			return null;
		}

		org.drip.analytics.definition.DiscountCurve[] aDC = new
				org.drip.analytics.definition.DiscountCurve[_aCalibInst.length];

		for (int i = 0; i < aDC.length; ++i) {
			double[] adblTenorQuotes = new double [aDC.length];

			for (int j = 0; j < aDC.length; ++j) {
				if (j == i)
					adblTenorQuotes[j] = adblQuotes[j] + dblBump;
				else
					adblTenorQuotes[j] = adblQuotes[j];
			}

			if (null == (aDC[i] = createIRCurve (valParams, dcTSY, dcEDSF, adblQuotes, 0., astrCalibMeasure,
				mmFixings, quotingParams)))
				return null;
		}

		return aDC;
	}

	/**
	 * Calibrates a tenor map of tenor bumped discount curves
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Tenor map of tenor bumped discount curves
	 */

	public java.util.Map<java.lang.String, org.drip.analytics.definition.DiscountCurve> createTenorIRCurveMap
		(final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == adblQuotes || null == astrCalibMeasure || adblQuotes.length != astrCalibMeasure.length ||
			_aCalibInst.length != astrCalibMeasure.length || null == valParams || java.lang.Double.isNaN
				(dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createTenorIRCurveMap!");

			return null;
		}

		java.util.Map<java.lang.String, org.drip.analytics.definition.DiscountCurve> mapTenorDC = new
			java.util.HashMap<java.lang.String, org.drip.analytics.definition.DiscountCurve>();

		for (int i = 0; i < _aCalibInst.length; ++i) {
			double[] adblTenorQuotes = new double [_aCalibInst.length];

			for (int j = 0; j < _aCalibInst.length; ++j) {
				if (j == i)
					adblTenorQuotes[j] = adblQuotes[j] + dblBump;
				else
					adblTenorQuotes[j] = adblQuotes[j];
			}

			mapTenorDC.put (org.drip.analytics.date.JulianDate.fromJulian
				(_aCalibInst[i].getMaturityDate().getJulian()), createIRCurve (valParams, dcTSY, dcEDSF,
					adblTenorQuotes, 0., astrCalibMeasure, mmFixings, quotingParams));
		}

		return mapTenorDC;
	}
}