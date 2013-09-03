	
package org.drip.math.grid;

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
 * Span is the interface that exposes functionality that spans multiple segments. Its derived instances hold
 * 	the ordered segment sequence, the segment control parameters, and, if available, the spanning Jacobian.
 *  Span exports the following group of functionality:
 * 	- Construct adjoining segment sequences in accordance with the segment control parameters
 * 	- Calibrate according to a varied set of (i.e., NATURAL/FINANCIAL) boundary conditions
 * 	- Interpolate both the value, the ordered derivatives, and the Jacobian at the given ordinate
 * 	- Compute the monotonicity details - segment/span level monotonicity, co-monotonicity, local
 * 		monotonicity.
 * 
 * It also exports several static Span creation/calibration methods to generate customized basis splines,
 * 	with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface MultiSegmentSpan extends org.drip.math.grid.SingleSegmentSpan {

	/**
	 * Calibration Mode: Natural Boundary Condition
	 */

	public static final java.lang.String SPLINE_BOUNDARY_MODE_NATURAL = "Natural";

	/**
	 * Calibration Mode: Financial Boundary Condition
	 */

	public static final java.lang.String SPLINE_BOUNDARY_MODE_FINANCIAL = "Financial";

	/**
	 * Retrieve the Span Builder Parameters
	 * 
	 * @return The Span Builder Parameters
	 */

	public abstract org.drip.math.grid.SegmentBuilderParams[] getSegmentBuilderParams();

	/**
	 * Retrieve the Span Segments
	 * 
	 * @return The Span Segments
	 */

	public abstract org.drip.math.grid.Segment[] getSegments();

	/**
	 * Sets up (i.e., calibrates) the individual segment in the span to the target segment edge parameters
	 * 	and any constraints.
	 * 
	 * @param aSEPLeft Array of Left Segment Edge Parameters
	 * @param aSEPRight Array of Left Segment Edge Parameters
	 * @param aaSNWC Double Array of Constraints - Outer Index corresponds to Segment Index, and the Inner
	 * 		Index to Constraint Array within each Segment
	 * @param iSetupMode Set up Mode (i.e., set up ITEP only, or fully calibrate the Span, or calibrate Span
	 * 		plus compute Jacobian)
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final org.drip.math.grid.SegmentEdgeParams[] aSEPLeft,
		final org.drip.math.grid.SegmentEdgeParams[] aSEPRight,
		final org.drip.math.spline.SegmentNodeWeightConstraint[][] aaSNWC,
		final int iSetupMode);

	/**
	 * Sets the left slope
	 * 
	 * @param dblLeftValue Left most node value
	 * @param dblLeftSlope Left most node slope
	 * @param dblRightValue Left most node value
	 * 
	 * @return TRUE => Left slope successfully set
	 */

	public abstract boolean setLeftNode (
		final double dblLeftValue,
		final double dblLeftSlope,
		final double dblRightValue);

	/**
	 * Calculates the full node SEP at the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Interpolated output
	 */

	public abstract org.drip.math.grid.SegmentEdgeParams calcSEP (
		final double dblX);

	/**
	 * Calculate the tail derivative of the requested order for the given node
	 * 
	 * @param iOrder Order of the derivative
	 * 
	 * @return The Tail Derivative
	 * 
	 * @throws java.lang.Exception Thrown if the derivative cannot be calculated
	 */

	public abstract double calcTailDerivative (
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Checks if the point is inside the span range
	 * 
	 * @param dblX Input point
	 * 
	 * @return TRUE => Point is inside the span range
	 */

	public abstract boolean isInRange (
		final double dblX);

	/**
	 * Sets up (i.e., calibrates) the individual segment in the span to the target node constraints.
	 * 
	 * @param dblYLeading Span Left-most Y
	 * @param aSNWC Array of Segment Node Weight Constraints
	 * @param strCalibrationMode Calibration Mode (i.e., the calibration boundary condition)
	 * @param iSetupMode Set up Mode (Fully calibrate the Span, or calibrate Span plus compute Jacobian)
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final double dblYLeading,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC,
		final java.lang.String strCalibrationMode,
		final int iSetupMode);
}
