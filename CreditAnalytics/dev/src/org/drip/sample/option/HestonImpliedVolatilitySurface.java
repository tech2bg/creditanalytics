
package org.drip.sample.option;

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
 * HestonImpliedVolatilitySurface displays the Heston (1993) Price/Vol Surface across the Range of Strikes
 * 	and Maturities, demonstrating the smiles and the skews.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class HestonImpliedVolatilitySurface {
	private static final double CallPrice (
		final double dblATMFactor,
		final double dblTimeToExpiry,
		final int iPayoffTransformScheme)
		throws Exception
	{
		double dblRho = 0.3;
		double dblKappa = 1.;
		double dblSigma = 0.5;
		double dblTheta = 0.2;
		double dblLambda = 0.;

		HestonOptionPricerParams fphp = new HestonOptionPricerParams (
			iPayoffTransformScheme,
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL);

		HestonStochasticVolatilityAlgorithm hsva = new HestonStochasticVolatilityAlgorithm (fphp);

		double dblStrike = dblATMFactor;
		double dblRiskFreeRate = 0.0;
		double dblSpot = 1.;
		double dblInitialVolatility = 0.1;

		hsva.compute (dblStrike, dblTimeToExpiry, dblRiskFreeRate, dblSpot, false, dblInitialVolatility);

		return hsva.callPrice();
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblATMFactor = new double[] {0.8, 0.9, 1.0, 1.1, 1.2};
		double[] adblTTE = new double[] {0.5, 1., 2., 3., 4., 5., 7., 10., 12., 15., 20., 25., 30.};

		System.out.println ("\n\t|------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println ("\t\t\t----    HESTON 1993 TRANSFORM    ----");

		System.out.print ("\t|------------------------------------------------------------------------------------------------------------------------------------|\n\t|  ATM/TTE  =>");

		for (double dblTTE : adblTTE)
			System.out.print ("  " + FormatUtil.FormatDouble (dblTTE, 2, 2, 1.) + " ");

		System.out.println ("  |\n\t|------------------------------------------------------------------------------------------------------------------------------------|");

		for (double dblATMFactor : adblATMFactor) {
			System.out.print ("\t|  " + FormatUtil.FormatDouble (dblATMFactor, 2, 2, 1.) + "   =>");

			for (double dblTTE : adblTTE)
				System.out.print ("  " + FormatUtil.FormatDouble (CallPrice (dblATMFactor, dblTTE,
					HestonStochasticVolatilityAlgorithm.PAYOFF_TRANSFORM_SCHEME_HESTON_1993), 1, 4, 1.));

			System.out.print ("  |\n");
		}

		System.out.println ("  \t|------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println ("\n\t|------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println ("\t\t\t----    ALBRECHER, MAYER, SCHOUTENS, TISTAERT 2007 TRANSFORM    ----");

		System.out.print ("\t|------------------------------------------------------------------------------------------------------------------------------------|\n\t|  ATM/TTE  =>");

		for (double dblTTE : adblTTE)
			System.out.print ("  " + FormatUtil.FormatDouble (dblTTE, 2, 2, 1.) + " ");

		System.out.println ("  |\n\t|------------------------------------------------------------------------------------------------------------------------------------|");

		for (double dblATMFactor : adblATMFactor) {
			System.out.print ("\t|  " + FormatUtil.FormatDouble (dblATMFactor, 2, 2, 1.) + "   =>");

			for (double dblTTE : adblTTE)
				System.out.print ("  " + FormatUtil.FormatDouble (CallPrice (dblATMFactor, dblTTE,
					HestonStochasticVolatilityAlgorithm.PAYOFF_TRANSFORM_SCHEME_AMST_2007), 1, 4, 1.));

			System.out.print ("  |\n");
		}

		System.out.println ("  \t|------------------------------------------------------------------------------------------------------------------------------------|");
	}
}
