	
package org.drip.state.estimator;

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
 * GlobalControlCurveParams enhances the SmoothingCurveStretchParams to produce globally customized curve
 *  smoothing.
 * 
 * Currently, GlobalControlCurveParams uses custom boundary setting and spline details to implement the
 * 	global smoothing pass.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GlobalControlCurveParams extends org.drip.state.estimator.SmoothingCurveStretchParams {
	private org.drip.spline.stretch.BoundarySettings _bs = null;

	/**
	 * GlobalControlCurveParams constructor
	 * 
	 * @param strSmootheningQuantificationMetric Curve Smoothening Quantification Metric
	 * @param prbp Segment Builder Parameters
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * @param sbfr Curve Fitness Weighted Response
	 * @param sbfrSensitivity Curve Fitness Weighted Response Sensitivity
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public GlobalControlCurveParams (
		final java.lang.String strSmootheningQuantificationMetric,
		final org.drip.spline.params.SegmentCustomBuilderControl prbp,
		final org.drip.spline.stretch.BoundarySettings bs,
		final int iCalibrationDetail,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final org.drip.spline.params.StretchBestFitResponse sbfrSensitivity)
		throws java.lang.Exception
	{
		super (strSmootheningQuantificationMetric, prbp, iCalibrationDetail, sbfr, sbfrSensitivity);

		if (null == (_bs = bs))
			throw new java.lang.Exception ("GlobalControlCurveParams ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Calibration Boundary Condition
	 * 
	 * @return The Calibration Boundary Condition
	 */

	public org.drip.spline.stretch.BoundarySettings calibrationBoundaryCondition()
	{
		return _bs;
	}
}
