
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
 * ForwardRateUpdate contains the Increment and Snapshot of the Forward Rate Latent State evolved through
 *  the SABR Dynamics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ForwardRateUpdate extends org.drip.dynamics.evolution.LSQMPointUpdate {
	private org.drip.state.identifier.ForwardLabel _lslForward = null;

	/**
	 * ForwardRateUpdate Creator
	 * 
	 * @param lslForward The Forward Rate Latent State Label
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblForwardRate The Forward Rate
	 * @param dblForwardRateIncrement The Forward Rate Increment
	 * @param dblForwardRateVolatility The Forward Volatility 
	 * @param dblForwardRateVolatilityIncrement The Forward Volatility Rate
	 * 
	 * @return Instance of ForwardRateUpdate
	 */

	public static final ForwardRateUpdate Create (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblForwardRate,
		final double dblForwardRateIncrement,
		final double dblForwardRateVolatility,
		final double dblForwardRateVolatilityIncrement)
	{
		org.drip.dynamics.evolution.LSQMPointRecord lrSnapshot = new
			org.drip.dynamics.evolution.LSQMPointRecord();

		if (!lrSnapshot.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE, dblForwardRate))
			return null;

		if (!lrSnapshot.setQM (org.drip.state.identifier.VolatilityLabel.Standard (lslForward),
			org.drip.analytics.definition.LatentStateStatic.VOLATILITY_QM_SABR_VOLATILITY,
				dblForwardRateVolatility))
			return null;

		org.drip.dynamics.evolution.LSQMPointRecord lrIncrement = new
			org.drip.dynamics.evolution.LSQMPointRecord();

		if (!lrIncrement.setQM (lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE,
				dblForwardRateIncrement))
			return null;

		if (!lrIncrement.setQM (org.drip.state.identifier.VolatilityLabel.Standard (lslForward),
			org.drip.analytics.definition.LatentStateStatic.VOLATILITY_QM_SABR_VOLATILITY,
				dblForwardRateVolatilityIncrement))
			return null;

		try {
			return new ForwardRateUpdate (lslForward, dblInitialDate, dblFinalDate, lrSnapshot, lrIncrement);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ForwardRateUpdate (
		final org.drip.state.identifier.ForwardLabel lslForward,
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.dynamics.evolution.LSQMPointRecord lrSnapshot,
		final org.drip.dynamics.evolution.LSQMPointRecord lrIncrement)
		throws java.lang.Exception
	{
		super (dblInitialDate, dblFinalDate, lrSnapshot, lrIncrement);

		if (null == (_lslForward = lslForward))
			throw new java.lang.Exception ("ForwardRateUpdate ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Forward Rate
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate is not available
	 */

	public double forwardRate()
		throws java.lang.Exception
	{
		return snapshot().qm (_lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE);
	}

	/**
	 * Retrieve the Forward Rate Increment
	 * 
	 * @return The Forward Rate Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate Increment is not available
	 */

	public double forwardRateIncrement()
		throws java.lang.Exception
	{
		return increment().qm (_lslForward,
			org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE);
	}

	/**
	 * Retrieve the Forward Rate Volatility
	 * 
	 * @return The Forward Rate Volatility
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate Volatility is not available
	 */

	public double forwardRateVolatility()
		throws java.lang.Exception
	{
		return snapshot().qm (org.drip.state.identifier.VolatilityLabel.Standard (_lslForward),
			org.drip.analytics.definition.LatentStateStatic.VOLATILITY_QM_SABR_VOLATILITY);
	}

	/**
	 * Retrieve the Forward Rate Volatility Increment
	 * 
	 * @return The Forward Rate Volatility Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate Volatility Increment is not available
	 */

	public double forwardRateVolatilityIncrement()
		throws java.lang.Exception
	{
		return increment().qm (org.drip.state.identifier.VolatilityLabel.Standard (_lslForward),
			org.drip.analytics.definition.LatentStateStatic.VOLATILITY_QM_SABR_VOLATILITY);
	}
}
