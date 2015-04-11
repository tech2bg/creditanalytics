
package org.drip.spaces.function;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * RdToR1 is the abstract class underlying the f : R^d -> R^1 Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class RdToR1 {
	private int _iPNorm = -1;
	private org.drip.function.deterministic.AbstractMultivariate _am = null;

	protected RdToR1 (
		final org.drip.function.deterministic.AbstractMultivariate am,
		final int iPNorm)
		throws java.lang.Exception
	{
		if (null == (_am = am) || 0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("RdToR1 ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Multivariate Function
	 * 
	 * @return The Underlying Multivariate Function
	 */

	public org.drip.function.deterministic.AbstractMultivariate function()
	{
		return _am;
	}

	/**
	 * Retrieve the P-Norm Index
	 * 
	 * @return The P-Norm Index
	 */

	public int pNorm()
	{
		return _iPNorm;
	}
}
