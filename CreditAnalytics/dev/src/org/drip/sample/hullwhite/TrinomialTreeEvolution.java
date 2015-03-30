
package org.drip.sample.hullwhite;

import org.drip.analytics.date.*;
import org.drip.dynamics.hullwhite.*;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.BoxMullerGaussian;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.FundingLabel;

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
 * TrinomialTreeEvolution demonstrates the Construction and Usage of the Hull-White Trinomial Tree and the
 *  Eventual Evolution of the Short Rate on it.
 *
 * @author Lakshmi Krishnamurthy
 */

public class TrinomialTreeEvolution {

	private static final SingleFactorStateEvolver HullWhiteEvolver (
		final String strCurrency,
		final double dblSigma,
		final double dblA,
		final double dblStartingForwardRate)
		throws Exception
	{
		return new SingleFactorStateEvolver (
			FundingLabel.Standard (strCurrency),
			dblSigma,
			dblA,
			new FlatUnivariate (dblStartingForwardRate),
			new BoxMullerGaussian (0., 1.)
		);
	}

	private static final void DumpMetrics (
		final TrinomialTreeTransitionMetrics hwtm)
		throws Exception
	{
		System.out.println ("\t| [" + new JulianDate (hwtm.initialDate()) + " -> " +
			new JulianDate (hwtm.terminalDate()) + "] => " +
			FormatUtil.FormatDouble (hwtm.expectedTerminalX(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (Math.sqrt (hwtm.xVariance()), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (hwtm.xStochasticShift(), 1, 4, 1.) + " || " +
			FormatUtil.FormatDouble (hwtm.probabilityUp(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.upNodeMetrics().x(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.upNodeMetrics().shortRate(), 1, 2, 100.) + "% || " +
			FormatUtil.FormatDouble (hwtm.probabilityDown(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.downNodeMetrics().x(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.downNodeMetrics().shortRate(), 1, 2, 100.) + "% || " +
			FormatUtil.FormatDouble (hwtm.probabilityStay(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.stayNodeMetrics().x(), 1, 4, 1.) + " | " +
			FormatUtil.FormatDouble (hwtm.stayNodeMetrics().shortRate(), 1, 2, 100.) + "% ||"
		);
	}

	private static final void TreeHeader (
		final String strEvolutionComment)
		throws Exception
	{
		System.out.println ("\n\n\t|----------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                                                                                                                                                    ||");

		System.out.println (strEvolutionComment);

		System.out.println ("\t|    ---------------------------------------------------                                                                                             ||");

		System.out.println ("\t|                                                                                                                                                    ||");

		System.out.println ("\t|    L->R:                                                                                                                                           ||");

		System.out.println ("\t|                                                                                                                                                    ||");

		System.out.println ("\t|        Initial Date                                                                                                                                ||");

		System.out.println ("\t|        Final Date                                                                                                                                  ||");

		System.out.println ("\t|        Expected Final X                                                                                                                            ||");

		System.out.println ("\t|        X Volatility (%)                                                                                                                            ||");

		System.out.println ("\t|        X Stochastic Shift                                                                                                                          ||");

		System.out.println ("\t|        Move-Up Probability                                                                                                                         ||");

		System.out.println ("\t|        Move-Up X Node Value                                                                                                                        ||");

		System.out.println ("\t|        Move-Up Short Rate                                                                                                                          ||");

		System.out.println ("\t|        Move-Down Probability                                                                                                                       ||");

		System.out.println ("\t|        Move-Down X Node Value                                                                                                                      ||");

		System.out.println ("\t|        Move-Down Short Rate                                                                                                                        ||");

		System.out.println ("\t|        Stay Probability                                                                                                                            ||");

		System.out.println ("\t|        Stay X Node Value                                                                                                                           ||");

		System.out.println ("\t|        Stay Short Rate                                                                                                                             ||");

		System.out.println ("\t|                                                                                                                                                    ||");

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------------------------------------------------||");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.Today();

		String strCurrency = "USD";
		double dblStartingShortRate = 0.05;
		double dblSigma = 0.01;
		double dblA = 1.;

		SingleFactorStateEvolver hw = HullWhiteEvolver (
			strCurrency,
			dblSigma,
			dblA,
			dblStartingShortRate
		);

		double dblSpotDate = dtSpot.julian();

		double dblFinalDate = dtSpot.addMonths (6).julian();

		double dblInitialDate = dblSpotDate;
		TrinomialTreeTransitionMetrics hwtm = null;

		TreeHeader ("\t|    Hull-White Trinomial Tree Upwards Evolution Metrics                                                                                             ||");

		while (dblInitialDate < dblFinalDate) {
			DumpMetrics (hwtm =
				hw.evolveTrinomialTree (
					dblSpotDate,
					dblInitialDate,
					dblFinalDate,
					null == hwtm ? null : hwtm.upNodeMetrics()
				)
			);

			dblInitialDate += 10;
		}

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------------------------------------------------||");

		hwtm = null;
		dblInitialDate = dblSpotDate;

		TreeHeader ("\t|    Hull-White Trinomial Tree Downwards Evolution Metrics                                                                                           ||");

		while (dblInitialDate < dblFinalDate) {
			DumpMetrics (hwtm =
				hw.evolveTrinomialTree (
					dblSpotDate,
					dblInitialDate,
					dblFinalDate,
					null == hwtm ? null : hwtm.downNodeMetrics()
				)
			);

			dblInitialDate += 10;
		}

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------------------------------------------------||");

		hwtm = null;
		dblInitialDate = dblSpotDate;

		TreeHeader ("\t|    Hull-White Trinomial Tree Stay-Put Evolution Metrics                                                                                            ||");

		while (dblInitialDate < dblFinalDate) {
			DumpMetrics (hwtm =
				hw.evolveTrinomialTree (
					dblSpotDate,
					dblInitialDate,
					dblFinalDate,
					null == hwtm ? null : hwtm.stayNodeMetrics()
				)
			);

			dblInitialDate += 10;
		}

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------------------------------------------------||");
	}
}
