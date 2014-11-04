
package org.drip.sample.option;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.VolatilityTermStructure;
import org.drip.param.creator.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;

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
 * DeterministicVolatilityTermStructure contains an illustration of the Calibration and Extraction of the
 * 	Implied and the Deterministic Volatility Term Structures. This does not deal with Local Volatility
 * 	Surfaces.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DeterministicVolTermStructure {

	private static final void OnGrid (
		final VolatilityTermStructure vts,
		final String[] astrMaturityTenor,
		final double[] dblNodeInput)
		throws Exception
	{
		System.out.println ("\n\t" + vts.label());

		System.out.println ("\n\t|------------------------------------|");

		System.out.println ("\t| TNR =>   CALC  |   IMPL  |  INPUT  |");

		System.out.println ("\t|------------------------------------|");

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			System.out.println ("\t| " + astrMaturityTenor[i] + " => " +
				FormatUtil.FormatDouble (vts.node (astrMaturityTenor[i]), 2, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (vts.impliedVol (astrMaturityTenor[i]), 2, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblNodeInput[i], 2, 2, 100.) + "% |");

		System.out.println ("\t|------------------------------------|");
	}

	private static final void OffGrid (
		final String strHeader,
		final String[] astrLabel,
		final VolatilityTermStructure[] aVTS,
		final String[] astrMaturityTenor)
		throws Exception
	{
		System.out.println ("\n\n\t\t" + strHeader + "\n");

		System.out.print ("\t| TNR =>");

		for (int i = 0; i < aVTS.length; ++i)
			System.out.print (" " + astrLabel[i] + " | ");

		System.out.println ("\n");

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			System.out.print ("\t| " + astrMaturityTenor[i] + " =>");

			for (int j = 0; j < aVTS.length; ++j)
				System.out.print ("  " + FormatUtil.FormatDouble (aVTS[j].node (astrMaturityTenor[i]), 2, 2, 100.) + "%   | ");

			System.out.print ("\n");
		}

		System.out.println ("\n");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		String[] astrMaturityTenor = new String[] {"01Y", "02Y", "03Y", "04Y", "05Y", "06Y", "07Y", "08Y", "09Y"};
		double[] adblImpliedCallVolatility = new double[] {0.700, 0.672, 0.661, 0.596, 0.551, 0.518, 0.492, 0.471, 0.452};

		VolatilityTermStructure tsCallVolatilityCubicPoly =
			ScenarioDeterministicVolatilityBuilder.CubicPolynomialTermStructure (
				"CUBIC_POLY_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility
			);

		VolatilityTermStructure tsCallVolatilityQuarticPoly =
			ScenarioDeterministicVolatilityBuilder.QuarticPolynomialTermStructure (
				"QUARTIC_POLY_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility
			);

		VolatilityTermStructure tsCallVolatilityKaklisPandelis =
			ScenarioDeterministicVolatilityBuilder.KaklisPandelisTermStructure (
				"KAKLIS_PANDELIS_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility
			);

		VolatilityTermStructure tsCallVolatilityKLKHyperbolic =
			ScenarioDeterministicVolatilityBuilder.KLKHyperbolicTermStructure (
				"KLK_HYPERBOLIC_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility,
				1.
			);

		VolatilityTermStructure tsCallVolatilityKLKRationalLinear =
			ScenarioDeterministicVolatilityBuilder.KLKRationalLinearTermStructure (
				"KLK_RATIONAL_LINEAR_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility,
				1.
			);

		VolatilityTermStructure tsCallVolatilityKLKRationalQuadratic =
			ScenarioDeterministicVolatilityBuilder.KLKRationalQuadraticTermStructure (
				"KLK_RATIONAL_QUADRATIC_CALLVOL_TERMSTRUCTURE",
				dtToday,
				"USD",
				null,
				astrMaturityTenor,
				adblImpliedCallVolatility,
				0.0001
			);

		OnGrid (
			tsCallVolatilityKLKHyperbolic,
			astrMaturityTenor,
			adblImpliedCallVolatility
		);

		String[] astrOffGridTenor = new String[] {"18M", "30M", "42M", "54M", "06Y", "09Y"};

		OffGrid ("ATM_CALLVOL_TERM_STRUCTURE",
			new String[] {"Cubic Poly", "Quart Poly", "KaklisPand", "KLKHyperbl", "KLKRatlLin", "KLKRatlQua"},
			new VolatilityTermStructure[] {
				tsCallVolatilityCubicPoly,
				tsCallVolatilityQuarticPoly,
				tsCallVolatilityKaklisPandelis,
				tsCallVolatilityKLKHyperbolic,
				tsCallVolatilityKLKRationalLinear,
				tsCallVolatilityKLKRationalQuadratic},
			astrOffGridTenor
		);
	}
}
