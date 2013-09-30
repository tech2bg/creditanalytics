
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
 * PredictorOrdinateResponseDerivative contains the segment local parameters used for the segment calibration. It
 * 	holds the edge Y value and the derivatives.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PredictorOrdinateResponseDerivative {
	private double _dblResponse = java.lang.Double.NaN;
	private double[] _adblDResponseDPredictorOrdinate = null;

	/**
	 * Aggregate the 2 Predictor Ordinate Response Derivatives by applying the Cardinal Tension Weight
	 * 
	 * @param pordA Predictor Ordinate Response Derivative A
	 * @param pordB Predictor Ordinate Response Derivative B
	 * @param dblCardinalTension Cardinal Tension
	 * 
	 * @return The Aggregated Predictor Ordinate Response Derivatives
	 */

	public static final PredictorOrdinateResponseDerivative CardinalEdgeAggregate (
		final org.drip.math.segment.PredictorOrdinateResponseDerivative pordA,
		final org.drip.math.segment.PredictorOrdinateResponseDerivative pordB,
		final double dblCardinalTension)
	{
		if (null == pordA || null == pordB || !org.drip.math.common.NumberUtil.IsValid (dblCardinalTension))
			return null;

		int iNumDeriv = 0;

		double[] adblEdgeDResponseDPredictorOrdinateA = pordA.getDResponseDPredictorOrdinate();

		double[] adblEdgeDResponseDPredictorOrdinateB = pordB.getDResponseDPredictorOrdinate();

		if ((null != adblEdgeDResponseDPredictorOrdinateA && null == adblEdgeDResponseDPredictorOrdinateB) ||
			(null == adblEdgeDResponseDPredictorOrdinateA && null != adblEdgeDResponseDPredictorOrdinateB) ||
				(null != adblEdgeDResponseDPredictorOrdinateA && null != adblEdgeDResponseDPredictorOrdinateB
					&& (iNumDeriv = adblEdgeDResponseDPredictorOrdinateA.length) !=
						adblEdgeDResponseDPredictorOrdinateB.length))
			return null;

		double dblAggregatedEdgeResponse = 0.5 * (1. - dblCardinalTension) * (pordA.response() +
			pordB.response());

		if (null == adblEdgeDResponseDPredictorOrdinateA || null == adblEdgeDResponseDPredictorOrdinateB || 0
			== iNumDeriv) {
			try {
				return new PredictorOrdinateResponseDerivative (dblAggregatedEdgeResponse, null);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		double[] adblEdgeDResponseDPredictorOrdinate = new double[iNumDeriv];

		for (int i = 0; i < iNumDeriv; ++i)
			adblEdgeDResponseDPredictorOrdinate[i] = 0.5 * (1. - dblCardinalTension) *
				(adblEdgeDResponseDPredictorOrdinateA[i] + adblEdgeDResponseDPredictorOrdinateB[i]);

		try {
			return new PredictorOrdinateResponseDerivative (dblAggregatedEdgeResponse,
				adblEdgeDResponseDPredictorOrdinate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * PredictorOrdinateResponseDerivative constructor
	 * 
	 * @param dblResponse Edge Response Value
	 * @param adblDResponseDPredictorOrdinate Array of ordered Edge Derivatives
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public PredictorOrdinateResponseDerivative (
		final double dblResponse,
		final double[] adblDResponseDPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (_dblResponse = dblResponse))
			throw new java.lang.Exception ("PredictorOrdinateResponseDerivative ctr: Ivalid Inputs!");

		_adblDResponseDPredictorOrdinate = adblDResponseDPredictorOrdinate;
	}

	/**
	 * Retrieve the Response
	 * 
	 * @return The Response
	 */

	public double response()
	{
		return _dblResponse;
	}

	/**
	 * Retrieve the DResponseDPredictorOrdinate Array
	 * 
	 * @return DResponseDPredictorOrdinate Array
	 */

	public double[] getDResponseDPredictorOrdinate()
	{
		return _adblDResponseDPredictorOrdinate;
	}
}
