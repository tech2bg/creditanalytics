
package org.drip.dynamics.hullwhite;

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
 * SingleFactorStateEvolver provides the Hull-White One-Factor Gaussian HJM Short Rate Dynamics
 * 	Implementation.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SingleFactorStateEvolver implements org.drip.dynamics.evolution.PointStateEvolver {
	private double _dblA = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usg = null;
	private org.drip.function.deterministic.R1ToR1 _auIFRInitial = null;

	/**
	 * SingleFactorStateEvolver Constructor
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param dblSigma Sigma
	 * @param dblA A
	 * @param auIFRInitial The Initial Instantaneous Forward Rate Term Structure
	 * @param usg Univariate Random Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public SingleFactorStateEvolver (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final double dblSigma,
		final double dblA,
		final org.drip.function.deterministic.R1ToR1 auIFRInitial,
		final org.drip.sequence.random.UnivariateSequenceGenerator usg)
		throws java.lang.Exception
	{
		if (null == (_lslFunding = lslFunding) || !org.drip.quant.common.NumberUtil.IsValid (_dblSigma =
			dblSigma) || !org.drip.quant.common.NumberUtil.IsValid (_dblA = dblA) || null == (_auIFRInitial =
				auIFRInitial) || null == (_usg = usg))
			throw new java.lang.Exception ("SingleFactorStateEvolver ctr: Invalid Inputs");
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

	public org.drip.function.deterministic.R1ToR1 ifrInitialTermStructure()
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
			throw new java.lang.Exception ("SingleFactorStateEvolver::alpha => Invalid Inputs");

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
			throw new java.lang.Exception ("SingleFactorStateEvolver::theta => Invalid Inputs");

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
			throw new java.lang.Exception ("SingleFactorStateEvolver::shortRateIncrement => Invalid Inputs");

		return (theta (dblSpotDate, dblViewDate) - _dblA * dblShortRate) * dblViewTimeIncrement + _dblSigma *
			java.lang.Math.sqrt (dblViewTimeIncrement) * _usg.random();
	}

	@Override public org.drip.dynamics.evolution.LSQMPointUpdate evolve (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblViewTimeIncrement,
		final org.drip.dynamics.evolution.LSQMPointUpdate lsqmPrev)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblViewDate < dblSpotDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement) || null == lsqmPrev ||
					!(lsqmPrev instanceof org.drip.dynamics.hullwhite.ShortRateUpdate))
			return null;

		double dblDate = dblViewDate;
		double dblTimeIncrement = 1. / 365.25;
		double dblInitialShortRate = java.lang.Double.NaN;
		double dblFinalDate = dblViewDate + dblViewTimeIncrement;

		try {
			dblInitialShortRate = ((org.drip.dynamics.hullwhite.ShortRateUpdate)
				lsqmPrev).realizedFinalShortRate();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblShortRate = dblInitialShortRate;

		while (dblDate < dblFinalDate) {
			try {
				dblShortRate += shortRateIncrement (dblSpotDate, dblDate, dblShortRate, dblTimeIncrement);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			++dblDate;
		}

		double dblADF = java.lang.Math.exp (-1. * _dblA * dblViewTimeIncrement);

		double dblB = (1. - dblADF) / _dblA;

		try {
			return org.drip.dynamics.hullwhite.ShortRateUpdate.Create (_lslFunding, dblViewDate,
				dblFinalDate, dblInitialShortRate, dblShortRate, dblInitialShortRate * dblADF + alpha
					(dblSpotDate, dblFinalDate) - alpha (dblSpotDate, dblViewDate) * dblADF, 0.5 * _dblSigma
						* _dblSigma * (1. - dblADF * dblADF) / _dblA, java.lang.Math.exp (dblB *
							_auIFRInitial.evaluate (dblViewDate) - 0.25 * _dblSigma * _dblSigma * (1. -
								java.lang.Math.exp (-2. * _dblA * (dblViewDate - dblSpotDate) / 365.25)) *
									dblB * dblB / _dblA));
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
	 * @param dblTerminalDate The Terminal Date
	 * @param hwnmInitial The Initial Node Metrics
	 * 
	 * @return The Hull White Transition Metrics
	 */

	public org.drip.dynamics.hullwhite.TrinomialTreeTransitionMetrics evolveTrinomialTree (
		final double dblSpotDate,
		final double dblInitialDate,
		final double dblTerminalDate,
		final org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics hwnmInitial)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblInitialDate) || dblInitialDate < dblSpotDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblTerminalDate) || dblTerminalDate <=
					dblInitialDate)
			return null;

		long lTreeTimeIndex = 0L;
		double dblExpectedTerminalX = 0.;
		long lTreeStochasticBaseIndex = 0L;

		if (null != hwnmInitial) {
			dblExpectedTerminalX = hwnmInitial.x();

			lTreeTimeIndex = hwnmInitial.timeIndex() + 1;

			lTreeStochasticBaseIndex = hwnmInitial.xStochasticIndex();
		}

		double dblADF = java.lang.Math.exp (-1. * _dblA * (dblTerminalDate - dblInitialDate) / 365.25);

		try {
			return new org.drip.dynamics.hullwhite.TrinomialTreeTransitionMetrics (dblInitialDate, dblTerminalDate,
				lTreeTimeIndex, lTreeStochasticBaseIndex, dblExpectedTerminalX * dblADF, 0.5 * _dblSigma *
					_dblSigma * (1. - dblADF * dblADF) / _dblA, alpha (dblSpotDate, dblTerminalDate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Evolve the Trinomial Tree Sequence
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblInitialDate The Initial Date
	 * @param iDayIncrement The Day Increment
	 * @param iNumIncrement Number of Times to Increment
	 * @param hwnm Starting Node Metrics
	 * @param hwsm The Sequence Metrics
	 * 
	 * @return TRUE => The Tree Successfully Evolved
	 */

	public boolean evolveTrinomialTreeSequence (
		final double dblSpotDate,
		final double dblInitialDate,
		final int iDayIncrement,
		final int iNumIncrement,
		final org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics hwnm,
		final org.drip.dynamics.hullwhite.TrinomialTreeSequenceMetrics hwsm)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblInitialDate) || dblInitialDate < dblSpotDate || 0
				>= iDayIncrement || null == hwsm)
			return false;

		if (0 == iNumIncrement) return true;

		org.drip.dynamics.hullwhite.TrinomialTreeTransitionMetrics hwtm = evolveTrinomialTree (dblSpotDate,
			dblInitialDate, dblInitialDate + iDayIncrement, hwnm);

		if (!hwsm.addTransitionMetrics (hwtm)) return false;

		org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics hwnmUp = hwtm.upNodeMetrics();

		if (!hwsm.addNodeMetrics (hwnmUp) || (null != hwnm && !hwsm.setTransitionProbability (hwnm, hwnmUp,
			hwtm.probabilityUp())) || !evolveTrinomialTreeSequence (dblSpotDate, dblInitialDate +
				iDayIncrement, iDayIncrement, iNumIncrement - 1, hwnmUp, hwsm))
			return false;

		org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics hwnmDown = hwtm.downNodeMetrics();

		if (!hwsm.addNodeMetrics (hwnmDown) || (null != hwnm && !hwsm.setTransitionProbability (hwnm,
			hwnmDown, hwtm.probabilityDown())) || !evolveTrinomialTreeSequence (dblSpotDate, dblInitialDate +
				iDayIncrement, iDayIncrement, iNumIncrement - 1, hwnmDown, hwsm))
			return false;

		org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics hwnmStay = hwtm.stayNodeMetrics();

		if (!hwsm.addNodeMetrics (hwnmStay) || (null != hwnm && !hwsm.setTransitionProbability (hwnm,
			hwnmStay, hwtm.probabilityStay())) || !evolveTrinomialTreeSequence (dblSpotDate, dblInitialDate +
				iDayIncrement, iDayIncrement, iNumIncrement - 1, hwnmStay, hwsm))
			return false;

		return true;
	}

	/**
	 * Evolve the Trinomial Tree Sequence
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param iDayIncrement The Day Increment
	 * @param iNumIncrement Number of Times to Increment
	 * 
	 * @return The Sequence Metrics
	 */

	public org.drip.dynamics.hullwhite.TrinomialTreeSequenceMetrics evolveTrinomialTreeSequence (
		final double dblSpotDate,
		final int iDayIncrement,
		final int iNumIncrement)
	{
		org.drip.dynamics.hullwhite.TrinomialTreeSequenceMetrics hwsm = new
			org.drip.dynamics.hullwhite.TrinomialTreeSequenceMetrics();

		return evolveTrinomialTreeSequence (dblSpotDate, dblSpotDate, iDayIncrement, iNumIncrement, null,
			hwsm) ? hwsm : null;
	}
}
