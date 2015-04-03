
package org.drip.kernel.machine;

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
 * BoundedLinearOperatorClass implements the Class of all the Linear Operators that Bounded across from the E
 * 	to the F Banach Metric Spaces.
 * 
 * 	The Reference is:
 * 
 * 		Williamson, R. C., A. J. Smola, and B. Scholkopf (2001): Generalization Performance of Regularization
 * 			Networks and Support Vector Machines via Entropy Numbers of Compact Operators, IEEE Transactions
 * 			On Information Theory, 47 (6), 2516-2532.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedLinearOperatorClass {
	private org.drip.kernel.spaces.BanachSpace _bsE = null;
	private org.drip.kernel.spaces.BanachSpace _bsF = null;

	/**
	 * BoundedLinearOperatorClass Constructor
	 * 
	 * @param bsE The Input Normed Banach Space
	 * @param bsF The Output Normed Banach Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BoundedLinearOperatorClass (
		final org.drip.kernel.spaces.BanachSpace bsE,
		final org.drip.kernel.spaces.BanachSpace bsF)
		throws java.lang.Exception
	{
		if (null == (_bsE = bsE) || null == (_bsF = bsF))
			throw new java.lang.Exception ("BoundedLinearOperatorClass ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Input Normed Banach Space
	 * 
	 * @return The Input Normed Banach Space
	 */

	public org.drip.kernel.spaces.BanachSpace e()
	{
		return _bsE;
	}

	/**
	 * Retrieve the Output Normed Banach Space
	 * 
	 * @return The Output Normed Banach Space
	 */

	public org.drip.kernel.spaces.BanachSpace f()
	{
		return _bsF;
	}
}
