
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
 * ContinuousForwardRateUpdate contains the Instantaneous Snapshot of the Evolving Discount Latent State
 *  Quantification Metrics Updated using the Continuously Compounded Forward Rate Dynamics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousForwardRateUpdate extends org.drip.dynamics.evolution.LSQMPointUpdate {
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;
	private double _dblDContinuousForwardDXInitial = java.lang.Double.NaN;
	private double _dblDContinuousForwardDXTerminal = java.lang.Double.NaN;

	/**
	 * Construct an Instance of ContinuousForwardRateUpdate
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblContinuousForwardRate The Continuously Compounded Forward Rate
	 * @param dblContinuousForwardRateIncrement The Continuously Compounded Forward Rate Increment
	 * @param dblSpotRate The Spot Rate
	 * @param dblSpotRateIncrement The Spot Rate Increment
	 * @param dblDiscountFactor The Discount Factor
	 * @param dblDiscountFactorIncrement The Discount Factor Increment
	 * @param dblDContinuousForwardDXInitial Initial D {Continuously Compounded Forward Rate} / DX
	 * @param dblDContinuousForwardDXTerminal Terminal D {Continuously Compounded Forward Rate} / DX
	 * 
	 * @return Instance of ContinuousForwardRateUpdate
	 */

	public static final ContinuousForwardRateUpdate Create (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblContinuousForwardRate,
		final double dblContinuousForwardRateIncrement,
		final double dblSpotRate,
		final double dblSpotRateIncrement,
		final double dblDiscountFactor,
		final double dblDiscountFactorIncrement,
		final double dblDContinuousForwardDXInitial,
		final double dblDContinuousForwardDXTerminal)
	{
		org.drip.dynamics.evolution.LSQMPointRecord lrSnapshot = new org.drip.dynamics.evolution.LSQMPointRecord();

		if (!lrSnapshot.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE,
				dblContinuousForwardRate))
			return null;

		if (!lrSnapshot.setQM (lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE, dblSpotRate))
			return null;

		if (!lrSnapshot.setQM (lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR, dblDiscountFactor))
			return null;

		org.drip.dynamics.evolution.LSQMPointRecord lrIncrement = new org.drip.dynamics.evolution.LSQMPointRecord();

		if (!lrIncrement.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE,
				dblContinuousForwardRateIncrement))
			return null;

		if (!lrIncrement.setQM (lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE, dblSpotRateIncrement))
			return null;

		if (!lrIncrement.setQM (lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
				dblDiscountFactorIncrement))
			return null;

		try {
			return new ContinuousForwardRateUpdate (lslFunding, lslForward, dblInitialDate, dblFinalDate, lrSnapshot,
				lrIncrement, dblDContinuousForwardDXInitial, dblDContinuousForwardDXTerminal);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ContinuousForwardRateUpdate (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.dynamics.evolution.LSQMPointRecord lrSnapshot,
		final org.drip.dynamics.evolution.LSQMPointRecord lrIncrement,
		final double dblDContinuousForwardDXInitial,
		final double dblDContinuousForwardDXTerminal)
		throws java.lang.Exception
	{
		super (dblInitialDate, dblFinalDate, lrSnapshot, lrIncrement);

		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblDContinuousForwardDXTerminal =
				dblDContinuousForwardDXTerminal) || !org.drip.quant.common.NumberUtil.IsValid
					(_dblDContinuousForwardDXInitial = dblDContinuousForwardDXInitial))
			throw new java.lang.Exception ("ContinuousForwardRateUpdate ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Continuously Compounded Forward Rate
	 * 
	 * @return The Continuously Compounded Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Continuously Compounded Forward Rate is not available
	 */

	public double continuousForwardRate()
		throws java.lang.Exception
	{
		return snapshot().qm (_lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE);
	}

	/**
	 * Retrieve the Continuously Compounded Forward Rate Increment
	 * 
	 * @return The Continuously Compounded Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Continuously Compounded Forward Rate Increment is not available
	 */

	public double continuousForwardRateIncrement()
		throws java.lang.Exception
	{
		return increment().qm (_lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE);
	}

	/**
	 * Retrieve the Spot Rate
	 * 
	 * @return The Spot Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Spot Rate is not available
	 */

	public double spotRate()
		throws java.lang.Exception
	{
		return snapshot().qm (_lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE);
	}

	/**
	 * Retrieve the Spot Rate Increment
	 * 
	 * @return The Spot Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Spot Rate Increment is not available
	 */

	public double spotRateIncrement()
		throws java.lang.Exception
	{
		return increment().qm (_lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE);
	}

	/**
	 * Retrieve the Discount Factor
	 * 
	 * @return The Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor is not available
	 */

	public double discountFactor()
		throws java.lang.Exception
	{
		return snapshot().qm (_lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR);
	}

	/**
	 * Retrieve the Discount Factor Increment
	 * 
	 * @return The Discount Factor Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor Increment is not available
	 */

	public double discountFactorIncrement()
		throws java.lang.Exception
	{
		return increment().qm (_lslFunding,
			org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR);
	}

	/**
	 * Retrieve the Initial D {Continuously Compounded Forward Rate} / DX
	 * 
	 * @return The Initial D {Continuously Compounded Forward Rate} / DX
	 */

	public double dContinuousForwardDXInitial()
	{
		return _dblDContinuousForwardDXInitial;
	}

	/**
	 * Retrieve the Terminal D {Continuously Compounded Forward Rate} / DX
	 * 
	 * @return The Terminal D {Continuously Compounded Forward Rate} / DX
	 */

	public double dContinuousForwardDXTerminal()
	{
		return _dblDContinuousForwardDXTerminal;
	}
}
