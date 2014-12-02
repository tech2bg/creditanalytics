
package org.drip.param.market;

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
 * CreditCurveScenarioContainer contains the place holder for the bump parameters and the curves for the
 * 	different credit curve scenarios. Contains the spread and the recovery bumps, and the credit curve
 * 	scenario generator object that wraps the calibration instruments. It also contains the base credit curve,
 *  spread bumped up/down credit curves, recovery bumped up/down credit curves, and the tenor mapped up/down
 *  credit curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditCurveScenarioContainer extends org.drip.param.definition.ScenarioCreditCurve {
	private static final boolean s_bBlog = false;

	private double _dblCouponBump = 0.0001;
	private double _dblRecoveryBump = 0.01;
	private org.drip.analytics.definition.CreditCurve _ccBase = null;
	private org.drip.analytics.definition.CreditCurve _ccBumpUp = null;
	private org.drip.analytics.definition.CreditCurve _ccBumpDn = null;
	private org.drip.analytics.definition.CreditCurve _ccRecoveryUp = null;
	private org.drip.analytics.definition.CreditCurve _ccRecoveryDn = null;
	private org.drip.state.estimator.CreditCurveScenarioGenerator _ccsg = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapCustomCC = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapTenorCCBumpUp = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapTenorCCBumpDn = null;

	/**
	 * Construct CreditCurveScenarioContainer from the array of calibration instruments, the coupon bump
	 * 	parameter, and the recovery bump parameter
	 * 
	 * @param aCalibInst Array of calibration instruments
	 * @param dblCouponBump Coupon Bump
	 * @param dblRecoveryBump Recovery Bump
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public CreditCurveScenarioContainer (
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst,
		final double dblCouponBump,
		final double dblRecoveryBump)
		throws java.lang.Exception
	{
		if (null == aCalibInst || 0 == aCalibInst.length || !org.drip.quant.common.NumberUtil.IsValid
			(_dblCouponBump = dblCouponBump) || !org.drip.quant.common.NumberUtil.IsValid (_dblRecoveryBump =
				dblRecoveryBump) || null == (_ccsg = new
					org.drip.state.estimator.CreditCurveScenarioGenerator (aCalibInst)))
			throw new java.lang.Exception ("CreditCurveScenarioContainer ctr => Invalid Inputs!");
	}

	@Override public boolean cookScenarioCC (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final double dblRecovery,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat,
		final int iCCScenario)
	{
		if (null == _ccsg || null == dc || null == adblQuotes || 0 == adblQuotes.length ||
			!org.drip.quant.common.NumberUtil.IsValid (dblRecovery) || null == astrCalibMeasure || 0 ==
				astrCalibMeasure.length || astrCalibMeasure.length != adblQuotes.length) {
			if (s_bBlog)
				System.out.println ("Bad CreditCurveScenarioContainer.cookScenarioCC Input params!");

			return false;
		}

		if (null == (_ccBase = _ccsg.createCC (strName, valParams, dc, dcTSY, adblQuotes, dblRecovery,
			astrCalibMeasure, lsfc, quotingParams, bFlat))) {
			if (s_bBlog)
				System.out.println ("CreditCurveScenarioContainer.cookScenarioCC => Bad ccBase Cook!");

			return false;
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_FLAT_UP & iCCScenario)) {
			if (null == (_ccBumpUp = _ccsg.createCC (strName, valParams, dc, dcTSY,
				org.drip.analytics.support.AnalyticsHelper.BumpQuotes (adblQuotes, _dblCouponBump, false),
					dblRecovery, astrCalibMeasure, lsfc, quotingParams, bFlat))) {
				if (s_bBlog)
					System.out.println ("CreditCurveScenarioContainer.cookScenarioCC => Bad ccBumpUp Cook!");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_FLAT_DN & iCCScenario)) {
			if (null == (_ccBumpDn = _ccsg.createCC (strName, valParams, dc, dcTSY,
				org.drip.analytics.support.AnalyticsHelper.BumpQuotes (adblQuotes, -_dblCouponBump,
					false), dblRecovery, astrCalibMeasure, lsfc, quotingParams, bFlat))) {
				if (s_bBlog)
					System.out.println ("CreditCurveScenarioContainer.cookScenarioCC => Bad ccBumpDn Cook!");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_TENOR_UP & iCCScenario)) {
			if (null == (_mapTenorCCBumpUp = _ccsg.createTenorCCMap (strName, valParams, dc, dcTSY,
				adblQuotes, _dblCouponBump, dblRecovery, astrCalibMeasure, lsfc, quotingParams, bFlat)))
			{
				if (s_bBlog)
					System.out.println
						("CreditCurveScenarioContainer.cookScenarioCC => Bad ccPartialUp Cook!");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_TENOR_DN & iCCScenario)) {
			if (null == (_mapTenorCCBumpDn = _ccsg.createTenorCCMap (strName, valParams, dc, dcTSY,
				adblQuotes, -_dblCouponBump, dblRecovery, astrCalibMeasure, lsfc, quotingParams, bFlat))) {
				if (s_bBlog)
					System.out.println
						("CreditCurveScenarioContainer.cookScenarioCC => Bad ccPartialDn Cook!");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_RR_FLAT_UP & iCCScenario)) {
			if (null == (_ccRecoveryUp = _ccsg.createCC (strName, valParams, dc, dcTSY, adblQuotes,
				dblRecovery + _dblRecoveryBump, astrCalibMeasure, lsfc, quotingParams, bFlat))) {
				if (s_bBlog)
					System.out.println ("CreditCurveScenarioContainer.cookScenarioCC => Bad ccRRUp Cook!");

				return false;
			}
		}

		if (0 != (org.drip.param.definition.ScenarioCreditCurve.CC_RR_FLAT_DN & iCCScenario)) {
			if (null == (_ccRecoveryDn = _ccsg.createCC (strName, valParams, dc, dcTSY, adblQuotes,
				dblRecovery - _dblRecoveryBump, astrCalibMeasure, lsfc, quotingParams, bFlat))) {
				if (s_bBlog)
					System.out.println ("CreditCurveScenarioContainer.cookScenarioCC => Bad ccRRDn Cook!");

				return false;
			}
		}

		return true;
	}

	@Override public boolean cookCustomCC (
		final java.lang.String strName,
		final java.lang.String strCustomName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final double[] adblQuotes,
		final double dblRecovery,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat,
		final org.drip.param.definition.ResponseValueTweakParams mmtpDC,
		final org.drip.param.definition.ResponseValueTweakParams mmtpTSY,
		final org.drip.param.definition.ResponseValueTweakParams mmtpCC)
	{
		if (null == strCustomName || strCustomName.isEmpty() || null == _ccsg || null == dc || null ==
			adblQuotes || 0 == adblQuotes.length || !org.drip.quant.common.NumberUtil.IsValid (dblRecovery) ||
				null == astrCalibMeasure || 0 == astrCalibMeasure.length || astrCalibMeasure.length !=
					adblQuotes.length || (null == mmtpDC && null == mmtpTSY && null == mmtpCC)) {
			if (s_bBlog)
				System.out.println ("CreditCurveScenarioContainer.cookCustomCC => Bad Input params!");

			return false;
		}

		org.drip.analytics.rates.DiscountCurve dcAdj = (org.drip.analytics.rates.DiscountCurve)
			dc.customTweakManifestMeasure ("Rate", mmtpDC);

		org.drip.analytics.rates.DiscountCurve dcTSYAdj = (org.drip.analytics.rates.DiscountCurve)
			dcTSY.customTweakManifestMeasure ("Rate", mmtpTSY);

		org.drip.analytics.definition.CreditCurve ccBaseCustom = _ccsg.createCC (strName, valParams, null ==
			dcAdj ? dc : dcAdj, null == dcTSYAdj ? dcTSY : dcTSYAdj, adblQuotes, dblRecovery,
				astrCalibMeasure, lsfc, quotingParams, bFlat);

		if (null == ccBaseCustom) {
			if (s_bBlog)
				System.out.println
					("CreditCurveScenarioContainer.cookCustomCC => Cannot create ccBaseCustom!");

			return false;
		}

		if (null == _mapCustomCC)
			_mapCustomCC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		org.drip.analytics.definition.CreditCurve ccCustom = (org.drip.analytics.definition.CreditCurve)
			ccBaseCustom.customTweakManifestMeasure ("Rate", mmtpCC);

		if (null == ccCustom)
			_mapCustomCC.put (strCustomName, ccBaseCustom);
		else
			_mapCustomCC.put (strCustomName, ccCustom);

		return true;
	}

	@Override public org.drip.analytics.definition.CreditCurve base()
	{
		return _ccBase;
	}

	@Override public org.drip.analytics.definition.CreditCurve bumpUp()
	{
		return _ccBumpUp;
	}

	@Override public org.drip.analytics.definition.CreditCurve bumpDown()
	{
		return _ccBumpDn;
	}

	@Override public org.drip.analytics.definition.CreditCurve bumpRecoveryUp()
	{
		return _ccRecoveryUp;
	}

	@Override public org.drip.analytics.definition.CreditCurve bumpRecoveryDown()
	{
		return _ccRecoveryDn;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			tenorBumpUp()
	{
		return _mapTenorCCBumpUp;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			tenorBumpDown()
	{
		return _mapTenorCCBumpDn;
	}
}
