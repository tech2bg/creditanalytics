
package org.drip.spline.regime;

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
 * SingleSegmentSequence is the interface that exposes functionality that spans multiple segments. Its
 * 	derived instances hold the ordered segment sequence, the segment control parameters, and, if available,
 * 	the spanning Jacobian. SingleSegmentRegime exports the following group of functionality:
 * 	- Construct adjoining segment sequences in accordance with the segment control parameters
 * 	- Calibrate according to a varied set of (i.e., NATURAL/FINANCIAL) boundary conditions
 * 	- Estimate both the value, the ordered derivatives, and the Jacobian at the given ordinate
 * 	- Compute the monotonicity details - segment/regime level monotonicity, co-monotonicity, local
 * 		monotonicity.
 * 
 * It also exports several static regime creation/calibration methods to generate customized basis splines,
 * 	with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface SingleSegmentSequence {

	/**
	 * Set up (i.e., calibrate) the individual Segments in the Regime to the Response Values corresponding
	 * 	to each Segment Predictor right Ordinate.
	 * 
	 * @param dblRegimeLeadingResponse Regime Left-most Response
	 * @param adblSegmentRightEdgeResponse Array of Segment Right Edge Responses
	 * @param rbfr Regime Fitness Weighted Response
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final double dblRegimeLeadingResponse,
		final double[] adblSegmentRightEdgeResponse,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final org.drip.spline.regime.BoundarySettings bs,
		final int iCalibrationDetail);

	/**
	 * Calculate the Response Value for the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return The Response Value
	 * 
	 * @throws java.lang.Exception Thrown if the Response Value cannot be calculated
	 */

	public abstract double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * Calculate the Response Derivative to the Calibration Response Inputs at the specified Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return Jacobian of the Response Derivative to the Calibration Response Inputs at the Ordinate
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDResponseDResponseInput (
		final double dblPredictorOrdinate);

	/**
	 * Identify the Monotone Type for the Segment underlying the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return Segment Monotone Type
	 */

	public abstract org.drip.spline.segment.Monotonocity monotoneType (
		final double dblPredictorOrdinate);

	/**
	 * Indicate if all the comprising Segments are Monotone
	 * 
	 * @return TRUE => Fully locally monotonic
	 * 
	 * @throws java.lang.Exception Thrown if the Segment Monotone Type could not be estimated
	 */

	public abstract boolean isLocallyMonotone()
		throws java.lang.Exception;

	/**
	 * Verify whether the Regime mini-max Behavior matches the Measurement
	 * 
	 * @param adblMeasuredResponse The Array of Measured Responses
	 * 
	 * @return TRUE => Regime is co-monotonic with the measured Responses
	 * 
	 * @throws java.lang.Exception Thrown if the Segment Monotone Type could not be estimated
	 */

	public abstract boolean isCoMonotone (
		final double[] adblMeasuredResponse)
		throws java.lang.Exception;

	/**
	 * Is the given Predictor Ordinate a Knot Location
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return TRUE => Given Predictor Ordinate corresponds to a Knot
	 */

	public abstract boolean isKnot (
		final double dblPredictorOrdinate);

	/**
	 * Reset the Predictor Ordinate Node Index with the given Response
	 * 
	 * @param iPredictorOrdinateNodeIndex The Predictor Ordinate Node Index whose Response is to be reset
	 * @param dblResetResponse The Response to reset
	 * 
	 * @return TRUE => Reset succeeded
	 */

	public abstract boolean resetNode (
		final int iPredictorOrdinateNodeIndex,
		final double dblResetResponse);

	/**
	 * Reset the Predictor Ordinate Node Index with the given Segment Constraint
	 * 
	 * @param iPredictorOrdinateNodeIndex The Predictor Ordinate Node Index whose Response is to be reset
	 * @param srvcReset The Segment Constraint
	 * 
	 * @return TRUE => Reset succeeded
	 */

	public abstract boolean resetNode (
		final int iNodeIndex,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcReset);

	/**
	 * Return the Left Predictor Ordinate Edge
	 * 
	 * @return The Left Predictor Ordinate Edge
	 */

	public abstract double getLeftPredictorOrdinateEdge();

	/**
	 * Return the Right Predictor Ordinate Edge
	 * 
	 * @return The Right Predictor Ordinate Edge
	 */

	public abstract double getRightPredictorOrdinateEdge();
}
