
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
 * BoundedGaussian implements the Bounded Gaussian Distribution, with a Gaussian Distribution between a lower
 *  and an upper Bound.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedGaussian extends org.drip.quant.discrete.BoxMullerGaussian {
	private double _dblLowerBound = java.lang.Double.NaN;
	private double _dblUpperBound = java.lang.Double.NaN;

	/**
	 * BoundedGaussian Constructor
	 * 
	 * @param dblMean The Mean
	 * @param dblVariance The Variance
	 * @param dblLowerBound The Lower Bound
	 * @param dblUpperBound The Upper Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BoundedGaussian (
		final double dblMean,
		final double dblVariance,
		final double dblLowerBound,
		final double dblUpperBound)
		throws java.lang.Exception
	{
		super (dblMean, dblVariance);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLowerBound = dblLowerBound) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblUpperBound = dblUpperBound) || dblUpperBound <=
				dblLowerBound)
			throw new java.lang.Exception ("BoundedGaussian ctr: Invalid Inputs");
	}

	@Override public double random()
	{
		double dblGaussian = super.random();

		while (dblGaussian < _dblLowerBound || dblGaussian > _dblUpperBound)
			dblGaussian = super.random();

		return dblGaussian;
	}
}
