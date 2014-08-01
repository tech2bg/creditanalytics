
package org.drip.state.estimator;

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
 * RatesCurveScenarioGenerator uses the interest rate calibration instruments along with the component
 *  calibrator to produce scenario interest rate curves.
 *
 * RatesCurveScenarioGenerator typically first constructs the actual curve calibrator instance to localize
 * 	the intelligence around curve construction. It then uses this curve calibrator instance to build
 *  individual curves or the sequence of node bumped scenario curves. The curves in the set may be an array,
 *  or tenor-keyed.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesCurveScenarioGenerator {
	private static final boolean s_bBlog = false;

	private java.lang.String _strCurrency = "";
	private org.drip.product.definition.CalibratableFixedIncomeComponent[] _aCalibInst = null;
	private java.lang.String _strBootstrapMode =
		org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD;

	private org.drip.state.estimator.NonlinearCurveCalibrator _compCalib = new
		org.drip.state.estimator.NonlinearCurveCalibrator();

	/**
	 * Construct a RatesCurveScenarioGenerator instance from the calibratable instrument array
	 * 
	 * @param strCurrency Currency
	 * @param strBootstrapMode Bootstrap Mode - one of the choices in DiscountCurveBuilder.BOOTSTRAP_MODE_xxx
	 * @param aCalibInst Array of calibration instruments
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public RatesCurveScenarioGenerator (
		final java.lang.String strCurrency,
		final java.lang.String strBootstrapMode,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst)
		throws java.lang.Exception
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == aCalibInst || 0 == aCalibInst.length)
			throw new java.lang.Exception ("IRCurveScenarioGenerator.ctr: Invalid ccy/calib inst inputs");

		_aCalibInst = aCalibInst;
		_strCurrency = strCurrency;

		if (null == (_strBootstrapMode = strBootstrapMode) || _strBootstrapMode.isEmpty())
			_strBootstrapMode =
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD;
	}

	/**
	 * Return the array of the calibration instruments
	 * 
	 * @return Array of the calibration instruments
	 */

	public org.drip.product.definition.FixedIncomeComponent[] getInstruments()
	{
		return _aCalibInst;
	}

	/**
	 * Calibrate a discount curve
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param lsfc Latent State Fixings Container
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return DiscountCurve
	 */

	public org.drip.analytics.rates.DiscountCurve createIRCurve (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == adblQuotes || null == astrCalibMeasure || adblQuotes.length != astrCalibMeasure.length ||
			_aCalibInst.length != astrCalibMeasure.length || null == valParams ||
				!org.drip.quant.common.NumberUtil.IsValid (dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createIRCurve!");

			return null;
		}

		double adblDates[] = new double[adblQuotes.length];
		double adblRates[] = new double[adblQuotes.length];
		org.drip.analytics.rates.ExplicitBootDiscountCurve dc = null;

		for (int i = 0; i < adblQuotes.length; ++i) {
			adblRates[i] = 0.02;

			if (null == _aCalibInst[i] || null == _aCalibInst[i].maturity()) {
				System.out.println ("Param " + i + " invalid in IRCurveScenarioGenerator::createIRCurve!");

				return null;
			}

			adblDates[i] = _aCalibInst[i].maturity().julian();
		}

		try {
			dc = org.drip.state.creator.DiscountCurveBuilder.CreateDC (new org.drip.analytics.date.JulianDate
				(valParams.valueDate()), _strCurrency, null == quotingParams ? null :
					quotingParams.coreCollateralizationParams(), adblDates, adblRates, _strBootstrapMode);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (!_compCalib.bootstrapInterestRateSequence (dc, dcTSY, _aCalibInst, valParams, astrCalibMeasure,
			adblQuotes, dblBump, lsfc, quotingParams, false))
			return null;

		if (s_bBlog) {
			for (int i = 0; i < adblQuotes.length; ++i) {
				try {
					System.out.println (i + "=" +_aCalibInst[i].measureValue (valParams, null,
						org.drip.param.creator.MarketParamsBuilder.Create (dc, null, null, null,
							null, null, lsfc), null, astrCalibMeasure[i]));
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}
		}

		dc.setCCIS (org.drip.analytics.definition.BootCurveConstructionInput.Create (valParams, quotingParams,
			_aCalibInst, adblQuotes, astrCalibMeasure, lsfc));

		return dc;
	}

	/**
	 * Calibrate an array of tenor bumped discount curves
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param lsfc Latent State Fixings Container
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Array of tenor bumped discount curves
	 */

	public org.drip.analytics.rates.DiscountCurve[] createTenorIRCurves (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == _aCalibInst || 0 == _aCalibInst.length || null == adblQuotes || null == astrCalibMeasure
			|| adblQuotes.length != astrCalibMeasure.length || _aCalibInst.length != astrCalibMeasure.length
				|| null == valParams || !org.drip.quant.common.NumberUtil.IsValid (dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createTenorIRCurves!");

			return null;
		}

		org.drip.analytics.rates.DiscountCurve[] aDC = new
				org.drip.analytics.rates.DiscountCurve[_aCalibInst.length];

		for (int i = 0; i < aDC.length; ++i) {
			double[] adblTenorQuotes = new double [aDC.length];

			for (int j = 0; j < aDC.length; ++j) {
				if (j == i)
					adblTenorQuotes[j] = adblQuotes[j] + dblBump;
				else
					adblTenorQuotes[j] = adblQuotes[j];
			}

			if (null == (aDC[i] = createIRCurve (valParams, dcTSY, adblQuotes, 0., astrCalibMeasure, lsfc,
				quotingParams)))
				return null;
		}

		return aDC;
	}

	/**
	 * Calibrate a tenor map of tenor bumped discount curves
	 * 
	 * @param valParams ValuationParams
	 * @param dcTSY Treasury Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Quote bump
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param lsfc Latent State Fixings Container
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Tenor map of tenor bumped discount curves
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		createTenorIRCurveMap (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.rates.DiscountCurve dcTSY,
			final double[] adblQuotes,
			final double dblBump,
			final java.lang.String[] astrCalibMeasure,
			final org.drip.param.market.LatentStateFixingsContainer lsfc,
			final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == adblQuotes || null == astrCalibMeasure || adblQuotes.length != astrCalibMeasure.length ||
			_aCalibInst.length != astrCalibMeasure.length || null == valParams ||
				!org.drip.quant.common.NumberUtil.IsValid (dblBump)) {
			System.out.println ("Invalid params to IRCurveScenarioGenerator::createTenorIRCurveMap!");

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapTenorDC
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		for (int i = 0; i < _aCalibInst.length; ++i) {
			double[] adblTenorQuotes = new double [_aCalibInst.length];

			for (int j = 0; j < _aCalibInst.length; ++j) {
				if (j == i)
					adblTenorQuotes[j] = adblQuotes[j] + dblBump;
				else
					adblTenorQuotes[j] = adblQuotes[j];
			}

			mapTenorDC.put (org.drip.analytics.date.JulianDate.fromJulian
				(_aCalibInst[i].maturity().julian()), createIRCurve (valParams, dcTSY, adblTenorQuotes, 0.,
					astrCalibMeasure, lsfc, quotingParams));
		}

		return mapTenorDC;
	}
}
