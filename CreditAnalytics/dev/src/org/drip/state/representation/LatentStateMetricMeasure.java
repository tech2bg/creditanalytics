
package org.drip.state.representation;

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
	private java.lang.String _strManifestMeasure = "";
	private java.lang.String _strQuantificationMetric = "";
	private double _dblMeasureQuoteValue = java.lang.Double.NaN;

	/**
	 * LatentStateMetricMeasure constructor
	 * 
	 * @param strID Name/ID of the Hidden State
	 * @param strQuantificationMetric The Quantification Metric of the Latent State
	 * @param strManifestMeasure Name of the Product Measure from which the Calibration/Inference estimates
	 * 	the Latent State's Quantification Metric
	 * @param dblMeasureQuoteValue Manifest Measure Quote Value
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LatentStateMetricMeasure (
		final java.lang.String strID,
		final java.lang.String strQuantificationMetric,
		final java.lang.String strManifestMeasure,
		final double dblMeasureQuoteValue)
		throws java.lang.Exception
	{
		if (null == (_strID = strID) || _strID.isEmpty() || null == (_strQuantificationMetric =
			strQuantificationMetric) || _strQuantificationMetric.isEmpty() || null == (_strManifestMeasure =
				strManifestMeasure) || _strManifestMeasure.isEmpty() ||
					!org.drip.math.common.NumberUtil.IsValid (_dblMeasureQuoteValue = dblMeasureQuoteValue))
			throw new java.lang.Exception ("LatentStateMetricMeasure ctr: Invalid Inputs!");
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
	 * Retrieve the Product Manifest Measure
	 * 
	 * @return The Product Manifest Measure
	 */

	public java.lang.String getManifestMeasure()
	{
		return _strManifestMeasure;
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
