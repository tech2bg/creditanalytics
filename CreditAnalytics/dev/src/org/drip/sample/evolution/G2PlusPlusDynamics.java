
package org.drip.sample.evolution;

import org.drip.analytics.date.*;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.dynamics.G2PlusPlus;

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
 * G2PlusPlusDynamics demonstrates the Construction and Usage of the G2++ 2-Factor HJM Model Dynamics for the
 *  Evolution of the Short Rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class G2PlusPlusDynamics {

	private static final G2PlusPlus G2PlusPlusEvolver (
		final double dblSigma,
		final double dblA,
		final double dblEta,
		final double dblB,
		final double dblRho,
		final double dblStartingForwardRate)
		throws Exception
	{
		return new G2PlusPlus (
			dblSigma,
			dblA,
			dblEta,
			dblB,
			new RandomSequenceGenerator[] {
				new BoxMullerGaussian (0., 1.),
				new BoxMullerGaussian (0., 1.)
			},
			dblRho,
			new FlatUnivariate (dblStartingForwardRate)
		);
	}

	private static final void ShortRateEvolution (
		final G2PlusPlus g2pp,
		final JulianDate dtStart,
		final String strCurrency,
		final String strViewTenor,
		final double dblStartingShortRate)
		throws Exception
	{
		int iDayStep = 2;
		double dblX = 0.;
		double dblY = 0.;
		JulianDate dtSpot = dtStart;
		double dblShortRate = dblStartingShortRate;

		double dblStartDate = dtStart.julian();

		double dblEndDate = dtStart.addTenor (strViewTenor).julian();

		System.out.println ("\t|-----------------------------------------------------------------------||");

		System.out.println ("\t|                                                                       ||");

		System.out.println ("\t|         G2++ - 2-factor HJM Model - Short Rate Evolution Run          ||");

		System.out.println ("\t|-----------------------------------------------------------------------||");

		System.out.println ("\t|                                                                       ||");

		System.out.println ("\t|    L->R:                                                              ||");

		System.out.println ("\t|        Date                                                           ||");

		System.out.println ("\t|        X (%)                                                          ||");

		System.out.println ("\t|        X - Increment (%)                                              ||");

		System.out.println ("\t|        Y (%)                                                          ||");

		System.out.println ("\t|        Y - Increment (%)                                              ||");

		System.out.println ("\t|        Phi (%)                                                        ||");

		System.out.println ("\t|        Short Rate (%)                                                 ||");

		System.out.println ("\t|-----------------------------------------------------------------------||");

		while (dtSpot.julian() < dblEndDate) {
			double dblSpotDate = dtSpot.julian();

			double dblDeltaX = g2pp.deltaX (
				dblStartDate,
				dblSpotDate,
				dblX,
				iDayStep / 365.25
			);

			dblX += dblDeltaX;

			double dblDeltaY = g2pp.deltaY (
				dblStartDate,
				dblSpotDate,
				dblY,
				iDayStep / 365.25
			);

			dblY += dblDeltaY;

			double dblPhi = g2pp.phi (
				dblStartDate,
				dblSpotDate
			);

			dblShortRate = dblX + dblY + dblPhi;

			System.out.println ("\t| [" + dtSpot + "] = " +
				FormatUtil.FormatDouble (dblX, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblDeltaX, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblY, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblDeltaY, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblPhi, 1, 2, 100.) + "% || " +
				FormatUtil.FormatDouble (dblShortRate, 1, 2, 100.) + "% || "
			);

			dtSpot = dtSpot.addBusDays (iDayStep, strCurrency);
		}

		System.out.println ("\t|-----------------------------------------------------------------------||");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.Today();

		String strCurrency = "USD";
		double dblStartingShortRate = 0.05;
		double dblSigma = 0.05;
		double dblA = 0.5;
		double dblEta = 0.05;
		double dblB = 0.5;
		double dblRho = 0.5;

		G2PlusPlus g2pp = G2PlusPlusEvolver (
			dblSigma,
			dblA,
			dblEta,
			dblB,
			dblRho,
			dblStartingShortRate
		);

		ShortRateEvolution (
			g2pp,
			dtSpot,
			strCurrency,
			"4M",
			dblStartingShortRate
		);
	}
}
