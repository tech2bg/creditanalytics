
package org.drip.quant.function1D;

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
 * BennettFunction is implementation of the Bennett's Function used in the Estimation of the Bennett's
 * 	Concentration Inequality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BennettFunction extends org.drip.quant.function.AbstractUnivariate {

	/**
	 * BennettFunction constructor
	 * 
	 * @throws java.lang.Exception Thrown if BennettFunction cannot be instantiated
	 */

	public BennettFunction()
		throws java.lang.Exception
	{
		super (null);
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || dblVariate < 0.)
			throw new java.lang.Exception ("BennettFunction::evaluate => Invalid Inputs");

		return (1. + dblVariate) * java.lang.Math.log (1. + dblVariate) - dblVariate;
	}
}
