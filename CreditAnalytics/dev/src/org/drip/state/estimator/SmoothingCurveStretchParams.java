
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
 * SmoothingCurveStretchParams contains the Parameters needed to hold the Stretch - the Calibration Boundary
 *  Condition, the Calibration Detail, and the BestFitWeightedResponse Instance.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class SmoothingCurveStretchParams {
	private int _iCalibrationDetail = -1;
	private java.lang.String _strSmootheningQuantificationMetric = "";
	private org.drip.spline.params.StretchBestFitResponse _sbfr = null;
	private org.drip.spline.params.SegmentCustomBuilderControl _scbc = null;

	/**
	 * SmoothingCurveStretchParams constructor
	 * 
	 * @param strSmootheningQuantificationMetric Curve Smoothening Quantification Metric
	 * @param scbc Segment Builder Parameters
	 * @param iCalibrationDetail The Calibration Detail
	 * @param sbfr Stretch Fitness Weighted Response
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SmoothingCurveStretchParams (
		final java.lang.String strSmootheningQuantificationMetric,
		final org.drip.spline.params.SegmentCustomBuilderControl scbc,
		final int iCalibrationDetail,
		final org.drip.spline.params.StretchBestFitResponse sbfr)
		throws java.lang.Exception
	{
		if (null == (_scbc = scbc))
			throw new java.lang.Exception ("SmoothingCurveStretchParams ctr: Invalid Inputs");

		_sbfr = sbfr;
		_iCalibrationDetail = iCalibrationDetail;
		_strSmootheningQuantificationMetric = strSmootheningQuantificationMetric;
	}

	/**
	 * Retrieve the Curve Smoothening Quantification Metric
	 * 
	 * @return The Curve Smoothening Quantification Metric
	 */

	public java.lang.String smootheningQuantificationMetric()
	{
		return _strSmootheningQuantificationMetric;
	}

	/**
	 * Retrieve the Calibration Detail
	 * 
	 * @return The Calibration Detail
	 */

	public int calibrationDetail()
	{
		return _iCalibrationDetail;
	}

	/**
	 * Retrieve the Segment Builder Parameters
	 * 
	 * @return The Segment Builder Parameters
	 */

	public org.drip.spline.params.SegmentCustomBuilderControl segmentBuilderControl()
	{
		return _scbc;
	}

	/**
	 * Retrieve the Best Fit Weighted Response
	 * 
	 * @return The Best Fit Weighted Response
	 */

	public org.drip.spline.params.StretchBestFitResponse bestFitWeightedResponse()
	{
		return _sbfr;
	}
}
