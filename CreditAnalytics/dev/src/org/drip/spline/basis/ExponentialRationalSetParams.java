
package org.drip.spline.basis;

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
 * ExponentialRationalSetParams implements per-segment parameters for the exponential rational basis set
 *  - the exponential tension and the rational tension parameters.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExponentialRationalSetParams implements org.drip.spline.basis.FunctionSetBuilderParams {
	private double _dblRationalTension = java.lang.Double.NaN;
	private double _dblExponentialTension = java.lang.Double.NaN;

	/**
	 * ExponentialRationalSetParams constructor
	 * 
	 * @param dblExponentialTension Segment Tension
	 * @param dblRationalTension Segment Tension
	 * 
	 * @throws java.lang.Exception
	 */

	public ExponentialRationalSetParams (
		final double dblExponentialTension,
		final double dblRationalTension)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblExponentialTension = dblExponentialTension) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblRationalTension = dblRationalTension))
			throw new java.lang.Exception ("ExponentialRationalSetParams ctr: Invalid Inputs");
	}

	/**
	 * Get the Exponential Tension
	 * 
	 * @return The Exponential Tension
	 */

	public double exponentialTension()
	{
		return _dblExponentialTension;
	}

	/**
	 * Get the Rational Tension
	 * 
	 * @return The Rational Tension
	 */

	public double rationalTension()
	{
		return _dblRationalTension;
	}
}
