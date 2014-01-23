
package org.drip.spline.params;

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
 * SegmentResponseValueConstraint holds the following set of fields that characterize a single global
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
 * SegmentResponseValueConstraint exports the following functionality:
 * 	- Retrieve the Array of Predictor Ordinates.
 * 	- Retrieve the Array of Response Weights at each Predictor Ordinate.
 * 	- Retrieve the Constraint Value.
 * 	- Convert the Segment Constraint onto Local Predictor Ordinates, the corresponding Response Basis
 * 		Function, and the Shape Controller Realizations.
 * 	- Get the Position of the Predictor Knot relative to the Constraints.
 * 	- Generate a SegmentResponseValueConstraint instance from the given predictor/response pair.
 * 
 * SegmentResponseValueConstraint can be viewed as the global response point value transform of
 *  SegmentBasisFlexureConstraint.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentResponseValueConstraint {

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

	private double[] _adblPredictorOrdinate = null;
	private double[] _adblResponseValueWeight = null;
	private double _dblWeightedResponseValueConstraint = java.lang.Double.NaN;

	/**
	 * Generate a SegmentResponseValueConstraint instance from the given predictor/response pair.
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * @param dblResponseValue The Response Value
	 * 
	 * @return The SegmentResponseValueConstraint instance
	 */

	public static final SegmentResponseValueConstraint FromPredictorResponsePair (
		final double dblPredictorOrdinate,
		final double dblResponseValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblResponseValue))
			return null;

		try {
			return new SegmentResponseValueConstraint (new double[] {dblPredictorOrdinate}, new double[]
				{1.}, dblResponseValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * SegmentResponseValueConstraint constructor
	 * 
	 * @param adblPredictorOrdinate The Array of Global Predictor Ordinates
	 * @param adblResponseValueWeight The Array of the Weights to be applied to the Response at each
	 *  Predictor Ordinate
	 * @param dblWeightedResponseValueConstraint The Value of the Weighted Response Value Constraint
	 * 
	 * @throws java.lang.Exception
	 */

	public SegmentResponseValueConstraint (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValueWeight,
		final double dblWeightedResponseValueConstraint)
		throws java.lang.Exception
	{
		if (null == (_adblPredictorOrdinate = adblPredictorOrdinate) || null == (_adblResponseValueWeight =
			adblResponseValueWeight) || !org.drip.quant.common.NumberUtil.IsValid
				(_dblWeightedResponseValueConstraint = dblWeightedResponseValueConstraint))
			throw new java.lang.Exception ("SegmentResponseValueConstraint ctr: Invalid Inputs");

		int iNumPredictorOrdinate = adblPredictorOrdinate.length;

		if (0 == iNumPredictorOrdinate || _adblResponseValueWeight.length != iNumPredictorOrdinate)
			throw new java.lang.Exception ("SegmentResponseValueConstraint ctr: Invalid Inputs");
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
		return _adblResponseValueWeight;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double constraintValue()
	{
		return _dblWeightedResponseValueConstraint;
	}

	/**
	 * Convert the Segment Constraint onto Local Predictor Ordinates, the corresponding Response Basis
	 *  Function, and the Shape Controller Realizations
	 * 
	 * @param lbe The Local Basis Evaluator
	 * @param ics Inelastics transformer to convert coordinate space to Local from Global
	 * 
	 * @return The Segment Basis Function Constraint
	 */

	public org.drip.spline.params.SegmentBasisFlexureConstraint responseIndexedBasisConstraint (
		final org.drip.spline.segment.BasisEvaluator lbe,
		final org.drip.spline.segment.InelasticConstitutiveState ics)
	{
		if (null == lbe || null == ics) return null;

		int iNumResponseBasis = lbe.numBasis();

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;
		double[] adblResponseBasisWeight = new double[iNumResponseBasis];

		if (0 == iNumResponseBasis) return null;

		try {
			for (int i = 0; i < iNumResponseBasis; ++i) {
				adblResponseBasisWeight[i] = 0.;

				for (int j = 0; j < iNumPredictorOrdinate; ++j)
					adblResponseBasisWeight[i] += _adblResponseValueWeight[j] *
						lbe.shapedBasisFunctionResponse (_adblPredictorOrdinate[j], i);
			}

			return new org.drip.spline.params.SegmentBasisFlexureConstraint (adblResponseBasisWeight,
				_dblWeightedResponseValueConstraint);
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
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorKnot))
			throw new java.lang.Exception ("SegmentResponseValueConstraint::knotPosition => Invalid Inputs");

		if (dblPredictorKnot < _adblPredictorOrdinate[0]) return LEFT_OF_CONSTRAINT;

		if (dblPredictorKnot > _adblPredictorOrdinate[_adblPredictorOrdinate.length - 1])
			return RIGHT_OF_CONSTRAINT;

		return SPLITS_CONSTRAINT;
	}
}
