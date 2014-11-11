
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

public class CCBSStretchBuilder {

	/**
	 * Construct an instance of LatentStateStretchSpec for the Construction of the Forward Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param mktParams The Basket Market Parameters to imply the Market Quote Measure
	 * @param adblReferenceComponentBasis Array of the Reference Component Reference Leg Basis Spread
	 * @param bBasisOnDerivedLeg TRUE => Apply the Basis on the Derived Leg (FALSE => Reference Leg)
	 * 
	 * return Instance of LatentStateStretchSpec
	 */

	public static final org.drip.state.inference.LatentStateStretchSpec ForwardStretch (
		final java.lang.String strName,
		final org.drip.product.fx.ComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet mktParams,
		final double[] adblReferenceComponentBasis,
		final boolean bBasisOnDerivedLeg)
	{
		if (null == aCCSP || null == mktParams || null == adblReferenceComponentBasis) return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP || adblReferenceComponentBasis.length != iNumCCSP) return null;

		org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = new
			org.drip.state.inference.LatentStateSegmentSpec[iNumCCSP];

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			org.drip.product.calib.ProductQuoteSet pqs = null;
			org.drip.state.identifier.ForwardLabel forwardLabel = null;

			org.drip.product.definition.CalibratableFixedIncomeComponent comp = aCCSP[i].derivedComponent();

			if (comp instanceof org.drip.product.rates.DualStreamComponent)
				forwardLabel = ((org.drip.product.rates.DualStreamComponent)
					comp).derivedStream().forwardLabel();
			else {
				org.drip.state.identifier.ForwardLabel[] aForwardLabel =  comp.forwardLabel();

				if (null != aForwardLabel && 0 != aForwardLabel.length) forwardLabel = aForwardLabel[0];
			}

			try { 
				pqs = comp.calibQuoteSet (new org.drip.state.representation.LatentStateSpecification[] {new
					org.drip.state.representation.LatentStateSpecification
						(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FORWARD,
							org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE,
								forwardLabel)});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = aCCSP[i].value
				(valParams, null, mktParams, null);

			org.drip.product.definition.CalibratableFixedIncomeComponent rcReference =
				aCCSP[i].referenceComponent();

			java.lang.String strReferenceComponentName = rcReference.name();

			java.lang.String strReferenceComponentPV = strReferenceComponentName + "[PV]";
			java.lang.String strReferenceComponentReferenceLegCleanDV01 = strReferenceComponentName +
				"[ReferenceCleanDV01]";
			java.lang.String strReferenceComponentDerivedLegCleanDV01 = strReferenceComponentName +
				"[DerivedCleanDV01]";

			if (null == mapOP || !mapOP.containsKey (strReferenceComponentPV) || !mapOP.containsKey
				(strReferenceComponentReferenceLegCleanDV01) || !mapOP.containsKey
					(strReferenceComponentDerivedLegCleanDV01))
				return null;

			if (!pqs.set ("PV", -1. * (mapOP.get (strReferenceComponentPV) + 10000. * (bBasisOnDerivedLeg ?
				mapOP.get (strReferenceComponentDerivedLegCleanDV01) : mapOP.get
					(strReferenceComponentReferenceLegCleanDV01)) * adblReferenceComponentBasis[i])))
				return null;

			try {
				aSegmentSpec[i] = new org.drip.state.inference.LatentStateSegmentSpec (comp, pqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.state.inference.LatentStateStretchSpec (strName, aSegmentSpec);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of LatentStateStretchSpec for the Construction of the Discount Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param mktParams The Basket Market Parameters to imply the Market Quote Measure
	 * @param adblReferenceComponentBasis Array of the Reference Component Reference Leg Basis Spread
	 * @param adblSwapRate Array of the IRS Calibration Swap Rates
	 * @param bBasisOnDerivedLeg TRUE => Apply the Basis on the Derived Leg (FALSE => Reference Leg)
	 * 
	 * return Instance of LatentStateStretchSpec
	 */

	public static final org.drip.state.inference.LatentStateStretchSpec DiscountStretch (
		final java.lang.String strName,
		final org.drip.product.fx.ComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet mktParams,
		final double[] adblReferenceComponentBasis,
		final double[] adblSwapRate,
		final boolean bBasisOnDerivedLeg)
	{
		if (null == aCCSP || null == mktParams || null == adblReferenceComponentBasis || null ==
			adblSwapRate)
			return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP || adblReferenceComponentBasis.length != iNumCCSP || adblSwapRate.length !=
			iNumCCSP)
			return null;

		org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = new
			org.drip.state.inference.LatentStateSegmentSpec[iNumCCSP];

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			org.drip.product.definition.CalibratableFixedIncomeComponent compDerived =
				aCCSP[i].derivedComponent();

			org.drip.product.definition.CalibratableFixedIncomeComponent compReference =
				aCCSP[i].referenceComponent();

			double dblFX = 1.;
			org.drip.product.calib.ProductQuoteSet pqs = null;
			org.drip.state.identifier.ForwardLabel forwardLabel = null;
			org.drip.state.identifier.FundingLabel fundingLabel = null;

			if (compDerived instanceof org.drip.product.rates.DualStreamComponent) {
				org.drip.product.rates.Stream streamDerived = ((org.drip.product.rates.DualStreamComponent)
					compDerived).derivedStream();

				forwardLabel = streamDerived.forwardLabel();

				fundingLabel = streamDerived.fundingLabel();
			} else {
				org.drip.state.identifier.ForwardLabel[] aForwardLabel = compDerived.forwardLabel();

				org.drip.state.identifier.FundingLabel[] aFundingLabel = compDerived.fundingLabel();

				if (null != aForwardLabel && 0 != aForwardLabel.length) forwardLabel = aForwardLabel[0];

				if (null != aFundingLabel && 0 != aFundingLabel.length) fundingLabel = aFundingLabel[0];
			}

			try { 
				pqs = compDerived.calibQuoteSet (new org.drip.state.representation.LatentStateSpecification[]
					{new org.drip.state.representation.LatentStateSpecification
						(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
							org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
								fundingLabel), new org.drip.state.representation.LatentStateSpecification
									(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FORWARD,
										org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE,
					forwardLabel)});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			java.lang.String strDerivedCurrency = compDerived.payCurrency()[0];

			java.lang.String strReferenceCurrency = compReference.payCurrency()[0];

			if (!strDerivedCurrency.equalsIgnoreCase (strReferenceCurrency)) {
				org.drip.quant.function1D.AbstractUnivariate auFX = mktParams.fxCurve
					(org.drip.state.identifier.FXLabel.Standard (strDerivedCurrency + "/" +
						strReferenceCurrency));

				if (null == auFX) return null;

				try {
					dblFX = auFX.evaluate (compDerived.effective().julian());
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = compReference.value
				(valParams, null, mktParams, null);

			if (null == mapOP || !mapOP.containsKey ("PV") || !mapOP.containsKey ("ReferenceCleanDV01"))
				return null;

			if (!pqs.set ("SwapRate", adblSwapRate[i]) || !pqs.set ("PV", -1. * dblFX * (mapOP.get ("PV") +
				10000. * mapOP.get ("ReferenceCleanDV01") * adblReferenceComponentBasis[i])))
				return null;

			try {
				aSegmentSpec[i] = new org.drip.state.inference.LatentStateSegmentSpec (compDerived, pqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.state.inference.LatentStateStretchSpec (strName, aSegmentSpec);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
