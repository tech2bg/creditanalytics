
package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.creator.ScenarioDiscountCurveBuilder;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.CashBuilder;
import org.drip.product.creator.EDFutureBuilder;
import org.drip.product.creator.RatesStreamBuilder;
import org.drip.product.definition.CalibratableComponent;
import org.drip.product.definition.Component;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * TemplatedDiscountCurveBuilder sample demonstrates the usage of the different pre-built Discount Curve
 * 	Builders. It shows the following:
 * 	- Construct the Array of Cash Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Array of Swap Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver.
 * 	- Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver with Zero Rate
 * 		Smoothening applied.
 * 	- Construct the Cubic Polynomial Discount Factor Shape Preserver.
 * 	- Construct the Cubic Polynomial Discount Factor Shape Preserver with Zero Rate Smoothening applied.
 * 	- Construct the Discount Curve using the Bear Sterns' DENSE Methodology.
 * 	- Construct the Discount Curve using the Bear Sterns' DUALDENSE Methodology.
 * 	- Cross-Comparison of the Cash Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Cross-Comparison of the Swap Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Cross-Comparison of the generated Discount Factor across the different curve construction
 * 		Methodologies for different node points.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class TemplatedDiscountCurveBuilder {

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), "USD");

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "USD"), 0., "USD", "USD-LIBOR-6M", "USD");

		return aCalibComp;
	}

	/*
	 * Compute the desired component Metric
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final double ComponentMetric (
		final Component comp,
		final ValuationParams valParams,
		final DiscountCurve dc,
		final String strMeasure)
		throws Exception
	{
		return comp.calcMeasureValue (
			valParams,
			null,
			ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
			null,
			strMeasure);
	}

	/*
	 * This sample demonstrates the usage of the different pre-built Discount Curve Builders. It shows the
	 * 	following:
	 * 	- Construct the Array of Cash Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Array of Swap Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver.
	 * 	- Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver with Zero Rate
	 * 		Smoothening applied.
	 * 	- Construct the Cubic Polynomial Discount Factor Shape Preserver.
	 * 	- Construct the Cubic Polynomial Discount Factor Shape Preserver with Zero Rate Smoothening applied.
	 * 	- Construct the Discount Curve using the Bear Sterns' DENSE Methodology.
	 * 	- Construct the Discount Curve using the Bear Sterns' DUALDENSE Methodology.
	 * 	- Cross-Comparison of the Cash Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Cross-Comparison of the Swap Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Cross-Comparison of the generated Discount Factor across the different curve construction
	 * 		Methodologies for different node points.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static final void TemplatedDiscountCurveBuilderSample()
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "USD");

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		/*
		 * Construct the Array of Cash Instruments and their Quotes from the given set of parameters
		 */

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new int[] {1, 2, 7, 14, 30, 60},
			8);

		double[] adblCashQuote = new double[] {
			0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, // Cash Rate
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}; // EDF Rate;

		/*
		 * Construct the Array of Swap Instruments and their Quotes from the given set of parameters
		 */

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"});

		double[] adblSwapQuote = new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409};

		/*
		 * Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver
		 */

		DiscountCurve dcKLKHyperbolicShapePreserver = ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			valParams,
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			false);

		/*
		 * Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver with Zero Rate
		 * 	Smoothening applied
		 */

		DiscountCurve dcKLKHyperbolicSmoother = ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SMOOTH_TEMPLATE",
			valParams,
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			true);

		/*
		 * Construct the Cubic Polynomial Discount Factor Shape Preserver
		 */

		DiscountCurve dcCubicPolyShapePreserver = ScenarioDiscountCurveBuilder.CubicPolyDFRateShapePreserver (
			"CUBIC_POLY_SHAPE_TEMPLATE",
			valParams,
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			false);

		/*
		 * Construct the Cubic Polynomial Discount Factor Shape Preserver with Zero Rate Smoothening applied.
		 */

		DiscountCurve dcCubicPolySmoother = ScenarioDiscountCurveBuilder.CubicPolyDFRateShapePreserver (
			"CUBIC_POLY_SMOOTH_TEMPLATE",
			valParams,
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			true);

		/*
		 * Construct the Discount Curve using the Bear Sterns' DENSE Methodology.
		 */

		DiscountCurve dcDENSE = ScenarioDiscountCurveBuilder.DENSE (
			"DENSE",
			valParams,
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			null);

		/*
		 * Construct the Discount Curve using the Bear Sterns' DUAL DENSE Methodology.
		 */

		DiscountCurve dcDualDENSE = ScenarioDiscountCurveBuilder.DUALDENSE (
			"DENSE",
			valParams,
			aCashComp,
			adblCashQuote,
			"1M",
			aSwapComp,
			adblSwapQuote,
			"3M",
			null);

		/*
		 * Cross-Comparison of the Cash Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\t\t\t\t\t\tCASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t   MATURITY  | KLK HYPER SHAPE | KLK HYPER SMOTH | CUBE POLY SHAPE | CUBE POLY SMOTH |      DENSE      |   DUAL  DENSE   |      INPUT");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] =    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcKLKHyperbolicShapePreserver, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcKLKHyperbolicSmoother, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcCubicPolyShapePreserver, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcCubicPolySmoother, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcDENSE, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aCashComp[i], valParams, dcDualDENSE, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Swap Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\t\t\t\t\t\tSWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t   MATURITY  | KLK HYPER SHAPE | KLK HYPER SMOTH | CUBE POLY SHAPE | CUBE POLY SMOTH |      DENSE      |   DUAL  DENSE   |      INPUT");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] =    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcKLKHyperbolicShapePreserver, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcKLKHyperbolicSmoother, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcCubicPolyShapePreserver, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcCubicPolySmoother, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcDENSE, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aSwapComp[i], valParams, dcDualDENSE, "CalibSwapRate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the generated Discount Factor across the different curve construction
		 * 	methodologies for different node points.
		 */

		System.out.println ("\n\t-----------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t      DF     |   KLK HYPER SHAPE |  KLK HYPER SMOTH  |  CUBE POLY SHAPE  |  CUBE POLY SMOTH  |       DENSE       |     DUAL DENSE    ");

		System.out.println ("\t-----------------------------------------------------------------------------------------------------------------------------------");

		double dblStartDate = aCashComp[0].getMaturityDate().getJulian();

		double dblEndDate = aSwapComp[aSwapComp.length - 1].getMaturityDate().getJulian();

		double dblDateIncrement = 0.05 * (dblEndDate - dblStartDate);

		for (double dblDate = dblStartDate; dblDate <= dblEndDate; dblDate += dblDateIncrement) {
				System.out.println ("\t[" + new JulianDate (dblDate) + "] =    " +
					FormatUtil.FormatDouble (dcKLKHyperbolicShapePreserver.df (dblDate), 1, 8, 1.) + "    |    " +
					FormatUtil.FormatDouble (dcKLKHyperbolicSmoother.df (dblDate), 1, 8, 1.) + "    |    " +
					FormatUtil.FormatDouble (dcCubicPolyShapePreserver.df (dblDate), 1, 8, 1.) + "    |    " +
					FormatUtil.FormatDouble (dcCubicPolySmoother.df (dblDate), 1, 8, 1.) + "    |    " +
					FormatUtil.FormatDouble (dcDENSE.df (dblDate), 1, 8, 1.) + "    |    " +
					FormatUtil.FormatDouble (dcDualDENSE.df (dblDate), 1, 8, 1.));
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		TemplatedDiscountCurveBuilderSample();
	}
}
