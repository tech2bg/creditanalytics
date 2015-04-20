
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
 * MultiFactorBGMEvolver sets up and implements the Multi-Factor No-arbitrage Dynamics of the Rates State
 *  Quantifiers as formulated in:
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiFactorBGMEvolver implements org.drip.dynamics.evolution.StateEvolver {
	private org.drip.dynamics.hjm.MultiFactorVolatility _mfv = null;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;

	private double volatilityRandomDotProduct (
		final double dblViewDate,
		final double dblTargetDate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg = _mfv.msg();

		double[] adblMultivariateRandom = pfsg.random();

		double dblVolatilityRandomDotProduct = 0.;

		int iNumFactor = pfsg.numFactor();

		for (int i = 0; i < iNumFactor; ++i)
			dblVolatilityRandomDotProduct += _mfv.factorPointVolatility (i, dblViewDate, dblTargetDate) *
				adblMultivariateRandom[i] * dblViewTimeIncrementSQRT;

		return dblVolatilityRandomDotProduct;
	}

	private double volatilityRandomDotDerivative (
		final double dblViewDate,
		final double dblTargetDate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 pointVolatilityFunctionR1ToR1 = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return volatilityRandomDotProduct (dblViewDate, dblX, dblViewTimeIncrement);
			}
		};

		return pointVolatilityFunctionR1ToR1.derivative (dblTargetDate, 1);
	}

	/**
	 * MultiFactorBGMEvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param mfv The Multi-Factor Volatility Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public MultiFactorBGMEvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final org.drip.dynamics.hjm.MultiFactorVolatility mfv,
		final org.drip.function.deterministic.R1ToR1 auInitialInstantaneousForwardRate)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || null == (_mfv = mfv))
			throw new java.lang.Exception ("MultiFactorBGMEvolver ctr: Invalid Inputs");
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

	@Override public org.drip.dynamics.lmm.BGMUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.evolution.LSQMUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || (null != lsqmPrev &&
					!(lsqmPrev instanceof org.drip.dynamics.lmm.BGMUpdate)))
			return null;

		org.drip.dynamics.lmm.BGMUpdate bgmPrev = (org.drip.dynamics.lmm.BGMUpdate) lsqmPrev;

		double dblDContinuousForwardDX = bgmPrev.dContinuousForwardDX();

		try {
			return org.drip.dynamics.lmm.BGMUpdate.Create (_lslFunding, _lslForward, dblViewDate, dblViewDate
				+ dblViewTimeIncrement, bgmPrev.continuousForwardRate(), dblDContinuousForwardDX + 0.5 *
					dblViewTimeIncrement * _mfv.pointVolatilityNormDerivative (dblSpotDate, dblViewDate, 1,
						true) + volatilityRandomDotDerivative (dblSpotDate, dblViewDate,
							dblViewTimeIncrement), dblDContinuousForwardDX);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
