
package org.drip.spline.stretch;

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
 * SegmentSequenceBuilder is the interface that contains the stubs required for the construction of the
 * 	segment stretch. It exposes the following functions:
 * 	- Set the Stretch whose Segments are to be calibrated
 * 	- Retrieve the Calibration Boundary Condition
 * 	- Calibrate the Starting Segment using the LeftSlope
 * 	- Calibrate the Segment Sequence in the Stretch
 *
 * @author Lakshmi Krishnamurthy
 */

public interface SegmentSequenceBuilder {

	/**
	 * Set the Stretch whose Segments are to be calibrated
	 * 
	 * @param mss The Stretch that needs to be calibrated
	 * 
	 * @return TRUE => Stretch successfully set
	 */

	public abstract boolean setStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss);

	/**
	 * Retrieve the Calibration Boundary Condition
	 * 
	 * @return The Calibration Boundary Condition
	 */

	public abstract org.drip.spline.stretch.BoundarySettings getCalibrationBoundaryCondition();

	/**
	 * Calibrate the Starting Segment using the LeftSlope
	 * 
	 * @param dblLeftSlope The Slope
	 * 
	 * @return TRUE => The Segment was successfully set up.
	 */

	public abstract boolean calibStartingSegment (
		final double dblLeftSlope);

	/**
	 * Calibrate the Segment Sequence in the Stretch
	 * 
	 * @param iStartingSegment The Starting Segment in the Sequence
	 * 
	 * @return TRUE => The Segment Sequence successfully calibrated
	 */

	public abstract boolean calibSegmentSequence (
		final int iStartingSegment);

	/**
	 * Compute the Stretch Manifest Measure Sensitivity Sequence
	 * 
	 * @param dblLeftSlopeSensitivity The Leading Segment Left Slope Sensitivity
	 * 
	 * @return TRUE => The Stretch Manifest Measure Sensitivity Sequence successfully computed
	 */

	public boolean manifestMeasureSensitivity (
		final double dblLeftSlopeSensitivity);
}
