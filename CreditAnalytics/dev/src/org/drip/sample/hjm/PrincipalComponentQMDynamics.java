
package org.drip.sample.hjm;

import org.drip.analytics.date.*;
import org.drip.analytics.definition.MarketSurface;
import org.drip.analytics.support.AnalyticsHelper;
import org.drip.dynamics.hjm.*;
import org.drip.function.deterministic.R1ToR1;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.param.creator.ScenarioMarketSurfaceBuilder;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;
import org.drip.state.identifier.ForwardLabel;
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
 * PrincipalComponentQMDynamics demonstrates the Construction and Usage of the Principal Component-Based
 *  Gaussian Model Dynamics for the Evolution of the Discount Factor Quantification Metrics - the
 *  Instantaneous Forward Rate, the LIBOR Forward Rate, the Shifted LIBOR Forward Rate, the Short Rate, the
 *  Compounded Short Rate, and the Price.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PrincipalComponentQMDynamics {

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
		final String strTenor,
		final MarketSurface mktSurfFlatVol1,
		final MarketSurface mktSurfFlatVol2,
		final MarketSurface mktSurfFlatVol3,
		final R1ToR1 auForwardRate,
		final int iNumFactor)
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
				iNumFactor
			)
		);

		return new MultiFactorStateEvolver (
			FundingLabel.Standard (strCurrency),
			ForwardLabel.Create (strCurrency, strTenor),
			mfv,
			auForwardRate
		);
	}

	private static final ShortForwardRateUpdate InitQMSnap (
		final JulianDate dtStart,
		final String strCurrency,
		final String strViewTenor,
		final String strTenor,
		final double dblInitialForwardRate,
		final double dblInitialPrice)
		throws Exception
	{
		return ShortForwardRateUpdate.Create (
			FundingLabel.Standard (strCurrency),
			ForwardLabel.Create (strCurrency, strTenor),
			dtStart.julian(),
			dtStart.julian(),
			dblInitialForwardRate,
			0.,
			dblInitialForwardRate,
			0.,
			dblInitialForwardRate + (365.25 / AnalyticsHelper.TenorToDays (strTenor)),
			0.,
			dblInitialForwardRate,
			0.,
			dblInitialForwardRate,
			0.,
			dblInitialPrice,
			0.
		);
	}

	private static final void QMEvolution (
		final MultiFactorStateEvolver hjm,
		final JulianDate dtStart,
		final String strCurrency,
		final String strViewTenor,
		final ShortForwardRateUpdate qmInitial)
		throws Exception
	{
		double dblViewDate = dtStart.addTenor (strViewTenor).julian();

		int iDayStep = 2;
		ShortForwardRateUpdate qm = qmInitial;
		JulianDate dtSpot = dtStart;

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                                                                                                                               ||");

		System.out.println ("\t|    Multi-Factor PCA-Based Gaussian HJM Quantification Metric Run                                                              ||");

		System.out.println ("\t|    -------------------------------------------------------------                                                              ||");

		System.out.println ("\t|                                                                                                                               ||");

		System.out.println ("\t|        Number of Prinicipal Components: " + hjm.mfv().msg().numFactor() + "                                                                                     ||");

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

			qm = (ShortForwardRateUpdate) hjm.evolve (
				dblSpotDate,
				dblViewDate,
				iDayStep / 365.25,
				qm
			);

			System.out.println ("\t| [" + dtSpot + "] = " +
				FormatUtil.FormatDouble (qm.instantaneousForwardRate(), 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (qm.instantaneousForwardRateIncrement(), 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (qm.liborForwardRate(), 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (qm.liborForwardRateIncrement(), 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (qm.shiftedLIBORForwardRate(), 1, 4, 1.) + " | " +
				FormatUtil.FormatDouble (qm.shiftedLIBORForwardRateIncrement(), 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (qm.shortRate(), 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (qm.shortRateIncrement(), 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (qm.compoundedShortRate(), 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (qm.compoundedShortRateIncrement(), 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (qm.price(), 2, 2, 100.) + " | " +
				FormatUtil.FormatDouble (qm.priceIncrement(), 1, 2, 100.) + " || "
			);

			dtSpot = dtSpot.addBusDays (
				iDayStep,
				strCurrency
			);
		}

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------||");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		String strCurrency = "USD";
		double dblFlatVol1 = 0.007;
		double dblFlatVol2 = 0.009;
		double dblFlatVol3 = 0.004;
		double dblFlatForwardRate = 0.05;
		double dblInitialPrice = 0.9875;
		String strViewTenor = "3M";
		String strTenor = "3M";

		JulianDate dtSpot = DateUtil.Today();

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

		ShortForwardRateUpdate qmInitial = InitQMSnap (
			dtSpot,
			strCurrency,
			strViewTenor,
			strTenor,
			dblFlatForwardRate,
			dblInitialPrice
		);

		int[] aiNumFactor = new int[] {
			1, 2, 3
		};

		for (int iNumFactor : aiNumFactor) {
			MultiFactorStateEvolver hjm = HJMInstance (
				dtSpot,
				strCurrency,
				strTenor,
				mktSurfFlatVol1,
				mktSurfFlatVol2,
				mktSurfFlatVol3,
				new FlatUnivariate (dblFlatForwardRate),
				iNumFactor
			);

			QMEvolution (
				hjm,
				dtSpot,
				strCurrency,
				strViewTenor,
				qmInitial
			);
		}
	}
}
