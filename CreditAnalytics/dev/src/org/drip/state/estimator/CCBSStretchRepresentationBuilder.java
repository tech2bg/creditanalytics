
package org.drip.state.estimator;

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
 * CCBSStretchRepresentationBuilder contains the Representation Spec Creation Routines for Forward Curve
 * 	Stretches to be built out of CCBS Quotes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CCBSStretchRepresentationBuilder {

	/**
	 * Construct an instance of StretchRepresentationSpec for the Construction of the Forward Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param bmp The Basket Market Parameters to imply the Market Quote Measure
	 * @param adblReferenceComponentBasis Array of the Reference Component Reference Leg Basis Spread
	 * @param bBasisOnDerivedLeg TRUE => Apply the Basis on the Derived Leg (FALSE => Reference Leg)
	 * 
	 * return Instance of StretchRepresentationSpec
	 */

	public static final StretchRepresentationSpec ForwardCurveSRS (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.fx.CrossCurrencyComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final double[] adblReferenceComponentBasis,
		final boolean bBasisOnDerivedLeg)
	{
		if (null == aCCSP || null == bmp || null == adblReferenceComponentBasis) return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP || adblReferenceComponentBasis.length != iNumCCSP) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumCCSP];

		java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuote = new
				java.util.ArrayList<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			aCalibComp[i] = aCCSP[i].derivedComponent();

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = aCCSP[i].value
				(valParams, null, bmp, null);

			org.drip.product.definition.RatesComponent rcReference = aCCSP[i].referenceComponent();

			java.lang.String strReferenceComponentName = rcReference.componentName();

			double dblFX = java.lang.Double.NaN;
			java.lang.String strReferenceComponentPV = strReferenceComponentName + "[PV]";
			java.lang.String strReferenceComponentReferenceLegCleanDV01 = strReferenceComponentName +
				"[ReferenceCleanDV01]";
			java.lang.String strReferenceComponentDerivedLegCleanDV01 = strReferenceComponentName +
				"[DerivedCleanDV01]";

			if (null == mapOP || !mapOP.containsKey (strReferenceComponentPV) || !mapOP.containsKey
				(strReferenceComponentReferenceLegCleanDV01) || !mapOP.containsKey
					(strReferenceComponentDerivedLegCleanDV01))
				return null;

			org.drip.quant.function1D.AbstractUnivariate auFX = bmp.fxCurve (aCCSP[i].fxCode());

			if (null == auFX) return null;

			try {
				dblFX = auFX.evaluate (aCalibComp[i].effective().getJulian());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestQuote = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			mapManifestQuote.put ("PV", -1. * dblFX * (mapOP.get (strReferenceComponentPV) + 10000. *
				(bBasisOnDerivedLeg ? mapOP.get (strReferenceComponentDerivedLegCleanDV01) : mapOP.get
					(strReferenceComponentReferenceLegCleanDV01)) * adblReferenceComponentBasis[i]));

			lsMapManifestQuote.add (mapManifestQuote);
		}

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aCalibComp, lsMapManifestQuote, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of StretchRepresentationSpec for the Construction of the Discount Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param bmp The Basket Market Parameters to imply the Market Quote Measure
	 * @param adblReferenceComponentBasis Array of the Reference Component Reference Leg Basis Spread
	 * @param adblSwapRate Array of the IRS Calibration Swap Rates
	 * @param bBasisOnDerivedLeg TRUE => Apply the Basis on the Derived Leg (FALSE => Reference Leg)
	 * 
	 * return Instance of StretchRepresentationSpec
	 */

	public static final StretchRepresentationSpec DiscountCurveSRS (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.fx.CrossCurrencyComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final double[] adblReferenceComponentBasis,
		final double[] adblSwapRate,
		final boolean bBasisOnDerivedLeg)
	{
		if (null == aCCSP || null == bmp || null == adblReferenceComponentBasis || null == adblSwapRate)
			return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP || adblReferenceComponentBasis.length != iNumCCSP || adblSwapRate.length !=
			iNumCCSP)
			return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumCCSP];

		java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuote = new
				java.util.ArrayList<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			aCalibComp[i] = aCCSP[i].derivedComponent();

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = aCCSP[i].value
				(valParams, null, bmp, null);

			org.drip.product.definition.RatesComponent rcReference = aCCSP[i].referenceComponent();

			java.lang.String strReferenceComponentName = rcReference.componentName();

			double dblFX = java.lang.Double.NaN;
			java.lang.String strReferenceComponentPV = strReferenceComponentName + "[PV]";
			java.lang.String strReferenceComponentReferenceLegCleanDV01 = strReferenceComponentName +
				"[ReferenceCleanDV01]";
			java.lang.String strReferenceComponentDerivedLegCleanDV01 = strReferenceComponentName +
				"[DerivedCleanDV01]";

			if (null == mapOP || !mapOP.containsKey (strReferenceComponentPV) || !mapOP.containsKey
				(strReferenceComponentReferenceLegCleanDV01) || !mapOP.containsKey
					(strReferenceComponentDerivedLegCleanDV01))
				return null;

			org.drip.quant.function1D.AbstractUnivariate auFX = bmp.fxCurve (aCCSP[i].fxCode());

			if (null == auFX) return null;

			try {
				dblFX = auFX.evaluate (aCalibComp[i].effective().getJulian());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestQuote = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			mapManifestQuote.put ("Rate", adblSwapRate[i]);

			mapManifestQuote.put ("Upfront", -1. * dblFX * (mapOP.get (strReferenceComponentPV) + 10000. *
				(bBasisOnDerivedLeg ? mapOP.get (strReferenceComponentDerivedLegCleanDV01) : mapOP.get
					(strReferenceComponentReferenceLegCleanDV01)) * adblReferenceComponentBasis[i]));

			lsMapManifestQuote.add (mapManifestQuote);
		}

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aCalibComp, lsMapManifestQuote, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
