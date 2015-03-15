
package org.drip.sequence.cover;

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
 * CoveringNumber implements the Lower/Upper Bounds for the Derived Class Functions. The Main Reference is:
 * 
 * 	- P. L. Bartlett, S. R. Kulkarni, and S. E. Posner (1997): Covering Numbers for Real-valued Function
 * 		Classes, IEEE Transactions on Information Theory 43 (5) 1721-1724.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface CoveringNumber {

	/**
	 * Log of the Lower Bound of the Function Covering Number
	 * 
	 * @param dblCover The Size of the Cover
	 * 
	 * @return Log of the Lower Bound of the Function Covering Number
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double logLowerBound (
		final double dblCover)
		throws java.lang.Exception;

	/**
	 * Log of the Upper Bound of the Function Covering Number
	 * 
	 * @param dblCover The Size of the Cover
	 * 
	 * @return Log of the Upper Bound of the Function Covering Number
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double logUpperBound (
		final double dblCover)
		throws java.lang.Exception;
}
