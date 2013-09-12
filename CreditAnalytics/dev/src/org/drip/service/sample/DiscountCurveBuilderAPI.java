
package org.drip.service.sample;

import org.drip.analytics.calibration.*;
import org.drip.analytics.date.JulianDate;
import org.drip.math.common.FormatUtil;
import org.drip.math.grid.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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

/**
 * DiscountCurveBuilderAPI contains the sample demonstrating the full functionality behind creating highly
 * 	customized spline based discount curves.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DiscountCurveBuilderAPI {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i) {
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), "USD");

			System.out.println ("Cash Mat: " + aCalibComp[i].getMaturityDate());
		}

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i) {
			aCalibComp[i] = aEDF[i - aiDay.length];

			System.out.println ("EDF Mat: " + aCalibComp[i].getMaturityDate());
		}

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (dtEffective, dtEffective.addTenor (astrTenor[i]), 0., "USD", "USD-LIBOR-6M", "USD");

			System.out.println ("Swap Mat: " + aCalibComp[i].getMaturityDate());
		}

		return aCalibComp;
	}

	private static final LatentStateMetricMeasure[] LSMMFromQuotes (
		final double[] adblQuote)
		throws Exception
	{
		LatentStateMetricMeasure[] aLSMM = new LatentStateMetricMeasure[adblQuote.length];

		for (int i = 0; i < adblQuote.length; ++i)
			aLSMM[i] = new LatentStateMetricMeasure (
				LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
				LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
				"Rate",
				adblQuote[i]);

		return aLSMM;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			1.,
			new RegimeCalibrationSetting (RegimeCalibrationSetting.BOUNDARY_CONDITION_NATURAL, RegimeCalibrationSetting.CALIBRATE));

		JulianDate dtToday = JulianDate.Today();

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (dtToday, new int[] {1, 2, 7, 14, 30, 60}, 8);

		LatentStateMetricMeasure[] aCashLSMM = LSMMFromQuotes (new double[]
			{0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, // Cash Rate
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}); // EDF Rate

		MultiSegmentRegime regime = lcc.regimeFromCashInstruments (aCashComp,
			ValuationParams.CreateSpotValParams (dtToday.getJulian()), null, null, null, aCashLSMM);

		for (double dblX = regime.getLeftPredictorOrdinateEdge(); dblX <= regime.getRightPredictorOrdinateEdge(); dblX += 0.1 *
			(regime.getRightPredictorOrdinateEdge() - regime.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\tDiscount Factor[" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (regime.response (dblX), 1, 8, 1.) + " | " + regime.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"});

		LatentStateMetricMeasure[] aSwapLSMM = LSMMFromQuotes (new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409});

		regime = lcc.regimeFromSwapInstruments (regime, aSwapComp,
			ValuationParams.CreateSpotValParams (dtToday.getJulian()), null, null, null, aSwapLSMM);

		for (double dblX = regime.getLeftPredictorOrdinateEdge(); dblX <= regime.getRightPredictorOrdinateEdge(); dblX += 0.05 *
			(regime.getRightPredictorOrdinateEdge() - regime.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\t\tDiscount Factor[" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (regime.response (dblX), 1, 8, 1.) + " | " + regime.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}
	}
}
