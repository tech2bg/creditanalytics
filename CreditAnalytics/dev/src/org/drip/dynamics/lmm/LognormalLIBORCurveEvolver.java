
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
	private int _iNumForwardTenor = -1;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;

	private double forwardDerivative (
		final org.drip.analytics.rates.ForwardCurve fc,
		final double dblTargetPointDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 freR1ToR1 = new org.drip.function.deterministic.R1ToR1 (null)
		{
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				return fc.forward (dblDate);
			}
		};

		return freR1ToR1.derivative (dblTargetPointDate, 1);
	}

	private double continuousForwardRateIncrement (
		final double dblTargetPointDate,
		final double dblViewTimeIncrement,
		final double dblViewTimeIncrementSQRT,
		final double[] adblMultivariateRandom,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv)
		throws java.lang.Exception
	{
		final int iNumFactor = adblMultivariateRandom.length;

		org.drip.function.deterministic.R1ToR1 continuousForwardRateR1ToR1 = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				double dblForwardPointVolatilityModulus = 0.;
				double dblPointVolatilityMultifactorRandom = 0.;

				double[] adblContinuousForwardVolatility = llv.continuousForwardVolatility (dblDate, fc);

				if (null != adblContinuousForwardVolatility) {
					for (int i = 0; i < iNumFactor; ++i) {
						dblForwardPointVolatilityModulus += adblContinuousForwardVolatility[i] *
							adblContinuousForwardVolatility[i];
						dblPointVolatilityMultifactorRandom += adblContinuousForwardVolatility[i] *
							adblMultivariateRandom[i];
					}
				}

				return (fc.forward (dblDate) + 0.5 * dblForwardPointVolatilityModulus) * dblViewTimeIncrement
					+ dblPointVolatilityMultifactorRandom * dblViewTimeIncrementSQRT;
			}
		};

		return continuousForwardRateR1ToR1.derivative (dblTargetPointDate, 1);
	}

	private double spotRateIncrement (
		final double dblSpotDate,
		final double dblTargetPointDate,
		final double dblViewTimeIncrement,
		final double dblViewTimeIncrementSQRT,
		final double[] adblMultivariateRandom,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv)
		throws java.lang.Exception
	{
		final int iNumFactor = adblMultivariateRandom.length;

		org.drip.function.deterministic.R1ToR1 spotRateR1ToR1 = new org.drip.function.deterministic.R1ToR1
			(null) {
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				double dblPointVolatilityMultifactorRandom = 0.;

				double[] adblContinuousForwardVolatility = llv.continuousForwardVolatility (dblDate, fc);

				if (null != adblContinuousForwardVolatility) {
					for (int i = 0; i < iNumFactor; ++i)
						dblPointVolatilityMultifactorRandom += adblContinuousForwardVolatility[i] *
							adblMultivariateRandom[i];
				}

				return fc.forward (dblDate) * dblViewTimeIncrement + dblPointVolatilityMultifactorRandom *
					dblViewTimeIncrementSQRT;
			}
		};

		return spotRateR1ToR1.derivative (dblSpotDate, 1);
	}

	private org.drip.dynamics.lmm.BGMForwardTimeSnap timeSnap (
		final double dblSpotDate,
		final double dblTargetPointDate,
		final double dblViewTimeIncrement,
		final double dblViewTimeIncrementSQRT,
		final java.lang.String strForwardTenor,
		final org.drip.analytics.rates.ForwardCurve fcLIBOR,
		final org.drip.analytics.rates.ForwardCurve fcContinuouslyCompounded,
		final org.drip.analytics.rates.DiscountCurve dcSpotRate,
		final org.drip.analytics.rates.DiscountCurve dcDiscountFactor,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv)
	{
		double[] adblLognormalFactorPointVolatility = llv.factorPointVolatility (dblSpotDate,
			dblTargetPointDate);

		double[] adblContinuousForwardVolatility = llv.continuousForwardVolatility (dblTargetPointDate,
			fcContinuouslyCompounded);

		double[] adblMultivariateRandom = llv.msg().random();

		double dblCrossVolatilityDotProduct = 0.;
		double dblLognormalPointVolatilityModulus = 0.;
		double dblLIBORVolatilityMultiFactorRandom = 0.;
		double dblContinuousForwardVolatilityModulus = 0.;
		double dblForwardVolatilityMultiFactorRandom = 0.;
		int iNumFactor = adblLognormalFactorPointVolatility.length;

		for (int i = 0; i < iNumFactor; ++i) {
			dblLognormalPointVolatilityModulus += adblLognormalFactorPointVolatility[i] *
				adblLognormalFactorPointVolatility[i];
			dblCrossVolatilityDotProduct += adblLognormalFactorPointVolatility[i] *
				adblContinuousForwardVolatility[i];
			dblLIBORVolatilityMultiFactorRandom += adblLognormalFactorPointVolatility[i] *
				adblMultivariateRandom[i] * dblViewTimeIncrementSQRT;
			dblContinuousForwardVolatilityModulus += adblContinuousForwardVolatility[i] *
				adblContinuousForwardVolatility[i];
			dblForwardVolatilityMultiFactorRandom += adblContinuousForwardVolatility[i] *
				adblMultivariateRandom[i] * dblViewTimeIncrementSQRT;
		}

		try {
			double dblLIBOR = fcLIBOR.forward (dblTargetPointDate);

			double dblDiscountFactor = dcDiscountFactor.df (dblTargetPointDate);

			double dblSpotRate = dcSpotRate.forward (dblSpotDate, dblSpotDate + 1.);

			double dblContinuousForwardRate = fcContinuouslyCompounded.forward (dblTargetPointDate);

			double dblLIBORDCF = org.drip.analytics.support.AnalyticsHelper.TenorToYearFraction
				(strForwardTenor) * dblLIBOR;

			double dblLIBORIncrement = dblViewTimeIncrement * (forwardDerivative (fcLIBOR,
				dblTargetPointDate) + dblLIBOR * dblCrossVolatilityDotProduct +
					(dblLognormalPointVolatilityModulus * dblLIBOR * dblLIBORDCF / (1. + dblLIBORDCF))) +
						dblLIBOR * dblLIBORVolatilityMultiFactorRandom;

			double dblContinuousForwardRateIncrement = continuousForwardRateIncrement (dblTargetPointDate,
				dblViewTimeIncrement, dblViewTimeIncrementSQRT, adblMultivariateRandom,
					fcContinuouslyCompounded, llv);

			double dblSpotRateIncrement = spotRateIncrement (dblSpotDate, dblTargetPointDate,
				dblViewTimeIncrement, dblViewTimeIncrementSQRT, adblMultivariateRandom,
					fcContinuouslyCompounded, llv);

			double dblDiscountFactorIncrement = dblDiscountFactor * (dblSpotRate - dblContinuousForwardRate)
				* dblViewTimeIncrement - dblForwardVolatilityMultiFactorRandom;

			return new org.drip.dynamics.lmm.BGMForwardTimeSnap (dblLIBOR + dblLIBORIncrement,
				dblLIBORIncrement, dblContinuousForwardRate + dblContinuousForwardRateIncrement,
					dblContinuousForwardRateIncrement, dblSpotRate + dblSpotRateIncrement,
						dblSpotRateIncrement, dblDiscountFactor + dblDiscountFactorIncrement,
							dblDiscountFactorIncrement, java.lang.Math.sqrt
								(dblLognormalPointVolatilityModulus), java.lang.Math.sqrt
									(dblContinuousForwardVolatilityModulus));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * LognormalLIBORCurveEvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param iNumForwardTenor Number of Forward Tenors to Build the Span
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public LognormalLIBORCurveEvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final int iNumForwardTenor)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || 0 >=
			(_iNumForwardTenor = iNumForwardTenor))
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
	 * Retrieve the Number of Forward Tenors comprising the Span Tenor
	 * 
	 * @return Number of Forward Tenors comprising the Span Tenor
	 */

	public int spanTenor()
	{
		return _iNumForwardTenor;
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

		org.drip.analytics.date.JulianDate dtTargetPoint = null;
		org.drip.dynamics.lmm.BGMCurveUpdate bgmPrev = (org.drip.dynamics.lmm.BGMCurveUpdate) lsqmPrev;
		org.drip.dynamics.lmm.BGMForwardTimeSnap[] aBGMTS = new
			org.drip.dynamics.lmm.BGMForwardTimeSnap[_iNumForwardTenor + 1];

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		org.drip.analytics.rates.ForwardCurve fcLIBOR = bgmPrev.libor();

		org.drip.analytics.rates.ForwardCurve fcContinuouslyCompounded = bgmPrev.continuousForwardRate();

		org.drip.analytics.rates.DiscountCurve dcSpotRate = bgmPrev.spotRate();

		org.drip.analytics.rates.DiscountCurve dcDiscountFactor = bgmPrev.discountFactor();

		org.drip.dynamics.lmm.LognormalLIBORVolatility llv = bgmPrev.lognormalLIBORVolatility();

		try {
			dtTargetPoint = new org.drip.analytics.date.JulianDate (dblViewDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.lang.String strForwardTenor = _lslForward.tenor();

		for (int i = 0; i <= _iNumForwardTenor; ++i) {
			if (null == (aBGMTS[i] = timeSnap (dblSpotDate, dtTargetPoint.julian(), dblViewTimeIncrement,
				dblViewTimeIncrementSQRT, strForwardTenor, fcLIBOR, fcContinuouslyCompounded, dcSpotRate,
					dcDiscountFactor, llv)) || null == (dtTargetPoint = dtTargetPoint.addTenor
						(strForwardTenor)))
				return null;

			System.out.println ("\t" + aBGMTS[i]);
		}

		return null;
	}
}
