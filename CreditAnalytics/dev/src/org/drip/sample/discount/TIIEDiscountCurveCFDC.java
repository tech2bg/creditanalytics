
package org.drip.sample.discount;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.math.common.FormatUtil;
import org.drip.math.function.QuadraticRationalShapeControl;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.*;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.estimator.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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

public class TIIEDiscountCurveCFDC {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final java.lang.String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"), "MXN");

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"),
				0.,
				"MXN",
				"MXN-LIBOR-6M",
				"MXN");

		return aCalibComp;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "MXN");

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new java.lang.String[] {"1M"});

		double[] adblCashQuote = new double[] {0.0403};

		RegimeRepresentationSpec rbsCash = RegimeRepresentationSpec.CreateRegimeBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y"});
			// {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"});

		double[] adblSwapQuote = new double[]
			{0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596};
			// {0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776};

		RegimeRepresentationSpec rbsSwap = RegimeRepresentationSpec.CreateRegimeBuilderSet (
			"SWAP",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote);

		RegimeRepresentationSpec[] aRBS = new RegimeRepresentationSpec[] {rbsCash, rbsSwap};

		LinearCurveCalibrator lccShapePreserving = new LinearCurveCalibrator (
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (3),
				DesignInelasticParams.Create (1, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentRegime.CALIBRATE,
			null);

		GlobalControlCurveParams crbpSmoothening1 = new GlobalControlCurveParams (
			org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (3),
				DesignInelasticParams.Create (1, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentRegime.CALIBRATE,
			null);

		GlobalControlCurveParams crbpSmoothening2 = new GlobalControlCurveParams (
			org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (3),
				DesignInelasticParams.Create (1, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentRegime.CALIBRATE,
			null);

		DiscountCurve dcShapePreserving = RatesScenarioCurveBuilder.ShapePreservingBuild (
			lccShapePreserving,
			aRBS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		DiscountCurve dcSmoothening1 = RatesScenarioCurveBuilder.SmoothingGlobalControlBuild (
			dcShapePreserving,
			lccShapePreserving,
			crbpSmoothening1,
			aRBS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		DiscountCurve dcSmoothening2 = RatesScenarioCurveBuilder.SmoothingGlobalControlBuild (
			dcShapePreserving,
			lccShapePreserving,
			crbpSmoothening2,
			aRBS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening1, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening2, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.)
			);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening1, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening2, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.)
			);

		CalibratableComponent[] aCC = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3Y", "6Y", "9Y", "12Y", "15Y", "18Y", "21Y", "24Y", "27Y", "30Y"});

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t           BESPOKE SWAPS PAR RATE");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  SMOOTHING #1 |  SMOOTHING #2");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCC.length; ++i)
			System.out.println ("\t[" + aCC[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening1, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcSmoothening2, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.)
			);
	}
}
