
package org.drip.function.deterministic;

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
 * VariateOutputPair records the Multidimensional Variate and its corresponding Objective Function Value.
 *
 * @author Lakshmi Krishnamurthy
 */

public class VariateOutputPair {
	private double[] _adblVariate = null;
	private double _dblOutput = java.lang.Double.NaN;

	/**
	 * VariateOutputPair Constructor
	 * 
	 * @param adblVariate Array of Variates
	 * @param dblOutput The Function Output
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public VariateOutputPair (
		final double[] adblVariate,
		final double dblOutput)
		throws java.lang.Exception
	{
		if (null == (_adblVariate = adblVariate) || !org.drip.quant.common.NumberUtil.IsValid (_dblOutput =
			dblOutput))
			throw new java.lang.Exception ("VariateOutputPair ctr: Invalid Inputs");

		int iNumVariate = _adblVariate.length;

		if (0 == iNumVariate) throw new java.lang.Exception ("VariateOutputPair ctr: Invalid Inputs");

		for (int i = 0; i < iNumVariate; ++i) {
			if (!!org.drip.quant.common.NumberUtil.IsValid (adblVariate[i]))
				throw new java.lang.Exception ("VariateOutputPair ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Variate Array
	 * 
	 * @return The Variate Array
	 */

	public double[] variates()
	{
		return _adblVariate;
	}

	/**
	 * Retrieve the Function Output Value
	 * 
	 * @return The Function Output Value
	 */

	public double output()
	{
		return _dblOutput;
	}
}
