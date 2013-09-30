
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
 * ResponseValueConstraint holds the following set of fields that characterize a single global
 * 	linear constraint between the predictor and the response variables within a single segment, expressed
 *  linearly across the constituent nodes. Constraints are expressed as
 * 
 * 			C = Sigma_j [W_j * y_j] = V where
 * 
 * 	x_j => Predictor j
 * 	y_j => Response j
 * 	W_j => Weight at ordinate j
 * 	V => Value of the Constraint
 * 
 * ResponseValueConstraint can be viewed as the global response point value transform of
 *  ResponseBasisConstraint.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ResponseValueConstraint {

	/**
	 * Indicator specifying that the knot is to the left of the constraint ordinates
	 */

	public static final int LEFT_OF_CONSTRAINT = 1;

	/**
	 * Indicator specifying that the knot is to the right of the constraint ordinates
	 */

	public static final int RIGHT_OF_CONSTRAINT = 2;

	/**
	 * Indicator specifying that the knot splits the constraint ordinates
	 */

	public static final int SPLITS_CONSTRAINT = 4;

	private double[] _adblResponseWeight = null;
	private double[] _adblPredictorOrdinate = null;
	private double _dblConstraintValue = java.lang.Double.NaN;

	/**
	 * Generate a ResponseValueConstraint instance from the given predictor/response pair.
	 * 
	 * @param dblPredictor The Predictor
	 * @param dblResponse The Response
	 * 
	 * @return The ResponseValueConstraint instance
	 */

	public static final ResponseValueConstraint FromPredictorResponse (
		final double dblPredictor,
		final double dblResponse)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictor) ||
			!org.drip.math.common.NumberUtil.IsValid (dblResponse))
			return null;

		try {
			return new ResponseValueConstraint (new double[] {dblPredictor}, new double[] {1.},
				dblResponse);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ResponseValueConstraint constructor
	 * 
	 * @param adblPredictorOrdinate The Array of Global Predictor Ordinates
	 * @param adblResponseWeight The Array of the Weights to be applied to the Response at each Predictor
	 *  Ordinate
	 * @param dblConstraintValue The Constraint Value
	 * 
	 * @throws java.lang.Exception
	 */

	public ResponseValueConstraint (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseWeight,
		final double dblConstraintValue)
		throws java.lang.Exception
	{
		if (null == (_adblPredictorOrdinate = adblPredictorOrdinate) || null == (_adblResponseWeight =
			adblResponseWeight) || !org.drip.math.common.NumberUtil.IsValid (_dblConstraintValue =
				dblConstraintValue))
			throw new java.lang.Exception ("ResponseValueConstraint ctr: Invalid Inputs");

		int iNumPredictorOrdinate = adblPredictorOrdinate.length;

		if (0 == iNumPredictorOrdinate || _adblResponseWeight.length != iNumPredictorOrdinate)
			throw new java.lang.Exception ("ResponseValueConstraint ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Predictor Ordinates
	 * 
	 * @return The Array of Predictor Ordinates
	 */

	public double[] predictorOrdinates()
	{
		return _adblPredictorOrdinate;
	}

	/**
	 * Retrieve the Array of Response Weights at each Predictor Ordinate
	 * 
	 * @return The Array of Response Weights at each Predictor Ordinate
	 */

	public double[] responseWeights()
	{
		return _adblResponseWeight;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double getConstraintValue()
	{
		return _dblConstraintValue;
	}

	/**
	 * Convert the Segment Constraint onto Local Predictor Ordinates, the corresponding Response Basis
	 *  Function, and the Shape Controller Realizations
	 * 
	 * @param aAUResponseBasis Array of the Response Basis Functions
	 * @param rssc Shape Controller
	 * @param inel Inelastics transformer to convert coordinate space to Local from Global
	 * 
	 * @return The Segment Basis Function Constraint
	 */

	public org.drip.math.segment.ResponseBasisConstraint responseBasisConstraint (
		final org.drip.math.function.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.math.segment.ResponseScalingShapeController rssc,
		final org.drip.math.segment.Inelastics inel)
	{
		if (null == aAUResponseBasis || null == inel) return null;

		int iNumResponseBasis = aAUResponseBasis.length;
		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;
		double[] adblResponseBasisWeight = new double[iNumResponseBasis];
		double[] adblLocalPredictorOrdinate = new double[iNumPredictorOrdinate];

		if (0 == iNumResponseBasis) return null;

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			try {
				adblLocalPredictorOrdinate[i] = inel.localize (_adblPredictorOrdinate[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			for (int i = 0; i < iNumResponseBasis; ++i) {
				adblResponseBasisWeight[i] = 0.;

				for (int j = 0; j < iNumPredictorOrdinate; ++j)
					adblResponseBasisWeight[i] += _adblResponseWeight[j] * aAUResponseBasis[i].evaluate
						(adblLocalPredictorOrdinate[j]) * (null == rssc ? 1. :
							rssc.getShapeController().evaluate (rssc.isLocal() ?
								adblLocalPredictorOrdinate[j] : inel.delocalize
									(adblLocalPredictorOrdinate[j])));
			}

			return new org.drip.math.segment.ResponseBasisConstraint (adblResponseBasisWeight,
				_dblConstraintValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get the Position of the Predictor Knot relative to the Constraints
	 * 
	 * @param dblPredictorKnot The Predictor Knot Ordinate
	 * 
	 * @return Indicator specifying whether the Knot is Left of the constraints, Right of the Constraints, or
	 *  splits the Constraints
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public int knotPosition (
		final double dblPredictorKnot)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorKnot))
			throw new java.lang.Exception ("ResponseValueConstraint::knotPosition => Invalid Inputs");

		if (dblPredictorKnot < _adblPredictorOrdinate[0]) return LEFT_OF_CONSTRAINT;

		if (dblPredictorKnot > _adblPredictorOrdinate[_adblPredictorOrdinate.length - 1])
			return RIGHT_OF_CONSTRAINT;

		return SPLITS_CONSTRAINT;
	}
}
