
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
 * GaussianHJM sets up and implements the Base No-arbitrage Dynamics of the Rates State Quantifiers as
 * 	formulated in:
 * 
 * 		Heath, D., R. Jarrow, and A. Morton (1992): Bond Pricing and Term Structure of Interest Rates: A New
 * 			Methodology for Contingent Claims Valuation, Econometrica 60 (1), 77-105.
 *
 * In particular it looks to evolve Gaussian Instantaneous Forward Rates.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GaussianHJM {
	private org.drip.sequence.random.RandomSequenceGenerator[] _aRSG = null;
	private org.drip.function.deterministic.AbstractUnivariate _auIFRInitial = null;
	private org.drip.analytics.definition.MarketSurface[] _aIFRVolatilitySurface = null;

	private org.drip.function.deterministic.AbstractUnivariate customDateVolatilityFunction (
		final int iIndex,
		final double dblViewDate)
	{
		org.drip.analytics.definition.TermStructure tsVolatilityViewDate =
			_aIFRVolatilitySurface[iIndex].xAnchorTermStructure (dblViewDate);

		return null == tsVolatilityViewDate ? null : tsVolatilityViewDate.function();
	}

	private double viewTargetVolatilityIntegral (
		final int iIndex,
		final double dblViewDate,
		final double dblTargetDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.AbstractUnivariate auVolatilityFunction =
			customDateVolatilityFunction (iIndex, dblViewDate);

		if (null == auVolatilityFunction)
			throw new java.lang.Exception
				("GaussianHJM::viewTargetVolatilityIntegral => Cannot extract View Date Volatility Function");

		return auVolatilityFunction.integrate (dblViewDate, dblTargetDate) / 365.25;
	}

	/**
	 * GaussianHJM Constructor
	 * 
	 * @param aIFRVolatilitySurface Array of the Instantaneous Forward Rate Volatility Surfaces
	 * @param auIFRInitial The Initial Instantaneous Forward Rate Term Structure
	 * @param aRSG Array of Random Sequence Generators
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public GaussianHJM (
		final org.drip.analytics.definition.MarketSurface[] aIFRVolatilitySurface,
		final org.drip.function.deterministic.AbstractUnivariate auIFRInitial,
		final org.drip.sequence.random.RandomSequenceGenerator[] aRSG)
		throws java.lang.Exception
	{
		if (null == (_aIFRVolatilitySurface = aIFRVolatilitySurface) || null == (_auIFRInitial =
			auIFRInitial) || null == (_aRSG = aRSG))
			throw new java.lang.Exception ("GaussianHJM ctr: Invalid Inputs");

		int iNumFactor = _aRSG.length;

		if (_aIFRVolatilitySurface.length != iNumFactor)
			throw new java.lang.Exception ("GaussianHJM ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Instantaneous Forward Rate Volatility Surfaces
	 * 
	 * @return The Array of Instantaneous Forward Rate Volatility Surfaces
	 */

	public org.drip.analytics.definition.MarketSurface[] ifrVolatilitySurface()
	{
		return _aIFRVolatilitySurface;
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
	 * Retrieve the Array of Random Sequence Generator Instances
	 * 
	 * @return The Array of Random Sequence Generator Instances
	 */

	public org.drip.sequence.random.RandomSequenceGenerator[] rsg()
	{
		return _aRSG;
	}

	/**
	 * Compute the Instantaneous Forward Rate Increment given the View Date, the Target Date, and the View
	 * 	Time Increment
	 * 
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Instantaneous Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Instantaneous Forward Rate Increment cannot be computed
	 */

	public double instantaneousForwardRateIncrement (
		final double dblViewDate,
		final double dblTargetDate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception
				("GaussianHJM::instantaneousForwardRateIncrement => Invalid Inputs");

		double dblIFRIncrement = 0.;
		int iNumFactor = _aRSG.length;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewTargetDateVol = _aIFRVolatilitySurface[i].node (dblViewDate, dblTargetDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetDateVol))
				throw new java.lang.Exception
					("GaussianHJM::instantaneousForwardRateIncrement => Cannot compute View/Target Date Volatility");

			dblIFRIncrement += viewTargetVolatilityIntegral (i, dblViewDate, dblTargetDate) *
				dblViewTargetDateVol * dblViewTimeIncrement + dblViewTargetDateVol * java.lang.Math.sqrt
					(dblViewTimeIncrement) * _aRSG[i].random();
		}

		return dblIFRIncrement;
	}

	/**
	 * Compute the Proportional Price Increment given the View Date, the Target Date, the Short Rate, and the
	 *  View Time Increment
	 * 
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblShortRate The Short Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Proportional Price Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Proportional Price Increment cannot be computed
	 */

	public double proportionalPriceIncrement (
		final double dblViewDate,
		final double dblTargetDate,
		final double dblShortRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblTargetDate <= dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblShortRate) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::proportionalPriceIncrement => Invalid Inputs");

		int iNumFactor = _aRSG.length;
		double dblProportionalPriceIncrement = dblShortRate * dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i)
			dblProportionalPriceIncrement -= viewTargetVolatilityIntegral (i, dblViewDate, dblTargetDate) *
				java.lang.Math.sqrt (dblViewTimeIncrement) * _aRSG[i].random();

		return dblProportionalPriceIncrement;
	}

	/**
	 * Compute the Short Rate Increment given the Spot Date, the View Date, and the View Time Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Short Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Short Rate Increment cannot be computed
	 */

	public double shortRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::shortRateIncrement => Invalid Inputs");

		int iNumFactor = _aRSG.length;
		double dblShortRateIncrement = 0.;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewDateVol = _aIFRVolatilitySurface[i].node (dblViewDate, dblViewDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDateVol))
				throw new java.lang.Exception
					("GaussianHJM::shortRateIncrement => Cannot compute View Date Volatility");

			dblShortRateIncrement += viewTargetVolatilityIntegral (i, dblSpotDate, dblViewDate) *
				dblViewDateVol * dblViewTimeIncrement + dblViewDateVol * java.lang.Math.sqrt
					(dblViewTimeIncrement) * _aRSG[i].random();
		}

		return dblShortRateIncrement;
	}

	/**
	 * Compute the Continuously Compounded Short Rate Increment given the Spot Date, the View Date, the
	 *  Target Date, the Continuously Compounded Short Rate, the Current Short Rate, and the View Time
	 *  Increment.
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblCompoundedShortRate The Compounded Short Rate
	 * @param dblShortRate The Short Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Short Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Continuously Compounded Short Rate Increment cannot be
	 * computed
	 */

	public double compoundedShortRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblTargetDate,
		final double dblCompoundedShortRate,
		final double dblShortRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblViewDate >= dblTargetDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblCompoundedShortRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblShortRate) ||
							!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::compoundedShortRateIncrement => Invalid Inputs");

		int iNumFactor = _aRSG.length;
		double dblCompoundedShortRateIncrement = (dblCompoundedShortRate - dblShortRate) *
			dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewTargetVolatilityIntegral = viewTargetVolatilityIntegral (i, dblViewDate,
				dblTargetDate);

			dblCompoundedShortRateIncrement += 0.5 * dblViewTargetVolatilityIntegral *
				dblViewTargetVolatilityIntegral * dblViewTimeIncrement + dblViewTargetVolatilityIntegral *
					java.lang.Math.sqrt (dblViewTimeIncrement) * _aRSG[i].random();
		}

		return dblCompoundedShortRateIncrement * 365.25 / (dblTargetDate - dblViewDate);
	}

	/**
	 * Compute the Forward Rate Increment given the Spot Date, the View Date, the Target Date, the Current
	 *  Forward Rate, and the View Time Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblForwardRate The Forward Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate Increment cannot be computed
	 */

	public double forwardRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblTargetDate,
		final double dblForwardRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblViewDate >= dblTargetDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblForwardRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::forwardRateIncrement => Invalid Inputs");

		int iNumFactor = _aRSG.length;
		double dblForwardRateIncrement = 0.;
		double dblViewTargetVolatilityIntegral = 0.;

		for (int i = 0; i < iNumFactor; ++i) {
			dblViewTargetVolatilityIntegral += viewTargetVolatilityIntegral (i, dblViewDate, dblTargetDate);

			dblForwardRateIncrement += viewTargetVolatilityIntegral (i, dblSpotDate, dblTargetDate) +
				java.lang.Math.sqrt (dblViewTimeIncrement) * _aRSG[i].random();
		}

		return (dblForwardRate + (365.25 / (dblViewDate - dblSpotDate))) * dblViewTargetVolatilityIntegral *
			dblForwardRateIncrement;
	}
}
