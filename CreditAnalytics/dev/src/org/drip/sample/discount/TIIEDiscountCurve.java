
package org.drip.sample.discount;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.analytics.period.CouponPeriod;
import org.drip.math.common.FormatUtil;
import org.drip.math.function.RationalShapeControl;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.PolynomialBasisSetParams;
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

public class TIIEDiscountCurve {
	private static final String FIELD_SEPARATOR = "    ";

	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final java.lang.String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addTenor (astrTenor[i]), "MXN");

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			System.out.println ("Mat Date: " + dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"));

			aCalibComp[i] = RatesStreamBuilder.CreateIRS (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"),
				0.,
				"MXN",
				"MXN-LIBOR-6M",
				"MXN");
		}

		return aCalibComp;
	}

	private static final void DisplayFlow (
		final org.drip.product.definition.RatesComponent comp)
	{
		for (CouponPeriod p : comp.getCouponPeriod())
			System.out.println ("\t\t" +
				JulianDate.fromJulian (p.getStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getEndDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR
			);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new java.lang.String[] {"1M"});

		double[] adblCashQuote = new double[] {0.0403};

		RegimeBuilderSet rbsCash = RegimeBuilderSet.CreateRegimeBuilderSet (
			"CASH",
			LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
			LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote);

		/* CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"});

		double[] adblSwapQuote = new double[]
			{0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776}; */

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[] {"2Y", "3Y"});

		double[] adblSwapQuote = new double[] {0.077, 0.084};

		RegimeBuilderSet rbsSwap = RegimeBuilderSet.CreateRegimeBuilderSet (
			"SWAP",
			LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
			LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote);

		RegimeBuilderSet[] aRBS = new RegimeBuilderSet[] {rbsSwap};

		DiscountCurve dc = RatesScenarioCurveBuilder.LinearBuild (
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (4),
				new DesignInelasticParams (2, 2),
				new ResponseScalingShapeController (true, new RationalShapeControl (0.))),
			new RegimeCalibrationSetting (
				RegimeCalibrationSetting.BOUNDARY_CONDITION_NATURAL,
				RegimeCalibrationSetting.CALIBRATE),
			aRBS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i) {
			System.out.println ("\n\t----------------------------------------------------------------");

			System.out.println ("\t     FLOAT FLOW");

			System.out.println ("\t----------------------------------------------------------------");

			DisplayFlow (((org.drip.product.rates.IRSComponent) rbsSwap.getCalibComp()[i]).getFloatStream());

			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));
		}
	}
}
