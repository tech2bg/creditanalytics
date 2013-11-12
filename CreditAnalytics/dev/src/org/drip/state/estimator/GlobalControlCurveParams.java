	
package org.drip.state.estimator;

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
 * GlobalControlCurveParams contains the Parameters needed to hold the regime - the Calibration Boundary
 *  Condition, the Calibration Detail, and the BestFitWeightedResponse Instance.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GlobalControlCurveParams extends org.drip.state.estimator.SmoothingCurveRegimeParams {
	private int _iCalibrationBoundaryCondition = -1;

	/**
	 * GlobalControlCurveParams constructor
	 * 
	 * @param strSmootheningQuantificationMetric Curve Smoothening Quantification Metric
	 * @param prbp Segment Builder Parameters
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * @param rfwr Curve Fitness Weighted Response
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public GlobalControlCurveParams (
		final java.lang.String strSmootheningQuantificationMetric,
		final org.drip.spline.params.SegmentCustomBuilderControl prbp,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail,
		final org.drip.spline.params.RegimeBestFitResponse rfwr)
		throws java.lang.Exception
	{
		super (strSmootheningQuantificationMetric, prbp, iCalibrationDetail, rfwr);

		_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition;
	}

	/**
	 * Retrieve the Calibration Boundary Condition
	 * 
	 * @return The Calibration Boundary Condition
	 */

	public int calibrationBoundaryCondition()
	{
		return _iCalibrationBoundaryCondition;
	}
}
