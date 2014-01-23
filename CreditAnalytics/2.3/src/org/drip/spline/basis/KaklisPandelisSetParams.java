
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
 * KaklisPandelisSetParams implements per-segment parameters for the Kaklis Pandelis basis set -
 *  currently it only holds the polynomial tension degree.
 *
 * @author Lakshmi Krishnamurthy
 */

public class KaklisPandelisSetParams implements org.drip.spline.basis.FunctionSetBuilderParams {
	private int _iPolynomialTensionDegree = -1;

	/**
	 * KaklisPandelisSetParams constructor
	 * 
	 * @param iPolynomialTensionDegree Segment Polynomial Tension Degree
	 * 
	 * @throws java.lang.Exception
	 */

	public KaklisPandelisSetParams (
		final int iPolynomialTensionDegree)
		throws java.lang.Exception
	{
		if (0 >= (_iPolynomialTensionDegree = iPolynomialTensionDegree))
			throw new java.lang.Exception ("KaklisPandelisSetParams ctr: Invalid Inputs");
	}

	/**
	 * Get the Segment Polynomial Tension Degree
	 * 
	 * @return The Segment Polynomial Tension Degree
	 */

	public int polynomialTensionDegree()
	{
		return _iPolynomialTensionDegree;
	}
}
