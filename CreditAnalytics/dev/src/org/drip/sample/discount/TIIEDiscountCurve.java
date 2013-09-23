
package org.drip.sample.discount;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.DiscountCurve;
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

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (dtEffective, dtEffective.addTenor (astrTenor[i]), 0., "MXN", "MXN-LIBOR-6M", "MXN");

		return aCalibComp;
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

		double[] adblCashQuote = new double[] {0.040425};

		RegimeBuilderSet rbsCash = RegimeBuilderSet.CreateRegimeBuilderSet (
			"CASH",
			LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
			LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"});

		double[] adblSwapQuote = new double[]
			{0.0394, 0.0384, 0.038475, 0.03855, 0.0408, 0.044, 0.0479, 0.0518, 0.0584, 0.0639, 0.0706, 0.074, 0.0767};

		RegimeBuilderSet rbsSwap = RegimeBuilderSet.CreateRegimeBuilderSet (
			"SWAP",
			LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
			LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote);

		RegimeBuilderSet[] aRBS = new RegimeBuilderSet[] {rbsCash, rbsSwap};

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

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aCashComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 4, 1.) + " | " + FormatUtil.FormatDouble (adblCashQuote[i], 1, 4, 1.));

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 4, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 4, 1.));
	}
}
