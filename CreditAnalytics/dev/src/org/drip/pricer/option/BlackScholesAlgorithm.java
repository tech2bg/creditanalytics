
package org.drip.pricer.option;

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
 * BlackScholesAlgorithm implements the Black Scholes based European Call and Put Options Pricer.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BlackScholesAlgorithm implements org.drip.pricer.option.FokkerPlanckGenerator {
	private double _dblDF = java.lang.Double.NaN;
	private double _dblPrice = java.lang.Double.NaN;
	private double _dblProb1 = java.lang.Double.NaN;
	private double _dblProb2 = java.lang.Double.NaN;

	/**
	 * Construct an Instance of the priced BlackScholesAlgorithm
	 * 
	 * @param dblStrike The Strike
	 * @param dbTimeToExpiry Time to Option Expiration
	 * @param dblRiskFreeRate The Risk Free Discounting Rate
	 * @param dblSpot Underlier Spot Value
	 * @param dblVolatility Risk Neutral Volatility
	 * 
	 * return Instance of the fully priced Black Scholes Pricer
	 */

	public static final org.drip.pricer.option.FokkerPlanckGenerator Price (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblVolatility)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblSpot) || !org.drip.quant.common.NumberUtil.IsValid
				(dblVolatility) || !org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return null;

		BlackScholesAlgorithm bs = new BlackScholesAlgorithm();

		return bs.compute (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot, dblVolatility) ? bs : null;
	}

	/**
	 * Empty BlackScholesAlgorithm Constructor - nothing to be filled in with
	 */

	public BlackScholesAlgorithm()
	{
	}

	@Override public boolean compute (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblSpot) || !org.drip.quant.common.NumberUtil.IsValid
				(dblSpotVolatility) || !org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return false;

		double dblD1D2Diff = dblSpotVolatility * java.lang.Math.sqrt (dbTimeToExpiry);

		double dblD1 = (java.lang.Math.log (dblSpot / dblStrike) + dbTimeToExpiry * (dblRiskFreeRate + 0.5 *
			dblSpotVolatility * dblSpotVolatility)) / dblD1D2Diff;

		double dblD2 = dblD1 - dblD1D2Diff;

		try {
			_dblProb1 = org.drip.quant.distribution.Gaussian.CDF (dblD1);

			_dblProb2 = org.drip.quant.distribution.Gaussian.CDF (dblD2);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry);

		_dblPrice = dblSpot * _dblProb1 - dblStrike * _dblDF * _dblProb2;
		return true;
	}

	@Override public double df()
	{
		return _dblDF;
	}

	@Override public double delta()
	{
		return _dblProb1;
	}

	@Override public double price()
	{
		return _dblPrice;
	}

	@Override public double prob1()
	{
		return _dblProb1;
	}

	@Override public double prob2()
	{
		return _dblProb2;
	}
}
