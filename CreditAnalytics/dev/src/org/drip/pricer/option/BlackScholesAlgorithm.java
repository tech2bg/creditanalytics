
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
	private double _dblPutPrice = java.lang.Double.NaN;
	private double _dblPutProb1 = java.lang.Double.NaN;
	private double _dblPutProb2 = java.lang.Double.NaN;
	private double _dblCallPrice = java.lang.Double.NaN;
	private double _dblCallProb1 = java.lang.Double.NaN;
	private double _dblCallProb2 = java.lang.Double.NaN;
	private double _dblPutPriceFromParity = java.lang.Double.NaN;

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

		double dblD1 = java.lang.Double.NaN;
		double dblD2 = java.lang.Double.NaN;

		if (0. != dblSpotVolatility) {
			dblD1 = (java.lang.Math.log (dblSpot / dblStrike) + dbTimeToExpiry * (dblRiskFreeRate + 0.5 *
				dblSpotVolatility * dblSpotVolatility)) / dblD1D2Diff;

			dblD2 = dblD1 - dblD1D2Diff;
		} else {
			dblD1 = dblSpot > dblStrike * java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry) ?
				java.lang.Double.POSITIVE_INFINITY : java.lang.Double.NEGATIVE_INFINITY;
			dblD2 = dblD1;
		}

		try {
			_dblCallProb1 = org.drip.quant.distribution.Gaussian.CDF (dblD1);

			_dblCallProb2 = org.drip.quant.distribution.Gaussian.CDF (dblD2);

			_dblPutProb1 = org.drip.quant.distribution.Gaussian.CDF (-1. * dblD1);

			_dblPutProb2 = org.drip.quant.distribution.Gaussian.CDF (-1. * dblD2);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry);

		_dblCallPrice = dblSpot * _dblCallProb1 - dblStrike * _dblDF * _dblCallProb2;
		_dblPutPrice = -1. * dblSpot * _dblPutProb1 + dblStrike * _dblDF * _dblPutProb2;
		_dblPutPriceFromParity = _dblCallPrice + dblStrike * _dblDF - dblSpot;
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

	public double implyBlackScholesVolatility (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
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
				// System.out.println ("dblSpotVolatility = " + java.lang.Math.abs (dblSpotVolatility));

				if (!compute (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot, java.lang.Math.abs
					(dblSpotVolatility)))
					throw new java.lang.Exception
						("BlackScholesAlgorithm::implyVolatility => Cannot compute Measure");

				return callPrice() - dblCallPrice;
			}
		};

		org.drip.quant.solver1D.FixedPointFinderOutput fpop = new
			org.drip.quant.solver1D.FixedPointFinderBrent (0., au, true).findRoot();

		if (null == fpop || !fpop.containsRoot())
			throw new java.lang.Exception
				("BlackScholesAlgorithm::implyVolatility => Cannot compute Measure");

		return java.lang.Math.abs (fpop.getRoot());
	}
}
