
package org.drip.quant.function1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * ExponentialDecay implements the scaled exponential decay Univariate Function.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ExponentialDecay extends org.drip.quant.function1D.AbstractUnivariate {
	private double _dblEpoch = java.lang.Double.NaN;
	private double _dblHazard = java.lang.Double.NaN;

	/**
	 * ExponentialDecay constructor
	 * 
	 * @param dblEpoch The Starting Epoch
	 * @param dblHazard The Exponential Decay Hazard Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public ExponentialDecay (
		final double dblEpoch,
		final double dblHazard)
		throws java.lang.Exception
	{
		super (null);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpoch = dblEpoch) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblHazard = dblHazard))
			throw new java.lang.Exception ("ExponentialDecay ctr => Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("ExponentialDecay::evaluate => Invalid Inputs");

		return java.lang.Math.exp (-1. * _dblHazard * (dblVariate - _dblEpoch));
	}

	@Override public double calcDerivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || 0 >= iOrder)
			throw new java.lang.Exception ("ExponentialDecay::calcDerivative => Invalid Inputs");

		double dblDerivativeFactor = 1;

		for (int i = 0; i < iOrder; ++i)
			dblDerivativeFactor *= (-1. * _dblHazard);

		return dblDerivativeFactor * evaluate (dblVariate);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("ExponentialDecay::integrate => Invalid Inputs");

		return (evaluate (dblEnd) - evaluate (dblBegin)) / (-1. * _dblHazard);
	}
}
