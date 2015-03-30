
package org.drip.sample.sabr;

import org.drip.analytics.date.*;
import org.drip.dynamics.sabr.*;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.BoxMullerGaussian;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ForwardRateEvolution demonstrates the Construction and Usage of the SABR Model Dynamics for the Evolution
 *  of Forward Rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ForwardRateEvolution {

	private static StochasticVolatilityStateEvolver SABREvolver (
		final double dblBeta,
		final double dblRho,
		final double dblVolatilityOfVolatility)
		throws Exception
	{
		return new StochasticVolatilityStateEvolver (
			ForwardLabel.Create ("USD", "6M"),
			dblBeta,
			dblRho,
			dblVolatilityOfVolatility,
			new BoxMullerGaussian (0., 1.),
			new BoxMullerGaussian (0., 1.)
		);
	}

	private static void SABREvolution (
		final StochasticVolatilityStateEvolver seSABR1,
		final StochasticVolatilityStateEvolver seSABR2,
		final StochasticVolatilityStateEvolver seSABR3,
		final double dblSpotDate,
		final double dblTerminalDate,
		final ForwardRateUpdate lsqmInitial1,
		final ForwardRateUpdate lsqmInitial2,
		final ForwardRateUpdate lsqmInitial3)
		throws Exception
	{
		int iDayStep = 2;
		double dblDate = dblSpotDate;
		ForwardRateUpdate lsqm1 = lsqmInitial1;
		ForwardRateUpdate lsqm2 = lsqmInitial2;
		ForwardRateUpdate lsqm3 = lsqmInitial3;
		double dblTimeIncrement = iDayStep / 365.25;

		System.out.println ("\n\t||---------------------------------------------------------------------------------||");

		System.out.println ("\t||     SABR  EVOLUTION  DYNAMICS                                                   ||");

		System.out.println ("\t||---------------------------------------------------------------------------------||");

		System.out.println ("\t||    L -> R:                                                                      ||");

		System.out.println ("\t||        Forward Rate (%)  - Gaussian (beta = 0.0)                                ||");

		System.out.println ("\t||        Forward Rate Vol (%)  - Gaussian (beta = 0.0)                            ||");

		System.out.println ("\t||        Forward Rate (%)  - beta = 0.5                                           ||");

		System.out.println ("\t||        Forward Rate Vol (%)  - beta = 0.5                                       ||");

		System.out.println ("\t||        Forward Rate (%)  - Lognormal (beta = 1.0)                               ||");

		System.out.println ("\t||        Forward Rate Vol (%)  - Lognormal (beta = 1.0)                           ||");

		System.out.println ("\t||---------------------------------------------------------------------------------||");

		while (dblDate < dblTerminalDate) {
			lsqm1 = (ForwardRateUpdate) seSABR1.evolve (
				dblSpotDate,
				dblDate,
				dblTimeIncrement,
				lsqm1
			);

			lsqm2 = (ForwardRateUpdate) seSABR2.evolve (
				dblSpotDate,
				dblDate,
				dblTimeIncrement,
				lsqm2
			);

			lsqm3 = (ForwardRateUpdate) seSABR3.evolve (
				dblSpotDate,
				dblDate,
				dblTimeIncrement,
				lsqm3
			);

			System.out.println (
				"\t|| " + new JulianDate (dblDate) + " => " +
				FormatUtil.FormatDouble (lsqm1.forwardRate(), 1, 4, 100.) + " % | " +
				FormatUtil.FormatDouble (lsqm1.forwardRateVolatility(), 1, 2, 100.) + " % || " +
				FormatUtil.FormatDouble (lsqm2.forwardRate(), 1, 4, 100.) + " % | " +
				FormatUtil.FormatDouble (lsqm2.forwardRateVolatility(), 1, 1, 100.) + " % || " +
				FormatUtil.FormatDouble (lsqm3.forwardRate(), 1, 4, 100.) + " % | " +
				FormatUtil.FormatDouble (lsqm3.forwardRateVolatility(), 1, 1, 100.) + " % ||"
			);

			dblDate += iDayStep;
		}

		System.out.println ("\t||---------------------------------------------------------------------------------||");
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.Today();

		double dblRho = 0.1;
		double dblForwardRate = 0.04;
		double dblVolatilityOfVolatility = 0.59;
		double[] adblBeta = {0.00, 0.50, 1.00};
		double[] adblForwardRateVolatility = {0.03, 0.26, 0.51};

		StochasticVolatilityStateEvolver seSABR1 = SABREvolver (
			adblBeta[0],
			dblRho,
			dblVolatilityOfVolatility
		);

		StochasticVolatilityStateEvolver seSABR2 = SABREvolver (
			adblBeta[1],
			dblRho,
			dblVolatilityOfVolatility
		);

		StochasticVolatilityStateEvolver seSABR3 = SABREvolver (
			adblBeta[2],
			dblRho,
			dblVolatilityOfVolatility
		);

		ForwardRateUpdate lsqmInitial1 = ForwardRateUpdate.Create (
			ForwardLabel.Create ("USD", "6M"),
			dtSpot.julian(),
			dtSpot.julian(),
			dblForwardRate,
			0.,
			adblForwardRateVolatility[0],
			0.
		);

		ForwardRateUpdate lsqmInitial2 = ForwardRateUpdate.Create (
			ForwardLabel.Create ("USD", "6M"),
			dtSpot.julian(),
			dtSpot.julian(),
			dblForwardRate,
			0.,
			adblForwardRateVolatility[1],
			0.
		);

		ForwardRateUpdate lsqmInitial3 = ForwardRateUpdate.Create (
			ForwardLabel.Create ("USD", "6M"),
			dtSpot.julian(),
			dtSpot.julian(),
			dblForwardRate,
			0.,
			adblForwardRateVolatility[2],
			0.
		);

		SABREvolution (
			seSABR1,
			seSABR2,
			seSABR3,
			dtSpot.julian(),
			dtSpot.addTenor ("3M").julian(),
			lsqmInitial1,
			lsqmInitial2,
			lsqmInitial3
		);
	}
}
