
package org.drip.param.market;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * RatesCurveScenarioContainer implements the RatesScenarioCurve abstract class that exposes the interface
 *  the constructs scenario discount curves. The following curve construction scenarios are supported:
 *  - Base, flat/tenor up/down by arbitrary bumps
 *  - Tenor bumped discount curve set - keyed using the tenor.
 *	- NTP-based custom scenario curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesCurveScenarioContainer extends org.drip.param.definition.ScenarioDiscountCurve {
	private static final boolean s_bBlog = false;

	private org.drip.analytics.rates.DiscountCurve _dcBase = null;
	private org.drip.analytics.rates.DiscountCurve _dcBumpUp = null;
	private org.drip.analytics.rates.DiscountCurve _dcBumpDn = null;
	private org.drip.state.estimator.RatesCurveScenarioGenerator _irsg = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapCustomDC = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapDCBumpUp = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapDCBumpDn = null;

	/**
	 * Constructs an IRCurveScenarioContainer instance from the corresponding IRCurveScenarioGenerator
	 * 
	 * @param irsg IRCurveScenarioGenerator instance
	 * 
	 * @throws java.lang.Exception Thrown if the IRCurveScenarioGenerator instance is invalid
	 */

	public RatesCurveScenarioContainer (
		final org.drip.state.estimator.RatesCurveScenarioGenerator irsg)
		throws java.lang.Exception
	{
		if (null == (_irsg = irsg))
			throw new java.lang.Exception ("RatesCurveScenarioContainer ctr => Invalid Inputs");
	}

	@Override public boolean cookScenarioDC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final int iDCMode)
	{
		if (null == valParams || null == adblQuotes || null == astrCalibMeasure || 0 == adblQuotes.length ||
			0 == astrCalibMeasure.length || adblQuotes.length != astrCalibMeasure.length ||
				!org.drip.quant.common.NumberUtil.IsValid (dblBump) || null == _irsg) {
			if (s_bBlog)
				System.out.println ("Invalid inputs/state in RatesCurveScenarioContainer.cookScenarioDC");

			return false;
		}

		if (null == (_dcBase = _irsg.createIRCurve (valParams, dcTSY, adblQuotes, 0., astrCalibMeasure, lsfc,
			quotingParams))) {
			if (s_bBlog)
				System.out.println ("Base DC cook problem in RatesCurveScenarioContainer.cookScenarioDC");

			return false;
		}

		if (0 != (org.drip.param.definition.ScenarioDiscountCurve.DC_FLAT_UP & iDCMode)) {
			if (null == (_dcBumpUp = _irsg.createIRCurve (valParams, dcTSY, adblQuotes, dblBump,
				astrCalibMeasure, lsfc, quotingParams))) {
				if (s_bBlog)
					System.out.println
						("Flat bump up DC cook problem in RatesCurveScenarioContainer.cookScenarioDC");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioDiscountCurve.DC_FLAT_DN & iDCMode)) {
			if (null == (_dcBumpDn = _irsg.createIRCurve (valParams, dcTSY, adblQuotes, -dblBump,
				astrCalibMeasure, lsfc, quotingParams))) {
				if (s_bBlog)
					System.out.println
						("Flat bump dn DC cook problem in RatesCurveScenarioContainer.cookScenarioDC");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioDiscountCurve.DC_TENOR_UP & iDCMode)) {
			if (null == (_mapDCBumpUp = _irsg.createTenorIRCurveMap (valParams, dcTSY, adblQuotes, dblBump,
				astrCalibMeasure, lsfc, quotingParams))) {
				if (s_bBlog)
					System.out.println
						("Partial bump up DC cook problem in RatesCurveScenarioContainer.cookScenarioDC");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioDiscountCurve.DC_TENOR_DN & iDCMode)) {
			if (null == (_mapDCBumpDn = _irsg.createTenorIRCurveMap (valParams, dcTSY, adblQuotes, -dblBump,
				astrCalibMeasure, lsfc, quotingParams))) {
				if (s_bBlog)
					System.out.println
						("Partial bump dn DC cook problem in RatesCurveScenarioContainer.cookScenarioDC");

				return false;
			}
		}

		return true;
	}

	@Override public boolean cookCustomDC (
		final java.lang.String strCurveName,
		final java.lang.String strCustomName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.param.definition.ResponseValueTweakParams mmtpTSY,
		final org.drip.param.definition.ResponseValueTweakParams mmtpDC)
	{
		if (null == strCustomName || strCustomName.isEmpty() || null == _irsg || null == adblQuotes || 0 ==
			adblQuotes.length || null == astrCalibMeasure || 0 == astrCalibMeasure.length ||
				astrCalibMeasure.length != adblQuotes.length || (null == mmtpTSY && null == mmtpDC))
			return false;

		org.drip.analytics.rates.DiscountCurve dcTSYAdj = (org.drip.analytics.rates.DiscountCurve)
			dcTSY.customTweakManifestMeasure ("Rate", mmtpTSY);

		if (null == dcTSYAdj) dcTSYAdj = dcTSY;

		org.drip.analytics.rates.DiscountCurve dcBaseCustom = _irsg.createIRCurve (valParams, dcTSYAdj,
			adblQuotes, 0., astrCalibMeasure, lsfc, quotingParams);

		if (null == dcBaseCustom) return false;

		if (null == _mapCustomDC)
			_mapCustomDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		org.drip.analytics.rates.DiscountCurve dcCustom = (org.drip.analytics.rates.DiscountCurve)
			dcBaseCustom.customTweakManifestMeasure ("Rate", mmtpDC);

		if (null == dcCustom)
			_mapCustomDC.put (strCustomName, dcBaseCustom);
		else
			_mapCustomDC.put (strCustomName, dcCustom);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve base()
	{
		return _dcBase;
	}

	@Override public org.drip.analytics.rates.DiscountCurve bumpUp()
	{
		return _dcBumpUp;
	}

	@Override public org.drip.analytics.rates.DiscountCurve bumpDown()
	{
		return _dcBumpDn;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			tenorBumpUp()
	{
		return _mapDCBumpUp;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			tenorBumpDown()
	{
		return _mapDCBumpDn;
	}
}
