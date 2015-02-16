
package org.drip.quant.discrete;

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
 * MeanDepartureBounds holds the Lower/Upper Probability Bounds related to the Specified Mean-Centered
 *  Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MeanDepartureBounds {
	private double _dblLower = java.lang.Double.NaN;
	private double _dblUpper = java.lang.Double.NaN;

	/**
	 * MeanDepartureBounds Constructor
	 * 
	 * @param dblLower Lower Bound
	 * @param dblUpper Upper Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MeanDepartureBounds (
		final double dblLower,
		final double dblUpper)
		throws java.lang.Exception
	{
		_dblLower = dblLower;
		_dblUpper = dblUpper;

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLower) &&
			!org.drip.quant.common.NumberUtil.IsValid (_dblUpper))
			throw new java.lang.Exception ("MeanDepartureBounds ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Lower Probability Bound
	 * 
	 * @return The Lower Probability Bound
	 */

	public double lower()
	{
		return _dblLower;
	}

	/**
	 * Retrieve the Upper Probability Bound
	 * 
	 * @return The Upper Probability Bound
	 */

	public double upper()
	{
		return _dblUpper;
	}
}
