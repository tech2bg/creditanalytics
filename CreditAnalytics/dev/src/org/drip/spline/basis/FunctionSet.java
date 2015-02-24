
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
 * This class implements the basis spline function set.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FunctionSet {
	private org.drip.quant.function.AbstractUnivariate[] _aAUResponseBasis = null;

	/**
	 * @param aAUResponseBasis Array of the Basis Function Set
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public FunctionSet (
		final org.drip.quant.function.AbstractUnivariate[] aAUResponseBasis)
		throws java.lang.Exception
	{
		if (null == (_aAUResponseBasis = aAUResponseBasis) || 0 == _aAUResponseBasis.length)
			throw new java.lang.Exception ("FunctionSet ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Number of Basis Functions
	 * 
	 * @return Number of Basis Functions
	 */

	public int numBasis()
	{
		return _aAUResponseBasis.length;
	}

	/**
	 * Retrieve the Basis Function identified by the specified Index
	 * 
	 * @param iBasisIndex The Basis Function Index
	 * 
	 * @return The Basis Function identified by the specified Index
	 */

	public org.drip.quant.function.AbstractUnivariate indexedBasisFunction (
		final int iBasisIndex)
	{
		if (iBasisIndex >= numBasis()) return null;

		return _aAUResponseBasis[iBasisIndex];
	}
}
