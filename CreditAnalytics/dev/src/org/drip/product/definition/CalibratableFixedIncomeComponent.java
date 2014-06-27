
package org.drip.product.definition;

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
 * CalibratableFixedIncomeComponent abstract class provides implementation of Component's calibration
 * 	interface. It exposes stubs for getting/setting the component’s calibration code, generate calibrated
 * 	measure values from the market inputs, and compute micro-Jacobians (QuoteDF and PVDF micro-Jacks).
 * 
 * @author Lakshmi Krishnamurthy
 */

public abstract class CalibratableFixedIncomeComponent extends
	org.drip.product.definition.FixedIncomeComponent {
	protected abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams);

	/**
	 * Return the primary code
	 * 
	 * @return Primary Code
	 */

	public abstract java.lang.String primaryCode();

	/**
	 * Set the component's primary code
	 * 
	 * @param strCode Primary Code
	 */

	public abstract void setPrimaryCode (
		final java.lang.String strCode);

	/**
	 * Get the component's secondary codes
	 * 
	 * @return Array of strings containing the secondary codes
	 */

	public java.lang.String[] secondaryCode()
	{
		java.lang.String strPrimaryCode = primaryCode();

		int iNumTokens = 0;
		java.lang.String astrCodeTokens[] = new java.lang.String[3];

		java.util.StringTokenizer stCodeTokens = new java.util.StringTokenizer (strPrimaryCode, ".");

		while (stCodeTokens.hasMoreTokens())
			astrCodeTokens[iNumTokens++] = stCodeTokens.nextToken();

		java.lang.String[] astrSecCode = new java.lang.String[2];
		astrSecCode[1] = astrCodeTokens[0] + "." + astrCodeTokens[1];
		astrSecCode[0] = astrCodeTokens[1];
		return astrSecCode;
	}

	/**
	 * Compute the Jacobian of the Dirty PV to the Calibrated Input Manifest Measures
	 * 
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param mktParams Component Market Parameters
	 * @param quotingParams Component Quoting Parameters
	 * 
	 * @return The micro-Jacobian
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams);

	/**
	 * Compute the micro-Jacobian of the given measure to the DF
	 * 
	 * @param strMainfestMeasure Manifest Measure Name
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param mktParams Component Market Parameters
	 * @param quotingParams Component Quoting Parameters
	 * 
	 * @return The micro-Jacobian
	 */

	public abstract org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strMainfestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams);

	/**
	 * Generate the Calibratable Linearized Predictor/Response Constraints for the Component from the Market
	 *  Inputs. The Constraints here typically correspond to Date/Cash Flow pairs and the corresponding PV.
	 * 
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param mktParams Component Market Parameters
	 * @param quotingParams Component Quoting Parameters
	 * @param lsmm The Latent State Metric and the Component Measure
	 * 
	 * @return The Calibratable Linearized Predictor/Response Constraints (Date/Cash Flow pairs and the
	 * 	corresponding PV)
	 */

	public abstract org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm);

	/**
	 * Return the last Date that is relevant for the Calibration
	 * 
	 * @return The Terminal Date
	 */

	public org.drip.analytics.date.JulianDate terminalDate()
	{
		return maturity();
	}
}
