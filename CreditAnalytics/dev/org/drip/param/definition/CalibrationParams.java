
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * CalibrationParams the calibration parameters - the measure to be calibrated, the type/nature of the
 * 	calibration to be performed, and the work-out date to which the calibration is done.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CalibrationParams {
	private int _iType = 0;
	private java.lang.String _strMeasure = "";
	private org.drip.param.valuation.WorkoutInfo _wi = null;

	/**
	 * Creates a standard calibration parameter instance around the price measure and base type
	 * 
	 * @return CalibrationParams instance
	 */

	public static final CalibrationParams MakeStdCalibParams()
	{
		try {
			return new CalibrationParams ("Price", 0, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CalibrationParams constructor
	 * 
	 * @param strMeasure Measure to be calibrated
	 * @param iType Calibration Type
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public CalibrationParams (
		final java.lang.String strMeasure,
		final int iType,
		final org.drip.param.valuation.WorkoutInfo wi)
		throws java.lang.Exception
	{
		if (null == (_strMeasure = strMeasure) || _strMeasure.isEmpty())
			throw new java.lang.Exception ("CalibrationParams ctr => Invalid Inputs");

		_wi = wi;
		_iType = iType;
	}

	/**
	 * Retrieve the Calibration Type
	 * 
	 * @return The Calibration Type
	 */

	public int type()
	{
		return _iType;
	}

	/**
	 * Retrieve the Calibration Measure
	 * 
	 * @return The Calibration Measure
	 */

	public java.lang.String measure()
	{
		return _strMeasure;
	}

	/**
	 * Retrieve the Work-out Info
	 * 
	 * @return The Work-out Info
	 */

	public org.drip.param.valuation.WorkoutInfo workout()
	{
		return _wi;
	}
}
