
package org.drip.sample.quant;

import java.util.Map;

import org.drip.param.pricer.HestonOptionPricerParams;
import org.drip.pricer.option.HestonStochasticVolatilityAlgorithm;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.fourier.PhaseAdjuster;

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
 * PhaseTrackerComparison demonstrates the Log + Power Complex Number Phase Correction Functionality
 * 	implemented by three different ways for the calculation of the Inverse Fourier Transforms. The sample
 * 	problem chosen is the stochastic volatility evolution using the Heston Method.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PhaseTrackerComparison {

	private static final Map<Double, Double> PhaseSet (
		final double dblRho,
		final double dblKappa,
		final double dblSigma,
		final double dblTheta,
		final double dblLambda,
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility,
		final int iPhaseTrackerType)
		throws Exception
	{
		HestonOptionPricerParams fphp = new HestonOptionPricerParams (
			HestonStochasticVolatilityAlgorithm.PAYOFF_TRANSFORM_SCHEME_HESTON_1993,
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			iPhaseTrackerType);

		HestonStochasticVolatilityAlgorithm hsva = new HestonStochasticVolatilityAlgorithm (fphp);

		return hsva.recordPhase (
			dblStrike,
			dbTimeToExpiry,
			dblRiskFreeRate,
			dblSpot,
			dblSpotVolatility,
			true);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblRho = 0.3;
		double dblKappa = 1.;
		double dblSigma = 0.5;
		double dblTheta = 0.2;
		double dblLambda = 0.;

		double dblStrike = 1.;
		double dbTimeToExpiry = 0.5;
		double dblRiskFreeRate = 0.0;
		double dblSpot = 1.;
		double dblSpotVolatility = 0.1;

		Map<Double, Double> mapFreqPhaseNoAdjust = PhaseSet (
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			dblStrike,
			dbTimeToExpiry,
			dblRiskFreeRate,
			dblSpot,
			dblSpotVolatility,
			PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_NONE);

		Map<Double, Double> mapFreqPhaseRotationCount = PhaseSet (
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			dblStrike,
			dbTimeToExpiry,
			dblRiskFreeRate,
			dblSpot,
			dblSpotVolatility,
			PhaseAdjuster.MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT);

		Map<Double, Double> mapFreqPhaseKahlJackel = PhaseSet (
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			dblStrike,
			dbTimeToExpiry,
			dblRiskFreeRate,
			dblSpot,
			dblSpotVolatility,
			PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL);

		System.out.println ("\t|--------------------------------------------|");

		System.out.println ("\t|  u =>  NO CORECT | ROT COUNT | KAHL JACKEL |");

		System.out.println ("\t|--------------------------------------------|");

		for (Map.Entry<Double, Double> me : mapFreqPhaseKahlJackel.entrySet()) {
			Double dblKey = me.getKey();

			System.out.println ("\t|" +
				FormatUtil.FormatDouble (dblKey, 2, 0, 1.) + " =>  " +
				FormatUtil.FormatDouble (mapFreqPhaseNoAdjust.get (dblKey), 1, 6, 1.)  + " | " +
				FormatUtil.FormatDouble (mapFreqPhaseRotationCount.get (dblKey), 1, 6, 1.)  + " | " +
				FormatUtil.FormatDouble (mapFreqPhaseKahlJackel.get (dblKey), 1, 6, 1.) + "   |"
			);
		}

		System.out.println ("\t|--------------------------------------------|");
	}
}
