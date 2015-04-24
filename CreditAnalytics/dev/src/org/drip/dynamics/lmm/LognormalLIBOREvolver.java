
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

public class LognormalLIBOREvolver implements org.drip.dynamics.evolution.PointStateEvolver {
	private org.drip.analytics.rates.ForwardCurve _fc = null;
	private org.drip.analytics.rates.DiscountCurve _dc = null;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private org.drip.dynamics.lmm.LognormalLIBORVolatility _llv = null;

	private double forwardDerivative (
		final double dblViewDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 freR1ToR1 = new org.drip.function.deterministic.R1ToR1 (null)
		{
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				return _fc.forward (dblDate);
			}
		};

		return freR1ToR1.derivative (dblViewDate, 1);
	}

	private double continuousForwardRateIncrement (
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final double[] adblMultivariateRandom)
		throws java.lang.Exception
	{
		final int iNumFactor = adblMultivariateRandom.length;
		final org.drip.analytics.rates.DiscountCurve dc = _dc;

		final double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		org.drip.function.deterministic.R1ToR1 continuousForwardRateR1ToR1 = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				double dblForwardPointVolatilityModulus = 0.;
				double dblPointVolatilityMultifactorRandom = 0.;

				double[] adblContinuousForwardVolatility = _llv.continuousForwardVolatility (dblDate, _fc);

				if (null != adblContinuousForwardVolatility) {
					for (int i = 0; i < iNumFactor; ++i) {
						dblForwardPointVolatilityModulus += adblContinuousForwardVolatility[i] *
							adblContinuousForwardVolatility[i];
						dblPointVolatilityMultifactorRandom += adblContinuousForwardVolatility[i] *
							adblMultivariateRandom[i];
					}
				}

				return (dc.forward (dblDate, dblDate + 1.) + 0.5 * dblForwardPointVolatilityModulus) *
					dblViewTimeIncrement + dblPointVolatilityMultifactorRandom * dblViewTimeIncrementSQRT;
			}
		};

		return continuousForwardRateR1ToR1.derivative (dblViewDate, 1);
	}

	private double spotRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final double[] adblMultivariateRandom)
		throws java.lang.Exception
	{
		final int iNumFactor = adblMultivariateRandom.length;
		final org.drip.analytics.rates.DiscountCurve dc = _dc;

		final double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		org.drip.function.deterministic.R1ToR1 spotRateR1ToR1 = new org.drip.function.deterministic.R1ToR1
			(null) {
			@Override public double evaluate (
				final double dblDate)
				throws java.lang.Exception
			{
				double dblPointVolatilityMultifactorRandom = 0.;

				double[] adblContinuousForwardVolatility = _llv.continuousForwardVolatility (dblDate, _fc);

				if (null != adblContinuousForwardVolatility) {
					for (int i = 0; i < iNumFactor; ++i)
						dblPointVolatilityMultifactorRandom += adblContinuousForwardVolatility[i] *
							adblMultivariateRandom[i];
				}

				return dc.forward (dblDate, dblDate + 1.) * dblViewTimeIncrement +
					dblPointVolatilityMultifactorRandom * dblViewTimeIncrementSQRT;
			}
		};

		return spotRateR1ToR1.derivative (dblSpotDate, 1);
	}

	/**
	 * LognormalLIBOREvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param llv The Log-normal LIBOR Volatility Instance
	 * @param fc The Forward Curve Instance
	 * @param dc The Discount Curve Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public LognormalLIBOREvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final org.drip.dynamics.lmm.LognormalLIBORVolatility llv,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.analytics.rates.DiscountCurve dc)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) || null == (_llv = llv)
			|| null == (_fc = fc) || null == (_dc = dc))
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
	 * Retrieve the Forward Curve Instance
	 * 
	 * @return The Forward Curve Instance
	 */

	public org.drip.analytics.rates.ForwardCurve forwardCurve()
	{
		return _fc;
	}

	/**
	 * Retrieve the Discount Curve Instance
	 * 
	 * @return The Discount Curve Instance
	 */

	public org.drip.analytics.rates.DiscountCurve discountCurve()
	{
		return _dc;
	}

	@Override public org.drip.dynamics.lmm.BGMUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.evolution.LSQMPointUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || (null != lsqmPrev &&
					!(lsqmPrev instanceof org.drip.dynamics.lmm.BGMUpdate)))
			return null;

		double dblViewTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

		double[] adblMultivariateRandom = _llv.msg().random();

		java.lang.String strTenor = _lslForward.tenor();

		double dblLIBOR = java.lang.Double.NaN;
		double dblSpotRate = java.lang.Double.NaN;
		double dblDiscountFactor = java.lang.Double.NaN;
		double dblContinuouslyCompoundedForwardRate = java.lang.Double.NaN;
		org.drip.dynamics.lmm.BGMUpdate bgmPrev = null == lsqmPrev ? null : (org.drip.dynamics.lmm.BGMUpdate)
			lsqmPrev;

		try {
			double dblForwardDate = new org.drip.analytics.date.JulianDate (dblViewDate).addTenor
				(strTenor).julian();

			if (null == bgmPrev) {
				dblLIBOR = _fc.forward (dblForwardDate);

				dblDiscountFactor = _dc.df (dblViewDate);

				dblSpotRate = _dc.forward (dblSpotDate, dblSpotDate + 1.);

				dblContinuouslyCompoundedForwardRate = _dc.forward (dblViewDate, dblViewDate + 1.);
			} else {
				dblLIBOR = bgmPrev.libor();

				dblSpotRate = bgmPrev.spotRate();

				dblDiscountFactor = bgmPrev.discountFactor();

				dblContinuouslyCompoundedForwardRate = bgmPrev.continuousForwardRate();
			}

			double[] adblLognormalFactorPointVolatility = _llv.factorPointVolatility (dblSpotDate,
				dblViewDate);

			double[] adblContinuousForwardVolatility = _llv.continuousForwardVolatility (dblViewDate, _fc);

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

			double dblLIBORDCF = org.drip.analytics.support.AnalyticsHelper.TenorToYearFraction (strTenor) *
				dblLIBOR;

			double dblLIBORIncrement = dblViewTimeIncrement * (forwardDerivative (dblForwardDate) + dblLIBOR
				* dblCrossVolatilityDotProduct + (dblLognormalPointVolatilityModulus * dblLIBOR * dblLIBORDCF
					/ (1. + dblLIBORDCF))) + dblLIBOR * dblLIBORVolatilityMultiFactorRandom;

			double dblContinuousForwardRateIncrement = continuousForwardRateIncrement (dblViewDate,
				dblViewTimeIncrement, adblMultivariateRandom);

			double dblSpotRateIncrement = spotRateIncrement (dblSpotDate, dblViewDate, dblViewTimeIncrement,
				adblMultivariateRandom);

			double dblDiscountFactorIncrement = dblDiscountFactor * (dblSpotRate -
				dblContinuouslyCompoundedForwardRate) * dblViewTimeIncrement -
					dblForwardVolatilityMultiFactorRandom;

			return org.drip.dynamics.lmm.BGMUpdate.Create (_lslFunding, _lslForward, dblViewDate, dblViewDate
				+ dblViewTimeIncrement * 365.25, dblLIBOR + dblLIBORIncrement, dblLIBORIncrement,
					dblContinuouslyCompoundedForwardRate + dblContinuousForwardRateIncrement,
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
}
