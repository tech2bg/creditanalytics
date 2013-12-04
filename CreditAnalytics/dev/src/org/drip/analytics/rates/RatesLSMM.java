
package org.drip.analytics.rates;

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
 * RatesLSMM contains the Rates specific Latent State MM for the Rates Curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesLSMM extends org.drip.state.representation.LatentStateMetricMeasure {
	private org.drip.analytics.rates.TurnListDiscountFactor _tldf = null;

	/**
	 * RatesLSMM constructor
	 * 
	 * @param strID Name/ID of the Hidden State
	 * @param strQuantificationMetric The Quantification Metric of the Latent State
	 * @param strManifestMeasure Name of the Product Measure from which the Calibration/Inference estimates
	 * 	the Latent State's Quantification Metric
	 * @param dblMeasureQuoteValue Manifest Measure Quote Value
	 * @param tldf The Turn List Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public RatesLSMM (
		final java.lang.String strID,
		final java.lang.String strQuantificationMetric,
		final java.lang.String strManifestMeasure,
		final double dblMeasureQuoteValue,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
		throws java.lang.Exception
	{
		super (strID, strQuantificationMetric, strManifestMeasure, dblMeasureQuoteValue);

		_tldf = tldf;
	}

	/**
	 * Retrieve the Turn List Discount Factor
	 * 
	 * @return The Turn List Discount Factor
	 */

	public org.drip.analytics.rates.TurnListDiscountFactor turnsDiscount()
	{
		return _tldf;
	}
}
