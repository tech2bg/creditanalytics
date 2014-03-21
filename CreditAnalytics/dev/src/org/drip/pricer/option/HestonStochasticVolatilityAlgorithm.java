
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
 * HestonStochasticVolatilityAlgorithm implements the Heston 1993 Stochastic Volatility European Call and Put
 * 	Options Pricer.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class HestonStochasticVolatilityAlgorithm implements org.drip.pricer.option.FokkerPlanckGenerator {
	private static final double FOURIER_FREQ_INIT = 0.01;
	private static final double FOURIER_FREQ_INCREMENT = 1.;
	private static final double FOURIER_FREQ_FINAL = 100.;

	private org.drip.pricer.option.FPHestonParams _fphp = null;

	private double _dblDF = java.lang.Double.NaN;
	private double _dblDelta = java.lang.Double.NaN;
	private double _dblPrice = java.lang.Double.NaN;
	private double _dblProb1 = java.lang.Double.NaN;
	private double _dblProb2 = java.lang.Double.NaN;

	/**
	 * Construct an Instance of the priced HestonStochasticVolatilityAlgorithm
	 * 
	 * @param dblStrike Strike
	 * @param dbTimeToExpiry Time To Expiry
	 * @param dblRiskFreeRate Risk Free Rate
	 * @param dblSpot Spot
	 * @param dblSpotVolatility Spot Volatility
	 * @param fphp The Heston Fokker-Planck Parameters
	 * 
	 * @return Instance of the priced Heston Pricer
	 */

	public static final org.drip.pricer.option.FokkerPlanckGenerator Price (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final org.drip.pricer.option.FPHestonParams fphp)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||!org.drip.quant.common.NumberUtil.IsValid
			(dblSpot) ||!org.drip.quant.common.NumberUtil.IsValid (dblSpotVolatility) ||
				!org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return null;

		try {
			HestonStochasticVolatilityAlgorithm h = new HestonStochasticVolatilityAlgorithm (fphp);

			return h.compute (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot, dblSpotVolatility) ? h : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.quant.common.ComplexNumber fourier (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final double dblA,
		final double dblFreq,
		final double dblB,
		final double dblU)
	{
		try {
			org.drip.quant.common.ComplexNumber cnSmallDLHS = new org.drip.quant.common.ComplexNumber (dblB,
				-1. * _fphp.rho() * _fphp.sigma() * dblFreq);

			org.drip.quant.common.ComplexNumber cnSmallD = org.drip.quant.common.ComplexNumber.Square
				(cnSmallDLHS);

			if (null == cnSmallD) return null;

			double dblSigmaScaler = _fphp.sigma() * _fphp.sigma();

			if (null == (cnSmallD = org.drip.quant.common.ComplexNumber.Add (cnSmallD, new
				org.drip.quant.common.ComplexNumber (dblSigmaScaler * dblFreq * dblFreq, 2. * dblSigmaScaler *
					dblFreq * dblU))))
				return null;

			if (null == (cnSmallD = org.drip.quant.common.ComplexNumber.SquareRoot (cnSmallD))) return null;

			org.drip.quant.common.ComplexNumber cnGNumerator = org.drip.quant.common.ComplexNumber.Add
				(cnSmallDLHS, cnSmallD);

			if (null == cnGNumerator) return null;

			org.drip.quant.common.ComplexNumber cnG = org.drip.quant.common.ComplexNumber.Subtract
				(cnSmallDLHS, cnSmallD);

			if (null == cnG) return null;

			if (null == (cnG = org.drip.quant.common.ComplexNumber.Divide (cnGNumerator, cnG))) return null;

			int iM = (int) ((cnG.argument() + java.lang.Math.PI) / (2. * java.lang.Math.PI));

			int iN = (int) ((cnG.argument() + (dbTimeToExpiry * cnSmallD.argument()) + java.lang.Math.PI) /
				(2. * java.lang.Math.PI));

			org.drip.quant.common.ComplexNumber cnExpTTEScaledSmallD =
				org.drip.quant.common.ComplexNumber.Scale (cnSmallD, dbTimeToExpiry);

			if (null == cnExpTTEScaledSmallD) return null;

			if (null == (cnExpTTEScaledSmallD = org.drip.quant.common.ComplexNumber.Exponentiate
				(cnExpTTEScaledSmallD)))
				return null;

			org.drip.quant.common.ComplexNumber cnD = new org.drip.quant.common.ComplexNumber (1. -
				cnExpTTEScaledSmallD.real(), -1. * cnExpTTEScaledSmallD.imaginary());

			org.drip.quant.common.ComplexNumber cnInvGExpTTEScaledSmallD =
				org.drip.quant.common.ComplexNumber.Multiply (cnG, cnExpTTEScaledSmallD);

			if (null == cnInvGExpTTEScaledSmallD) return null;

			cnInvGExpTTEScaledSmallD = new org.drip.quant.common.ComplexNumber (1. -
				cnInvGExpTTEScaledSmallD.real(), -1. * cnInvGExpTTEScaledSmallD.imaginary());

			if (null == (cnD = org.drip.quant.common.ComplexNumber.Divide (cnD, cnInvGExpTTEScaledSmallD)))
				return null;

			if (null == (cnD = org.drip.quant.common.ComplexNumber.Multiply (cnGNumerator, cnD)))
				return null;

			dblSigmaScaler = 1. / dblSigmaScaler;

			if (null == (cnD = org.drip.quant.common.ComplexNumber.Scale (cnD, dblSigmaScaler))) return null;

			org.drip.quant.common.ComplexNumber cnC = new org.drip.quant.common.ComplexNumber (1. -
				cnG.real(), -1. * cnG.imaginary());

			if (null == (cnC = org.drip.quant.common.FourierUtil.KahlJackelComplexLog
				(cnInvGExpTTEScaledSmallD, cnC, iN, iM)))
				return null;

			if (null == (cnC = org.drip.quant.common.ComplexNumber.Scale (cnC, -2.))) return null;

			org.drip.quant.common.ComplexNumber cnTTEScaledGNumerator =
				org.drip.quant.common.ComplexNumber.Scale (cnGNumerator, dbTimeToExpiry);

			if (null == cnTTEScaledGNumerator) return null;

			if (null == (cnC = org.drip.quant.common.ComplexNumber.Add (cnTTEScaledGNumerator, cnC)))
				return null;

			if (null == (cnC = org.drip.quant.common.ComplexNumber.Scale (cnC, dblA * dblSigmaScaler)))
				return null;

			if (null == (cnC = org.drip.quant.common.ComplexNumber.Add (new
				org.drip.quant.common.ComplexNumber (0., dblRiskFreeRate * dbTimeToExpiry * dblFreq),
					cnC)))
				return null;

			org.drip.quant.common.ComplexNumber cnF = org.drip.quant.common.ComplexNumber.Scale (cnD,
				dblSpotVolatility);

			if (null == cnF) return null;

			if (null == (cnF = org.drip.quant.common.ComplexNumber.Add (cnF, new
				org.drip.quant.common.ComplexNumber (0., java.lang.Math.log (dblSpot) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.common.ComplexNumber.Add (cnF, cnC))) return null;

			if (null == (cnF = org.drip.quant.common.ComplexNumber.Add (cnF, new
				org.drip.quant.common.ComplexNumber (0., -1. * java.lang.Math.log (dblStrike) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.common.ComplexNumber.Exponentiate (cnF))) return null;

			if (null == (cnF = org.drip.quant.common.ComplexNumber.Divide (cnF, new
				org.drip.quant.common.ComplexNumber (0., dblFreq))))
				return null;

			return cnF;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * HestonStochasticVolatilityAlgorithm constructor
	 * 
	 * @param fphp The Heston Algorithm Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public HestonStochasticVolatilityAlgorithm (
		final org.drip.pricer.option.FPHestonParams fphp)
		throws java.lang.Exception
	{
		if (null == (_fphp = fphp))
			throw new java.lang.Exception ("HestonStochasticVolatilityAlgorithm ctr: Invalid Inputs");
	}

	@Override public boolean compute (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||!org.drip.quant.common.NumberUtil.IsValid
			(dblSpot) ||!org.drip.quant.common.NumberUtil.IsValid (dblSpotVolatility) ||
				!org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return false;

		_dblProb1 = 0.;
		_dblProb2 = 0.;
		double dblU1 = 0.5;
		double dblU2 = -0.5;

		double dblA = _fphp.kappa() * _fphp.theta();

		double dblB2 = _fphp.kappa() + _fphp.lambda();

		double dblB1 = dblB2 - _fphp.rho() * _fphp.sigma();

		for (double dblFreq = FOURIER_FREQ_INIT; dblFreq <= FOURIER_FREQ_FINAL; dblFreq +=
			FOURIER_FREQ_INCREMENT) {
			org.drip.quant.common.ComplexNumber cnF1 = fourier (dblStrike, dbTimeToExpiry, dblRiskFreeRate,
				dblSpot, dblSpotVolatility, dblA, dblFreq, dblB1, dblU1);

			org.drip.quant.common.ComplexNumber cnF2 = fourier (dblStrike, dbTimeToExpiry, dblRiskFreeRate,
				dblSpot, dblSpotVolatility, dblA, dblFreq, dblB2, dblU2);

			_dblProb1 += cnF1.real() * FOURIER_FREQ_INCREMENT;

			_dblProb2 += cnF2.real() * FOURIER_FREQ_INCREMENT;
		}

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry);

		double dblPIScaler = 1. / java.lang.Math.PI;
		_dblProb1 = 0.5 + _dblProb1 * dblPIScaler;
		_dblProb2 = 0.5 + _dblProb2 * dblPIScaler;
		_dblDelta = _dblProb1;
		_dblPrice = dblSpot * _dblProb1 - dblStrike * _dblDF * _dblProb2;
		return true;
	}

	@Override public double df()
	{
		return _dblDF;
	}

	@Override public double prob1()
	{
		return _dblProb1;
	}

	@Override public double prob2()
	{
		return _dblProb2;
	}

	@Override public double delta()
	{
		return _dblDelta;
	}

	@Override public double price()
	{
		return _dblPrice;
	}
}
