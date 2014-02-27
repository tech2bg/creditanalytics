
package org.drip.state.representation;

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
 * LatentStateMetricMeasure holds the latent state that is estimated, its quantification metric, and the
 * 	corresponding product manifest measure, and its value that it is estimated off of during the calibration
 * 	run.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateMetricMeasure {

	/**
	 * Survival Latent State
	 */

	public static final java.lang.String LATENT_STATE_SURVIVAL = "LATENT_STATE_SURVIVAL";

	/**
	 * Survival Latent State Quantification Metric - Survival Probability
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_SURVIVAL_PROBABILITY =
		"QUANTIFICATION_METRIC_SURVIVAL_PROBABILITY";

	/**
	 * Discount Latent State Quantification Metric - Zero Hazard Rate
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_ZERO_HAZARD_RATE =
		"QUANTIFICATION_METRIC_ZERO_HAZARD_RATE";

	/**
	 * Discount Latent State Quantification Metric - Forward Hazard Rate
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_FORWARD_HAZARD_RATE =
		"QUANTIFICATION_METRIC_FORWARD_HAZARD_RATE";

	private java.lang.String _strID = "";
	private java.lang.String _strQuantificationMetric = "";
	private java.lang.String[] _astrManifestMeasure = null;
	private double _dblMeasureQuoteValue = java.lang.Double.NaN;

	/**
	 * LatentStateMetricMeasure constructor
	 * 
	 * @param strID Name/ID of the Hidden State
	 * @param strQuantificationMetric The Quantification Metric of the Latent State
	 * @param astrManifestMeasure Array of the Product Measure Names from which the Calibration/Inference
	 * 	estimates the Latent State's Quantification Metric
	 * @param dblMeasureQuoteValue Manifest Measure Quote Value
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LatentStateMetricMeasure (
		final java.lang.String strID,
		final java.lang.String strQuantificationMetric,
		final java.lang.String[] astrManifestMeasure,
		final double dblMeasureQuoteValue)
		throws java.lang.Exception
	{
		if (null == (_strID = strID) || _strID.isEmpty() || null == (_strQuantificationMetric =
			strQuantificationMetric) || _strQuantificationMetric.isEmpty() || null == (_astrManifestMeasure =
				astrManifestMeasure) || !org.drip.quant.common.NumberUtil.IsValid (_dblMeasureQuoteValue =
					dblMeasureQuoteValue))
			throw new java.lang.Exception ("LatentStateMetricMeasure ctr: Invalid Inputs!");

		int iNumManifestMeasure = _astrManifestMeasure.length;

		if (0 == iNumManifestMeasure)
			throw new java.lang.Exception ("LatentStateMetricMeasure ctr: Invalid Inputs!");

		for (int i = 0; i < iNumManifestMeasure; ++i) {
			if (null == _astrManifestMeasure[i] || _astrManifestMeasure[i].isEmpty())
				throw new java.lang.Exception ("LatentStateMetricMeasure ctr: Invalid Inputs!");
		}
	}

	/**
	 * Retrieve the Latent State ID
	 * 
	 * @return The Latent State ID
	 */

	public java.lang.String getID()
	{
		return _strID;
	}

	/**
	 * Retrieve the Latent State Quantification Metric
	 * 
	 * @return The Latent State Quantification Metric
	 */

	public java.lang.String getQuantificationMetric()
	{
		return _strQuantificationMetric;
	}

	/**
	 * Retrieve the Product Manifest Measure Array
	 * 
	 * @return The Product Manifest Measure Array
	 */

	public java.lang.String[] getManifestMeasures()
	{
		return _astrManifestMeasure;
	}

	/**
	 * Retrieve the Manifest Measure Quote Value
	 * 
	 * @return The Manifest Measure Quote Value
	 */

	public double getMeasureQuoteValue()
	{
		return _dblMeasureQuoteValue;
	}
}
