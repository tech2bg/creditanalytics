	
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * LocalControlCurveParams enhances the SmoothingCurveStretchParams to produce locally customized curve
 *  smoothing.
 * 
 * Flags implemented by LocalControlCurveParams control the following:
 *  - The C1 generator scheme to be used
 *  - Whether to eliminate spurious extrema, 
 *  - Whether or not to apply monotone filtering
 *
 * @author Lakshmi Krishnamurthy
 */

public class LocalControlCurveParams extends org.drip.state.estimator.SmoothingCurveStretchParams {
	private boolean _bApplyMonotoneFilter = false;
	private boolean _bEliminateSpuriousExtrema = false;
	private java.lang.String _strC1GeneratorScheme = "";

	/**
	 * LocalControlCurveParams constructor
	 * 
	 * @param strC1GeneratorScheme C1 Generator Stretch
	 * @param strSmootheningQuantificationMetric Curve Smoothening Quantification Metric
	 * @param scbc Segment Builder Parameters
	 * @param iCalibrationDetail The Calibration Detail
	 * @param sbfr Curve Fitness Weighted Response
	 * @param sbfrSensitivity Curve Fitness Weighted Response Sensitivity
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LocalControlCurveParams (
		final java.lang.String strC1GeneratorScheme,
		final java.lang.String strSmootheningQuantificationMetric,
		final org.drip.spline.params.SegmentCustomBuilderControl scbc,
		final int iCalibrationDetail,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final org.drip.spline.params.StretchBestFitResponse sbfrSensitivity,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
		throws java.lang.Exception
	{
		super (strSmootheningQuantificationMetric, scbc, iCalibrationDetail, sbfr, sbfrSensitivity);

		if (null == (_strC1GeneratorScheme = strC1GeneratorScheme))
			throw new java.lang.Exception ("LocalControlCurveParams ctr: Invalid Inputs!");

		_bApplyMonotoneFilter = bApplyMonotoneFilter;
		_bEliminateSpuriousExtrema = bEliminateSpuriousExtrema;
	}

	/**
	 * Retrieve the Apply Monotone Filter Flag
	 * 
	 * @return The Apply Monotone Filter Flag
	 */

	public boolean applyMonotoneFilter()
	{
		return _bApplyMonotoneFilter;
	}

	/**
	 * Retrieve the Eliminate Spurious Extrema Flag
	 * 
	 * @return The Eliminate Spurious Extrema Flag
	 */

	public boolean eliminateSpuriousExtrema()
	{
		return _bEliminateSpuriousExtrema;
	}

	/**
	 * Retrieve the C1 Generator Scheme
	 * 
	 * @return The C1 Generator Scheme
	 */

	public java.lang.String C1GeneratorScheme()
	{
		return _strC1GeneratorScheme;
	}
}
