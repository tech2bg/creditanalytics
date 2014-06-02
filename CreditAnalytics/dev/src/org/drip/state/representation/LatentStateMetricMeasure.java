
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
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
		_mapManifestMeasureCalibQuote = null;

	/**
	 * LatentStateMetricMeasure constructor
	 * 
	 * @param strID Name/ID of the Hidden State
	 * @param strQuantificationMetric The Quantification Metric of the Latent State
	 * @param strManifestMeasure The Manifest Measure
	 * @param dblMeasureQuoteValue The Manifest Measure Quote Value
	 * 
	 * @return The LatentStateMetricMeasure Instance
	 */

	public static final LatentStateMetricMeasure Create (
		final java.lang.String strID,
		final java.lang.String strQuantificationMetric,
		final java.lang.String strManifestMeasure,
		final double dblMeasureQuoteValue)
	{
		if (null == strManifestMeasure || strManifestMeasure.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMeasureQuoteValue))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestMeasureCalibQuote =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapManifestMeasureCalibQuote.put (strManifestMeasure, dblMeasureQuoteValue);

		try {
			new LatentStateMetricMeasure (strID, strQuantificationMetric, mapManifestMeasureCalibQuote);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * LatentStateMetricMeasure constructor
	 * 
	 * @param strID Name/ID of the Hidden State
	 * @param strQuantificationMetric The Quantification Metric of the Latent State
	 * @param mapManifestMeasureCalibQuote Array of the Manifest Measure Calibration Quotes
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LatentStateMetricMeasure (
		final java.lang.String strID,
		final java.lang.String strQuantificationMetric,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
			mapManifestMeasureCalibQuote)
		throws java.lang.Exception
	{
		if (null == (_strID = strID) || _strID.isEmpty() || null == (_strQuantificationMetric =
			strQuantificationMetric) || _strQuantificationMetric.isEmpty() || null ==
				(_mapManifestMeasureCalibQuote = mapManifestMeasureCalibQuote) || 0 ==
					_mapManifestMeasureCalibQuote.size())
			throw new java.lang.Exception ("LatentStateMetricMeasure ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Latent State ID
	 * 
	 * @return The Latent State ID
	 */

	public java.lang.String id()
	{
		return _strID;
	}

	/**
	 * Retrieve the Latent State Quantification Metric
	 * 
	 * @return The Latent State Quantification Metric
	 */

	public java.lang.String quantificationMetric()
	{
		return _strQuantificationMetric;
	}

	/**
	 * Retrieve the Product Manifest Measure Array
	 * 
	 * @return The Product Manifest Measure Array
	 */

	public java.lang.String[] manifestMeasures()
	{
		java.lang.String[] astrManifestMeasure = new java.lang.String[_mapManifestMeasureCalibQuote.size()];

		int i = 0;

		for (java.lang.String strManifestMeasure : _mapManifestMeasureCalibQuote.keySet())
			astrManifestMeasure[i++] = strManifestMeasure;

		return astrManifestMeasure;
	}

	/**
	 * Retrieve the Manifest Measure Quote Value
	 * 
	 * @param The Manifest Measure
	 * 
	 * @return The Manifest Measure Quote Value
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public double measureQuoteValue (
		final java.lang.String strManifestMeasure)
		throws java.lang.Exception
	{
		if (null == strManifestMeasure || !_mapManifestMeasureCalibQuote.containsKey (strManifestMeasure))
			throw new java.lang.Exception
				("LatentStateMetricMeasure::measureQuoteValue => Invalid Manifest Measure");

		return _mapManifestMeasureCalibQuote.get (strManifestMeasure);
	}

	/**
	 * Retrieve the Entire Manifest Measure Quote Value Map
	 * 
	 * @return The Full Manifest Measure Quote Value Map
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> quoteMap()
	{
		return _mapManifestMeasureCalibQuote;
	}
}
