
package org.drip.sample.evolution;

import org.drip.analytics.date.*;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.BoxMullerGaussian;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.dynamics.*;

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
 * HullWhiteDynamics demonstrates the Construction and Usage of the Hull-White Metrics Using Hull-White 1F
 * 	Model Dynamics for the Evolution of the Short Rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HullWhiteMetrics {

	private static final HullWhite HullWhiteEvolver (
		final double dblSigma,
		final double dblA,
		final double dblStartingForwardRate)
		throws Exception
	{
		return new HullWhite (
			dblSigma,
			dblA,
			new FlatUnivariate (dblStartingForwardRate),
			new BoxMullerGaussian (0., 1.)
		);
	}

	private static final void DumpMetrics (
		final HullWhiteEvolutionMetrics hwem)
		throws Exception
	{
		System.out.println ("\t| [" + new JulianDate (hwem.initialDate()) + " -> " +
			new JulianDate (hwem.finalDate()) + "] => " +
			FormatUtil.FormatDouble (hwem.initialShortRate(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (hwem.realizedFinalShortRate(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (hwem.expectedFinalShortRate(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (hwem.zeroCouponBondPrice (0.975), 1, 2, 100.) + " | " +
			FormatUtil.FormatDouble (Math.sqrt (hwem.finalShortRateVariance()), 1, 2, 100.) + "% || "
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.Today();

		double dblStartingShortRate = 0.05;
		double dblSigma = 0.03;
		double dblA = 1.;
		int iNumRun = 50;

		HullWhite hw = HullWhiteEvolver (
			dblSigma,
			dblA,
			dblStartingShortRate
		);

		double dblSpotDate = dtSpot.julian();

		double dblInitialDate = dtSpot.addMonths (1).julian();

		double dblFinalDate = dtSpot.addMonths (7).julian();

		System.out.println ("\n\t|--------------------------------------------------------------------------||");

		System.out.println ("\t|                                                                          ||");

		System.out.println ("\t|    Hull-White Scenario Evolution Metrics                                 ||");

		System.out.println ("\t|    -------------------------------------                                 ||");

		System.out.println ("\t|                                                                          ||");

		System.out.println ("\t|    L->R:                                                                 ||");

		System.out.println ("\t|        Initial Date                                                      ||");

		System.out.println ("\t|        Final Date                                                        ||");

		System.out.println ("\t|        Initial Short Rate (%)                                            ||");

		System.out.println ("\t|        Realized Final Short Rate (%)                                     ||");

		System.out.println ("\t|        Expected Final Short Rate (%)                                     ||");

		System.out.println ("\t|        Zero Coupon Bond Price                                            ||");

		System.out.println ("\t|        Final Short Rate Variance (%)                                     ||");

		System.out.println ("\t|--------------------------------------------------------------------------||");

		for (int i = 0; i < iNumRun; ++i)
			DumpMetrics (
				hw.evolve (
					dblSpotDate,
					dblInitialDate,
					dblFinalDate,
					dblStartingShortRate
				)
			);

		System.out.println ("\t|--------------------------------------------------------------------------||");
	}
}
