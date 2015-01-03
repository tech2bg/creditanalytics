
package org.drip.spline.basis;

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
 * ExponentialTensionSetParams implements per-segment parameters for the exponential tension basis set -
 *  currently it only contains the tension parameter.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExponentialTensionSetParams implements org.drip.spline.basis.FunctionSetBuilderParams {
	private double _dblTension = java.lang.Double.NaN;

	/**
	 * ExponentialTensionSetParams constructor
	 * 
	 * @param dblTension Segment Tension
	 * 
	 * @throws java.lang.Exception
	 */

	public ExponentialTensionSetParams (
		final double dblTension)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblTension = dblTension))
			throw new java.lang.Exception ("ExponentialTensionSetParams ctr: Invalid Inputs");
	}

	/**
	 * Get the Segment Tension
	 * 
	 * @return The Segment Tension
	 */

	public double tension()
	{
		return _dblTension;
	}
}
