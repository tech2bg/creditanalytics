
package org.drip.sample.rates;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.*;

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
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DepositComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		DepositComponent[] aDeposit = new DepositComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
				strCurrency
			);

		return aDeposit;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor)
		throws Exception
	{
		FixFloatComponent[] aIRS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<CashflowPeriod> lsFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				null
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				null,
				null
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				0.,
				1.,
				null,
				lsFixedPeriods
			);

			FixFloatComponent irs = new FixFloatComponent (fixStream, floatStream);

			irs.setPrimaryCode ("IRS." + astrMaturityTenor + "." + strCurrency);

			aIRS[i] = irs;
		}

		return aIRS;
	}

	/*
	 * Compute the desired component Metric
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final double ComponentMetric (
		final FixedIncomeComponent comp,
		final ValuationParams valParams,
		final DiscountCurve dc,
		final String strMeasure)
		throws Exception
	{
		return comp.measureValue (
			valParams,
			null,
			MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
			null,
			strMeasure
		);
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

	public static final void TemplatedDiscountCurveBuilderSample (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		ValuationParams valParams = new ValuationParams (
			dtSpot,
			dtSpot,
			strCurrency
		);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		DepositComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				2, 7, 14, 30, 60, 90, 180, 270, 360, 450, 540, 630, 720
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0017, 0.0017, 0.0018, 0.0020, 0.0023, 0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160
		};

		String[] astrDepositManifestMeasure = new String[] {
			"Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate"
		};

		/*
		 * Construct the Array of Swap Instruments and their Quotes from the given set of parameters
		 */

		FixFloatComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"
			}
		);

		double[] adblSwapQuote = new double[] {
			0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409
		};

		String[] astrSwapManifestMeasure = new String[] {
			"SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate", "SwapRate"
		};

		/*
		 * Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver
		 */

		DiscountCurve dcKLKHyperbolicShapePreserver = ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			false
		);

		/*
		 * Construct the Cubic Tension KLK Hyperbolic Discount Factor Shape Preserver with Zero Rate
		 * 	Smoothening applied
		 */

		DiscountCurve dcKLKHyperbolicSmoother = ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SMOOTH_TEMPLATE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true
		);

		/*
		 * Construct the Cubic Polynomial Discount Factor Shape Preserver
		 */

		DiscountCurve dcCubicPolyShapePreserver = ScenarioDiscountCurveBuilder.CubicPolyDFRateShapePreserver (
			"CUBIC_POLY_SHAPE_TEMPLATE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			false
		);

		/*
		 * Construct the Cubic Polynomial Discount Factor Shape Preserver with Zero Rate Smoothening applied.
		 */

		DiscountCurve dcCubicPolySmoother = ScenarioDiscountCurveBuilder.CubicPolyDFRateShapePreserver (
			"CUBIC_POLY_SMOOTH_TEMPLATE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true
		);

		/*
		 * Construct the Discount Curve using the Bear Sterns' DENSE Methodology.
		 */

		DiscountCurve dcDENSE = ScenarioDiscountCurveBuilder.DENSE (
			"DENSE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			null
		);

		/*
		 * Construct the Discount Curve using the Bear Sterns' DUAL DENSE Methodology.
		 */

		DiscountCurve dcDualDENSE = ScenarioDiscountCurveBuilder.DUALDENSE (
			"DENSE",
			valParams,
			aDepositComp,
			adblDepositQuote,
			"1M",
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			"3M",
			astrSwapManifestMeasure,
			null
		);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\t\t\t\t\t\tDEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t   MATURITY  | KLK HYPER SHAPE | KLK HYPER SMOTH | CUBE POLY SHAPE | CUBE POLY SMOTH |      DENSE      |   DUAL  DENSE   |      INPUT");

		System.out.println ("\t---------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].maturity() + "] =    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcKLKHyperbolicShapePreserver, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcKLKHyperbolicSmoother, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcCubicPolyShapePreserver, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcCubicPolySmoother, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcDENSE, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (ComponentMetric (aDepositComp[i], valParams, dcDualDENSE, "Rate"), 1, 6, 1.) + "    |    " +
				FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

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
			System.out.println ("\t[" + aSwapComp[i].maturity() + "] =    " +
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

		double dblStartDate = aDepositComp[0].maturity().julian();

		double dblEndDate = aSwapComp[aSwapComp.length - 1].maturity().julian();

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
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		String strCurrency = "EUR";

		TemplatedDiscountCurveBuilderSample (dtToday, strCurrency);
	}
}
