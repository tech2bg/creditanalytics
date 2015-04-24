
package org.drip.dynamics.hjm;

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
 * MultiFactorStateEvolver sets up and implements the Base Multi-Factor No-arbitrage Dynamics of the Rates
 * 	State Quantifiers as formulated in:
 * 
 * 		Heath, D., R. Jarrow, and A. Morton (1992): Bond Pricing and Term Structure of Interest Rates: A New
 * 			Methodology for Contingent Claims Valuation, Econometrica 60 (1), 77-105.
 *
 * In particular it looks to evolve the Multi-factor Instantaneous Forward Rates.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiFactorStateEvolver implements org.drip.dynamics.evolution.PointStateEvolver {
	private org.drip.dynamics.hjm.MultiFactorVolatility _mfv = null;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private org.drip.function.deterministic.R1ToR1 _auInitialInstantaneousForwardRate = null;

	/**
	 * MultiFactorStateEvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param mfv The Multi-Factor Volatility Instance
	 * @param auInitialInstantaneousForwardRate The Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public MultiFactorStateEvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final org.drip.dynamics.hjm.MultiFactorVolatility mfv,
		final org.drip.function.deterministic.R1ToR1 auInitialInstantaneousForwardRate)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || null == (_mfv = mfv)
			|| null == (_auInitialInstantaneousForwardRate = auInitialInstantaneousForwardRate))
			throw new java.lang.Exception ("MultiFactorStateEvolver ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return _lslFunding;
	}

	/**
	 * Retrieve the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _lslForward;
	}

	/**
	 * Retrieve the Multi-factor Volatility Instance
	 * 
	 * @return The Multi-factor Volatility Instance
	 */

	public org.drip.dynamics.hjm.MultiFactorVolatility mfv()
	{
		return _mfv;
	}

	/**
	 * Retrieve the Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @return The Initial Instantaneous Forward Rate Term Structure
	 */

	public org.drip.function.deterministic.R1ToR1 instantaneousForwardInitialTermStructure()
	{
		return _auInitialInstantaneousForwardRate;
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
				("MultiFactorStateEvolver::instantaneousForwardRateIncrement => Invalid Inputs");

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		int iNumFactor = pfsg.numFactor();

		double[] adblMultivariateRandom = pfsg.random();

		double dblIntantaneousForwardRateIncrement = 0.;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblWeightedFactorPointVolatility = _mfv.weightedFactorPointVolatility (i, dblViewDate,
				dblTargetDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblWeightedFactorPointVolatility))
				throw new java.lang.Exception
					("MultiFactorStateEvolver::instantaneousForwardRateIncrement => Cannot compute View/Target Date Point Volatility");

			dblIntantaneousForwardRateIncrement += _mfv.volatilityIntegral (i, dblViewDate, dblTargetDate) *
				dblWeightedFactorPointVolatility * dblViewTimeIncrement + dblWeightedFactorPointVolatility *
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
			throw new java.lang.Exception
				("MultiFactorStateEvolver::proportionalPriceIncrement => Invalid Inputs");

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		int iNumFactor = pfsg.numFactor();

		double[] adblMultivariateRandom = pfsg.random();

		double dblProportionalPriceIncrement = dblShortRate * dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i)
			dblProportionalPriceIncrement -= _mfv.volatilityIntegral (i, dblViewDate, dblTargetDate) *
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
			throw new java.lang.Exception ("MultiFactorStateEvolver::shortRateIncrement => Invalid Inputs");

		double dblShortRateIncrement = 0.;

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		int iNumFactor = pfsg.numFactor();

		double[] adblMultivariateRandom = pfsg.random();

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewWeightedFactorVolatility = _mfv.weightedFactorPointVolatility (i, dblViewDate,
				dblViewDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblViewWeightedFactorVolatility))
				throw new java.lang.Exception
					("MultiFactorStateEvolver::shortRateIncrement => Cannot compute View Date Factor Volatility");

			dblShortRateIncrement += _mfv.volatilityIntegral (i, dblSpotDate, dblViewDate) *
				dblViewWeightedFactorVolatility * dblViewTimeIncrement + dblViewWeightedFactorVolatility *
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
			throw new java.lang.Exception
				("MultiFactorStateEvolver::compoundedShortRateIncrement => Invalid Inputs");

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		int iNumFactor = pfsg.numFactor();

		double[] adblMultivariateRandom = pfsg.random();

		double dblCompoundedShortRateIncrement = (dblCompoundedShortRate - dblShortRate) *
			dblViewTimeIncrement;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblViewTargetVolatilityIntegral = _mfv.volatilityIntegral (i, dblViewDate, dblTargetDate);

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
			throw new java.lang.Exception
				("MultiFactorStateEvolver::liborForwardRateIncrement => Invalid Inputs");

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		double[] adblMultivariateRandom = pfsg.random();

		double dblLIBORForwardRateVolIncrement = 0.;

		int iNumFactor = pfsg.numFactor();

		for (int i = 0; i < iNumFactor; ++i)
			dblLIBORForwardRateVolIncrement += _mfv.volatilityIntegral (i, dblViewDate, dblTargetDate) *
				(_mfv.volatilityIntegral (i, dblSpotDate, dblTargetDate) + java.lang.Math.sqrt
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
			throw new java.lang.Exception
				("MultiFactorStateEvolver::shiftedLIBORForwardIncrement => Invalid Inputs");

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		double[] adblMultivariateRandom = pfsg.random();

		double dblShiftedLIBORVolIncrement = 0.;

		int iNumFactor = pfsg.numFactor();

		for (int i = 0; i < iNumFactor; ++i)
			dblShiftedLIBORVolIncrement += _mfv.volatilityIntegral (i, dblViewDate, dblTargetDate) *
				(_mfv.volatilityIntegral (i, dblSpotDate, dblTargetDate) + java.lang.Math.sqrt
					(dblViewTimeIncrement) * adblMultivariateRandom[i]);

		return dblShiftedLIBORForwardRate * dblShiftedLIBORVolIncrement;
	}

	@Override public org.drip.dynamics.evolution.LSQMPointUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.evolution.LSQMPointUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || null == lsqmPrev ||
					!(lsqmPrev instanceof org.drip.dynamics.hjm.ShortForwardRateUpdate))
			return null;

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		double[] adblMultivariateRandom = pfsg.random();

		int iNumFactor = pfsg.numFactor();

		org.drip.dynamics.hjm.ShortForwardRateUpdate qmInitial =
			(org.drip.dynamics.hjm.ShortForwardRateUpdate) lsqmPrev;

		try {
			double dblInitialPrice = qmInitial.price();

			double dblInitialShortRate = qmInitial.shortRate();

			double dblInitialLIBORForwardRate = qmInitial.liborForwardRate();

			double dblInitialCompoundedShortRate = qmInitial.compoundedShortRate();

			double dblTargetDate = new org.drip.analytics.date.JulianDate (dblViewDate).addTenor
				(_lslForward.tenor()).julian();

			double dblShortRateIncrement = 0.;
			double dblShiftedLIBORForwardRateIncrement = 0.;
			double dblInstantaneousForwardRateIncrement = 0.;
			double dblPriceIncrement = dblInitialShortRate * dblViewTimeIncrement;
			double dblCompoundedShortRateIncrement = (dblInitialCompoundedShortRate - dblInitialShortRate) *
				dblViewTimeIncrement;

			for (int i = 0; i < iNumFactor; ++i) {
				double dblViewDateFactorVolatility = _mfv.weightedFactorPointVolatility (i, dblViewDate,
					dblViewDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewDateFactorVolatility)) return null;

				double dblViewTargetFactorVolatility = _mfv.weightedFactorPointVolatility (i, dblViewDate,
					dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetFactorVolatility)) return null;

				double dblViewTargetVolatilityIntegral = _mfv.volatilityIntegral (i, dblViewDate,
					dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblViewTargetVolatilityIntegral)) return null;

				double dblSpotViewVolatilityIntegral = _mfv.volatilityIntegral (i, dblSpotDate, dblViewDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotViewVolatilityIntegral)) return null;

				double dblSpotTargetVolatilityIntegral = _mfv.volatilityIntegral (i, dblSpotDate,
					dblTargetDate);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotTargetVolatilityIntegral)) return null;

				double dblScaledMultivariateRandom = dblViewTimeIncrementSQRT * adblMultivariateRandom[i];
				dblInstantaneousForwardRateIncrement += dblViewTargetVolatilityIntegral *
					dblViewTargetFactorVolatility * dblViewTimeIncrement + dblViewTargetFactorVolatility *
						dblScaledMultivariateRandom;
				dblShortRateIncrement += dblSpotViewVolatilityIntegral * dblViewDateFactorVolatility *
					dblViewTimeIncrement + dblViewDateFactorVolatility * dblScaledMultivariateRandom;
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

			return org.drip.dynamics.hjm.ShortForwardRateUpdate.Create (_lslFunding, _lslForward,
				dblViewDate, dblTargetDate, qmInitial.instantaneousForwardRate() +
					dblInstantaneousForwardRateIncrement, dblInstantaneousForwardRateIncrement,
						dblInitialLIBORForwardRate + dblLIBORForwardRateIncrement,
							dblLIBORForwardRateIncrement, qmInitial.shiftedLIBORForwardRate() +
								dblShiftedLIBORForwardRateIncrement, dblShiftedLIBORForwardRateIncrement,
									dblInitialShortRate + dblShortRateIncrement, dblShortRateIncrement,
										dblInitialCompoundedShortRate + dblCompoundedShortRateIncrement,
											dblCompoundedShortRateIncrement, dblInitialPrice +
												dblPriceIncrement, dblPriceIncrement);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
