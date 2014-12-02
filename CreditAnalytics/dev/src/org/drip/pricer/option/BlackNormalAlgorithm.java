
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
 * BlackNormalAlgorithm implements the Black Normal European Call and Put Options Pricer.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BlackNormalAlgorithm implements org.drip.pricer.option.FokkerPlanckGenerator {
	private double _dblDF = java.lang.Double.NaN;
	private double _dblPutPrice = java.lang.Double.NaN;
	private double _dblPutProb1 = java.lang.Double.NaN;
	private double _dblPutProb2 = java.lang.Double.NaN;
	private double _dblCallPrice = java.lang.Double.NaN;
	private double _dblCallProb1 = java.lang.Double.NaN;
	private double _dblCallProb2 = java.lang.Double.NaN;
	private double _dblPutPriceFromParity = java.lang.Double.NaN;

	/**
	 * Empty BlackNormalAlgorithm Constructor - nothing to be filled in with
	 */

	public BlackNormalAlgorithm()
	{
	}

	@Override public boolean compute (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblUnderlier,
		final boolean bIsForward,
		final double dblVolatility)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblUnderlier) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblVolatility) ||
					!org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return false;

		double dblD1D2Diff = dblVolatility * java.lang.Math.sqrt (dbTimeToExpiry);

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry);

		double dblForward = bIsForward ? dblUnderlier : dblUnderlier / _dblDF;
		double dblD = (dblForward - dblStrike) / dblD1D2Diff;

		double dblN = java.lang.Math.exp (-0.5 * dblD * dblD) / java.lang.Math.sqrt (2. * java.lang.Math.PI);

		_dblCallProb1 = dblD1D2Diff * dblN / dblForward;

		System.out.println ("\tdblForward: " + dblForward);

		System.out.println ("\tdblStrike: " + dblStrike);

		try {
			_dblPutProb1 = dblD * dblD1D2Diff * org.drip.quant.distribution.Gaussian.CDF (-1. * dblD) /
				dblForward;

			_dblCallProb2 = -1. * dblD1D2Diff * dblD * org.drip.quant.distribution.Gaussian.CDF (dblD) /
				dblStrike;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		_dblPutProb2 = dblD1D2Diff * dblN / dblStrike;

		_dblCallPrice = _dblDF * (dblForward * _dblCallProb1 - dblStrike * _dblCallProb2);
		_dblPutPrice = _dblDF * (-1. * dblForward * _dblPutProb1 + dblStrike * _dblPutProb2);
		_dblPutPriceFromParity = _dblDF * (_dblCallPrice + dblStrike - dblForward);
		return true;
	}

	@Override public double df()
	{
		return _dblDF;
	}

	@Override public double callDelta()
	{
		return _dblCallProb1;
	}

	@Override public double callPrice()
	{
		return _dblCallPrice;
	}

	@Override public double callProb1()
	{
		return _dblCallProb1;
	}

	@Override public double callProb2()
	{
		return _dblCallProb2;
	}

	@Override public double putDelta()
	{
		return -1. * _dblPutProb1;
	}

	@Override public double putPrice()
	{
		return _dblPutPrice;
	}

	@Override public double putPriceFromParity()
	{
		return _dblPutPriceFromParity;
	}

	@Override public double putProb1()
	{
		return _dblPutProb1;
	}

	@Override public double putProb2()
	{
		return _dblPutProb2;
	}

	/**
	 * Imply the Constant Black Scholes Volatility From the Call Price
	 * 
	 * @param dblStrike Strike
	 * @param dbTimeToExpiry Time To Expiry
	 * @param dblRiskFreeRate Risk Free Rate
	 * @param dblUnderlier The Underlier
	 * @param bIsForward TRUE => The Underlier represents the Forward, FALSE => it represents Spot
	 * @param dblCallPrice The Call Price
	 * 
	 * @return The Implied Constant Black Normal Volatility
	 * 
	 * @throws java.lang.Exception Thrown if the Constant Black Normal Volatility cannot be implied
	 */

	public double implyBlackScholesVolatility (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblUnderlier,
		final boolean bIsForward,
		final double dblCallPrice)
		throws java.lang.Exception
	{
		org.drip.quant.function1D.AbstractUnivariate au = new org.drip.quant.function1D.AbstractUnivariate
			(null)
		{
			@Override public double evaluate (
				final double dblSpotVolatility)
				throws java.lang.Exception
			{
				if (!compute (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblUnderlier, bIsForward,
					java.lang.Math.abs (dblSpotVolatility)))
					throw new java.lang.Exception
						("BlackNormalAlgorithm::implyBlackScholesVolatility => Cannot compute Measure");

				return callPrice() - dblCallPrice;
			}
		};

		org.drip.quant.solver1D.FixedPointFinderOutput fpop = new
			org.drip.quant.solver1D.FixedPointFinderBrent (0., au, true).findRoot();

		if (null == fpop || !fpop.containsRoot())
			throw new java.lang.Exception
				("BlackNormalAlgorithm::implyVolatility => Cannot compute Measure");

		return java.lang.Math.abs (fpop.getRoot());
	}
}
