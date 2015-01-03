
package org.drip.analytics.output;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * BulletMetrics holds the results of the Bullet Cash flow metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BulletMetrics {

	/*
	 * Bullet Latent State Identification Support Fields
	 */

	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.state.identifier.FundingLabel _fundingLabel = null;
	private org.drip.state.identifier.FXLabel _fxLabel = null;

	/*
	 * Bullet Parameters Specification Fields
	 */

	private double _dblTerminalDate = java.lang.Double.NaN;
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;

	/*
	 * Bullet State Point Value Fields
	 */

	private double _dblSurvival = java.lang.Double.NaN;
	private double _dblDF = java.lang.Double.NaN;
	private double _dblFX = java.lang.Double.NaN;

	/*
	 * Bullet Convexity Adjustment Fields
	 */

	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;

	/**
	 * BulletMetrics Constructor
	 * 
	 * @param dblTerminalDate Terminal Date
	 * @param dblPayDate Pay Date
	 * @param dblNotional Notional
	 * @param dblSurvival Terminal Survival
	 * @param dblDF Terminal Discount Factor
	 * @param dblFX Terminal FX Rate
	 * @param convAdj Terminal Convexity Adjustment
	 * @param creditLabel The Credit Label
	 * @param fundingLabel The Funding Label
	 * @param fxLabel The FX Label
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public BulletMetrics (
		final double dblTerminalDate,
		final double dblPayDate,
		final double dblNotional,
		final double dblSurvival,
		final double dblDF,
		final double dblFX,
		final org.drip.analytics.output.ConvexityAdjustment convAdj,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblTerminalDate = dblTerminalDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblSurvival = dblSurvival) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblDF = dblDF) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) || null ==
									(_convAdj = convAdj) || null == (_fundingLabel = fundingLabel))
			throw new java.lang.Exception ("BulletMetrics ctr: Invalid Inputs");

		_fxLabel = fxLabel;
		_creditLabel = creditLabel;
	}

	/**
	 * Retrieve the Terminal Date
	 * 
	 * @return The Terminal Date
	 */

	public double terminalDate()
	{
		return _dblTerminalDate;
	}

	/**
	 * Retrieve the Pay Date
	 * 
	 * @return The Pay Date
	 */

	public double payDate()
	{
		return _dblPayDate;
	}

	/**
	 * Retrieve the Terminal Notional
	 * 
	 * @return The Terminal Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the Terminal Survival Probability
	 * 
	 * @return The Terminal Survival Probability
	 */

	public double survival()
	{
		return _dblSurvival;
	}

	/**
	 * Retrieve the Terminal DF
	 * 
	 * @return The Terminal DF
	 */

	public double df()
	{
		return _dblDF;
	}

	/**
	 * Retrieve the Terminal FX Rate
	 * 
	 * @return The Terminal FX Rate
	 */

	public double fx()
	{
		return _dblFX;
	}

	/**
	 * Retrieve the Terminal Annuity in the Pay Currency
	 * 
	 * @return The Terminal Annuity in the Pay Currency
	 */

	public double annuity()
	{
		return _dblNotional * _dblSurvival * _dblDF * _dblFX;
	}

	/**
	 * Retrieve the Terminal Convexity Adjustment
	 * 
	 * @return The Terminal Convexity Adjustment
	 */

	public org.drip.analytics.output.ConvexityAdjustment convexityAdjustment()
	{
		return _convAdj;
	}

	/**
	 * Retrieve the Terminal Survival Probability Loading Coefficient for the specified Credit Latent State
	 * 
	 * @param creditLabel The Credit Label
	 * 
	 * @return The Terminal Survival Probability Loading Coefficient for the specified Credit Latent State
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> survivalProbabilityCreditLoading (
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == creditLabel || !creditLabel.match (_creditLabel)) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapSurvivalProbabilityLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		mapSurvivalProbabilityLoading.put (_dblPayDate, _dblNotional * _dblDF * _dblFX *
			_convAdj.cumulative());

		return mapSurvivalProbabilityLoading;
	}

	/**
	 * Retrieve the Discount Factor Loading Coefficient for the specified Funding Latent State
	 * 
	 * @param fundingLabel The Funding Label
	 * 
	 * @return The Discount Factor Loading Coefficient for the specified Funding Latent State
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> discountFactorFundingLoading (
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == fundingLabel || !fundingLabel.match (_fundingLabel)) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapDiscountFactorLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		mapDiscountFactorLoading.put (_dblPayDate, _dblNotional * _dblSurvival * _dblFX *
			_convAdj.cumulative());

		return mapDiscountFactorLoading;
	}

	/**
	 * Retrieve the FX Loading Coefficient for the specified FX Latent State
	 * 
	 * @param fxLabel The FX Label
	 * 
	 * @return The FX Loading Coefficient for the specified FX Latent State
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> fxFXLoading (
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fxLabel || !fxLabel.match (_fxLabel)) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapFXLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		mapFXLoading.put (_dblPayDate, _dblNotional * _dblSurvival * _dblDF * _convAdj.cumulative());

		return mapFXLoading;
	}

	/**
	 * Retrieve the Credit Label
	 * 
	 * @return The Credit Label
	 */

	public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return _creditLabel;
	}

	/**
	 * Retrieve the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return _fundingLabel;
	}

	/**
	 * Retrieve the FX Label
	 * 
	 * @return The FX Label
	 */

	public org.drip.state.identifier.FXLabel fxLabel()
	{
		return _fxLabel;
	}
}
