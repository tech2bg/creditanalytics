
package org.drip.dynamics.lmm;

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
 * ContinuouslyCompoundedForwardRate implements the Continuously Compounded Forward Rate defined in the LIBOR
 *  Market Model. The Reference is:
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuouslyCompoundedForwardRate {
	private double _dblSpotDate = java.lang.Double.NaN;
	private org.drip.function.stochastic.R1R1ToR1 _funcR1R1ToR1 = null;

	/**
	 * ContinuouslyCompoundedForwardRate Constructor
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param funcR1R1ToR1 The Stochastic Forward Rate Function
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuouslyCompoundedForwardRate (
		final double dblSpotDate,
		final org.drip.function.stochastic.R1R1ToR1 funcR1R1ToR1)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSpotDate = dblSpotDate) || null == (_funcR1R1ToR1
			= funcR1R1ToR1))
			throw new java.lang.Exception ("ContinuouslyCompoundedForwardRate ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Spot Date
	 * 
	 * @return The Spot Date
	 */

	public double spotDate()
	{
		return _dblSpotDate;
	}

	/**
	 * Retrieve the Stochastic Forward Rate Function
	 * 
	 * @return The Stochastic Forward Rate Function
	 */

	public org.drip.function.stochastic.R1R1ToR1 stochasticForwardRateFunction()
	{
		return _funcR1R1ToR1;
	}

	/**
	 * Retrieve a Realized Zero-Coupon Bond Price
	 * 
	 * @param dblMaturityDate The Maturity Date
	 * 
	 * @return The Realized Zero-Coupon Bond Price
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double realizedZeroCouponPrice (
		final double dblMaturityDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblMaturityDate) || dblMaturityDate <= _dblSpotDate)
			throw new java.lang.Exception
				("ContinuouslyCompoundedForwardRate::realizedZeroCouponPrice => Invalid Maturity Date");

		return java.lang.Math.exp (-1. * _funcR1R1ToR1.integralRealization (0., dblMaturityDate -
			_dblSpotDate));
	}

	/**
	 * Compute the Realized/Expected Instantaneous Forward Rate Integral to the Target Date
	 * 
	 * @param dblTargetDate The Target Date
	 * @param bRealized TRUE => Compute the Realized (TRUE) / Expected (FALSE) Instantaneous Forward Rate
	 *  Integral
	 * 
	 * @return The Realized/Expected Instantaneous Forward Rate Integral
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double instantaneousForwardRateIntegral (
		final double dblTargetDate,
		final boolean bRealized)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= _dblSpotDate)
			throw new java.lang.Exception
				("ContinuouslyCompoundedForwardRate::instantaneousForwardRateIntegral => Invalid Target Date");

		return bRealized ? java.lang.Math.exp (-1. * _funcR1R1ToR1.integralRealization (0., dblTargetDate -
			_dblSpotDate)) : java.lang.Math.exp (-1. * _funcR1R1ToR1.integralExpectation (0., dblTargetDate -
				_dblSpotDate));
	}

	/**
	 * Retrieve a Realized/Expected Value of the Discount to the Target Date
	 * 
	 * @param dblTargetDate The Target Date
	 * @param bRealized TRUE => Compute the Realized (TRUE) / Expected (FALSE) Instantaneous Forward Rate
	 *  Integral
	 * 
	 * @return The Realized/Expected Value of the Discount to the Target Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double discountFunctionValue (
		final double dblTargetDate,
		final boolean bRealized)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= _dblSpotDate)
			throw new java.lang.Exception
				("ContinuouslyCompoundedForwardRate::discountFunctionValue => Invalid Target Date");

		return bRealized ? java.lang.Math.exp (-1. * _funcR1R1ToR1.integralRealization (0., dblTargetDate -
			_dblSpotDate)) : java.lang.Math.exp (-1. * _funcR1R1ToR1.integralExpectation (0., dblTargetDate -
				_dblSpotDate));
	}
}
