
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
	private org.drip.sequence.random.MultivariateSequenceGenerator _msg = null;
	private org.drip.analytics.definition.MarketSurface[] _aMSInstantaneousForwardRateVolatility = null;
	private org.drip.function.deterministic.AbstractUnivariate _auInitialInstantaneousForwardRate = null;

	private org.drip.function.deterministic.AbstractUnivariate xDateVolatilityFunction (
		final int iFactorIndex,
		final double dblXDate)
	{
		org.drip.analytics.definition.TermStructure tsVolatilityXDate =
			_aMSInstantaneousForwardRateVolatility[iFactorIndex].xAnchorTermStructure (dblXDate);

		return null == tsVolatilityXDate ? null : tsVolatilityXDate.function();
	}

	private double volatilityIntegral (
		final int iFactorIndex,
		final double dblXDate,
		final double dblYDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.AbstractUnivariate auVolatilityFunction = xDateVolatilityFunction
			(iFactorIndex, dblXDate);

		if (null == auVolatilityFunction)
			throw new java.lang.Exception
				("GaussianHJM::volatilityIntegral => Cannot extract X Date Volatility Function");

		return auVolatilityFunction.integrate (dblXDate, dblYDate) / 365.25;
	}

	/**
	 * GaussianHJM Constructor
	 * 
	 * @param aMSInstantaneousForwardRateVolatility Array of the Instantaneous Forward Rate Volatility
	 * 	Surfaces
	 * @param auInitialInstantaneousForwardRate The Initial Instantaneous Forward Rate Term Structure
	 * @param msg Multivariate Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public GaussianHJM (
		final org.drip.analytics.definition.MarketSurface[] aMSInstantaneousForwardRateVolatility,
		final org.drip.function.deterministic.AbstractUnivariate auInitialInstantaneousForwardRate,
		final org.drip.sequence.random.MultivariateSequenceGenerator msg)
		throws java.lang.Exception
	{
		if (null == (_aMSInstantaneousForwardRateVolatility = aMSInstantaneousForwardRateVolatility) || null
			== (_auInitialInstantaneousForwardRate = auInitialInstantaneousForwardRate) || null == (_msg =
				msg))
			throw new java.lang.Exception ("GaussianHJM ctr: Invalid Inputs");

		int iNumFactor = _msg.numVariate();

		if (_aMSInstantaneousForwardRateVolatility.length != iNumFactor)
			throw new java.lang.Exception ("GaussianHJM ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Instantaneous Forward Rate Volatility Surfaces
	 * 
	 * @return The Array of Instantaneous Forward Rate Volatility Surfaces
	 */

	public org.drip.analytics.definition.MarketSurface[] instantaneousForwardVolatilitySurface()
	{
		return _aMSInstantaneousForwardRateVolatility;
	}

	/**
	 * Retrieve the Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @return The Initial Instantaneous Forward Rate Term Structure
	 */

	public org.drip.function.deterministic.AbstractUnivariate instantaneousForwardInitialTermStructure()
	{
		return _auInitialInstantaneousForwardRate;
	}

	/**
	 * Retrieve the Multivariate Sequence Generator
	 * 
	 * @return The Multivariate Sequence Generator
	 */

	public org.drip.sequence.random.MultivariateSequenceGenerator msg()
	{
		return _msg;
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

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		double dblIntantaneousForwardRateIncrement = 0.;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewTargetDatePointVolatility = _aMSInstantaneousForwardRateVolatility[i].node
				(dblViewDate, dblTargetDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetDatePointVolatility))
				throw new java.lang.Exception
					("GaussianHJM::instantaneousForwardRateIncrement => Cannot compute View/Target Date Point Volatility");

			dblIntantaneousForwardRateIncrement += volatilityIntegral (i, dblViewDate, dblTargetDate) *
				dblViewTargetDatePointVolatility * dblViewTimeIncrement + dblViewTargetDatePointVolatility *
					java.lang.Math.sqrt (dblViewTimeIncrement) * adblMultivariateRandom[i];
		}

		return dblIntantaneousForwardRateIncrement;
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

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		double dblProportionalPriceIncrement = dblShortRate * dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i)
			dblProportionalPriceIncrement -= volatilityIntegral (i, dblViewDate, dblTargetDate) *
				java.lang.Math.sqrt (dblViewTimeIncrement) * adblMultivariateRandom[i];

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

		double dblShortRateIncrement = 0.;

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewDatePointVolatility = _aMSInstantaneousForwardRateVolatility[i].node (dblViewDate,
				dblViewDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDatePointVolatility))
				throw new java.lang.Exception
					("GaussianHJM::shortRateIncrement => Cannot compute View Date Point Volatility");

			dblShortRateIncrement += volatilityIntegral (i, dblSpotDate, dblViewDate) *
				dblViewDatePointVolatility * dblViewTimeIncrement + dblViewDatePointVolatility *
					java.lang.Math.sqrt (dblViewTimeIncrement) * adblMultivariateRandom[i];
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

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		double dblCompoundedShortRateIncrement = (dblCompoundedShortRate - dblShortRate) *
			dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewTargetVolatilityIntegral = volatilityIntegral (i, dblViewDate, dblTargetDate);

			dblCompoundedShortRateIncrement += 0.5 * dblViewTargetVolatilityIntegral *
				dblViewTargetVolatilityIntegral * dblViewTimeIncrement + dblViewTargetVolatilityIntegral *
					java.lang.Math.sqrt (dblViewTimeIncrement) * adblMultivariateRandom[i];
		}

		return dblCompoundedShortRateIncrement * 365.25 / (dblTargetDate - dblViewDate);
	}

	/**
	 * Compute the LIBOR Forward Rate Increment given the Spot Date, the View Date, the Target Date, the
	 *  Current LIBOR Forward Rate, and the View Time Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblLIBORForwardRate The LIBOR Forward Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the LIBOR Forward Rate Increment cannot be computed
	 */

	public double liborForwardRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblTargetDate,
		final double dblLIBORForwardRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblViewDate >= dblTargetDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblLIBORForwardRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::liborForwardRateIncrement => Invalid Inputs");

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		double dblLIBORForwardRateVolIncrement = 0.;

		for (int i = 0; i < iNumFactor; ++i)
			dblLIBORForwardRateVolIncrement += volatilityIntegral (i, dblViewDate, dblTargetDate) *
				(volatilityIntegral (i, dblSpotDate, dblTargetDate) + java.lang.Math.sqrt
					(dblViewTimeIncrement) * adblMultivariateRandom[i]);

		return (dblLIBORForwardRate + (365.25 / (dblTargetDate - dblViewDate))) *
			dblLIBORForwardRateVolIncrement;
	}

	/**
	 * Compute the Shifted LIBOR Forward Rate Increment given the Spot Date, the View Date, the Target Date,
	 * 	the Current Shifted LIBOR Forward Rate, and the View Time Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblTargetDate The Target Date
	 * @param dblShiftedLIBORForwardRate The Shifted LIBOR Forward Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Shifted Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Shifted LIBOR Forward Rate Increment cannot be computed
	 */

	public double shiftedLIBORForwardIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblTargetDate,
		final double dblShiftedLIBORForwardRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblViewDate >= dblTargetDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblShiftedLIBORForwardRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("GaussianHJM::shiftedLIBORForwardIncrement => Invalid Inputs");

		int iNumFactor = _msg.numVariate();

		double[] adblMultivariateRandom = _msg.random();

		double dblShiftedLIBORVolIncrement = 0.;

		for (int i = 0; i < iNumFactor; ++i)
			dblShiftedLIBORVolIncrement += volatilityIntegral (i, dblViewDate, dblTargetDate) *
				(volatilityIntegral (i, dblSpotDate, dblTargetDate) + java.lang.Math.sqrt
					(dblViewTimeIncrement) * adblMultivariateRandom[i]);

		return dblShiftedLIBORForwardRate * dblShiftedLIBORVolIncrement;
	}

	public org.drip.state.dynamics.QMSnapshot qmIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblTargetDate,
		final double dblViewTimeIncrement,
		final org.drip.state.dynamics.QMSnapshot qmInitial)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTargetDate) || dblViewDate >= dblTargetDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || null == qmInitial)
			return null;

		int iNumFactor = _msg.numVariate();

		double dblInitialPrice = qmInitial.price();

		double[] adblMultivariateRandom = _msg.random();

		double dblInitialShortRate = qmInitial.shortRate();

		double dblInitialLIBORForwardRate = qmInitial.liborForwardRate();

		double dblInitialCompoundedShortRate = qmInitial.compoundedShortRate();

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		double dblShortRateIncrement = 0.;
		double dblShiftedLIBORForwardRateIncrement = 0.;
		double dblInstantaneousForwardRateIncrement = 0.;
		double dblPriceIncrement = dblInitialShortRate * dblViewTimeIncrement;
		double dblCompoundedShortRateIncrement = (dblInitialCompoundedShortRate - dblInitialShortRate) *
			dblViewTimeIncrement;

		try {
			for (int i = 0; i < iNumFactor; ++i) {
				double dblViewViewPointVolatility = _aMSInstantaneousForwardRateVolatility[i].node
					(dblViewDate, dblViewDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewViewPointVolatility)) return null;

				double dblViewTargetPointVolatility = _aMSInstantaneousForwardRateVolatility[i].node
					(dblViewDate, dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetPointVolatility)) return null;

				double dblViewTargetVolatilityIntegral = volatilityIntegral (i, dblViewDate, dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetVolatilityIntegral)) return null;

				double dblSpotViewVolatilityIntegral = volatilityIntegral (i, dblSpotDate, dblViewDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotViewVolatilityIntegral)) return null;

				double dblSpotTargetVolatilityIntegral = volatilityIntegral (i, dblSpotDate, dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotTargetVolatilityIntegral)) return null;

				double dblScaledMultivariateRandom = dblViewTimeIncrementSQRT * adblMultivariateRandom[i];
				dblInstantaneousForwardRateIncrement += dblViewTargetVolatilityIntegral *
					dblViewTargetPointVolatility * dblViewTimeIncrement + dblViewTargetPointVolatility *
						dblScaledMultivariateRandom;
				dblShortRateIncrement += dblSpotViewVolatilityIntegral * dblViewViewPointVolatility *
					dblViewTimeIncrement + dblViewViewPointVolatility * dblScaledMultivariateRandom;
				dblCompoundedShortRateIncrement += 0.5 * dblViewTargetVolatilityIntegral *
					dblViewTargetVolatilityIntegral * dblViewTimeIncrement + dblViewTargetVolatilityIntegral
						* dblScaledMultivariateRandom;
				dblShiftedLIBORForwardRateIncrement += dblViewTargetVolatilityIntegral *
					(dblSpotTargetVolatilityIntegral + dblScaledMultivariateRandom);
				dblPriceIncrement -= dblViewTargetVolatilityIntegral * dblScaledMultivariateRandom;
			}

			dblPriceIncrement *= dblInitialPrice;
			dblCompoundedShortRateIncrement *= 365.25 / (dblTargetDate - dblViewDate);
			double dblLIBORForwardRateIncrement = (dblInitialLIBORForwardRate + (365.25 / (dblTargetDate -
				dblViewDate))) * dblShiftedLIBORForwardRateIncrement;

			return new org.drip.state.dynamics.QMSnapshot (qmInitial.instantaneousForwardRate() +
				dblInstantaneousForwardRateIncrement, dblInstantaneousForwardRateIncrement,
					dblInitialLIBORForwardRate + dblLIBORForwardRateIncrement, dblLIBORForwardRateIncrement,
						qmInitial.shiftedLIBORForwardRate() + dblShiftedLIBORForwardRateIncrement,
							dblShiftedLIBORForwardRateIncrement, dblInitialShortRate + dblShortRateIncrement,
								dblShortRateIncrement, dblInitialCompoundedShortRate +
									dblCompoundedShortRateIncrement, dblCompoundedShortRateIncrement,
										dblInitialPrice + dblPriceIncrement, dblPriceIncrement);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
