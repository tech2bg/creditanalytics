
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * ExplicitBootDiscountCurve exposes the functionality associated with the bootstrapped Discount Curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ExplicitBootDiscountCurve extends org.drip.analytics.definition.DiscountCurve
	implements org.drip.analytics.definition.ExplicitBootCurve {

	protected ExplicitBootDiscountCurve (
		final double dblEpochDate,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		super (dblEpochDate, strCurrency);
	}

	/**
	 * Create a shifted curve from an array of basis shifts
	 * 
	 * @param adblDate Array of dates
	 * @param adblBasis Array of basis
	 * 
	 * @return Discount Curve
	 */

	public abstract ExplicitBootDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis);

	/**
	 * Retrieve the Latent State Quantification Metric
	 * 
	 * @return The Latent State Quantification Metric
	 */

	public abstract java.lang.String latentStateQuantificationMetric();

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		org.drip.product.definition.CalibratableComponent[] aCalibComp = calibComp();

		if (null == aCalibComp) return null;

		int iNumLSMM = aCalibComp.length;
		org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = new
			org.drip.state.representation.LatentStateMetricMeasure[iNumLSMM];

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> mapMeasure = _ccis.getMeasure();

		for (int i = 0; i < iNumLSMM; ++i) {
			java.lang.String strInstrumentCode = _ccis.getComponent()[i].getPrimaryCode();

			try {
				aLSMM[i] = new org.drip.state.representation.LatentStateMetricMeasure
					(org.drip.analytics.definition.DiscountCurve.LATENT_STATE_DISCOUNT,
						latentStateQuantificationMetric(), mapMeasure.get (strInstrumentCode),
							mapQuote.get (strInstrumentCode));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aLSMM;
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		return null != (_ccis = ccis);
	}

	@Override public org.drip.product.definition.CalibratableComponent[] calibComp()
	{
		return null == _ccis ? null : _ccis.getComponent();
	}

	@Override public double manifestMeasure (
		final java.lang.String strInstrumentCode)
		throws java.lang.Exception
	{
		if (null == _ccis)
			throw new java.lang.Exception ("ExplicitBootDiscountCurve::getManifestMeasure => Cannot get " +
				strInstrumentCode);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		if (null == mapQuote || !mapQuote.containsKey (strInstrumentCode))
			throw new java.lang.Exception ("ExplicitBootDiscountCurve::getManifestMeasure => Cannot get " +
				strInstrumentCode);

		return mapQuote.get (strInstrumentCode);
	}
}
