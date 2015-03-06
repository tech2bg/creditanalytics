
package org.drip.state.dynamics;

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
 * HeathJarrowMorton sets up and implements the Base No-arbitrage Dynamics of the Rates State Quantifiers as
 * 	formulated in:
 * 
 * 		Heath, D., R. Jarrow, and A. Morton (1992): Bond Pricing and Term Structure of Interest Rates: A New
 * 			Methodology for Contingent Claims Valuation, Econometrica 60 (1), 77-105.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HeathJarrowMorton {
	private org.drip.sequence.random.RandomSequenceGenerator _rsg = null;
	private org.drip.function.deterministic.AbstractUnivariate _auIFRInitial = null;
	private org.drip.analytics.definition.MarketSurface _mktSurfIFRVolatility = null;

	private org.drip.function.deterministic.AbstractUnivariate customDateVolatilityFunction (
		final double dblValueDate)
	{
		org.drip.analytics.definition.TermStructure tsVolatilityValueDate =
			_mktSurfIFRVolatility.strikeAnchorTermStructure (dblValueDate);

		return null == tsVolatilityValueDate ? null : tsVolatilityValueDate.function();
	}

	private double viewTargetVolatilityIntegral (
		final double dblViewDate,
		final double dblTargetDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.AbstractUnivariate auVolatilityFunction =
			customDateVolatilityFunction (dblViewDate);

		if (null == auVolatilityFunction)
			throw new java.lang.Exception
				("HeathJarrowMorton::viewForwardVolatilityIntegral => Cannot extract View Date Volatility Function");

		return auVolatilityFunction.integrate (dblViewDate, dblTargetDate);
	}

	/**
	 * HeathJarrowMorton Constructor
	 * 
	 * @param mktSurfIFRVolatility The Instantaneous Forward Rate Volatility Surface
	 * @param auIFRInitial The Initial Instantaneous Forward Rate Term Structure
	 * @param rsg The Random Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public HeathJarrowMorton (
		final org.drip.analytics.definition.MarketSurface mktSurfIFRVolatility,
		final org.drip.function.deterministic.AbstractUnivariate auIFRInitial,
		final org.drip.sequence.random.RandomSequenceGenerator rsg)
		throws java.lang.Exception
	{
		if (null == (_mktSurfIFRVolatility = mktSurfIFRVolatility) || null == (_auIFRInitial = auIFRInitial))
			throw new java.lang.Exception ("HeathJarrowMorton ctr: Invalid Inputs");

		if (null == (_rsg = rsg)) _rsg = new org.drip.sequence.random.BoxMullerGaussian (0., 1.);
	}

	/**
	 * Retrieve the Instantaneous Forward Rate Volatility Surface
	 * 
	 * @return The Instantaneous Forward Rate Volatility Surface
	 */

	public org.drip.analytics.definition.MarketSurface ifrVolatilitySurface()
	{
		return _mktSurfIFRVolatility;
	}

	/**
	 * Retrieve the Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @return The Initial Instantaneous Forward Rate Term Structure
	 */

	public org.drip.function.deterministic.AbstractUnivariate ifrInitialTermStructure()
	{
		return _auIFRInitial;
	}

	/**
	 * Retrieve the Random Sequence Generator Instance
	 * 
	 * @return The Random Sequence Generator Instance
	 */

	public org.drip.sequence.random.RandomSequenceGenerator rsg()
	{
		return _rsg;
	}

	/**
	 * Compute the Instantaneous Forward Rate Increment given the View Date, the Target Date, and the Time
	 * 	Increment
	 * 
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblTimeIncrement The Time Increment
	 * 
	 * @return The Instantaneous Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Instantaneous Forward Rate Increment cannot be computed
	 */

	public double instantaneousForwardRateIncrement (
		final double dblViewDate,
		final double dblTargetDate,
		final double dblTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTimeIncrement))
			throw new java.lang.Exception
				("HeathJarrowMorton::instantaneousForwardRateIncrement => Invalid Inputs");

		double dblViewTargetDateVol = _mktSurfIFRVolatility.node (dblViewDate, dblTargetDate);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetDateVol))
			throw new java.lang.Exception
				("HeathJarrowMorton::instantaneousForwardRateIncrement => Cannot compute View/Target Date Volatility");

		return viewTargetVolatilityIntegral (dblViewDate, dblTargetDate) * dblViewTargetDateVol *
			dblTimeIncrement + dblViewTargetDateVol * _rsg.random();
	}

	/**
	 * Compute the Proportional Price Increment given the View Date, the Target Date, the Short Rate Process,
	 *  and the Time Increment
	 * 
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param auShortRate The Short Rate Process Function
	 * @param dblTimeIncrement The Time Increment
	 * 
	 * @return The Proportional Price Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Proportional Price Increment cannot be computed
	 */

	public double proportionalPriceIncrement (
		final double dblViewDate,
		final double dblTargetDate,
		final org.drip.function.deterministic.AbstractUnivariate auShortRate,
		final double dblTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= dblViewDate ||
				null == auShortRate || !org.drip.quant.common.NumberUtil.IsValid (dblTimeIncrement))
			throw new java.lang.Exception
				("HeathJarrowMorton::proportionalPriceIncrement => Invalid Inputs");

		return viewTargetVolatilityIntegral (dblViewDate, dblTargetDate) * _rsg.random() -
			auShortRate.evaluate (dblViewDate) * dblTimeIncrement;
	}
}
