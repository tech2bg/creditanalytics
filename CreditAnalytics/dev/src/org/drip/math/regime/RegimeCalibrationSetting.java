
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
 * RegimeCalibrationSetting contains the fields to control the regime level calibration across all the
 *  segments. Specifically, it holds the detail of the calibration to be done, as well as the calibration type.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeCalibrationSetting {

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

	private int _iCalibrationBoundaryCondition = -1;
	private int _iCalibrationDetail = -1;

	/**
	 * RegimeCalibrationSetting constructor
	 * 
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public RegimeCalibrationSetting (
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
		throws java.lang.Exception
	{
		if ((BOUNDARY_CONDITION_FLOATING != (_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition)
			&& BOUNDARY_CONDITION_NATURAL != _iCalibrationBoundaryCondition && BOUNDARY_CONDITION_FINANCIAL
				!= _iCalibrationBoundaryCondition) | (CALIBRATE != (_iCalibrationDetail = iCalibrationDetail)
					&& CALIBRATE_JACOBIAN != _iCalibrationDetail))
			throw new java.lang.Exception ("RegimeCalibrationSetting ctr: Invalid Inputs");
	}

	/**
	 * Get the Calibration Boundary Condition
	 * 
	 * @return The Calibration Boundary Condition
	 */

	public int getCalibrationBoundaryCondition()
	{
		return _iCalibrationBoundaryCondition;
	}

	/**
	 * Get the Calibration Detail
	 * 
	 * @return The Calibration Detail
	 */

	public int getCalibrationDetail()
	{
		return _iCalibrationDetail;
	}
}
