
package org.drip.spline.basis;

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
 * ExponentialMixtureSetParams implements per-segment parameters for the exponential mixture basis set -
 *  the array of the exponential tension parameters, one per each entity in the mixture.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExponentialMixtureSetParams implements org.drip.spline.basis.FunctionSetBuilderParams {
	private double[] _adblTension = null;

	/**
	 * ExponentialMixtureSetParams constructor
	 * 
	 * @param adblTension Array of the Tension Parameters
	 * 
	 * @throws java.lang.Exception
	 */

	public ExponentialMixtureSetParams (
		final double[] adblTension)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_adblTension = adblTension))
			throw new java.lang.Exception ("ExponentialMixtureSetParams ctr: Invalid Inputs");
	}

	/**
	 * Get the Indexed Exponential Tension Entry
	 * 
	 * @return The Indexed Exponential Tension Entry
	 */

	public double tension (
		final int iIndex)
		throws java.lang.Exception
	{
		if (iIndex >= _adblTension.length)
			throw new java.lang.Exception ("ExponentialMixtureSetParams::tension => Invalid Index");

		return _adblTension[iIndex];
	}
}
