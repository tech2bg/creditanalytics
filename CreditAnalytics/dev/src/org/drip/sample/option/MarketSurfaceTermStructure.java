
package org.drip.sample.option;

import java.util.*;

import org.drip.analytics.date.*;
import org.drip.analytics.definition.*;
import org.drip.param.creator.ScenarioMarketSurfaceBuilder;
import org.drip.param.pricer.HestonOptionPricerParams;
import org.drip.param.valuation.CollateralizationParams;
import org.drip.pricer.option.HestonStochasticVolatilityAlgorithm;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.fourier.PhaseAdjuster;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * MarketSurfaceTermStructure contains an illustration of the Creation and Usage of the Strike Anchored and
 * 	Maturity Anchored Term Structures extracted from the given Market Surface.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MarketSurfaceTermStructure {

	private static final SegmentCustomBuilderControl CubicPolySCBC()
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			SegmentInelasticDesignControl.Create (2, 2),
			null,
			null
		);
	}

	private static final void EvaluateSplineSurface (
		final MarketSurface mktSurf,
		final double[] adblStrikeATMFactor,
		final String[] astrMaturityTenor)
		throws Exception
	{
		System.out.println ("\t|------------------------------------------------------------|");

		System.out.print ("\t|------------------------------------------------------------|\n\t|  ATM/TTE  =>");

		for (String strMaturity : astrMaturityTenor)
			System.out.print ("    " + strMaturity + "  ");

		System.out.println ("  |\n\t|------------------------------------------------------------|");

		for (double dblStrike : adblStrikeATMFactor) {
			System.out.print ("\t|  " + FormatUtil.FormatDouble (dblStrike, 1, 2, 1.) + "    =>");

			for (String strMaturity : astrMaturityTenor)
				System.out.print ("  " + FormatUtil.FormatDouble (mktSurf.node (dblStrike, strMaturity), 2, 2, 100.) + "%");

			System.out.print ("  |\n");
		}

		System.out.println ("\t|------------------------------------------------------------|");
	}

	private static final void EvaluateStrikeTermStructure (
		final MarketSurface mktSurf,
		final double[] adblStrikeATMFactor,
		final String[] astrMaturityTenor)
		throws Exception
	{
		System.out.println ("\n\t|--------- STRIKE TERM STRUCTURE FROM MARKET SURFACE --------|");

		System.out.println ("\t|------------------------------------------------------------|");

		System.out.print ("\t|------------------------------------------------------------|\n\t|  ATM/TTE  =>");

		for (String strMaturity : astrMaturityTenor)
			System.out.print ("    " + strMaturity + "  ");

		System.out.println ("  |\n\t|------------------------------------------------------------|");

		for (double dblStrike : adblStrikeATMFactor) {
			System.out.print ("\t|  " + FormatUtil.FormatDouble (dblStrike, 1, 2, 1.) + "    =>");

			TermStructure tsStrike = mktSurf.xAnchorTermStructure (dblStrike);

			for (String strMaturity : astrMaturityTenor)
				System.out.print ("  " + FormatUtil.FormatDouble (tsStrike.node (strMaturity), 2, 2, 100.) + "%");

			System.out.print ("  |\n");
		}

		System.out.println ("\t|------------------------------------------------------------|");
	}

	private static final void EvaluateMaturityTermStructure (
		final MarketSurface mktSurf,
		final double[] adblStrikeATMFactor,
		final String[] astrMaturityTenor)
		throws Exception
	{
		System.out.println ("\n\t|-------- MATURITY TERM STRUCTURE FROM MARKET SURFACE -------|");

		System.out.println ("\t|------------------------------------------------------------|");

		System.out.print ("\t|------------------------------------------------------------|\n\t|  ATM/TTE  =>");

		Map<String, TermStructure> mapMaturityTS = new TreeMap<String, TermStructure>();

		for (String strMaturity : astrMaturityTenor) {
			System.out.print ("    " + strMaturity + "  ");

			mapMaturityTS.put (strMaturity, mktSurf.maturityAnchorTermStructure (strMaturity));
		}

		System.out.println ("  |\n\t|------------------------------------------------------------|");

		for (double dblStrike : adblStrikeATMFactor) {
			System.out.print ("\t|  " + FormatUtil.FormatDouble (dblStrike, 1, 2, 1.) + "    =>");

			for (String strMaturity : astrMaturityTenor) {
				TermStructure tsMaturity = mapMaturityTS.get (strMaturity);

				System.out.print ("  " + FormatUtil.FormatDouble (tsMaturity.node (dblStrike), 2, 2, 100.) + "%");
			}

			System.out.print ("  |\n");
		}

		System.out.println ("\t|------------------------------------------------------------|");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		JulianDate dtStart = DateUtil.Today();

		double[] adblStrikeATMFactorCalib = new double[] {0.8, 0.9, 1.0, 1.1, 1.2};
		String[] astrMaturityTenorCalib = new String[] {"12M", "24M", "36M", "48M", "60M"};

		double dblRho = 0.3;
		double dblKappa = 1.;
		double dblSigma = 0.5;
		double dblTheta = 0.2;
		double dblLambda = 0.;

		HestonOptionPricerParams hopp = new HestonOptionPricerParams (
			HestonStochasticVolatilityAlgorithm.PAYOFF_TRANSFORM_SCHEME_AMST_2007,
			dblRho,
			dblKappa,
			dblSigma,
			dblTheta,
			dblLambda,
			PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL
		);

		MarketSurface priceSurfCubicPoly = ScenarioMarketSurfaceBuilder.HestonRunMarketSurface (
			"HESTON1993_CUBICPOLY_CALLPRICE_SURFACE",
			dtStart,
			"USD",
			0.01,
			1.,
			false,
			0.20,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			adblStrikeATMFactorCalib,
			astrMaturityTenorCalib,
			hopp,
			true,
			CubicPolySCBC(),
			CubicPolySCBC()
		);

		EvaluateSplineSurface (
			priceSurfCubicPoly,
			adblStrikeATMFactorCalib,
			astrMaturityTenorCalib
		);

		EvaluateSplineSurface (
			priceSurfCubicPoly,
			new double[] {0.500, 0.700, 0.850, 1.000, 1.150, 1.300, 1.500},
			new String[] {"06M", "21M", "36M", "51M", "66M"}
		);

		EvaluateStrikeTermStructure (
			priceSurfCubicPoly,
			new double[] {0.500, 0.700, 0.850, 1.000, 1.150, 1.300, 1.500},
			new String[] {"06M", "21M", "36M", "51M", "66M"}
		);

		EvaluateMaturityTermStructure (
			priceSurfCubicPoly,
			new double[] {0.500, 0.700, 0.850, 1.000, 1.150, 1.300, 1.500},
			new String[] {"06M", "21M", "36M", "51M", "66M"}
		);
	}
}
