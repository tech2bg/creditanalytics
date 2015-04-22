
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
 * MultiFactorVolatility implements the Volatility of the Multi-factor Stochastic Evolutionary Process. The
 * 	Factors may come from the Underlying Stochastic Variables, or from Principal Components.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiFactorVolatility {
	private org.drip.analytics.definition.MarketSurface[] _aMSVolatility = null;
	private org.drip.sequence.random.PrincipalFactorSequenceGenerator _pfsg = null;

	/**
	 * MultiFactorVolatility Constructor
	 * 
	 * @param aMSVolatility Array of the Multi-Factor Volatility Surfaces
	 * @param pfsg Principal Factor Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public MultiFactorVolatility (
		final org.drip.analytics.definition.MarketSurface[] aMSVolatility,
		final org.drip.sequence.random.PrincipalFactorSequenceGenerator pfsg)
		throws java.lang.Exception
	{
		if (null == (_aMSVolatility = aMSVolatility) || null == (_pfsg = pfsg))
			throw new java.lang.Exception ("MultiFactorVolatility ctr: Invalid Inputs");

		int iNumFactor = _pfsg.numFactor();

		if (0 == iNumFactor || _aMSVolatility.length < iNumFactor)
			throw new java.lang.Exception ("MultiFactorVolatility ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Volatility Surfaces
	 * 
	 * @return The Array of Volatility Surfaces
	 */

	public org.drip.analytics.definition.MarketSurface[] volatilitySurface()
	{
		return _aMSVolatility;
	}

	/**
	 * Retrieve the Principal Factor Sequence Generator
	 * 
	 * @return The Principal Factor Sequence Generator
	 */

	public org.drip.sequence.random.PrincipalFactorSequenceGenerator msg()
	{
		return _pfsg;
	}

	/**
	 * Retrieve the Factor-Specific Univariate Volatility Function for the Specified Date
	 * 
	 * @param iFactorIndex The Factor Index
	 * @param dblXDate The X Date
	 * 
	 * @return The Factor-Specific Univariate Volatility Function for the Specified Date
	 */

	public org.drip.function.deterministic.R1ToR1 xDateVolatilityFunction (
		final int iFactorIndex,
		final double dblXDate)
	{
		int iNumFactor = _pfsg.numFactor();

		if (iFactorIndex >= iNumFactor) return null;

		final int iNumVariate = _aMSVolatility.length;

		return new org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				double dblMultiFactorVol = 0.;

				double[] adblFactor = _pfsg.factors()[iFactorIndex];

				for (int i = 0; i < iNumVariate; ++i) {
					org.drip.analytics.definition.TermStructure tsVolatilityXDate =
						_aMSVolatility[iFactorIndex].xAnchorTermStructure (dblXDate);

					dblMultiFactorVol += adblFactor[i] * tsVolatilityXDate.node (dblX);
				}

				return _pfsg.factorWeight()[iFactorIndex] * dblMultiFactorVol;
			}
		};
	}

	/**
	 * Compute the Factor Volatility Integral
	 * 
	 * @param iFactorIndex The Factor Index
	 * @param dblXDate The X Date
	 * @param dblYDate The Y Date
	 * 
	 * @return The Factor Volatility Integral
	 * 
	 * @throws java.lang.Exception Thrown if the Factor Volatility Integral cannot be computed
	 */

	public double volatilityIntegral (
		final int iFactorIndex,
		final double dblXDate,
		final double dblYDate)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 auVolatilityFunction = xDateVolatilityFunction
			(iFactorIndex, dblXDate);

		if (null == auVolatilityFunction)
			throw new java.lang.Exception
				("MultiFactorVolatility::volatilityIntegral => Cannot extract X Date Volatility Function");

		return auVolatilityFunction.integrate (dblXDate, dblYDate) / 365.25;
	}

	/**
	 * Compute the Factor Point Volatility
	 * 
	 * @param iFactorIndex The Factor Index
	 * @param dblXDate The X Date
	 * @param dblYDate The Y Date
	 * 
	 * @return The Factor Point Volatility
	 * 
	 * @throws java.lang.Exception Thrown if the Factor Point Volatility cannot be computed
	 */

	public double factorPointVolatility (
		final int iFactorIndex,
		final double dblXDate,
		final double dblYDate)
		throws java.lang.Exception
	{
		int iNumFactor = _pfsg.numFactor();

		if (iFactorIndex >= iNumFactor)
			throw new java.lang.Exception
				("MultiFactorVolatility::factorPointVolatility => Invalid Factor Index");

		double[] adblFactor = _pfsg.factors()[iFactorIndex];

		int iNumVariate = adblFactor.length;
		double dblFactorPointVolatility = 0.;

		for (int i = 0; i < iNumVariate; ++i)
			dblFactorPointVolatility += adblFactor[i] * _aMSVolatility[i].node (dblXDate, dblYDate);

		return _pfsg.factorWeight()[iFactorIndex] * dblFactorPointVolatility;
	}

	/**
	 * Compute the Point Volatility Norm
	 * 
	 * @param dblXDate The X Date
	 * @param dblYDate The Y Date
	 * 
	 * @return The Point Volatility Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Point Volatility Norm cannot be computed
	 */

	public double pointVolatilityNorm (
		final double dblXDate,
		final double dblYDate)
		throws java.lang.Exception
	{
		int iNumFactor = _pfsg.numFactor();

		double dblPointVolatilityNorm = 0.;

		for (int i = 0; i < iNumFactor; ++i) {
			double dblFactorPointVolatility = factorPointVolatility (i, dblXDate, dblYDate);

			dblPointVolatilityNorm += dblFactorPointVolatility * dblFactorPointVolatility;
		}

		return dblPointVolatilityNorm;
	}

	/**
	 * Compute the Point Volatility Norm Derivative
	 * 
	 * @param dblXDate The X Date
	 * @param dblYDate The Y Date
	 * @param iOrder The Derivative Order
	 * @param bTerminal TRUE => Derivative off of the Y Date; FALSE => Derivative off of the X Date
	 * 
	 * @return The Point Volatility Norm Derivative
	 * 
	 * @throws java.lang.Exception Thrown if the Point Volatility Norm Derivative cannot be computed
	 */

	public double pointVolatilityNormDerivative (
		final double dblXDate,
		final double dblYDate,
		final int iOrder,
		final boolean bTerminal)
		throws java.lang.Exception
	{
		org.drip.function.deterministic.R1ToR1 pointVolatilityR1ToR1 = new
			org.drip.function.deterministic.R1ToR1 (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws java.lang.Exception
			{
				return bTerminal ? pointVolatilityNorm (dblXDate, dblVariate) : pointVolatilityNorm
					(dblVariate, dblYDate);
			}
		};

		return bTerminal ? pointVolatilityR1ToR1.derivative (dblYDate, iOrder) :
			pointVolatilityR1ToR1.derivative (dblXDate, iOrder);
	}
}
