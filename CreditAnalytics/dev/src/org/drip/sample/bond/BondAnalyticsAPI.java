
package org.drip.sample.bond;

/*
 * Credit Product imports
 */

import org.drip.analytics.cashflow.*;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.*;
import org.drip.param.definition.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.*;
import org.drip.product.params.*;
import org.drip.product.rates.GenericFixFloatComponent;
import org.drip.product.rates.GenericStream;
import org.drip.product.definition.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * BondAnalyticsAPI contains a demo of the bond analytics API Sample. It generates the value and the RV
 * 	measures for essentially the same bond (with identical cash flows) constructed in 3 different ways:
 * 	- As a fixed rate bond.
 * 	- As a floater.
 * 	- As a bond constructed from a set of custom coupon and principal flows.
 * 
 * It shows these measures reconcile where they should.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BondAnalyticsAPI {
	private static final String FIELD_SEPARATOR = "    ";

	private static final GenericFixFloatComponent IRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final double dblCoupon)
		throws Exception
	{
		GenericStream fixStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				2,
				"Act/360",
				false,
				"Act/360",
				false,
				true,
				strCurrency,
				1.,
				null,
				dblCoupon,
				strCurrency,
				strCurrency,
				null,
				null
			)
		);

		GenericStream floatStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				4,
				"Act/360",
				false,
				"Act/360",
				false,
				true,
				strCurrency,
				-1.,
				null,
				0.,
				strCurrency,
				strCurrency,
				ForwardLabel.Create (strCurrency, "LIBOR", "3M"),
				null
			)
		);

		GenericFixFloatComponent irs = new GenericFixFloatComponent (
			fixStream,
			floatStream,
			null
		);

		irs.setPrimaryCode ("IRS." + strTenor + "." + strCurrency);

		return irs;
	}

	/*
	 * Sample demonstrating building of rates curve from cash/future/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static DiscountCurve BuildRatesCurveFromInstruments (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableFixedIncomeComponent aCompCalib[] = new CalibratableFixedIncomeComponent[iNumDCInstruments];

		// Cash Calibration

		JulianDate dtCashEffective = dtStart.addBusDays (1, strCurrency);

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = DepositBuilder.CreateDeposit2 (dtCashEffective,
				new JulianDate (adblDate[i] = dtCashEffective.addTenor (astrCashTenor[i]).julian()),
				null,
				strCurrency
			);
		}

		// IRS Calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, strCurrency);

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			adblDate[i + astrCashTenor.length] = dtIRSEffective.addTenor (astrIRSTenor[i]).julian();

			aCompCalib[i + astrCashTenor.length] = IRS (
				dtIRSEffective,
				strCurrency,
				astrIRSTenor[i],
				0.
			);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return ScenarioDiscountCurveBuilder.NonlinearBuild (
			dtStart,
			strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib,
			adblCompCalibValue,
			astrCalibMeasure,
			null
		);
	}

	/*
	 * Sample demonstrating creation of discount curve
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DiscountCurve MakeDiscountCurve (
		final JulianDate dtCurve)
		throws Exception
	{
		String[] astrCashTenor = new String[] {};
		double[] adblCashRate = new double[] {};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
			   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};

		return BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor,
			adblIRSRate, 0., "USD");
	}

	/*
	 * Sample demonstrating creation of the principal factor schedule from date and factor array
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FactorSchedule MakeFSPrincipal()
		throws Exception
	{
		double[] adblDate = new double[5];
		double[] adblFactor = new double[] {1., 1.0, 1.0, 1.0, 1.0};
		// double[] adblFactor = new double[] {1., 0.9, 0.8, 0.7, 0.6};

		JulianDate dtEOSStart = JulianDate.Today().addDays (2);

		for (int i = 0; i < 5; ++i)
			adblDate[i] = dtEOSStart.addYears (i + 2).julian();

		return FactorSchedule.FromDateFactorArray (adblDate, adblFactor);
	}

	/*
	 * Sample demonstrating creation of the coupon factor schedule from date and factor array
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FactorSchedule MakeFSCoupon()
		throws Exception
	{
		double[] adblDate = new double[5];
		double[] adblFactor = new double[] {1., 1.0, 1.0, 1.0, 1.0};
		// double[] adblFactor = new double[] {1., 0.9, 0.8, 0.7, 0.6};

		JulianDate dtEOSStart = JulianDate.Today().addDays (2);

		for (int i = 0; i < 5; ++i)
			adblDate[i] = dtEOSStart.addYears (i + 2).julian();

		return FactorSchedule.FromDateFactorArray (adblDate, adblFactor);
	}

	/*
	 * Sample creates a custom named bond from the bond type and parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Bond CreateCustomBond (
		final String strName,
		final String strCreditCurve,
		final int iBondType)
		throws Exception
	{
		BondProduct bond = null;
		boolean bEOSOn = false;
		boolean bEOSAmerican = false;

		if (BondBuilder.BOND_TYPE_SIMPLE_FLOATER == iBondType)
			bond = BondBuilder.CreateSimpleFloater ( // Simple Floating Rate Bond
				strName,		// Name
				"USD",			// Currency
				"USD-LIBOR-6M", // Rate Index
				strCreditCurve, // Credit Curve
				0.01,			// Floating Spread
				2,				// Coupon Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2008, 9, 21), // Effective
				JulianDate.CreateFromYMD (2023, 9, 20),	// Maturity
				MakeFSPrincipal(),		// Principal Schedule
				MakeFSCoupon());		// Coupon Schedule
		else if (BondBuilder.BOND_TYPE_SIMPLE_FIXED == iBondType)
			bond = BondBuilder.CreateSimpleFixed (	// Simple Fixed Rate Bond
				strName,		// Name
				"USD",			// Currency
				strCreditCurve, // Credit Curve
				0.05,			// Bond Coupon
				2,				// Coupon Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2008, 9, 21), // Effective
				JulianDate.CreateFromYMD (2023, 9, 20),	// Maturity
				MakeFSPrincipal(),		// Principal Schedule
				MakeFSCoupon());		// Coupon Schedule
		else if (BondBuilder.BOND_TYPE_SIMPLE_FROM_CF == iBondType) {	// Bond from custom coupon and principal flows
			final int NUM_CF_ENTRIES = 30;
			double[] adblCouponAmount = new double[NUM_CF_ENTRIES];
			double[] adblPrincipalAmount = new double[NUM_CF_ENTRIES];
			JulianDate[] adt = new JulianDate[NUM_CF_ENTRIES];

			JulianDate dtEffective = JulianDate.CreateFromYMD (2008, 9, 20);

			for (int i = 0; i < NUM_CF_ENTRIES; ++i) {
				adt[i] = dtEffective.addMonths (6 * (i + 1));

				adblCouponAmount[i] = 0.025;
				adblPrincipalAmount[i] = 1.0;
			}

			bond = BondBuilder.CreateBondFromCF (
				strName,				// Name
				dtEffective,			// Effective
				"USD",					// Currency
				strCreditCurve, 		// Credit Curve
				"30/360",				// Day Count
				2,						// Frequency
				adt,					// Array of dates
				adblCouponAmount,		// Array of coupon amount
				adblPrincipalAmount,	// Array of principal amount
				false);					// Principal is an outstanding notional
		}

		/*
		 * Bonds with options embedded
		 */

		if (bEOSOn) {
			double[] adblDate = new double[5];
			double[] adblPutFactor = new double[5];
			double[] adblCallFactor = new double[5];
			EmbeddedOptionSchedule eosPut = null;
			EmbeddedOptionSchedule eosCall = null;

			JulianDate dtEOSStart = JulianDate.Today().addDays (2);

			for (int i = 0; i < 5; ++i) {
				adblPutFactor[i] = 0.9;
				adblCallFactor[i] = 1.0;

				adblDate[i] = dtEOSStart.addYears (i + 2).julian();
			}

			if (bEOSAmerican) {		// Creation of the American call and put schedule
				eosCall = EmbeddedOptionSchedule.fromAmerican (JulianDate.Today().julian() + 1, adblDate,
					adblCallFactor, false, 30, false, Double.NaN, "", Double.NaN);

				eosPut = EmbeddedOptionSchedule.fromAmerican (JulianDate.Today().julian(), adblDate,
					adblPutFactor, true, 30, false, Double.NaN, "", Double.NaN);
			} else {		// Creation of the European call and put schedule
				eosCall = new EmbeddedOptionSchedule (adblDate, adblCallFactor, false, 30, false, Double.NaN, "", Double.NaN);

				eosPut = new EmbeddedOptionSchedule (adblDate, adblPutFactor, true, 30, false, Double.NaN, "", Double.NaN);
			}

			bond.setEmbeddedCallSchedule (eosCall);

			bond.setEmbeddedPutSchedule (eosPut);
		}

		return (Bond) bond;
	}

	/*
	 * Sample demonstrating the creation/usage of the custom bond API
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CustomBondAPISample()
		throws Exception
	{
		Bond[] aBond = new Bond[3];
		String strCreditCurve = "CC";

		/*
		 * Creates a simple fixed coupon bond and adds it to the FI cache as a named object
		 */

		if (null == (aBond[0] = CreditAnalytics.GetBond ("CustomFixed")))
			CreditAnalytics.PutBond ("CustomFixed", aBond[0] = CreateCustomBond (
				"CustomFixed",
				strCreditCurve,
				BondBuilder.BOND_TYPE_SIMPLE_FIXED)
			);

		/*
		 * Creates a simple floater and adds it to the FI cache as a named object
		 */

		if (null == (aBond[1] = CreditAnalytics.GetBond ("CustomFRN")))
			CreditAnalytics.PutBond ("CustomFRN", aBond[1] = CreateCustomBond (
				"CustomFRN",
				strCreditCurve,
				BondBuilder.BOND_TYPE_SIMPLE_FLOATER)
			);

		/*
		 * Creates a custom bond from arbitrary cash flows and adds it to the FI cache as a named object
		 */

		if (null == (aBond[2] = CreditAnalytics.GetBond ("CustomBondFromCF")))
			CreditAnalytics.PutBond ("CustomBondFromCF", aBond[2] = CreateCustomBond (
				"CustomBondFromCF",
				strCreditCurve,
				BondBuilder.BOND_TYPE_SIMPLE_FROM_CF)
			);

		/*
		 * Base Discount Curve
		 */

		DiscountCurve dc = MakeDiscountCurve (JulianDate.Today());

		/*
		 * Treasury Discount Curve
		 */

		DiscountCurve dcTSY = DiscountCurveBuilder.CreateFromFlatRate (JulianDate.Today(), "USD", null, 0.03);

		/*
		 * Credit Curve
		 */

		CreditCurve cc = CreditCurveBuilder.FromFlatHazard (JulianDate.Today().julian(), strCreditCurve, "USD", 0.01, 0.4);

		for (int i = 0; i < aBond.length; ++i) {
			System.out.println ("\nAcc Start     Acc End     Pay Date      Cpn DCF       Pay01       Surv01");

			System.out.println ("---------    ---------    ---------    ---------    ---------    --------");

			/*
			 * Generates and displays the coupon period details for the bonds
			 */

			for (GenericCouponPeriod p : aBond[i].cashFlowPeriod())
				System.out.println (
					JulianDate.fromJulian (p.accrualStartDate()) + FIELD_SEPARATOR +
					JulianDate.fromJulian (p.accrualEndDate()) + FIELD_SEPARATOR +
					JulianDate.fromJulian (p.payDate()) + FIELD_SEPARATOR +
					FormatUtil.FormatDouble (p.couponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
					FormatUtil.FormatDouble (dc.df (p.payDate()), 1, 4, 1.) + FIELD_SEPARATOR +
					FormatUtil.FormatDouble (cc.survival (p.payDate()), 1, 4, 1.)
				);

			/*
			 * Create the bond's component market parameters from the market inputs
			 */

			CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (
				dc,		// Discount curve
				dcTSY,	// TSY Discount Curve (Includes Optional EDSF if available, or BILLS etc)
				cc,		// Credit Curve
				null,	// TSY quotes
				null,	// BOND ID
				null,	// Bond market quote
				AnalyticsHelper.CreateFixingsObject (aBond[i], JulianDate.Today(), 0.04)	// Fixings
			);

			/*
			 * Construct Valuation Parameters
			 */

			ValuationParams valParams = ValuationParams.CreateValParams (JulianDate.Today(), 0, "", Convention.DR_ACTUAL);

			ProductQuote cquote = ProductQuoteBuilder.CreateProductQuote();

			Quote q = QuoteBuilder.CreateQuote ("mid", 0.05, Double.NaN);

			cquote.addQuote ("Yield", q, true);

			for (Bond bond : aBond)
				mktParams.setProductQuote (bond.name(), cquote);

			aBond[i].value (valParams, null, mktParams, null);

			System.out.println ("\n" + aBond[i].name() + " Valuation OP: " + aBond[i].value (valParams, null, mktParams, null));

			System.out.println ("\nPrice From Yield: " + FormatUtil.FormatDouble (aBond[i].calcPriceFromYield
				(valParams, mktParams, null, 0.03), 1, 3, 100.));

			double dblPrice = aBond[i].calcPriceFromYield (valParams, mktParams, null, 0.03);

			WorkoutInfo wi = aBond[i].calcExerciseYieldFromPrice (valParams, mktParams, null, dblPrice);

			System.out.println ("Workout Date: " + JulianDate.fromJulian (wi.date()));

			System.out.println ("Workout Factor: " + wi.factor());

			System.out.println ("Workout Yield: " + FormatUtil.FormatDouble (wi.yield(), 1, 2, 100.));

			System.out.println ("Workout Yield From Price: " + FormatUtil.FormatDouble
				(aBond[i].calcYieldFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 2, 100.));

			if (!aBond[i].isFloater()) {
				System.out.println ("Z Spread From Price: " + FormatUtil.FormatDouble
					(aBond[i].calcZSpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

				/* System.out.println ("OAS From Price: " + FormatUtil.FormatDouble
					(aBond[i].calcOASFromPrice (valParams, mktParams, null, wi._dblDate, wi._dblExerciseFactor, 1.), 1, 0, 10000.)); */
			}

			System.out.println ("I Spread From Price: " + FormatUtil.FormatDouble (aBond[i].calcISpreadFromPrice
				(valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

			System.out.println ("Discount Margin From Price: " + FormatUtil.FormatDouble (aBond[i].calcDiscountMarginFromPrice
				(valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

			System.out.println ("TSY Spread From Price: " + FormatUtil.FormatDouble
				(aBond[i].calcTSYSpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

			System.out.println ("ASW From Price: " + FormatUtil.FormatDouble
				(aBond[i].calcASWFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

			System.out.println ("Credit Basis From Price: " + FormatUtil.FormatDouble
				(aBond[i].calcCreditBasisFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), 1.), 1, 0, 10000.));

			System.out.println ("Price From TSY Spread: " + FormatUtil.FormatDouble
				(aBond[i].calcPriceFromTSYSpread (valParams, mktParams, null, 0.0188), 1, 3, 100.));

			System.out.println ("Yield From TSY Spread: " + FormatUtil.FormatDouble
				(aBond[i].calcYieldFromTSYSpread (valParams, mktParams, null, 0.0188), 1, 2, 100.));

			System.out.println ("ASW From TSY Spread: " + FormatUtil.FormatDouble
				(aBond[i].calcASWFromTSYSpread (valParams, mktParams, null, 0.0188), 1, 0, 10000.));

			System.out.println ("Credit Basis From TSY Spread: " + FormatUtil.FormatDouble
				(aBond[i].calcCreditBasisFromTSYSpread (valParams, mktParams, null, 0.0188), 1, 0, 10000.));

			/* System.out.println ("PECS From TSY Spread: " + FIGen.FormatSpread
				(aBond[i].calcPECSFromTSYSpread (valParams, mktParams, null, 0.0188))); */

			System.out.println ("Theoretical Price: " + FormatUtil.FormatDouble
				(aBond[i].calcPriceFromCreditBasis (valParams, mktParams, null, wi.date(), wi.factor(), 0.), 1, 3, 100.));
		}
	}

	/*
	 * API demonstrating how to calibrate a CDS curve from CDS and bond quotes
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static void BondCDSCurveCalibration()
		throws Exception
	{
		/*
		 * Bond calibration instrument
		 */

		Bond bond = BondBuilder.CreateSimpleFixed ("CCCalibBond", "DKK", "CC", 0.05, 2, "30/360",
			JulianDate.CreateFromYMD (2008, 9, 21), JulianDate.CreateFromYMD (2023, 9, 20), null, null);

		/*
		 * Discount Curve
		 */

		DiscountCurve dc = DiscountCurveBuilder.CreateFromFlatRate (JulianDate.Today(), "DKK", null, 0.04);

		/*
		 * Credit Curve
		 */

		CreditCurve cc = CreditCurveBuilder.FromFlatHazard (JulianDate.Today().julian(), "CC", "USD", 0.01, 0.4);

		/*
		 * Component Market Parameters Container
		 */

		CurveSurfaceQuoteSet mktParams =  MarketParamsBuilder.Create (dc, null, null, cc, null, null, null, null);

		/*
		 * Valuation Parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (JulianDate.Today(), 0, "USD", Convention.DR_ACTUAL);

		/*
		 * Theoretical Price
		 */

		double dblTheoreticalPrice = bond.calcPriceFromCreditBasis (valParams, mktParams, null, bond.maturity().julian(), 1., 0.01);


		System.out.println ("Credit Price From DC and CC: " + dblTheoreticalPrice);

		/*
		 * CDS calibration instrument
		 */

		CreditDefaultSwap cds = CDSBuilder.CreateCDS (JulianDate.Today(), JulianDate.Today().addTenor ("5Y"),
			0.1, "DKK", 0.40, "CC", "DKK", true);

		/*
		 * Set up the calibration instruments
		 */

		CalibratableFixedIncomeComponent[] aCalibInst = new CalibratableFixedIncomeComponent[] {cds, bond};

		/*
		 * Set up the calibration measures
		 */

		String[] astrCalibMeasure = new String[] {"FairPremium", "FairPrice"};

		/*
		 * Set up the calibration quotes
		 */

		double[] adblQuotes = new double[] {100., dblTheoreticalPrice};

		/*
		 * Setup the curve scenario calibrator/generator and build the credit curve
		 */

		CreditCurve ccCalib = CreditScenarioCurveBuilder.CreateCreditCurve (
				"CC", 					// Name
				JulianDate.Today(), 	// Date
				aCalibInst,				// Calibration instruments
				dc,						// Discount Curve
				adblQuotes,				// Component Quotes
				astrCalibMeasure,		// Calibration Measures
				0.40,					// Recovery
				false);					// Calibration is not flat

		/*
		 * Calculate the survival probability, and recover the input quotes
		 */

		System.out.println ("Surv (2021, 1, 14): " + ccCalib.survival (JulianDate.CreateFromYMD (2021, 1, 14)));

		/*
		 * Calibrated Component Market Parameters Container
		 */

		CurveSurfaceQuoteSet mktParamsCalib = MarketParamsBuilder.Create (dc, null, null, ccCalib, null, null, null, null);

		/*
		 * Verify the CDS fair premium using the calibrated credit curve
		 */

		System.out.println (cds.primaryCode() + " => " + cds.measureValue (
			valParams,
			PricerParams.Standard(),
			mktParamsCalib,
			null,
			"FairPremium"));

		/*
		 * Verify the Bond fair price using the calibrated credit curve
		 */

		System.out.println (bond.primaryCode() + " => " + bond.calcPriceFromCreditBasis (
			valParams,
			mktParamsCalib,
			null,
			bond.maturity().julian(),
			1.,
			0.));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		CustomBondAPISample();

		BondCDSCurveCalibration();
	}
}
