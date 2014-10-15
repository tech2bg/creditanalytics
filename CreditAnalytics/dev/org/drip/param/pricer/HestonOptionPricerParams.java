
package org.drip.param.pricer;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * HestonOptionPricerParams holds the parameters that drive the dynamics of the Heston stochastic volatility
 * 	model.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class HestonOptionPricerParams implements org.drip.param.pricer.OptionPricerParams {
	private int _iPayoffTransformScheme = -1;
	private double _dblRho = java.lang.Double.NaN;
	private double _dblKappa = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;
	private double _dblTheta = java.lang.Double.NaN;
	private double _dblLambda = java.lang.Double.NaN;
	private int _iMultiValuePhaseTrackerType =
		org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL;

	/**
	 * HestonOptionPricerParams constructor
	 * 
	 * @param iPayoffTransformScheme The Payoff Transformation Scheme
	 * @param dblRho Rho
	 * @param dblKappa Kappa
	 * @param dblSigma Sigma
	 * @param dblTheta Theta
	 * @param dblLambda Lambda
	 * @param iMultiValuePhaseTrackerType The Multi Valued Phase Tracking Error Corrector
	 * 
	 * @throws Thrown if the Inputs are Invalid
	 */

	public HestonOptionPricerParams (
		final int iPayoffTransformScheme,
		final double dblRho,
		final double dblKappa,
		final double dblSigma,
		final double dblTheta,
		final double dblLambda,
		final int iMultiValuePhaseTrackerType)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblRho = dblRho) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblKappa = dblKappa) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblSigma = dblSigma) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblTheta = dblTheta) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblLambda = dblLambda))
			throw new java.lang.Exception ("HestonOptionPricerParams ctr: Invalid Inputs!");

		_iPayoffTransformScheme = iPayoffTransformScheme;
		_iMultiValuePhaseTrackerType = iMultiValuePhaseTrackerType;
	}

	/**
	 * Retrieve Kappa
	 * 
	 * @return The Kappa
	 */

	public double kappa()
	{
		return _dblKappa;
	}

	/**
	 * Retrieve Lambda
	 * 
	 * @return The Lambda
	 */

	public double lambda()
	{
		return _dblLambda;
	}

	/**
	 * Retrieve Rho
	 * 
	 * @return The Rho
	 */

	public double rho()
	{
		return _dblRho;
	}

	/**
	 * Retrieve Sigma
	 * 
	 * @return The Sigma
	 */

	public double sigma()
	{
		return _dblSigma;
	}

	/**
	 * Retrieve Theta
	 * 
	 * @return The Theta
	 */

	public double theta()
	{
		return _dblTheta;
	}

	/**
	 * Return the Multi Valued Principal Branch Maintaining Phase Tracker Type
	 * 
	 * @return The Multi Valued Principal Branch Maintaining Phase Tracker Type
	 */

	public int phaseTrackerType()
	{
		return _iMultiValuePhaseTrackerType;
	}

	/**
	 * Return the Payoff Fourier Transformation Scheme
	 * 
	 * @return The Payoff Fourier Transformation Scheme
	 */

	public int payoffTransformScheme()
	{
		return _iPayoffTransformScheme;
	}
}
