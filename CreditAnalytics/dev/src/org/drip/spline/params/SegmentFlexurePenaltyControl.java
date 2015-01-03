
package org.drip.spline.params;

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
 * SegmentFlexurePenaltyControl implements basis per-segment Flexure Penalty Parameter Set. Currently it
 *  contains the Flexure Penalty Derivative Order and the Roughness Coefficient Amplitude.
 *  
 *  Flexure Penalty Control may be used to implement Segment Curvature Control and/or Segment Length Control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentFlexurePenaltyControl {
	private int _iDerivativeOrder = -1;
	private double _dblAmplitude = java.lang.Double.NaN;

	/**
	 * SegmentFlexurePenaltyControl constructor
	 * 
	 * @param iDerivativeOrder Roughness Penalty Derivative Order
	 * @param dblAmplitude Roughness Curvature Penalty Amplitude
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SegmentFlexurePenaltyControl (
		final int iDerivativeOrder,
		final double dblAmplitude)
		throws java.lang.Exception
	{
		if (0 >= (_iDerivativeOrder = iDerivativeOrder) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblAmplitude = dblAmplitude))
			throw new java.lang.Exception ("SegmentFlexurePenaltyControl ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Derivative Order
	 * 
	 * @return The Derivative Order
	 */

	public int derivativeOrder()
	{
		return _iDerivativeOrder;
	}

	/**
	 * Retrieve the Roughness Curvature Penalty Amplitude
	 * 
	 * @return The Roughness Curvature Penalty Amplitude
	 */

	public double amplitude()
	{
		return _dblAmplitude;
	}
}
