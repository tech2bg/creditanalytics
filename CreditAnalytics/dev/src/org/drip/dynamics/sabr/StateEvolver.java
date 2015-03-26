
package org.drip.dynamics.sabr;

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
 * StateEvolver provides the SABR Stochastic Volatility Evolution Dynamics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class StateEvolver {
	private double _dblRho = java.lang.Double.NaN;
	private double _dblBeta = java.lang.Double.NaN;
	private double _dblIdiosyncraticRho = java.lang.Double.NaN;
	private double _dblVolatilityOfVolatility = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usgForwardRate = null;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usgForwardRateVolatilityIdiosyncratic =
		null;

	/**
	 * Create a Gaussian SABR Instance
	 * 
	 * @param lslForward The Forward Rate Latent State Label
	 * @param dblRho SABR Rho
	 * @param dblVolatilityOfVolatility SABR Volatility Of Volatility
	 * @param usgForwardRate The Forward Rate Univariate Sequence Generator
	 * @param usgForwardRateVolatilityIdiosyncratic The Idiosyncratic Component Forward Rate Volatility
	 *  Univariate Sequence Generator
	 * 
	 * @return The Gaussian SABR Instance
	 */

	public static final StateEvolver Gaussian (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblRho,
		final double dblVolatilityOfVolatility,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatilityIdiosyncratic)
	{
		try {
			return new StateEvolver (lslForward, 0., dblRho, dblVolatilityOfVolatility, usgForwardRate,
				usgForwardRateVolatilityIdiosyncratic);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Log-normal SABR Instance
	 * 
	 * @param lslForward The Forward Rate Latent State Label
	 * @param dblRho SABR Rho
	 * @param dblVolatilityOfVolatility SABR Volatility Of Volatility
	 * @param usgForwardRate The Forward Rate Univariate Sequence Generator
	 * @param usgForwardRateVolatilityIdiosyncratic The Idiosyncratic Component Forward Rate Volatility
	 *  Univariate Sequence Generator
	 * 
	 * @return The Log-normal SABR Instance
	 */

	public static final StateEvolver Lognormal (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblRho,
		final double dblVolatilityOfVolatility,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatilityIdiosyncratic)
	{
		try {
			return new StateEvolver (lslForward, 1., dblRho, dblVolatilityOfVolatility, usgForwardRate,
				usgForwardRateVolatilityIdiosyncratic);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Constant Elasticity of Variance SABR Instance
	 * 
	 * @param lslForward The Forward Rate Latent State Label
	 * @param dblBeta SABR Beta
	 * @param dblRho SABR Rho
	 * @param usgForwardRate The Forward Rate Univariate Sequence Generator
	 * @param usgForwardRateVolatilityIdiosyncratic The Idiosyncratic Component Forward Rate Volatility
	 *  Univariate Sequence Generator
	 * 
	 * @return The Constant Elasticity of Variance SABR Instance
	 */

	public static final StateEvolver CEV (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblBeta,
		final double dblRho,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatilityIdiosyncratic)
	{
		try {
			return new StateEvolver (lslForward, dblBeta, dblRho, 0., usgForwardRate,
				usgForwardRateVolatilityIdiosyncratic);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * SABR StateEvolver Constructor
	 * 
	 * @param lslForward The Forward Rate Latent State Label
	 * @param dblBeta SABR Beta
	 * @param dblRho SABR Rho
	 * @param dblVolatilityOfVolatility SABR Volatility Of Volatility
	 * @param usgForwardRate The Forward Rate Univariate Sequence Generator
	 * @param usgForwardRateVolatilityIdiosyncratic The Idiosyncratic Component Forward Rate Volatility
	 *  Univariate Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public StateEvolver (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblBeta,
		final double dblRho,
		final double dblVolatilityOfVolatility,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatilityIdiosyncratic)
		throws java.lang.Exception
	{
		if (null == (_lslForward = lslForward) || !org.drip.quant.common.NumberUtil.IsValid (_dblBeta =
			dblBeta) || !org.drip.quant.common.NumberUtil.IsValid (_dblRho = dblRho) || _dblRho < -1. ||
				_dblRho > 1. || !org.drip.quant.common.NumberUtil.IsValid (_dblVolatilityOfVolatility =
					dblVolatilityOfVolatility) || null == (_usgForwardRate = usgForwardRate) || (0. !=
						_dblVolatilityOfVolatility && null == (_usgForwardRateVolatilityIdiosyncratic =
							usgForwardRateVolatilityIdiosyncratic)))
			throw new java.lang.Exception ("StateEvolver ctr => Invalid Inputs");

		_dblIdiosyncraticRho = java.lang.Math.sqrt (1. - _dblRho * _dblRho);
	}

	/**
	 * Retrieve SABR Volatility of Volatility
	 * 
	 * @return SABR Volatility of Volatility
	 */

	public double volatilityOfVolatility()
	{
		return _dblVolatilityOfVolatility;
	}

	/**
	 * Retrieve SABR Beta
	 * 
	 * @return SABR Beta
	 */

	public double beta()
	{
		return _dblBeta;
	}

	/**
	 * Retrieve SABR Rho
	 * 
	 * @return SABR Rho
	 */

	public double rho()
	{
		return _dblRho;
	}

	/**
	 * The Forward Rate Univariate Random Variable Generator Sequence
	 * 
	 * @return The Forward Rate Univariate Random Variable Generator Sequence
	 */

	public org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate()
	{
		return _usgForwardRate;
	}

	/**
	 * The Idiosyncratic Component of Forward Rate Volatility Univariate Random Variable Generator Sequence
	 * 
	 * @return The Idiosyncratic Component of Forward Rate Volatility Univariate Random Variable Generator
	 *  Sequence
	 */

	public org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatilityIdiosyncratic()
	{
		return _usgForwardRateVolatilityIdiosyncratic;
	}

	/**
	 * Evolve the Latent State and return the LSQM Snapshot
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblViewTimeIncrement The View Time Increment
	 * @param lsqmPrev The Previous LSQM
	 * 
	 * @return The LSQM Snapshot
	 */

	public org.drip.dynamics.sabr.ForwardRateUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.sabr.ForwardRateUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblViewDate < dblSpotDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) || null == lsqmPrev)
			return null;

		try {
			double dblForwardRate = lsqmPrev.forwardRate();

			double dblForwardRateVolatility = lsqmPrev.forwardRateVolatility();

			double dblTimeIncrementSQRT = java.lang.Math.sqrt (dblViewTimeIncrement);

			double dblForwardRateZ = _usgForwardRate.random();

			double dblForwardRateIncrement = dblForwardRateVolatility * java.lang.Math.pow (dblForwardRate,
				_dblBeta) * dblTimeIncrementSQRT * dblForwardRateZ;

			double dblForwardRateVolatilityIncrement = _dblVolatilityOfVolatility * dblForwardRateVolatility
				* dblTimeIncrementSQRT * (_dblRho * dblForwardRateZ + _dblIdiosyncraticRho *
					_usgForwardRateVolatilityIdiosyncratic.random());

			return org.drip.dynamics.sabr.ForwardRateUpdate.Create (_lslForward, dblForwardRate +
				dblForwardRateIncrement, dblForwardRateIncrement, dblForwardRateVolatility +
					dblForwardRateVolatilityIncrement, dblForwardRateVolatilityIncrement);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Implied ATM Black Volatility for the ATM Forward Rate and the TTE
	 * 
	 * @param dblATMForwardRate ATM Forward Rate
	 * @param dblTTE Time to Expiry
	 * @param dblSigma0 Initial Sigma
	 * 
	 * @return The Implied Black Volatility Instance
	 */

	public org.drip.dynamics.sabr.ImpliedBlackVolatility computeATMBlackVolatility (
		final double dblATMForwardRate,
		final double dblTTE,
		final double dblSigma0)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblATMForwardRate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblTTE) || !org.drip.quant.common.NumberUtil.IsValid
				(dblSigma0))
			return null;

		double dblF0KExpSQRT = java.lang.Math.pow (dblATMForwardRate, 1. - _dblBeta);

		double dblA = dblSigma0 / dblF0KExpSQRT;
		double dblB = 1. + dblTTE * (((1. - _dblBeta) * (1. - _dblBeta) * dblSigma0 * dblSigma0 / (24. *
			dblF0KExpSQRT * dblF0KExpSQRT)) + (_dblRho * _dblBeta * _dblVolatilityOfVolatility * dblSigma0 /
				(4. * dblF0KExpSQRT)) + ((2. - 3. * _dblRho * _dblRho) * _dblVolatilityOfVolatility *
					_dblVolatilityOfVolatility / 24.));

		try {
			return new org.drip.dynamics.sabr.ImpliedBlackVolatility (dblATMForwardRate, dblATMForwardRate,
				dblTTE, dblA, 0., 0., dblB, dblA * dblB);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Implied Black Volatility for the Specified Strike, the ATM Forward Rate, and the TTE
	 * 
	 * @param dblStrike Strike
	 * @param dblATMForwardRate ATM Forward Rate
	 * @param dblTTE Time to Expiry
	 * @param dblSigma0 Initial Sigma
	 * 
	 * @return The Implied Black Volatility Instance
	 */

	public org.drip.dynamics.sabr.ImpliedBlackVolatility computeBlackVolatility (
		final double dblStrike,
		final double dblATMForwardRate,
		final double dblTTE,
		final double dblSigma0)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblATMForwardRate) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTTE) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblSigma0))
			return null;

		if (dblStrike == dblATMForwardRate)
			return computeATMBlackVolatility (dblATMForwardRate, dblTTE, dblSigma0);

		double dblLogF0ByK = java.lang.Math.log (dblATMForwardRate / dblStrike);

		double dblF0KExpSQRT = java.lang.Math.pow (dblATMForwardRate * dblStrike, 0.5 * (1. - _dblBeta));

		double dblZ = _dblVolatilityOfVolatility * dblF0KExpSQRT * dblLogF0ByK / dblSigma0;
		double dblOneMinusBetaLogF0ByK = (1. - _dblBeta) * (1. - _dblBeta) * dblLogF0ByK * dblLogF0ByK;
		double dblA = dblSigma0 / (dblF0KExpSQRT * (1. + (dblOneMinusBetaLogF0ByK / 24.) +
			(dblOneMinusBetaLogF0ByK * dblOneMinusBetaLogF0ByK / 1920.)));
		double dblB = 1. + dblTTE * (((1. - _dblBeta) * (1. - _dblBeta) * dblSigma0 * dblSigma0 / (24. *
			dblF0KExpSQRT * dblF0KExpSQRT)) + (_dblRho * _dblBeta * _dblVolatilityOfVolatility * dblSigma0 /
				(4. * dblF0KExpSQRT)) + ((2. - 3. * _dblRho * _dblRho) * _dblVolatilityOfVolatility *
					_dblVolatilityOfVolatility / 24.));

		double dblChiZ = java.lang.Math.log ((java.lang.Math.sqrt (1. - 2. * _dblRho * dblZ + dblZ * dblZ) +
			dblZ - _dblRho) / (1. - _dblRho));

		try {
			return new org.drip.dynamics.sabr.ImpliedBlackVolatility (dblStrike, dblATMForwardRate, dblTTE,
				dblA, dblZ, dblChiZ, dblB, dblA * dblZ * dblB / dblChiZ);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
