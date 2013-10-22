	
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
 * SmoothingCurveRegimeParams contains the Parameters needed to hold the regime - the Calibration Boundary
 *  Condition, the Calibration Detail, and the BestFitWeightedResponse Instance.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class SmoothingCurveRegimeParams {
	private int _iCalibrationDetail = -1;
	private java.lang.String _strSmootheningQuantificationMetric = "";
	private org.drip.math.segment.BestFitWeightedResponse _bfwr = null;
	private org.drip.math.segment.PredictorResponseBuilderParams _prbp = null;

	/**
	 * SmoothingCurveRegimeParams constructor
	 * 
	 * @param strSmootheningQuantificationMetric Curve Smoothening Quantification Metric
	 * @param prbp Segment Builder Parameters
	 * @param iCalibrationDetail The Calibration Detail
	 * @param bfwr Fitness Weighted Response
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SmoothingCurveRegimeParams (
		final java.lang.String strSmootheningQuantificationMetric,
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final int iCalibrationDetail,
		final org.drip.math.segment.BestFitWeightedResponse bfwr)
		throws java.lang.Exception
	{
		if (null == (_prbp = prbp))
			throw new java.lang.Exception ("SmoothingCurveRegimeParams ctr: Invalid Inputs");

		_bfwr = bfwr;
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

	public org.drip.math.segment.PredictorResponseBuilderParams prbp()
	{
		return _prbp;
	}

	/**
	 * Retrieve the Best Fit Weighted Response
	 * 
	 * @return The Best Fit Weighted Response
	 */

	public org.drip.math.segment.BestFitWeightedResponse bestFitWeightedResponse()
	{
		return _bfwr;
	}
}
