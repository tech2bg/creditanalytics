
package org.drip.quant.function1D;

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
 * NaturalLogSeriesElement implements an element in the natural log series expansion.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NaturalLogSeriesElement extends org.drip.quant.function1D.AbstractUnivariate {
	private int _iExponent = -1;

	/**
	 * NaturalLogSeriesElement constructor
	 * 
	 * @param iExponent The series exponent
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public NaturalLogSeriesElement (
		final int iExponent)
		throws java.lang.Exception
	{
		super (null);

		if (0 > (_iExponent = iExponent))
			throw new java.lang.Exception ("NaturalLogSeriesElement ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		return java.lang.Math.pow (dblVariate, _iExponent) / org.drip.quant.common.NumberUtil.Factorial
			(_iExponent);
	}

	@Override public double calcDerivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		return iOrder > _iExponent ? 0. : java.lang.Math.pow (dblVariate, _iExponent - iOrder) /
			org.drip.quant.common.NumberUtil.Factorial (_iExponent - iOrder);
	}

	/**
	 * Retrieve the exponent in the natural log series
	 * 
	 * @return Exponent in the natural log series
	 */

	public int getExponent()
	{
		return _iExponent;
	}
}
