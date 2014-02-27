
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * BootCurveConstructionInput contains the Parameters needed for the Curve Calibration/Estimation. It
 * 	contains the following:
 *  - Calibration Valuation Parameters
 *  - Calibration Quoting Parameters
 *  - Array of Calibration Instruments
 *  - Map of Calibration Quotes
 *  - Map of Calibration Measures
 *  - Double Map of the Date/Index Fixings
 *
 * @author Lakshmi Krishnamurthy
 */

public class BootCurveConstructionInput implements org.drip.analytics.definition.CurveConstructionInputSet {
	private org.drip.param.valuation.ValuationParams _valParam = null;
	private org.drip.param.valuation.QuotingParams _quotingParam = null;
	private org.drip.product.definition.CalibratableComponent[] _aCalibInst = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mapQuote = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> _mapMeasures = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixing = null;

	/**
	 * Create an Instance of BootCurveConstructionInput from the given Calibration Inputs
	 * 
	 * @param valParam Valuation Parameters
	 * @param quotingParam Quoting Parameters
	 * @param aCalibInst Array of the Calibration Instruments
	 * @param adblCalibQuote Array of the Calibration Quotes
	 * @param astrCalibMeasure Array of the Calibration Measures
	 * @param mmFixing Double Map of the Date/Index Fixings
	 * 
	 * @return Instance of BootCurveConstructionInput
	 */

	public static final BootCurveConstructionInput Create (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst,
		final double[] adblCalibQuote,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing)
	{
		if (null == aCalibInst || null == adblCalibQuote || null == astrCalibMeasure) return null;

		int iNumInst = aCalibInst.length;

		if (0 == iNumInst || adblCalibQuote.length != iNumInst || astrCalibMeasure.length != iNumInst)
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> mapMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]>();

		for (int i = 0; i < iNumInst; ++i) {
			if (null == aCalibInst[i]) return null;

			java.lang.String strInstrumentCode = aCalibInst[i].getPrimaryCode();

			if (null == strInstrumentCode || strInstrumentCode.isEmpty() || null == astrCalibMeasure[i] ||
				astrCalibMeasure[i].isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
					(adblCalibQuote[i]))
				return null;

			mapMeasures.put (strInstrumentCode, new java.lang.String[] {astrCalibMeasure[i]});

			mapQuote.put (strInstrumentCode, adblCalibQuote[i]);

			java.lang.String[] astrSecCode = aCalibInst[i].getSecondaryCode();

			if (null != astrSecCode) {
				int iNumSecCode = astrSecCode.length;

				for (int j = 0; j < iNumSecCode; ++j) {
					java.lang.String strSecCode = astrSecCode[j];

					if (null == strSecCode || strSecCode.isEmpty())
						mapQuote.put (strSecCode, adblCalibQuote[i]);
				}
			}
		}

		try {
			return new BootCurveConstructionInput (valParam, quotingParam, aCalibInst, mapQuote, mapMeasures,
				mmFixing);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * BootCurveConstructionInput constructor
	 * 
	 * @param valParam Valuation Parameter
	 * @param quotingParam Quoting Parameter
	 * @param aCalibInst Array of Calibration Instruments
	 * @param mapQuote Map of the Calibration Instrument Quotes
	 * @param mapMeasures Map containing the Array of the Calibration Instrument Measures
	 * @param mmFixing Double Map of the Date/Index Fixings
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public BootCurveConstructionInput (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> mapMeasures,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing)
		throws java.lang.Exception
	{
		if (null == (_valParam = valParam) || null == (_aCalibInst = aCalibInst) || null == (_mapQuote =
			mapQuote) || null == (_mapMeasures = mapMeasures))
			throw new java.lang.Exception ("BootCurveConstructionInput ctr: Invalid Inputs");

		int iNumInst = _aCalibInst.length;

		if (0 == iNumInst || iNumInst > _mapQuote.size() || iNumInst > _mapMeasures.size())
			throw new java.lang.Exception ("BootCurveConstructionInput ctr: Invalid Inputs");

		_mmFixing = mmFixing;
		_quotingParam = quotingParam;
	}

	@Override public org.drip.param.valuation.ValuationParams getValuationParameter()
	{
		return _valParam;
	}

	@Override public org.drip.param.valuation.QuotingParams getQuotingParameter()
	{
		return _quotingParam;
	}

	@Override public org.drip.product.definition.CalibratableComponent[] getComponent()
	{
		return _aCalibInst;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> getQuote()
	{
		return _mapQuote;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> getMeasures()
	{
		return _mapMeasures;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getFixing()
	{
		return _mmFixing;
	}
}
