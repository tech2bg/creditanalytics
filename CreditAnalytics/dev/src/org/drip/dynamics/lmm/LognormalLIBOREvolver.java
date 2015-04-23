
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
 * LognormalLIBOREvolver sets up and implements the Multi-Factor No-arbitrage Dynamics of the Rates State
 *  Quantifiers traced from the Evolution of the LIBOR Forward Rate as formulated in:
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LognormalLIBOREvolver implements org.drip.dynamics.evolution.StateEvolver {
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private org.drip.analytics.rates.ForwardRateEstimator _fre = null;
	private org.drip.dynamics.lmm.LognormalLIBORVolatility _llv = null;

	private double freDerivative (
		final double dblViewDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 freR1ToR1 = new org.drip.function.deterministic.R1ToR1 (null)
		{
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				return _fre.forward (dblDate);
			}
		};

		return freR1ToR1.derivative (dblViewDate, 1);
	}

	/**
	 * LognormalLIBOREvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param llv The Log-normal LIBOR Volatility Instance
	 * @param fre The Forward Rate Estimator Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public LognormalLIBOREvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv,
		final org.drip.analytics.rates.ForwardRateEstimator fre)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || null == (_llv = llv)
			|| null == (_fre = fre))
			throw new java.lang.Exception ("LognormalLIBOREvolver ctr: Invalid Inputs");
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

	/**
	 * Retrieve the Forward Rate Estimator Instance
	 * 
	 * @return The Forward Rate Estimator Instance
	 */

	public org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator()
	{
		return _fre;
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

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		double[] adblMultivariateRandom = _llv.msg().random();

		java.lang.String strTenor = _lslForward.tenor();

		try {
			double dblForwardDate = new org.drip.analytics.date.JulianDate (dblViewDate).addTenor
				(strTenor).julian();

			double dblForwardRate = null == lsqmPrev ? _fre.forward (dblForwardDate) :
				((org.drip.dynamics.lmm.BGMUpdate) lsqmPrev).libor();

			double[] adblLognormalFactorPointVolatility = _llv.factorPointVolatility (dblSpotDate,
				dblViewDate);

			double[] adblContinuousForwardVolatility = _llv.continuousForwardVolatility (_fre, dblViewDate);

			double dblCrossVolatilityDotProduct = 0.;
			double dblVolatilityRandomDotProduct = 0.;
			double dblLognormalPointVolatilityModulus = 0.;
			double dblContinuousForwardVolatilityModulus = 0.;
			int iNumFactor = adblLognormalFactorPointVolatility.length;

			for (int i = 0; i < iNumFactor; ++i) {
				dblLognormalPointVolatilityModulus += adblLognormalFactorPointVolatility[i] *
					adblLognormalFactorPointVolatility[i];
				dblCrossVolatilityDotProduct += adblLognormalFactorPointVolatility[i] *
					adblContinuousForwardVolatility[i];
				dblVolatilityRandomDotProduct += adblLognormalFactorPointVolatility[i] *
					adblMultivariateRandom[i] * dblViewTimeIncrementSQRT;
				dblContinuousForwardVolatilityModulus += adblContinuousForwardVolatility[i] *
					adblContinuousForwardVolatility[i];
			}

			double dblLIBORDCF = org.drip.analytics.support.AnalyticsHelper.TenorToYearFraction (strTenor) *
				dblForwardRate;

			double dblLIBORIncrement = dblViewTimeIncrement * (freDerivative (dblForwardDate) +
				dblForwardRate * dblCrossVolatilityDotProduct + (dblLognormalPointVolatilityModulus *
					dblForwardRate * dblLIBORDCF / (1. + dblLIBORDCF))) + dblForwardRate *
						dblVolatilityRandomDotProduct;

			return org.drip.dynamics.lmm.BGMUpdate.Create (_lslFunding, _lslForward, dblViewDate, dblViewDate
				+ dblViewTimeIncrement, dblForwardRate + dblLIBORIncrement, dblLIBORIncrement,
					java.lang.Math.sqrt (dblLognormalPointVolatilityModulus), java.lang.Math.sqrt
						(dblContinuousForwardVolatilityModulus));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
