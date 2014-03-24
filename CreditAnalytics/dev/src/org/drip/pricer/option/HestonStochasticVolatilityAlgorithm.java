
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

	/**
	 * Payoff Transformation Type - The Original Heston 1993 Scheme
	 */

	public static final int PAYOFF_TRANSFORM_SCHEME_HESTON_1993 = 1;

	/**
	 * Payoff Transformation Type - The Albrecher, Mayer, Schoutens, and Tistaert Scheme
	 */

	public static final int PAYOFF_TRANSFORM_SCHEME_AMST_2007 = 1;

	private static final double FOURIER_FREQ_INIT = 0.01;
	private static final double FOURIER_FREQ_INCREMENT = 0.1;
	private static final double FOURIER_FREQ_FINAL = 25.;

	private org.drip.param.pricer.HestonOptionPricerParams _fphp = null;

	private double _dblDF = java.lang.Double.NaN;
	private double _dblCallDelta = java.lang.Double.NaN;
	private double _dblCallPrice = java.lang.Double.NaN;
	private double _dblCallProb1 = java.lang.Double.NaN;
	private double _dblCallProb2 = java.lang.Double.NaN;
	private double _dblPutPriceFromParity = java.lang.Double.NaN;
	private double _dblBlackScholesImpliedVolatility = java.lang.Double.NaN;

	class PhaseCorrectedF {
		double _dblCorrectedPhase = java.lang.Double.NaN;
		org.drip.quant.fourier.ComplexNumber _cnF = null;

		PhaseCorrectedF (
			final double dblCorrectedPhase,
			final org.drip.quant.fourier.ComplexNumber cnF)
		{
			_cnF = cnF;
			_dblCorrectedPhase = dblCorrectedPhase;
		}
	}

	private PhaseCorrectedF fourierTransformHeston93 (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final double dblA,
		final double dblFreq,
		final double dblB,
		final double dblU,
		final org.drip.quant.fourier.RotationCountPhaseTracker rcpt)
	{
		try {
			org.drip.quant.fourier.ComplexNumber cnSmallDLHS = new org.drip.quant.fourier.ComplexNumber (dblB,
				-1. * _fphp.rho() * _fphp.sigma() * dblFreq);

			org.drip.quant.fourier.ComplexNumber cnSmallD = org.drip.quant.fourier.ComplexNumber.Square
				(cnSmallDLHS);

			if (null == cnSmallD) return null;

			double dblSigmaScaler = _fphp.sigma() * _fphp.sigma();

			if (null == (cnSmallD = org.drip.quant.fourier.ComplexNumber.Add (cnSmallD, new
				org.drip.quant.fourier.ComplexNumber (dblSigmaScaler * dblFreq * dblFreq, -2. * dblSigmaScaler
					* dblFreq * dblU))))
				return null;

			if (null == (cnSmallD = org.drip.quant.fourier.ComplexNumber.SquareRoot (cnSmallD))) return null;

			org.drip.quant.fourier.ComplexNumber cnGNumerator = org.drip.quant.fourier.ComplexNumber.Subtract
				(cnSmallDLHS, cnSmallD);

			if (null == cnGNumerator) return null;

			org.drip.quant.fourier.ComplexNumber cnG = org.drip.quant.fourier.ComplexNumber.Add (cnSmallDLHS,
				cnSmallD);

			if (null == cnG) return null;

			if (null == (cnG = org.drip.quant.fourier.ComplexNumber.Divide (cnGNumerator, cnG))) return null;

			int iM = 0;
			int iN = 0;

			if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL ==
				_fphp.phaseTrackerType()) {
				iM = (int) ((cnG.argument() + java.lang.Math.PI) / (2. * java.lang.Math.PI));

				iN = (int) ((cnG.argument() + (dbTimeToExpiry * cnSmallD.argument()) + java.lang.Math.PI) /
					(2. * java.lang.Math.PI));
			}

			org.drip.quant.fourier.ComplexNumber cnExpTTEScaledSmallD =
				org.drip.quant.fourier.ComplexNumber.Scale (cnSmallD, -1. * dbTimeToExpiry);

			if (null == cnExpTTEScaledSmallD) return null;

			if (null == (cnExpTTEScaledSmallD = org.drip.quant.fourier.ComplexNumber.Exponentiate
				(cnExpTTEScaledSmallD)))
				return null;

			org.drip.quant.fourier.ComplexNumber cnD = new org.drip.quant.fourier.ComplexNumber (1. -
				cnExpTTEScaledSmallD.real(), -1. * cnExpTTEScaledSmallD.imaginary());

			org.drip.quant.fourier.ComplexNumber cnInvGExpTTEScaledSmallD =
				org.drip.quant.fourier.ComplexNumber.Multiply (cnExpTTEScaledSmallD, cnG);

			if (null == cnInvGExpTTEScaledSmallD) return null;

			cnInvGExpTTEScaledSmallD = new org.drip.quant.fourier.ComplexNumber (1. -
				cnInvGExpTTEScaledSmallD.real(), -1. * cnInvGExpTTEScaledSmallD.imaginary());

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Divide (cnD, cnInvGExpTTEScaledSmallD)))
				return null;

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Multiply (cnGNumerator, cnD)))
				return null;

			dblSigmaScaler = 1. / dblSigmaScaler;

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Scale (cnD, dblSigmaScaler))) return null;

			org.drip.quant.fourier.ComplexNumber cnC = new org.drip.quant.fourier.ComplexNumber (1. -
				cnG.real(), -1. * cnG.imaginary());

			if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL ==
				_fphp.phaseTrackerType()) {
				if (null == (cnC = org.drip.quant.fourier.PhaseAdjuster.PowerLogPhaseTracker
					(cnInvGExpTTEScaledSmallD, cnC, iN, iM)))
					return null;
			} else if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT
				== _fphp.phaseTrackerType()) {
				if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Logarithm (cnC))) return null;

				cnC = new org.drip.quant.fourier.ComplexNumber (cnC.real(), rcpt.updateAndApply
					(cnC.argument(), true));
			}

			double dblCorrectedPhase = cnC.argument();

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Scale (cnC, -2.))) return null;

			org.drip.quant.fourier.ComplexNumber cnTTEScaledGNumerator =
				org.drip.quant.fourier.ComplexNumber.Scale (cnGNumerator, dbTimeToExpiry);

			if (null == cnTTEScaledGNumerator) return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Add (cnTTEScaledGNumerator, cnC)))
				return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Scale (cnC, dblA * dblSigmaScaler)))
				return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Add (new
				org.drip.quant.fourier.ComplexNumber (0., dblRiskFreeRate * dbTimeToExpiry * dblFreq),
					cnC)))
				return null;

			org.drip.quant.fourier.ComplexNumber cnF = org.drip.quant.fourier.ComplexNumber.Scale (cnD,
				dblSpotVolatility);

			if (null == cnF) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., java.lang.Math.log (dblSpot) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, cnC))) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., -1. * java.lang.Math.log (dblStrike) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Exponentiate (cnF))) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Divide (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., dblFreq))))
				return null;

			return new PhaseCorrectedF (dblCorrectedPhase, cnF);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private PhaseCorrectedF fourierTransformAMST07 (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final double dblA,
		final double dblFreq,
		final double dblB,
		final double dblU,
		final org.drip.quant.fourier.RotationCountPhaseTracker rcpt)
	{
		try {
			org.drip.quant.fourier.ComplexNumber cnSmallDLHS = new org.drip.quant.fourier.ComplexNumber (dblB,
				-1. * _fphp.rho() * _fphp.sigma() * dblFreq);

			org.drip.quant.fourier.ComplexNumber cnSmallD = org.drip.quant.fourier.ComplexNumber.Square
				(cnSmallDLHS);

			if (null == cnSmallD) return null;

			double dblSigmaScaler = _fphp.sigma() * _fphp.sigma();

			if (null == (cnSmallD = org.drip.quant.fourier.ComplexNumber.Add (cnSmallD, new
				org.drip.quant.fourier.ComplexNumber (dblSigmaScaler * dblFreq * dblFreq, -2. * dblSigmaScaler
					* dblFreq * dblU))))
				return null;

			if (null == (cnSmallD = org.drip.quant.fourier.ComplexNumber.SquareRoot (cnSmallD))) return null;

			org.drip.quant.fourier.ComplexNumber cnGNumerator = org.drip.quant.fourier.ComplexNumber.Add
				(cnSmallDLHS, cnSmallD);

			if (null == cnGNumerator) return null;

			org.drip.quant.fourier.ComplexNumber cnG = org.drip.quant.fourier.ComplexNumber.Subtract
				(cnSmallDLHS, cnSmallD);

			if (null == cnG) return null;

			if (null == (cnG = org.drip.quant.fourier.ComplexNumber.Divide (cnGNumerator, cnG))) return null;

			int iM = 0;
			int iN = 0;

			if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL ==
				_fphp.phaseTrackerType()) {
				iM = (int) ((cnG.argument() + java.lang.Math.PI) / (2. * java.lang.Math.PI));

				iN = (int) ((cnG.argument() + (dbTimeToExpiry * cnSmallD.argument()) + java.lang.Math.PI) /
					(2. * java.lang.Math.PI));
			}

			org.drip.quant.fourier.ComplexNumber cnExpTTEScaledSmallD =
				org.drip.quant.fourier.ComplexNumber.Scale (cnSmallD, dbTimeToExpiry);

			if (null == cnExpTTEScaledSmallD) return null;

			if (null == (cnExpTTEScaledSmallD = org.drip.quant.fourier.ComplexNumber.Exponentiate
				(cnExpTTEScaledSmallD)))
				return null;

			org.drip.quant.fourier.ComplexNumber cnD = new org.drip.quant.fourier.ComplexNumber (1. -
				cnExpTTEScaledSmallD.real(), -1. * cnExpTTEScaledSmallD.imaginary());

			org.drip.quant.fourier.ComplexNumber cnInvGExpTTEScaledSmallD =
				org.drip.quant.fourier.ComplexNumber.Multiply (cnG, cnExpTTEScaledSmallD);

			if (null == cnInvGExpTTEScaledSmallD) return null;

			cnInvGExpTTEScaledSmallD = new org.drip.quant.fourier.ComplexNumber (1. -
				cnInvGExpTTEScaledSmallD.real(), -1. * cnInvGExpTTEScaledSmallD.imaginary());

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Divide (cnD, cnInvGExpTTEScaledSmallD)))
				return null;

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Multiply (cnGNumerator, cnD)))
				return null;

			dblSigmaScaler = 1. / dblSigmaScaler;

			if (null == (cnD = org.drip.quant.fourier.ComplexNumber.Scale (cnD, dblSigmaScaler))) return null;

			org.drip.quant.fourier.ComplexNumber cnC = new org.drip.quant.fourier.ComplexNumber (1. -
				cnG.real(), -1. * cnG.imaginary());

			if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL ==
				_fphp.phaseTrackerType()) {
				if (null == (cnC = org.drip.quant.fourier.PhaseAdjuster.PowerLogPhaseTracker
					(cnInvGExpTTEScaledSmallD, cnC, iN, iM)))
					return null;
			} else if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT
				== _fphp.phaseTrackerType()) {
				if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Logarithm (cnC))) return null;

				cnC = new org.drip.quant.fourier.ComplexNumber (cnC.real(), rcpt.updateAndApply
					(cnC.argument(), true));
			}

			double dblCorrectedPhase = cnC.argument();

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Scale (cnC, -2.))) return null;

			org.drip.quant.fourier.ComplexNumber cnTTEScaledGNumerator =
				org.drip.quant.fourier.ComplexNumber.Scale (cnGNumerator, dbTimeToExpiry);

			if (null == cnTTEScaledGNumerator) return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Add (cnTTEScaledGNumerator, cnC)))
				return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Scale (cnC, dblA * dblSigmaScaler)))
				return null;

			if (null == (cnC = org.drip.quant.fourier.ComplexNumber.Add (new
				org.drip.quant.fourier.ComplexNumber (0., dblRiskFreeRate * dbTimeToExpiry * dblFreq),
					cnC)))
				return null;

			org.drip.quant.fourier.ComplexNumber cnF = org.drip.quant.fourier.ComplexNumber.Scale (cnD,
				dblSpotVolatility);

			if (null == cnF) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., java.lang.Math.log (dblSpot) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, cnC))) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Add (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., -1. * java.lang.Math.log (dblStrike) * dblFreq))))
				return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Exponentiate (cnF))) return null;

			if (null == (cnF = org.drip.quant.fourier.ComplexNumber.Divide (cnF, new
				org.drip.quant.fourier.ComplexNumber (0., dblFreq))))
				return null;

			return new PhaseCorrectedF (dblCorrectedPhase, cnF);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private PhaseCorrectedF payoffTransform (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final double dblA,
		final double dblFreq,
		final double dblB,
		final double dblU,
		final org.drip.quant.fourier.RotationCountPhaseTracker rcpt)
	{
		if (org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT ==
			_fphp.phaseTrackerType() && null == rcpt)
			return null;

		if (PAYOFF_TRANSFORM_SCHEME_HESTON_1993 == _fphp.payoffTransformScheme())
			return fourierTransformHeston93 (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot,
				dblSpotVolatility, dblA, dblFreq, dblB, dblU, rcpt);

		if (PAYOFF_TRANSFORM_SCHEME_AMST_2007 == _fphp.payoffTransformScheme())
			return fourierTransformAMST07 (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot,
				dblSpotVolatility, dblA, dblFreq, dblB, dblU, rcpt);

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
		final org.drip.param.pricer.HestonOptionPricerParams fphp)
		throws java.lang.Exception
	{
		if (null == (_fphp = fphp))
			throw new java.lang.Exception ("HestonStochasticVolatilityAlgorithm ctr: Invalid Inputs");
	}

	public java.util.Map<java.lang.Double, java.lang.Double> recordPhase (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final boolean bLeft)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStrike) ||!org.drip.quant.common.NumberUtil.IsValid
			(dblSpot) ||!org.drip.quant.common.NumberUtil.IsValid (dblSpotVolatility) ||
				!org.drip.quant.common.NumberUtil.IsValid (dbTimeToExpiry) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate))
			return null;

		int i = 0;
		double dblU1 = 0.5;
		double dblU2 = -0.5;
		double dblPreviousPhase = 0.;

		org.drip.quant.fourier.RotationCountPhaseTracker rcpt =
			org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT ==
				_fphp.phaseTrackerType() ? new org.drip.quant.fourier.RotationCountPhaseTracker() : null;

		double dblA = _fphp.kappa() * _fphp.theta();

		double dblB2 = _fphp.kappa() + _fphp.lambda();

		double dblB1 = dblB2 - _fphp.rho() * _fphp.sigma();

		java.util.Map<java.lang.Double, java.lang.Double> mapPhaseRun = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		for (double dblFreq = FOURIER_FREQ_INIT; dblFreq <= FOURIER_FREQ_FINAL; dblFreq +=
			FOURIER_FREQ_INCREMENT, ++i) {
			PhaseCorrectedF pcf = bLeft ? payoffTransform (dblStrike, dbTimeToExpiry, dblRiskFreeRate,
				dblSpot, dblSpotVolatility, dblA, dblFreq, dblB1, dblU1, rcpt) : payoffTransform (dblStrike,
					dbTimeToExpiry, dblRiskFreeRate, dblSpot, dblSpotVolatility, dblA, dblFreq, dblB2,
						dblU2, rcpt);

			if (null != rcpt) {
				if (0 == i)
					dblPreviousPhase = rcpt.getPreviousPhase();
				else if (1 == i) {
					double dblCurrentPhase = rcpt.getPreviousPhase();

					if (dblCurrentPhase < dblPreviousPhase) {
						if (!rcpt.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_BACKWARD))
							return null;
					} else if (dblCurrentPhase > dblPreviousPhase) {
						if (!rcpt.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_FORWARD))
							return null;
					} else
						return null;
				}
			}

			mapPhaseRun.put (dblFreq, pcf._dblCorrectedPhase);
		}

		return mapPhaseRun;
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

		int i = 0;
		_dblCallProb1 = 0.;
		_dblCallProb2 = 0.;
		double dblU1 = 0.5;
		double dblU2 = -0.5;
		double dblPreviousPhase = 0.;

		org.drip.quant.fourier.RotationCountPhaseTracker rcpt1 =
			org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT ==
				_fphp.phaseTrackerType() ? new org.drip.quant.fourier.RotationCountPhaseTracker() : null;

		org.drip.quant.fourier.RotationCountPhaseTracker rcpt2 =
			org.drip.quant.fourier.PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT ==
				_fphp.phaseTrackerType() ? new org.drip.quant.fourier.RotationCountPhaseTracker() : null;

		double dblA = _fphp.kappa() * _fphp.theta();

		double dblB2 = _fphp.kappa() + _fphp.lambda();

		double dblB1 = dblB2 - _fphp.rho() * _fphp.sigma();

		for (double dblFreq = FOURIER_FREQ_INIT; dblFreq <= FOURIER_FREQ_FINAL; dblFreq +=
			FOURIER_FREQ_INCREMENT, ++i) {
			PhaseCorrectedF pcf1 = payoffTransform (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot,
				dblSpotVolatility, dblA, dblFreq, dblB1, dblU1, rcpt1);

			if (null != rcpt1) {
				if (0 == i)
					dblPreviousPhase = rcpt1.getPreviousPhase();
				else if (1 == i) {
					double dblCurrentPhase = rcpt1.getPreviousPhase();

					if (dblCurrentPhase < dblPreviousPhase) {
						if (!rcpt1.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_BACKWARD))
							return false;
					} else if (dblCurrentPhase > dblPreviousPhase) {
						if (!rcpt1.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_FORWARD))
							return false;
					} else
						return false;
				}
			}

			PhaseCorrectedF pcf2 = payoffTransform (dblStrike, dbTimeToExpiry, dblRiskFreeRate, dblSpot,
				dblSpotVolatility, dblA, dblFreq, dblB2, dblU2, rcpt2);

			if (null != rcpt2) {
				if (0 == i)
					dblPreviousPhase = rcpt2.getPreviousPhase();
				else if (1 == i) {
					double dblCurrentPhase = rcpt2.getPreviousPhase();

					if (dblCurrentPhase < dblPreviousPhase) {
						if (!rcpt2.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_BACKWARD))
							return false;
					} else if (dblCurrentPhase > dblPreviousPhase) {
						if (!rcpt2.setDirection
							(org.drip.quant.fourier.RotationCountPhaseTracker.APPLY_FORWARD))
							return false;
					} else
						return false;
				}
			}

			_dblCallProb1 += pcf1._cnF.real() * FOURIER_FREQ_INCREMENT;

			_dblCallProb2 += pcf2._cnF.real() * FOURIER_FREQ_INCREMENT;
		}

		_dblDF = java.lang.Math.exp (-1. * dblRiskFreeRate * dbTimeToExpiry);

		double dblPIScaler = 1. / java.lang.Math.PI;
		_dblCallProb1 = 0.5 + _dblCallProb1 * dblPIScaler;
		_dblCallProb2 = 0.5 + _dblCallProb2 * dblPIScaler;
		_dblCallDelta = _dblCallProb1;
		_dblCallPrice = dblSpot * _dblCallProb1 - dblStrike * _dblDF * _dblCallProb2;
		_dblPutPriceFromParity = _dblCallPrice + dblStrike * _dblDF - dblSpot;

		try {
			_dblBlackScholesImpliedVolatility = new
				org.drip.pricer.option.BlackScholesAlgorithm().implyBlackScholesVolatility (dblStrike,
					dbTimeToExpiry, dblRiskFreeRate, dblSpot, _dblCallPrice);
		} catch (java.lang.Exception e) {
			// e.printStackTrace();

			// return false;
		}

		return true;
	}

	@Override public double df()
	{
		return _dblDF;
	}

	@Override public double callProb1()
	{
		return _dblCallProb1;
	}

	@Override public double callProb2()
	{
		return _dblCallProb2;
	}

	@Override public double callDelta()
	{
		return _dblCallDelta;
	}

	@Override public double callPrice()
	{
		return _dblCallPrice;
	}

	@Override public double putDelta()
	{
		return java.lang.Double.NaN;
	}

	@Override public double putPrice()
	{
		return java.lang.Double.NaN;
	}

	@Override public double putPriceFromParity()
	{
		return _dblPutPriceFromParity;
	}

	@Override public double putProb1()
	{
		return java.lang.Double.NaN;
	}

	@Override public double putProb2()
	{
		return java.lang.Double.NaN;
	}

	public double impliedBlackScholesVolatility()
	{
		return _dblBlackScholesImpliedVolatility;
	}
}
