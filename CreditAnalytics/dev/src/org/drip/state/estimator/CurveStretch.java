
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
 * CurveStretch expands the regular Multi-Segment Stretch to aid the calibration of Boot-strapped
 *  Instruments.
 *  
 * In particular, CurveStretch implements the following functions that are used at different stages of
 * 	curve construction sequence:
 * 	- Mark the Range of the "built" Segments
 * 	- Clear the built range mark to signal the start of a fresh calibration run
 * 	- Indicate if the specified Predictor Ordinate is inside the "Built" Range
 * 	- Retrieve the MergeSubStretchManager
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurveStretch extends org.drip.spline.stretch.CalibratableMultiSegmentSequence {
	private double _dblBuiltPredictorOrdinateRight = java.lang.Double.NaN;
	private org.drip.state.representation.MergeSubStretchManager _msm = null;

	/**
	 * CurveStretch constructor - Construct a sequence of Basis Spline Segments
	 * 
	 * @param strName Name of the Stretch
	 * @param aCS Array of Segments
	 * @param aSCBC Array of Segment Builder Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CurveStretch (
		final java.lang.String strName,
		final org.drip.spline.segment.LatentStateResponseModel[] aCS,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC)
		throws java.lang.Exception
	{
		super (strName, aCS, aSCBC);

		_dblBuiltPredictorOrdinateRight = getLeftPredictorOrdinateEdge();
	}

	/**
	 * Mark the Range of the "built" Segments
	 * 
	 * @param iSegment The Current Segment Range Built
	 * @param fri The Floating Rate Index
	 * 
	 * @return TRUE => Range successfully marked as "built"
	 */

	public boolean setSegmentBuilt (
		final int iSegment,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		org.drip.spline.segment.LatentStateResponseModel[] aCS = segments();

		if (iSegment >= aCS.length) return false;

		_dblBuiltPredictorOrdinateRight = aCS[iSegment].right();

		if (null == fri) return true;

		if (null == _msm) _msm = new org.drip.state.representation.MergeSubStretchManager();

		try {
			return _msm.addMergeStretch (new org.drip.state.representation.LatentStateMergeSubStretch
				(aCS[iSegment].left(), aCS[iSegment].right(), fri));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Clear the built range mark to signal the start of a fresh calibration run
	 * 
	 * @return TRUE => Built Range successfully cleared
	 */

	public boolean setClearBuiltRange()
	{
		_dblBuiltPredictorOrdinateRight = getLeftPredictorOrdinateEdge();

		return true;
	}

	/**
	 * Indicate if the specified Predictor Ordinate is inside the "Built" Range
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return TRUE => The specified Predictor Ordinate is inside the "Built" Range
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public boolean inBuiltRange (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("CurveStretch.inBuiltRange => Invalid Inputs");

		return dblPredictorOrdinate >= getLeftPredictorOrdinateEdge() && dblPredictorOrdinate <=
			_dblBuiltPredictorOrdinateRight;
	}

	@Override public org.drip.state.representation.MergeSubStretchManager msm()
	{
		return _msm;
	}
}
