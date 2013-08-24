	
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

public interface SingleSegmentSpan {

	/**
	 * Span Set Up Mode: Calibrate SPAN
	 */

	public static final int CALIBRATE_SPAN = 1;

	/**
	 * Span Set Up Mode: Calibrate Jacobian
	 */

	public static final int CALIBRATE_JACOBIAN = 2;

	/**
	 * Retrieve the Span Builder Parameters
	 * 
	 * @return The Span Builder Parameters
	 */

	public abstract org.drip.math.grid.SpanBuilderParams getSpanBuilderParams();

	/**
	 * Sets up (i.e., calibrates) the individual segment in the span to the target node values.
	 * 
	 * @param adblY Target Node values
	 * @param strCalibrationMode Calibration Mode (i.e., the calibration boundary condition)
	 * @param iSetupMode Set up Mode (Fully calibrate the Span, or calibrate Span plus compute Jacobian)
	 * 
	 * @return TRUE => Set up was successful
	 */

	public abstract boolean setup (
		final double[] adblY,
		final java.lang.String strCalibrationMode,
		final int iSetupMode);

	/**
	 * Calculates the interpolated value at the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Interpolated output
	 * 
	 * @throws java.lang.Exception Thrown if the interpolation did not succeed
	 */

	public abstract double calcValue (
		final double dblX)
		throws java.lang.Exception;

	/**
	 * Calculates the Jacobian to the inputs at the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Jacobian to the inputs
	 */

	public abstract org.drip.math.calculus.WengertJacobian calcValueJacobian (
		final double dblX);

	/**
	 * Identifies the monotone type for the segment underlying the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Segment monotone Type
	 */

	public abstract org.drip.math.grid.SegmentMonotonocity monotoneType (
		final double dblX);

	/**
	 * Indicates if all the comprising segments are monotone
	 * 
	 * @return TRUE => Fully locally monotonic
	 * 
	 * @throws java.lang.Exception Thrown if the Segment monotone Type could not be estimated
	 */

	public abstract boolean isLocallyMonotone() throws java.lang.Exception;

	/**
	 * Verify whether the segment and spline mini-max behavior matches
	 * 
	 * @param adblY Input Y array points
	 * 
	 * @return TRUE => Span is co-monotonic with the input points
	 * 
	 * @throws java.lang.Exception Thrown if the Segment monotone Type could not be estimated
	 */

	public abstract boolean isCoMonotone (
		final double[] adblY)
		throws java.lang.Exception;

	/**
	 * Is the given X a knot location
	 * 
	 * @param dblX Knot X
	 * 
	 * @return TRUE => Given Location corresponds to a Knot
	 */

	public abstract boolean isKnot (
		final double dblX);

	/**
	 * Reset the given node with the given value
	 * 
	 * @param iNodeIndex Node whose value is set
	 * @param dblNodeValue Node Value
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public abstract boolean resetNode (
		final int iNodeIndex,
		final double dblNodeValue);
}
