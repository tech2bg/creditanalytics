
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
 * LognormalLIBORCurveEvolver sets up and implements the Multi-Factor No-arbitrage Dynamics of the full Curve
 * 	Rates State Quantifiers traced from the Evolution of the LIBOR Forward Rate as formulated in:
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LognormalLIBORCurveEvolver implements org.drip.dynamics.evolution.CurveStateEvolver {
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private org.drip.dynamics.lmm.LognormalLIBORVolatility _llv = null;

	private double forwardDerivative (
		final org.drip.analytics.rates.ForwardCurve fc,
		final double dblViewDate)
		throws java.lang.Exception
	{
		if (null == fc)
			throw new java.lang.Exception
				("LognormalLIBORCurveEvolver::forwardDerivative => Invalid Inputs");

		org.drip.function.deterministic.R1ToR1 freR1ToR1 = new org.drip.function.deterministic.R1ToR1 (null)
		{
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				return fc.forward (dblDate);
			}
		};

		return freR1ToR1.derivative (dblViewDate, 1);
	}

	/**
	 * LognormalLIBORCurveEvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param llv The Log-normal LIBOR Volatility Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public LognormalLIBORCurveEvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || null == (_llv = llv))
			throw new java.lang.Exception ("LognormalLIBORCurveEvolver ctr: Invalid Inputs");
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
	 * Retrieve the Log-normal LIBOR Volatility Instance
	 * 
	 * @return The Log-normal LIBOR Volatility Instance
	 */

	public org.drip.dynamics.lmm.LognormalLIBORVolatility llv()
	{
		return _llv;
	}

	@Override public org.drip.dynamics.lmm.BGMCurveUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.evolution.LSQMCurveUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || null == lsqmPrev ||
					!(lsqmPrev instanceof org.drip.dynamics.lmm.BGMCurveUpdate))
			return null;

		org.drip.dynamics.lmm.BGMCurveUpdate bgmPrev = (org.drip.dynamics.lmm.BGMCurveUpdate) lsqmPrev;

		java.lang.String strTenor = _lslForward.tenor();

		double[] adblMultivariateRandom = _llv.msg().random();

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		org.drip.analytics.rates.ForwardCurve fcLIBOR = bgmPrev.libor();

		org.drip.analytics.rates.ForwardCurve fcContinuouslyCompounded = bgmPrev.continuousForwardRate();

		org.drip.analytics.rates.DiscountCurve dcSpotRate = bgmPrev.spotRate();

		org.drip.analytics.rates.DiscountCurve dcDiscountFactor = bgmPrev.discountFactor();

		return null;
	}
}
