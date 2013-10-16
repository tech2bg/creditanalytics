	
package org.drip.math.regime;

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
 * MultiSegmentRegime is the interface that exposes functionality that spans multiple segments. Its derived instances hold
 * 	the ordered segment sequence, the segment control parameters, and, if available, the spanning Jacobian.
 *  MultiSegmentRegime exports the following group of functionality:
 * 	- Construct adjoining segment sequences in accordance with the segment control parameters
 * 	- Calibrate according to a varied set of (i.e., NATURAL/FINANCIAL) boundary conditions
 * 	- Estimate both the value, the ordered derivatives, and the Jacobian at the given ordinate
 * 	- Compute the monotonicity details - segment/regime level monotonicity, co-monotonicity, local
 * 		monotonicity.
 * 
 * It also exports several static Regime creation/calibration methods to generate customized basis splines,
 * 	with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface MultiSegmentRegime extends org.drip.math.regime.SingleSegmentRegime {

	/**
	 * Calibration Detail: Calibrate the Regime as part of the set up
	 */

	public static final int CALIBRATE = 1;

	/**
	 * Calibration Detail: Calibrate the Regime AND compute Jacobian as part of the set up
	 */

	public static final int CALIBRATE_JACOBIAN = 2;

	/**
	 * Calibration Boundary Condition: Floating Boundary Condition
	 */

	public static final int BOUNDARY_CONDITION_FLOATING = 1;

	/**
	 * Calibration Boundary Condition: Natural Boundary Condition
	 */

	public static final int BOUNDARY_CONDITION_NATURAL = 2;

	/**
	 * Calibration Boundary Condition: Financial Boundary Condition
	 */

	public static final int BOUNDARY_CONDITION_FINANCIAL = 4;

	/**
	 * Retrieve the Segment Builder Parameters
	 * 
	 * @return The Segment Builder Parameters
	 */

	public abstract org.drip.math.segment.PredictorResponseBuilderParams[] getSegmentBuilderParams();

	/**
	 * Retrieve the Regime Name
	 * 
	 * @return The Regime Name
	 */

	public abstract java.lang.String name();

	/**
	 * Retrieve the Regime Segments
	 * 
	 * @return The Regime Segments
	 */

	public abstract org.drip.math.segment.PredictorResponse[] getSegments();

	/**
	 * Set up (i.e., calibrate) the individual Segment in the Regime to the Target Segment Edge Values and
	 * 	Constraints. This is also called the Hermite setup - where the segment boundaries are entirely
	 * 	locally set.
	 * 
	 * @param aPORDLeft Array of Left Segment Edge Values
	 * @param aPORDRight Array of Right Segment Edge Values
	 * @param aaRVC Double Array of Constraints - Outer Index corresponds to Segment Index, and the Inner
	 * 		Index to Constraint Array within each Segment
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Set up Mode (i.e., set up ITEP only, or fully calibrate the Regime, or calibrate
	 * 	 	Regime plus compute Jacobian)
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setupHermite (
		final org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft,
		final org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight,
		final org.drip.math.segment.ResponseValueConstraint[][] aaRVC,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode);

	/**
	 * Set the Slope at the left Edge of the Regime
	 * 
	 * @param dblRegimeLeftResponse Response Value at the Left Edge of the Regime
	 * @param dblRegimeLeftResponseSlope Response Slope Value at the Left Edge of the Regime
	 * @param dblRegimeRightResponse Response Value at the Right Edge of the Regime
	 * @param fwr Fitness Weighted Response
	 * 
	 * @return TRUE => Left slope successfully set
	 */

	public abstract boolean setLeftNode (
		final double dblRegimeLeftResponse,
		final double dblRegimeLeftResponseSlope,
		final double dblRegimeRightResponse,
		final org.drip.math.segment.BestFitWeightedResponse fwr);

	/**
	 * Calculate the PORD at the specified Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Computed PORD
	 */

	public abstract org.drip.math.segment.PredictorOrdinateResponseDerivative calcPORD (
		final double dblPredictorOrdinate);

	/**
	 * Calculate the Derivative of the requested order at the right Edge of the Regime
	 * 
	 * @param iOrder Order of the Derivative
	 * 
	 * @return The Derivative of the requested order at the right Edge of the Regime
	 * 
	 * @throws java.lang.Exception Thrown if the Derivative cannot be calculated
	 */

	public abstract double calcRightEdgeDerivative (
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Check if the Predictor Ordinate is in the Regime Range
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return TRUE => Predictor Ordinate is in the Range
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public abstract boolean in (
		final double dblPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * Return the Index for the Segment containing specified Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * @param bIncludeLeft TRUE => Less than or equal to the Left Predictor Ordinate
	 * @param bIncludeRight TRUE => Less than or equal to the Right Predictor Ordinate
	 * 
	 * @return Index for the Segment containing specified Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public int containingIndex (
		final double dblPredictorOrdinate,
		final boolean bIncludeLeft,
		final boolean bIncludeRight)
		throws java.lang.Exception;

	/**
	 * Generate a new Regime by clipping all the Segments to the Left of the specified Predictor Ordinate.
	 *  Smoothness Constraints will be maintained.
	 * 
	 * @param strName Name of the Clipped Regime 
	 * @param dblPredictorOrdinate Predictor Ordinate Left of which the Clipping is to be applied
	 * 
	 * @return The Clipped Regime
	 */

	public abstract MultiSegmentCalibratableRegime clipLeft (
		final java.lang.String strName,
		final double dblPredictorOrdinate);

	/**
	 * Generate a new Regime by clipping all the Segments to the Right of the specified Predictor Ordinate.
	 * 	Smoothness Constraints will be maintained.
	 * 
	 * @param strName Name of the Clipped Regime 
	 * @param dblPredictorOrdinate Predictor Ordinate Right of which the Clipping is to be applied
	 * 
	 * @return The Clipped Regime
	 */

	public abstract MultiSegmentCalibratableRegime clipRight (
		final java.lang.String strName,
		final double dblPredictorOrdinate);

	/**
	 * Set up (i.e., calibrate) the individual Segments in the Regime to the Regime Edge, the Target
	 *  Constraints, and the custom segment sequence builder.
	 * 
	 * @param ssb The Segment Sequence Builder Instance
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final org.drip.math.regime.SegmentSequenceBuilder ssb,
		final int iCalibrationDetail);

	/**
	 * Set up (i.e., calibrate) the individual Segments in the Regime to the Regime Left Edge and the Target
	 *  Constraints.
	 * 
	 * @param rvcLeading Regime Left-most Segment Response Value Constraint
	 * @param aRVC Array of Segment Response Value Constraints
	 * @param fwr Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final org.drip.math.segment.ResponseValueConstraint rvcLeading,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail);

	/**
	 * Set up (i.e., calibrate) the individual Segments in the Regime to the Regime Left Edge Response and
	 * 	the Target Constraints.
	 * 
	 * @param dblRegimeLeftResponseValue Regime Left-most Response Value
	 * @param aRVC Array of Segment Response Value Constraints
	 * @param fwr Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final double dblRegimeLeftResponseValue,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail);

	/**
	 * Retrieve the Span DCPE
	 * 
	 * @return The Span DCPE
	 * 
	 * @throws java.lang.Exception Thrown if the Span DCPE cannot be computed
	 */

	public abstract double dcpe()
		throws java.lang.Exception;
}
