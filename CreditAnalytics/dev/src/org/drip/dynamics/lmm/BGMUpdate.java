
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
 * BGMUpdate contains the Instantaneous Snapshot of the Evolving Discount Latent State Quantification Metrics
 * 	Updated using the BGM Dynamics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BGMUpdate extends org.drip.dynamics.evolution.LSQMUpdate {
	private double _dblDContinuousForwardDX = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _lslForward = null;
	private org.drip.state.identifier.FundingLabel _lslFunding = null;

	/**
	 * Construct an Instance of BGMUpdate
	 * 
	 * @param lslFunding The Funding Latent State Label
	 * @param lslForward The Forward Latent State Label
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblContinuousForwardRate The Continuously Compounded Forward Rate
	 * @param dblContinuousForwardRateIncrement The Continuously Compounded Forward Rate Increment
	 * @param dblDContinuousForwardDX D {Continuously Compounded Forward Rate} / DX
	 * 
	 * @return Instance of BGMUpdate
	 */

	public static final BGMUpdate Create (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblContinuousForwardRate,
		final double dblContinuousForwardRateIncrement,
		final double dblDContinuousForwardDX)
	{
		org.drip.dynamics.evolution.LSQMRecord lrSnapshot = new org.drip.dynamics.evolution.LSQMRecord();

		if (!lrSnapshot.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE,
				dblContinuousForwardRate))
			return null;

		org.drip.dynamics.evolution.LSQMRecord lrIncrement = new org.drip.dynamics.evolution.LSQMRecord();

		if (!lrIncrement.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_CONTINUOUSLY_COMPOUNDED_FORWARD_RATE,
				dblContinuousForwardRateIncrement))
			return null;

		try {
			return new BGMUpdate (lslFunding, lslForward, dblInitialDate, dblFinalDate, lrSnapshot,
				lrIncrement, dblDContinuousForwardDX);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private BGMUpdate (
		final org.drip.state.identifier.FundingLabel lslFunding,
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.dynamics.evolution.LSQMRecord lrSnapshot,
		final org.drip.dynamics.evolution.LSQMRecord lrIncrement,
		final double dblDContinuousForwardDX)
		throws java.lang.Exception
	{
		super (dblInitialDate, dblFinalDate, lrSnapshot, lrIncrement);

		if (null == (_lslFunding = lslFunding) || null == (_lslForward = lslForward) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblDContinuousForwardDX = dblDContinuousForwardDX))
			throw new java.lang.Exception ("BGMUpdate ctr: Invalid Inputs");
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
	 * Retrieve D {Continuously Compounded Forward Rate} / DX
	 * 
	 * @return D {Continuously Compounded Forward Rate} / DX
	 */

	public double dContinuousForwardDX()
	{
		return _dblDContinuousForwardDX;
	}
}
