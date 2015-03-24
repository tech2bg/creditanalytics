
package org.drip.sample.evolution;

import org.drip.analytics.date.*;
import org.drip.analytics.definition.MarketSurface;
import org.drip.dynamics.hjm.MultiFactorStateEvolver;
import org.drip.dynamics.hjm.MultiFactorVolatility;
import org.drip.function.deterministic.AbstractUnivariate;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.param.creator.ScenarioMarketSurfaceBuilder;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

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
 * MultiFactorHJMDynamics demonstrates the Construction and Usage of the Multi-Factor Gaussian HJM Model
 *  Dynamics for the Evolution of the Instantaneous Forward Rate, the Price, and the Short Rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiFactorHJMDynamics {

	private static final MarketSurface FlatVolatilitySurface (
		final JulianDate dtStart,
		final String strCurrency,
		final double dblFlatVol)
		throws Exception
	{
		return ScenarioMarketSurfaceBuilder.CustomSplineWireSurface (
			"VIEW_TARGET_VOLATILITY_SURFACE",
			dtStart,
			strCurrency,
			null,
			new double[] {
				dtStart.julian(),
				dtStart.addYears (2).julian(),
				dtStart.addYears (4).julian(),
				dtStart.addYears (6).julian(),
				dtStart.addYears (8).julian(),
				dtStart.addYears (10).julian()
			},
			new double[] {
				dtStart.julian(),
				dtStart.addYears (2).julian(),
				dtStart.addYears (4).julian(),
				dtStart.addYears (6).julian(),
				dtStart.addYears (8).julian(),
				dtStart.addYears (10).julian()
			},
			new double[][] {
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
				{dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol, dblFlatVol},
			},
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				null,
				null
			),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				null,
				null
			)
		);
	}

	private static final MultiFactorStateEvolver HJMInstance (
		final JulianDate dtStart,
		final String strCurrency,
		final MarketSurface mktSurfFlatVol1,
		final MarketSurface mktSurfFlatVol2,
		final MarketSurface mktSurfFlatVol3,
		final AbstractUnivariate auForwardRate)
		throws Exception
	{
		MultiFactorVolatility mfv = new MultiFactorVolatility (
			new MarketSurface[] {
				mktSurfFlatVol1,
				mktSurfFlatVol2,
				mktSurfFlatVol3
			},
			new PrincipalFactorSequenceGenerator (
				new UnivariateSequenceGenerator[] {
					new BoxMullerGaussian (0., 1.),
					new BoxMullerGaussian (0., 1.),
					new BoxMullerGaussian (0., 1.)
				},
				new double[][] {
					{1.0, 0.1, 0.2},
					{0.1, 1.0, 0.2},
					{0.2, 0.1, 1.0}
				},
				3
			)
		);

		return new MultiFactorStateEvolver (
			mfv,
			auForwardRate
		);
	}

	private static final void Evolve (
		final MultiFactorStateEvolver hjm,
		final JulianDate dtStart,
		final String strCurrency,
		final String strViewTenor,
		final String strTargetTenor,
		final double dblStartingForwardRate,
		final double dblStartingPrice)
		throws Exception
	{
		double dblViewDate = dtStart.addTenor (strViewTenor).julian();

		double dblTargetDate = dtStart.addTenor (strTargetTenor).julian();

		int iDayStep = 2;
		JulianDate dtSpot = dtStart;
		double dblPrice = dblStartingPrice;
		double dblShortRate = dblStartingForwardRate;
		double dblLIBORForwardRate = dblStartingForwardRate;
		double dblInstantaneousForwardRate = dblStartingForwardRate;
		double dblContinuouslyCompoundedShortRate = dblStartingForwardRate;
		double dblShiftedLIBORForwardRate = dblStartingForwardRate + (365.25 / (dblTargetDate - dblViewDate));

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                                                                                                                               ||");

		System.out.println ("\t|    Heath-Jarrow-Morton Gaussian Run                                                                                           ||");

		System.out.println ("\t|    --------------------------------                                                                                           ||");

		System.out.println ("\t|                                                                                                                               ||");

		System.out.println ("\t|        L->R:                                                                                                                  ||");

		System.out.println ("\t|            Date                                                                                                               ||");

		System.out.println ("\t|            Instantaneous Forward Rate (%)                                                                                     ||");

		System.out.println ("\t|            Instantaneous Forward Rate - Change (%)                                                                            ||");

		System.out.println ("\t|            LIBOR Forward Rate (%)                                                                                             ||");

		System.out.println ("\t|            LIBOR Forward Rate - Change (%)                                                                                    ||");

		System.out.println ("\t|            Shifted LIBOR Forward Rate (%)                                                                                     ||");

		System.out.println ("\t|            Shifted LIBOR Forward Rate - Change (%)                                                                            ||");

		System.out.println ("\t|            Short Rate (%)                                                                                                     ||");

		System.out.println ("\t|            Short Rate - Change (%)                                                                                            ||");

		System.out.println ("\t|            Continuously Compounded Short Rate (%)                                                                             ||");

		System.out.println ("\t|            Continuously Compounded Short Rate - Change (%)                                                                    ||");

		System.out.println ("\t|            Price                                                                                                              ||");

		System.out.println ("\t|            Price - Change                                                                                                     ||");

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------||");

		while (dtSpot.julian() < dblViewDate) {
			double dblSpotDate = dtSpot.julian();

			double dblIFRIncrement = hjm.instantaneousForwardRateIncrement (
				dblViewDate,
				dblTargetDate,
				iDayStep / 365.25
			);

			dblInstantaneousForwardRate += dblIFRIncrement;

			double dblLIBORForwardRateIncrement = hjm.liborForwardRateIncrement (
				dblSpotDate,
				dblViewDate,
				dblTargetDate,
				dblLIBORForwardRate,
				iDayStep / 365.25
			);

			dblLIBORForwardRate += dblLIBORForwardRateIncrement;

			double dblShiftedLIBORForwardRateIncrement = hjm.shiftedLIBORForwardIncrement (
				dblSpotDate,
				dblViewDate,
				dblTargetDate,
				dblShiftedLIBORForwardRate,
				iDayStep / 365.25
			);

			dblShiftedLIBORForwardRate += dblShiftedLIBORForwardRateIncrement;

			double dblShortRateIncrement = hjm.shortRateIncrement (
				dblSpotDate,
				dblViewDate,
				iDayStep / 365.25
			);

			dblShortRate += dblShortRateIncrement;

			double dblProportionalPriceIncrement = hjm.proportionalPriceIncrement (
				dblViewDate,
				dblTargetDate,
				dblShortRate,
				iDayStep / 365.25
			);

			dblPrice *= (1. + dblProportionalPriceIncrement);

			double dblContinuouslyCompoundedShortRateIncrement = hjm.compoundedShortRateIncrement (
				dblSpotDate,
				dblViewDate,
				dblTargetDate,
				dblContinuouslyCompoundedShortRate,
				dblShortRate,
				iDayStep / 365.25
			);

			dblContinuouslyCompoundedShortRate += dblContinuouslyCompoundedShortRateIncrement;

			System.out.println ("\t| [" + dtSpot + "] = " +
				FormatUtil.FormatDouble (dblInstantaneousForwardRate, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblIFRIncrement, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblLIBORForwardRate, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblLIBORForwardRateIncrement, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblShiftedLIBORForwardRate, 1, 4, 1.) + " | " +
				FormatUtil.FormatDouble (dblShiftedLIBORForwardRateIncrement, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblShortRate, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblShortRateIncrement, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblContinuouslyCompoundedShortRate, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblContinuouslyCompoundedShortRateIncrement, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblPrice, 2, 2, 100.) + " | " +
				FormatUtil.FormatDouble (dblProportionalPriceIncrement, 1, 2, 100.) + " || "
			);

			dtSpot = dtSpot.addBusDays (iDayStep, strCurrency);
		}

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------||");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		String strCurrency = "USD";

		JulianDate dtSpot = DateUtil.Today();

		double dblFlatVol1 = 0.01;
		double dblFlatVol2 = 0.02;
		double dblFlatVol3 = 0.03;
		double dblFlatForwardRate = 0.05;
		double dblStartingPrice = 0.9875;

		MarketSurface mktSurfFlatVol1 = FlatVolatilitySurface (
			dtSpot,
			strCurrency,
			dblFlatVol1
		);

		MarketSurface mktSurfFlatVol2 = FlatVolatilitySurface (
			dtSpot,
			strCurrency,
			dblFlatVol2
		);

		MarketSurface mktSurfFlatVol3 = FlatVolatilitySurface (
			dtSpot,
			strCurrency,
			dblFlatVol3
		);

		MultiFactorStateEvolver hjm = HJMInstance (
			dtSpot,
			strCurrency,
			mktSurfFlatVol1,
			mktSurfFlatVol2,
			mktSurfFlatVol3,
			new FlatUnivariate (dblFlatForwardRate)
		);

		Evolve (
			hjm,
			dtSpot,
			strCurrency,
			"3M",
			"6M",
			dblFlatForwardRate,
			dblStartingPrice
		);
	}
}
