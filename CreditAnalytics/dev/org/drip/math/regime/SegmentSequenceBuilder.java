	
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

public interface SegmentSequenceBuilder {

	/**
	 * Set the Regime whose Segments are to be calibrated
	 * 
	 * @param msr The Regime that needs to be calibrated
	 * 
	 * @return TRUE => Regime successfully set
	 */

	public abstract boolean setRegime (
		final org.drip.math.regime.MultiSegmentRegime msr);

	/**
	 * Retrieve the Calibration Boundary Condition
	 * 
	 * @return The Calibration Boundary Condition
	 */

	public abstract int getCalibrationBoundaryCondition();

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
	 * Calibrate the Segment Sequence in the Regime
	 * 
	 * @param iStartingSegment The Starting Segment in the Sequence
	 * 
	 * @return TRUE => The Segment Sequence successfully calibrated
	 */

	public abstract boolean calibSegmentSequence (
		final int iStartingSegment);
}
