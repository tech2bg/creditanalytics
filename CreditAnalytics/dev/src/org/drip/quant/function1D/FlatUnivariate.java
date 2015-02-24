
package org.drip.quant.function1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * FlatUnivariate implements the level constant Univariate Function.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FlatUnivariate extends org.drip.quant.function.AbstractUnivariate {
	private double _dblLevel = java.lang.Double.NaN;

	/**
	 * FlatUnivariate constructor
	 * 
	 * @param dblLevel The FlatUnivariate Level
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public FlatUnivariate (
		final double dblLevel)
		throws java.lang.Exception
	{
		super (null);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLevel = dblLevel))
			throw new java.lang.Exception ("FlatUnivariate ctr => Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("FlatUnivariate::evaluate => Invalid Inputs");

		return _dblLevel;
	}

	@Override public org.drip.quant.calculus.Differential calcDifferential (
		final double dblVariate,
		final double dblOFBase,
		final int iOrder)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || 0 >= iOrder) return null;

		try {
			return new org.drip.quant.calculus.Differential (_dc.getVariateInfinitesimal (dblVariate), 0.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("FlatUnivariate::integrate => Invalid Inputs");

		return (dblEnd - dblBegin) * _dblLevel;
	}
}
