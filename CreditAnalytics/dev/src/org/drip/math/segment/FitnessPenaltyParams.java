
package org.drip.math.segment;

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
 * FitnessPenaltyParams implements basis per-segment Fitness Penalty Parameter Set. Currently it contains the
 *  Fitness Penalty Weight Grid Matrix and the Segment Local Fitness Match Set.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FitnessPenaltyParams {
	private double[] _adblWeight = null;
	private double[] _adblResponse = null;
	private double[] _adblLocalPredictorOrdinate = null;

	/**
	 * Construct the FitnessPenaltyParams Instance from the given Inputs
	 * 
	 * @param adblLocalPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponse Array of Response Values
	 * @param adblWeight Array of Weights
	 * 
	 * @return Instance of FitnessPenaltyParams
	 */

	public static final FitnessPenaltyParams Create (
		final double[] adblLocalPredictorOrdinate,
		final double[] adblResponse,
		final double[] adblWeight)
	{
		FitnessPenaltyParams frp = null;

		try {
			frp = new FitnessPenaltyParams (adblWeight, adblResponse, adblLocalPredictorOrdinate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return frp.normalizeWeights() ? frp : null;
	}

	/**
	 * Construct the FitnessPenaltyParams Instance from the given Inputs
	 * 
	 * @param adblLocalPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponse Array of Response Values
	 * 
	 * @return Instance of FitnessPenaltyParams
	 */

	public static final FitnessPenaltyParams Create (
		final double[] adblLocalPredictorOrdinate,
		final double[] adblResponse)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (adblLocalPredictorOrdinate)) return null;

		int iNumWeight = adblLocalPredictorOrdinate.length;
		double[] adblWeight = new double[iNumWeight];

		for (int i = 0; i < iNumWeight; ++i)
			adblWeight[i] = 1.;

		return Create (adblLocalPredictorOrdinate, adblResponse, adblWeight);
	}

	private FitnessPenaltyParams (
		final double[] adblWeight,
		final double[] adblResponse,
		final double[] adblLocalPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (_adblWeight = adblWeight) ||
			!org.drip.math.common.NumberUtil.IsValid (_adblResponse = adblResponse) ||
				!org.drip.math.common.NumberUtil.IsValid (_adblLocalPredictorOrdinate =
					adblLocalPredictorOrdinate))
			throw new java.lang.Exception ("FitnessPenaltyParams ctr: Invalid Inputs");

		int iNumPointsToFit = _adblWeight.length;

		if (0 == iNumPointsToFit || _adblResponse.length != iNumPointsToFit ||
			_adblLocalPredictorOrdinate.length != iNumPointsToFit)
			throw new java.lang.Exception ("FitnessPenaltyParams ctr: Invalid Inputs");
	}

	private boolean normalizeWeights()
	{
		double dblCumulativeWeight = 0.;
		int iNumPointsToFit = _adblWeight.length;

		for (int i = 0; i < iNumPointsToFit; ++i) {
			if (_adblWeight[i] < 0.) return false;

			dblCumulativeWeight += _adblWeight[i];
		}

		if (0. >= dblCumulativeWeight) return false;

		for (int i = 0; i < iNumPointsToFit; ++i)
			_adblWeight[i] /= dblCumulativeWeight;

		return true;
	}

	/**
	 * Retrieve the Array of the Fitness Weights
	 * 
	 * @return The Array of the Fitness Weights
	 */

	public double[] weight()
	{
		return _adblWeight;
	}

	/**
	 * Retrieve the Indexed Fitness Weight Element
	 * 
	 * @return The Indexed Fitness Weight Element
	 * 
	 * @throws java.lang.Exception Thrown if the Index is Invalid
	 */

	public double weight (
		final int iIndex)
		throws java.lang.Exception
	{
		if (iIndex >= numPoint())
			throw new java.lang.Exception ("FitnessPenaltyParams::weight => Invalid Index");

		return _adblWeight[iIndex];
	}

	/**
	 * Retrieve the Array of Local Predictor Ordinates
	 * 
	 * @return The Array of Local Predictor Ordinates
	 */

	public double[] predictorOrdinate()
	{
		return _adblLocalPredictorOrdinate;
	}

	/**
	 * Retrieve the Indexed Predictor Ordinate Element
	 * 
	 * @return The Indexed Predictor Ordinate Element
	 * 
	 * @throws java.lang.Exception Thrown if the Index is Invalid
	 */

	public double predictorOrdinate (
		final int iIndex)
		throws java.lang.Exception
	{
		if (iIndex >= numPoint())
			throw new java.lang.Exception ("FitnessPenaltyParams::predictorOrdinate => Invalid Index");

		return _adblLocalPredictorOrdinate[iIndex];
	}

	/**
	 * Retrieve the Array of Responses
	 * 
	 * @return The Array of Responses
	 */

	public double[] response()
	{
		return _adblResponse;
	}

	/**
	 * Retrieve the Indexed Response Element
	 * 
	 * @return The Indexed Response Element
	 * 
	 * @throws java.lang.Exception Thrown if the Index is Invalid
	 */

	public double response (
		final int iIndex)
		throws java.lang.Exception
	{
		if (iIndex >= numPoint())
			throw new java.lang.Exception ("FitnessPenaltyParams::response => Invalid Index");

		return _adblResponse[iIndex];
	}

	/**
	 * Retrieve the Number of Fitness Points
	 * 
	 * @return The Number of Fitness Points
	 */

	public int numPoint()
	{
		return null == _adblResponse ? 0 : _adblResponse.length;
	}
}
