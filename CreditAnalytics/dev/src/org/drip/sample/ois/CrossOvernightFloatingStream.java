
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.product.stream.*;
import org.drip.quant.common.*;
import org.drip.quant.function1D.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;
import org.drip.state.identifier.*;

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
 * CrossOvernightFloatingStream demonstrates the construction, customization, and valuation of Cross-Currency
 * 	Overnight Floating Streams.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossOvernightFloatingStream {

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
				strCurrency);

		CalibratableFixedIncomeComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, strCurrency);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of OIS Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OISInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenor (astrTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GenerateDailyPeriod (
				dtEffective.julian(),
				dtMaturity.julian(),
				null,
				null,
				"Act/360",
				strCurrency,
				strCurrency
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				adblCoupon[i],
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	private static final DiscountCurve CustomOISCurveBuilderSample (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the Array of Cash Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			4,
			strCurrency);

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
			0.01612, 0.01580, 0.01589, 0.01598}; // Futures

		/*
		 * Construct the Cash Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsCash = StretchRepresentationSpec.CreateStretchBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote,
			null);

		/*
		 * Construct the Array of OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblOISQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488     // 10Y
		};

		CalibratableFixedIncomeComponent[] aOISComp = OISInstrumentsFromMaturityTenor (
			dtSpot,
			new java.lang.String[]
				{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"},
			adblOISQuote,
			strCurrency);

		/*
		 * Construct the OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aOISComp,
			"Rate",
			adblOISQuote,
			null);

		StretchRepresentationSpec[] aRBS = new StretchRepresentationSpec[] {srsCash, srsOIS};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Cash and Swap Stretches.
		 */

		return ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aRBS,
			new ValuationParams (dtSpot, dtSpot, strCurrency),
			null,
			null,
			null,
			1.);
	}

	private static final Map<JulianDate, CaseInsensitiveTreeMap<Double>> SetFlatOvernightFixings (
		final JulianDate dtStart,
		final JulianDate dtEnd,
		final JulianDate dtValue,
		final ForwardLabel fri,
		final double dblFlatFixing,
		final double dblNotional)
		throws Exception
	{
		Map<JulianDate, CaseInsensitiveTreeMap<Double>> mapFixings = new HashMap<JulianDate, CaseInsensitiveTreeMap<Double>>();

		double dblAccount = 1.;

		double dblPrevDate = dtStart.julian();

		JulianDate dt = dtStart.addDays (1);

		while (dt.julian() <= dtEnd.julian()) {
			CaseInsensitiveTreeMap<Double> mapFixing = new CaseInsensitiveTreeMap<Double>();

			mapFixing.put (fri.fullyQualifiedName(), dblFlatFixing);

			mapFixings.put (dt, mapFixing);

			if (dt.julian() <= dtValue.julian()) {
				double dblAccrualFraction = Convention.YearFraction (dblPrevDate, dt.julian(), "Act/360", false, Double.NaN, null, "USD");

				dblAccount *= (1. + dblFlatFixing * dblAccrualFraction);
			}

			dblPrevDate = dt.julian();

			dt = dt.addBusDays (1, "USD");
		}

		System.out.println ("\tManual Calc Float Accrued (Geometric Compounding): " + (dblAccount - 1.) * dblNotional);

		System.out.println ("\tManual Calc Float Accrued (Arithmetic Compounding): " +
			((dtValue.julian() - dtStart.julian()) * dblNotional * dblFlatFixing / 360.));

		return mapFixings;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{

		double dblOISVol = 0.3;
		double dblUSDFundingVol = 0.3;
		double dblUSDFundingUSDOISCorrelation = 0.3;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		String strCurrency = "USD";

		DiscountCurve dc = CustomOISCurveBuilderSample (
			dtToday,
			strCurrency);

		JulianDate dtCustomOISStart = dtToday.subtractTenor ("2M");

		JulianDate dtCustomOISMaturity = dtToday.addTenor ("4M");

		ForwardLabel fri = OvernightFRIBuilder.JurisdictionFRI (strCurrency);

		List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtCustomOISStart.julian(),
			"6M",
			null,
			4,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream floatStream = new FloatingStream (
			strCurrency,
			null,
			0.,
			-1.,
			null,
			lsFloatPeriods,
			OvernightFRIBuilder.JurisdictionFRI (strCurrency),
			false
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (
			dc,
			null,
			null,
			null,
			null,
			null,
			SetFlatOvernightFixings (
				dtCustomOISStart,
				dtCustomOISMaturity,
				dtToday,
				fri,
				0.003,
				-1.)
			);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

		mktParams.setFundingCurveVolSurface (fundingLabelUSD, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri, new FlatUnivariate (dblOISVol));

		mktParams.setForwardFundingCorrSurface (fri, fundingLabelUSD, new FlatUnivariate (dblUSDFundingUSDOISCorrelation));

		Map<String, Double> mapGeometricOutput = floatStream.value (
			valParams,
			null,
			mktParams,
			null);

		fri.setArithmeticCompounding (true);

		Map<String, Double> mapArithmeticOutput = floatStream.value (
			valParams,
			null,
			mktParams,
			null);

		System.out.println ("\n\t-----------------------------------");

		System.out.println ("\t  GEOMETRIC |  ARITHMETIC | CHECK");

		System.out.println ("\t-----------------------------------\n");

		for (Map.Entry<String, Double> meGeometric : mapGeometricOutput.entrySet()) {
			String strKey = meGeometric.getKey();

			double dblGeometricMeasure = meGeometric.getValue();

			double dblArithmeticMeasure = mapArithmeticOutput.get (strKey);

			String strMatch = NumberUtil.WithinTolerance (dblGeometricMeasure, dblArithmeticMeasure, 1.e-08, 1.e-04) ?
				"MATCH " :
				"DIFFER";

			System.out.println ("\t" +
				FormatUtil.FormatDouble (dblGeometricMeasure, 1, 8, 1.) + " | " +
				FormatUtil.FormatDouble (dblArithmeticMeasure, 1, 8, 1.) + " | " +
				strMatch + " <= " + strKey
			);
		}
	}
}
