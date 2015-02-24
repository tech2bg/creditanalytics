
package org.drip.pricer.option;

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
 * BlackScholesAlgorithm implements the Black Scholes based European Call and Put Options Pricer.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BlackScholesAlgorithm implements org.drip.pricer.option.FokkerPlanckGenerator {
	private double _dblDF = java.lang.Double.NaN;
	private double _dblPutRho = java.lang.Double.NaN;
	private double _dblCallRho = java.lang.Double.NaN;
	private double _dblPutVega = java.lang.Double.NaN;
	private double _dblPutVeta = java.lang.Double.NaN;
	private double _dblPutCharm = java.lang.Double.NaN;
	private double _dblPutColor = java.lang.Double.NaN;
	private double _dblPutGamma = java.lang.Double.NaN;
	private double _dblPutPrice = java.lang.Double.NaN;
	private double _dblPutProb1 = java.lang.Double.NaN;
	private double _dblPutProb2 = java.lang.Double.NaN;
	private double _dblPutSpeed = java.lang.Double.NaN;
	private double _dblPutTheta = java.lang.Double.NaN;
	private double _dblPutVanna = java.lang.Double.NaN;
	private double _dblPutVomma = java.lang.Double.NaN;
	private double _dblCallVega = java.lang.Double.NaN;
	private double _dblCallVeta = java.lang.Double.NaN;
	private double _dblCallCharm = java.lang.Double.NaN;
	private double _dblCallColor = java.lang.Double.NaN;
	private double _dblCallGamma = java.lang.Double.NaN;
	private double _dblCallPrice = java.lang.Double.NaN;
	private double _dblCallProb1 = java.lang.Double.NaN;
	private double _dblCallProb2 = java.lang.Double.NaN;
	private double _dblCallSpeed = java.lang.Double.NaN;
	private double _dblCallTheta = java.lang.Double.NaN;
	private double _dblCallVanna = java.lang.Double.NaN;
	private double _dblCallVomma = java.lang.Double.NaN;
	private double _dblPutUltima = java.lang.Double.NaN;
	private double _dblCallUltima = java.lang.Double.NaN;
	private double _dblPutPriceFromParity = java.lang.Double.NaN;

	/**
	 * Empty BlackScholesAlgorithm Constructor - nothing to be filled in with
	 */

	public BlackScholesAlgorithm()
	{
	}

	@Override public boolean compute (
		final double dblStrike,
		final double dblTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblUnderlier,
		final boolean bIsForward,
		final double dblVolatility,
		final boolean bCalibMode)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblUnderlier) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblVolatility) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblTimeToExpiry) ||
						!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return false;

		double dblD1D2Diff = dblVolatility * java.lang.Math.sqrt (dblTimeToExpiry);

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dblTimeToExpiry);

		double dblD1 = java.lang.Double.NaN;
		double dblD2 = java.lang.Double.NaN;
		double dblVega = java.lang.Double.NaN;
		double dblVeta = java.lang.Double.NaN;
		double dblCharm = java.lang.Double.NaN;
		double dblColor = java.lang.Double.NaN;
		double dblGamma = java.lang.Double.NaN;
		double dblSpeed = java.lang.Double.NaN;
		double dblVanna = java.lang.Double.NaN;
		double dblVomma = java.lang.Double.NaN;
		double dblUltima = java.lang.Double.NaN;
		double dblTimeDecay = java.lang.Double.NaN;
		double dblForward = bIsForward ? dblUnderlier : dblUnderlier / _dblDF;

		if (0. != dblVolatility) {
			dblD1 = (java.lang.Math.log (dblForward / dblStrike) + dblTimeToExpiry * (0.5 * dblVolatility *
				dblVolatility)) / dblD1D2Diff;

			dblD2 = dblD1 - dblD1D2Diff;
		} else {
			dblD1 = dblForward > dblStrike ? java.lang.Double.POSITIVE_INFINITY :
				java.lang.Double.NEGATIVE_INFINITY;
			dblD2 = dblD1;
		}

		try {
			_dblCallProb1 = org.drip.quant.distribution.Gaussian.CDF (dblD1);

			_dblCallProb2 = org.drip.quant.distribution.Gaussian.CDF (dblD2);

			_dblPutProb1 = org.drip.quant.distribution.Gaussian.CDF (-1. * dblD1);

			_dblPutProb2 = org.drip.quant.distribution.Gaussian.CDF (-1. * dblD2);

			if (!bCalibMode) {
				double dblD1Density = org.drip.quant.distribution.Gaussian.Density (dblD1);

				double dblTimeRoot = java.lang.Math.sqrt (dblTimeToExpiry);

				dblVega = dblD1Density * dblUnderlier * dblTimeRoot;
				dblVomma = dblVega * dblD1 * dblD2 / dblVolatility;
				dblGamma = dblD1Density / (dblUnderlier * dblVolatility * dblTimeRoot);
				dblUltima = dblGamma * (dblD1 * dblD2 - 1.) / dblVolatility;
				dblSpeed = -1. * dblGamma / dblUnderlier * (1. + (dblD1 / (dblVolatility * dblTimeRoot)));
				dblTimeDecay = -0.5 * dblUnderlier * dblD1Density * dblVolatility / dblTimeRoot;
				dblVanna = dblVega / dblUnderlier * (1. - (dblD1 / (dblVolatility * dblTimeRoot)));
				dblCharm = dblD1Density * (2. * dblRiskFreeRate * dblTimeToExpiry - dblVolatility * dblD2 *
					dblTimeRoot) / (2. * dblVolatility * dblTimeToExpiry * dblTimeRoot);
				dblVeta = dblUnderlier * dblD1Density * dblTimeRoot * ((dblRiskFreeRate * dblD1 /
					(dblVolatility * dblTimeRoot)) - ((1. + dblD1 * dblD2) / (2. * dblTimeToExpiry)));
				dblColor = -0.5 * dblD1Density / (dblUnderlier * dblVolatility * dblTimeToExpiry *
					dblTimeRoot) * (1. + dblD1 * (2. * dblRiskFreeRate * dblTimeToExpiry - dblVolatility *
						dblD2 * dblTimeRoot) / (dblVolatility * dblTimeRoot));
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		_dblCallVega = dblVega;
		_dblCallVeta = dblVeta;
		_dblCallCharm = dblCharm;
		_dblCallColor = dblColor;
		_dblCallGamma = dblGamma;
		_dblCallSpeed = dblSpeed;
		_dblCallVanna = dblVanna;
		_dblCallVomma = dblVomma;
		_dblCallUltima = dblUltima;
		_dblCallRho = dblUnderlier * dblTimeToExpiry * _dblCallProb2;
		_dblCallTheta = dblTimeDecay - dblRiskFreeRate * _dblCallRho;
		_dblCallPrice = _dblDF * (dblForward * _dblCallProb1 - dblStrike * _dblCallProb2);

		_dblPutVega = dblVega;
		_dblPutVeta = dblVeta;
		_dblPutCharm = dblCharm;
		_dblPutColor = dblColor;
		_dblPutGamma = dblGamma;
		_dblPutSpeed = dblSpeed;
		_dblPutVanna = dblVanna;
		_dblPutVomma = dblVomma;
		_dblPutUltima = dblUltima;
		_dblPutRho = -1. * dblUnderlier * dblTimeToExpiry * _dblPutProb2;
		_dblPutTheta = dblTimeDecay - dblRiskFreeRate * _dblPutRho;
		_dblPutPriceFromParity = _dblDF * (_dblCallPrice + dblStrike - dblForward);
		_dblPutPrice = _dblDF * (-1. * dblForward * _dblPutProb1 + dblStrike * _dblPutProb2);
		return true;
	}

	@Override public double df()
	{
		return _dblDF;
	}

	@Override public double callCharm()
	{
		return _dblCallCharm;
	}

	@Override public double callColor()
	{
		return _dblCallColor;
	}

	@Override public double callDelta()
	{
		return _dblCallProb1;
	}

	@Override public double callGamma()
	{
		return _dblCallGamma;
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

	@Override public double callRho()
	{
		return _dblCallRho;
	}

	@Override public double callSpeed()
	{
		return _dblCallSpeed;
	}

	@Override public double callTheta()
	{
		return _dblCallTheta;
	}

	@Override public double callUltima()
	{
		return _dblCallUltima;
	}

	@Override public double callVanna()
	{
		return _dblCallVanna;
	}

	@Override public double callVega()
	{
		return _dblCallVega;
	}

	@Override public double callVeta()
	{
		return _dblCallVeta;
	}

	@Override public double callVomma()
	{
		return _dblCallVomma;
	}

	@Override public double putCharm()
	{
		return _dblPutCharm;
	}

	@Override public double putColor()
	{
		return _dblPutColor;
	}

	@Override public double putDelta()
	{
		return -1. * _dblPutProb1;
	}

	@Override public double putGamma()
	{
		return _dblPutGamma;
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

	@Override public double putRho()
	{
		return _dblPutRho;
	}

	@Override public double putSpeed()
	{
		return _dblPutSpeed;
	}

	@Override public double putTheta()
	{
		return _dblPutTheta;
	}

	@Override public double putUltima()
	{
		return _dblPutUltima;
	}

	@Override public double putVanna()
	{
		return _dblPutVanna;
	}

	@Override public double putVega()
	{
		return _dblPutVega;
	}

	@Override public double putVeta()
	{
		return _dblPutVeta;
	}

	@Override public double putVomma()
	{
		return _dblPutVomma;
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
	 * @return The Implied Constant Black Scholes Volatility
	 * 
	 * @throws java.lang.Exception Thrown if the Constant Black Scholes Volatility cannot be implied
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
		org.drip.quant.function.AbstractUnivariate au = new org.drip.quant.function.AbstractUnivariate
			(null)
		{
			@Override public double evaluate (
				final double dblSpotVolatility)
				throws java.lang.Exception
			{
				if (!compute (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblUnderlier, bIsForward,
					java.lang.Math.abs (dblSpotVolatility), true))
					throw new java.lang.Exception
						("BlackScholesAlgorithm::implyBlackScholesVolatility => Cannot compute Measure");

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
