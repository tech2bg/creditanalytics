
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
 * HullWhite provides the Hull-White One-Factor Gaussian HJM Short Rate Dynamics Implementation.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HullWhite {
	private double _dblA = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usg = null;
	private org.drip.function.deterministic.AbstractUnivariate _auIFRInitial = null;

	/**
	 * HullWhite Constructor
	 * 
	 * @param dblSigma Sigma
	 * @param dblA A
	 * @param auIFRInitial The Initial Instantaneous Forward Rate Term Structure
	 * @param usg Univariate Random Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public HullWhite (
		final double dblSigma,
		final double dblA,
		final org.drip.function.deterministic.AbstractUnivariate auIFRInitial,
		final org.drip.sequence.random.UnivariateSequenceGenerator usg)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSigma = dblSigma) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblA = dblA) || null == (_auIFRInitial =
				auIFRInitial) || null == (_usg = usg))
			throw new java.lang.Exception ("HullWhite ctr: Invalid Inputs");
	}

	/**
	 * Retrieve Sigma
	 * 
	 * @return Sigma
	 */

	public double sigma()
	{
		return _dblSigma;
	}

	/**
	 * Retrieve A
	 * 
	 * @return A
	 */

	public double a()
	{
		return _dblA;
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
	 * Retrieve the Random Sequence Generator
	 * 
	 * @return The Random Sequence Generator
	 */

	public org.drip.sequence.random.UnivariateSequenceGenerator rsg()
	{
		return _usg;
	}

	/**
	 * Calculate the Alpha
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * 
	 * @return Alpha
	 * 
	 * @throws java.lang.Exception Thrown if Alpha cannot be computed
	 */

	public double alpha (
		final double dblSpotDate,
		final double dblViewDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate)
			throw new java.lang.Exception ("HullWhite::alpha => Invalid Inputs");

		double dblAlphaVol = _dblSigma * (1. - java.lang.Math.exp (_dblA * (dblViewDate - dblSpotDate) /
			365.25)) / _dblA;

		return _auIFRInitial.evaluate (dblViewDate) + 0.5 * dblAlphaVol * dblAlphaVol;
	}

	/**
	 * Calculate the Theta
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * 
	 * @return Theta
	 * 
	 * @throws java.lang.Exception Thrown if Theta cannot be computed
	 */

	public double theta (
		final double dblSpotDate,
		final double dblViewDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate)
			throw new java.lang.Exception ("HullWhite::theta => Invalid Inputs");

		return _auIFRInitial.derivative (dblViewDate, 1) + _dblA * _auIFRInitial.evaluate (dblViewDate) +
			_dblSigma * _dblSigma / (2. * _dblA) * (1. - java.lang.Math.exp (-2. * _dblA * (dblViewDate -
				dblSpotDate) / 365.25));
	}

	/**
	 * Calculate the Short Rate Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblShortRate The Short Rate
	 * @param dblViewTimeIncrement The View Time Increment
	 * 
	 * @return The Short Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Short Rate cannot be computed
	 */

	public double shortRateIncrement (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblShortRate,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblShortRate) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("HullWhite::shortRateIncrement => Invalid Inputs");

		return (theta (dblSpotDate, dblViewDate) - _dblA * dblShortRate) * dblViewTimeIncrement + _dblSigma *
			java.lang.Math.sqrt (dblViewTimeIncrement) * _usg.random();
	}

	/**
	 * Generate the Metrics associated with the Evolution of the Short Rate from an Initial to a Final Date
	 * 
	 * @param dblSpotDate The Spot/Epoch Date
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblInitialShortRate The Initial Short Rate
	 * 
	 * @return The Evolution Metrics
	 */

	public org.drip.state.dynamics.HullWhiteEvolutionMetrics evolve (
		final double dblSpotDate,
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblInitialShortRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblInitialDate) || dblInitialDate < dblSpotDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblFinalDate) || dblFinalDate <= dblInitialDate ||
					!org.drip.quant.common.NumberUtil.IsValid (dblInitialShortRate))
			return null;

		double dblDate = dblInitialDate;
		double dblTimeIncrement = 1. / 365.25;
		double dblShortRate = dblInitialShortRate;
		double dblEvolutionIncrement = (dblFinalDate - dblInitialDate) / 365.25;

		while (dblDate < dblFinalDate) {
			try {
				dblShortRate += shortRateIncrement (dblSpotDate, dblDate, dblShortRate, dblTimeIncrement);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			++dblDate;
		}

		double dblADF = java.lang.Math.exp (-1. * _dblA * dblEvolutionIncrement);

		double dblB = (1. - dblADF) / _dblA;

		try {
			return new org.drip.state.dynamics.HullWhiteEvolutionMetrics (dblInitialDate, dblFinalDate,
				dblInitialShortRate, dblShortRate, dblInitialShortRate * dblADF + alpha (dblSpotDate,
					dblFinalDate) - alpha (dblSpotDate, dblInitialDate) * dblADF, 0.5 * _dblSigma * _dblSigma
						* (1. - dblADF * dblADF) / _dblA, java.lang.Math.exp (dblB * _auIFRInitial.evaluate
							(dblInitialDate) - 0.25 * _dblSigma * _dblSigma * (1. - java.lang.Math.exp (-2. *
								_dblA * (dblInitialDate - dblSpotDate) / 365.25)) * dblB * dblB / _dblA));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the Metrics associated with the Transition that results from using a Trinomial Tree Using the
	 *  Starting Node Metrics
	 * 
	 * @param dblSpotDate The Spot/Epoch Date
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param hwnmInitial The Initial Node Metrics
	 * 
	 * @return The Hull White Transition Metrics
	 */

	public org.drip.state.dynamics.HullWhiteTransitionMetrics evolveTrinomialTree (
		final double dblSpotDate,
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.state.dynamics.HullWhiteNodeMetrics hwnmInitial)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblInitialDate) || dblInitialDate < dblSpotDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblFinalDate) || dblFinalDate <= dblInitialDate)
			return null;

		double dblADF = java.lang.Math.exp (-1. * _dblA * (dblFinalDate - dblInitialDate) / 365.25);

		try {
			return new org.drip.state.dynamics.HullWhiteTransitionMetrics (dblInitialDate, dblFinalDate, null
				== hwnmInitial ? 0. : hwnmInitial.x() * dblADF, 0.5 * _dblSigma * _dblSigma * (1. - dblADF *
					dblADF) / _dblA, alpha (dblSpotDate, dblFinalDate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
