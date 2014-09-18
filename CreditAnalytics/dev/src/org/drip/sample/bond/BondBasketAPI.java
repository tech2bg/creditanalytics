
package org.drip.sample.bond;

/*
 * Credit Product Imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.credit.*;
import org.drip.product.definition.*;
import org.drip.product.params.FactorSchedule;

/*
 * Credit Analytics API Imports
 */

import org.drip.product.rates.FixFloatComponent;
import org.drip.product.rates.Stream;
import org.drip.param.creator.*;
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
 * BondBasketAPI contains a demo of the bond basket API Sample. It shows the following:
 * 	- Build the IR Curve from the Rates' instruments.
 * 	- Build the Component Credit Curve from the CDS instruments.
 * 	- Create the basket market parameters and add the named discount curve and the credit curves to it.
 * 	- Create the bond basket from the component bonds and their weights.
 * 	- Construct the Valuation and the Pricing Parameters.
 * 	- Generate the bond basket measures from the valuation, the pricer, and the market parameters.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BondBasketAPI {

	private static final FixFloatComponent IRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final double dblCoupon)
		throws Exception
	{
		Stream fixStream = new Stream (
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

		Stream floatStream = new Stream (
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

		FixFloatComponent irs = new FixFloatComponent (
			fixStream,
			floatStream,
			null
		);

		irs.setPrimaryCode ("IRS." + strTenor + "." + strCurrency);

		return irs;
	}

	/*
	 * Sample demonstrating creation of a rates curve from instruments
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

			aCompCalib[i] = DepositBuilder.CreateDeposit (dtCashEffective,
				new JulianDate (adblDate[i] = dtCashEffective.addTenor (astrCashTenor[i]).julian()),
				null,
				strCurrency);
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
	 * Sample demonstrating creation of simple fixed coupon treasury bond
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Bond CreateTSYBond (
		final String strName,
		final double dblCoupon,
		final JulianDate dt,
		final String strTenor)
		throws Exception
	{
		return BondBuilder.CreateSimpleFixed (	// Simple Fixed Rate Bond
				strName,					// Name
				"USD",					// Fictitious Treasury Curve Name
                "",                         // Credit Curve - Empty for now
				dblCoupon,					// Bond Coupon
				2, 							// Frequency
				"Act/Act",					// Day Count
				dt, 						// Effective
				dt.addTenor (strTenor),		// Maturity
				null,						// Principal Schedule
				null);
	}

	/*
	 * Sample demonstrating creation of a set of the on-the-run treasury bonds
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Bond[] CreateOnTheRunTSYBondSet (
		final JulianDate dt,
		final String[] astrTenor,
		final double[] adblCoupon)
		throws Exception
	{
		Bond aTSYBond[] = new Bond[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aTSYBond[i] = CreateTSYBond ("TSY" + astrTenor[i] + "ON", adblCoupon[i], dt, astrTenor[i]);

		return aTSYBond;
	}

	/*
	 * Sample demonstrating building of the treasury discount curve based off the on-the run instruments and their yields
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DiscountCurve BuildOnTheRunTSYDiscountCurve (
		final JulianDate dt,
		final Bond[] aTSYBond,
		final double[] adblTSYYield)
		throws Exception
	{
		String astrCalibMeasure[] = new String[aTSYBond.length];

		for (int i = 0; i < aTSYBond.length; ++i)
			astrCalibMeasure[i] = "Yield";

		return ScenarioDiscountCurveBuilder.NonlinearBuild (dt,
			"USD", // Fake curve name to indicate it is a USD TSY curve, not the usual USD curve
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aTSYBond,
			adblTSYYield,
			astrCalibMeasure,
			null);
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

		return FactorSchedule.CreateFromDateFactorArray (adblDate, adblFactor);
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

		return FactorSchedule.CreateFromDateFactorArray (adblDate, adblFactor);
	}

	/*
	 * Sample demonstrating the creation/usage of the bond basket API
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void BasketBondAPISample()
		throws Exception
	{
		JulianDate dtCurve = JulianDate.CreateFromYMD (2013, 6, 27);

		JulianDate dtSettle = JulianDate.CreateFromYMD (2013, 7, 1);

		/*
		 * Build the IR Curve from the Rates' instruments
		 */

		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.00276};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
			   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};

		DiscountCurve dc = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor, adblIRSRate, 0., "USD");

		/*
		 * Construct the set of Treasury instruments (in the case on-the-run set)
		 */

		String[] astrTSYTenor = new String[] {   "1M",    "3M",    "6M",    "1Y",    "2Y",    "3Y",    "5Y",    "7Y",
				"10Y",  "30Y"};
		final double[] adblTSYCoupon = new double[] {0.0000, 0.0000, 0.0000, 0.0000, 0.00375, 0.00500, 0.0100, 0.01375,
			0.01375, 0.02875};

		Bond[] aTSYBond = CreateOnTheRunTSYBondSet (dtCurve, astrTSYTenor, adblTSYCoupon);

		/*
		 * Build the Treasury Curve from the Treasury instruments and their yields
		 */

		double[] adblTSYYield = new double[] {0.00018, 0.00058, 0.00104, 0.00160, 0.00397, 0.00696, 0.01421, 0.01955,
			0.02529, 0.03568};

		DiscountCurve dcTSY = BuildOnTheRunTSYDiscountCurve (dtCurve, aTSYBond, adblTSYYield);

		/*
		 * Construct the set of bonds and load them onto the basket
		 */

		BondComponent bond1 = BondBuilder.CreateSimpleFixed (
                "TEST1",                                               // Name
                "USD",                                  // Currency
                "",                                  	// Credit Curve - Empty for now
                0.09,                                      // Bond Coupon
                2,                                                            // Frequency
                "30/360",                             // Day Count
                JulianDate.CreateFromYMD (2011, 2, 23), // Effective
                JulianDate.CreateFromYMD (2021, 3, 1),               // Maturity
                null,                       // Principal Schedule
                null);

		BondComponent bond2 = BondBuilder.CreateSimpleFixed (    // Simple Fixed Rate Bond
                "TEST2",                                               // Name
                "USD",                                  // Currency
                "",                                  	// Credit Curve - Empty for now
                0.09,                                      // Bond Coupon
                2,                                                            // Frequency
                "30/360",                             // Day Count
                JulianDate.CreateFromYMD (2011, 2, 23), // Effective
                JulianDate.CreateFromYMD (2021, 3, 1),               // Maturity
                null,                       // Principal Schedule
                null);

		BondComponent bond3 = BondBuilder.CreateSimpleFixed (    // Simple Fixed Rate Bond
                "TEST3",                                               // Name
                "USD",                                  // Currency
                "",                                  	// Credit Curve - Empty for now
                0.09,                                      // Bond Coupon
                2,                                                            // Frequency
                "30/360",                             // Day Count
                JulianDate.CreateFromYMD (2011, 2, 23), // Effective
                JulianDate.CreateFromYMD (2021, 3, 1),               // Maturity
                null,                       // Principal Schedule
                null);

		BondComponent bond4 = BondBuilder.CreateSimpleFloater ( // Simple Floating Rate Bond
				"FLOATER1",		// Name
				"USD",			// Currency
				"USD-LIBOR-6M",	// Rate Index
                "",            	// Credit Curve - Empty for now
				0.01,			// Floating Spread
				2,				// Coupon Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2008, 9, 21), // Effective
				JulianDate.CreateFromYMD (2023, 9, 20),	// Maturity
				MakeFSPrincipal(),		// Principal Schedule
				MakeFSCoupon());		// Coupon Schedule

		BasketProduct bb = new BondBasket ("TurtlePower", new Bond[] {bond1, bond2, bond3, bond4},
			new double[] {0.1, 0.2, 0.3, 0.4}, JulianDate.Today(), 1.);

		/*
		 * Verify - Simple Bond Basket Serializer
		 */

		byte[] abBB = bb.serialize();

		System.out.println ("Before: " + new String (abBB));

		BasketProduct bbAfter = new BondBasket (abBB);

		System.out.println ("After: " + new String (bbAfter.serialize()));

		/*
		 * Create the basket market parameters and add the named discount curve and the treasury curves to it.
		 */

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dc);

		mktParams.setFundingCurve (dcTSY);

		/*
		 * Construct the Valuation and the Pricing Parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (dtSettle, 0, "USD", Convention.DR_ACTUAL);

		PricerParams pricerParams = new PricerParams (7, null, false, PricerParams.PERIOD_DISCRETIZATION_FULL_COUPON, false);

		/*
		 * Generate the bond basket measures from the valuation, the pricer, and the market parameters
		 */

		CaseInsensitiveTreeMap<Double> mapResult = bb.value (valParams, pricerParams, mktParams, null);

		System.out.println ("Clean Price:      " + FormatUtil.FormatDouble (mapResult.get ("CleanPrice"), 0, 2, 100.));

		System.out.println ("Fair Clean Price: " + FormatUtil.FormatDouble (mapResult.get ("FairCleanPrice"), 0, 2, 100.));

		System.out.println ("Fair Yield:       " + FormatUtil.FormatDouble (mapResult.get ("FairYield"), 0, 2, 100.));

		System.out.println ("Fair GSpread:     " + FormatUtil.FormatDouble (mapResult.get ("FairGSpread"), 0, 0, 10000.));

		System.out.println ("Fair ZSpread:     " + FormatUtil.FormatDouble (mapResult.get ("FairZSpread"), 0, 0, 10000.));

		System.out.println ("Fair ISpread:     " + FormatUtil.FormatDouble (mapResult.get ("FairISpread"), 0, 0, 10000.));

		System.out.println ("Fair Duration:    " + FormatUtil.FormatDouble (mapResult.get ("FairDuration"), 0, 2, 10000.));

		System.out.println ("Accrued:          " + FormatUtil.FormatDouble (mapResult.get ("Accrued"), 1, 2, 100.));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		BasketBondAPISample();
	}
}
