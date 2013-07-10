
package org.drip.service.sample;

/*
 * Credit Product imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.*;
import org.drip.param.definition.*;
import org.drip.param.market.MultiSidedQuote;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;

/*
 * Credit Analytics API imports
 */

import org.drip.analytics.creator.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.product.credit.BondComponent;
import org.drip.service.api.CreditAnalytics;

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

/**
 * Simple Bond RV Measures API Sample demonstrating the invocation and usage of Bond RV Measures
 * 	functionality
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BondRVMeasuresAPI {

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
		CalibratableComponent aCompCalib[] = new CalibratableComponent[iNumDCInstruments];
		String strIndex = strCurrency + "-LIBOR-3M";

		// Cash Calibration

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = CashBuilder.CreateCash (dtStart.addBusDays (2, strCurrency),
				new JulianDate (adblDate[i] = dtStart.addBusDays (2, strCurrency).addTenor (astrCashTenor[i]).getJulian()),
				strCurrency);
		}

		// IRS Calibration

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			aCompCalib[i + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtStart.addBusDays (2, strCurrency),
				new JulianDate (adblDate[i + astrCashTenor.length] = dtStart.addBusDays (2, strCurrency).addTenor (astrIRSTenor[i]).getJulian()),
				0., strCurrency, strIndex, strCurrency);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return RatesScenarioCurveBuilder.CreateDiscountCurve (dtStart, strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib, adblCompCalibValue, astrCalibMeasure, null);
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
				"USDTSY",					// Fictitious Treasury Curve Name
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

		return RatesScenarioCurveBuilder.CreateDiscountCurve (dt,
			"USDTSY", // Fake curve name to indicate it is a USD TSY curve, not the usual USD curve
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aTSYBond,
			adblTSYYield,
			astrCalibMeasure,
			null);
	}

	/*
	 * Put together a named map of treasury quotes
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final java.util.Map<java.lang.String, org.drip.param.definition.ComponentQuote> MakeTSYQuotes (
		final String[] astrTSYTenor,
		final double[] adblTSYYield)
		throws Exception
	{
		java.util.Map<java.lang.String, org.drip.param.definition.ComponentQuote> mTSYQuotes = new
			java.util.HashMap<java.lang.String, org.drip.param.definition.ComponentQuote>();

		for (int i = 0; i < astrTSYTenor.length; ++i) {
			org.drip.param.market.ComponentMultiMeasureQuote cmmq = new org.drip.param.market.ComponentMultiMeasureQuote();

			cmmq.addQuote ("Yield", new MultiSidedQuote ("mid", adblTSYYield[i], Double.NaN), true);

			mTSYQuotes.put (astrTSYTenor[i] + "ON", cmmq);
		}

		return mTSYQuotes;
	}

	/*
	 * Print the Bond RV Measures
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final boolean PrintRVMeasures (
		final java.lang.String strPrefix,
		final org.drip.analytics.output.BondRVMeasures rv)
	{
		if (null == rv) return false;

		System.out.println (strPrefix + "ASW: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblAssetSwapSpread, 0, 0, 10000.));

		System.out.println (strPrefix + "Bond Basis: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Convexity: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblConvexity, 0, 2, 1000000.));

		System.out.println (strPrefix + "Credit Basis: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblCreditBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Discount Margin: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblDiscountMargin, 0, 0, 10000.));

		System.out.println (strPrefix + "G Spread: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblGSpread, 0, 0, 10000.));

		System.out.println (strPrefix + "I Spread: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblISpread, 0, 0, 10000.));

		System.out.println (strPrefix + "Macaulay Duration: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblMacaulayDuration, 0, 2, 1.));

		System.out.println (strPrefix + "Modified Duration: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblModifiedDuration, 0, 2, 10000.));

		System.out.println (strPrefix + "OAS: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblOASpread, 0, 0, 10000.));

		System.out.println (strPrefix + "PECS: " + org.drip.math.common.FormatUtil.FormatDouble (rv._dblPECS,
			0, 0, 10000.));

		System.out.println (strPrefix + "Price: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblPrice, 0, 3, 100.));

		System.out.println (strPrefix + "TSY Spread: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblTSYSpread, 0, 0, 10000.));

		try {
			System.out.println (strPrefix + "Workout Date: " + new org.drip.analytics.date.JulianDate
				(rv._wi._dblDate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		System.out.println (strPrefix + "Workout Factor: " + rv._wi._dblExerciseFactor);

		System.out.println (strPrefix + "Workout Type: " + rv._wi._iWOType);

		System.out.println (strPrefix + "Workout Yield: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._wi._dblYield, 0, 3, 100.));

		System.out.println (strPrefix + "Yield01: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblYield01, 0, 2, 10000.));

		System.out.println (strPrefix + "Yield Basis: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Yield Spread: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Z Spread: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblZSpread, 0, 0, 10000.));

		return true;
	}

	/*
	 * Sample demonstrating invocation and extraction of RV Measures from a bond
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void BondRVMeasuresSample()
		throws Exception
	{
		JulianDate dtCurve = JulianDate.CreateFromYMD (2013, 6, 27);

		JulianDate dtSettle = JulianDate.CreateFromYMD (2013, 7, 1);

		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.00276};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
			   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};
		String[] astrTSYTenor = new String[] {   "1M",    "3M",    "6M",    "1Y",    "2Y",    "3Y",    "5Y",    "7Y",
				"10Y",  "30Y"};
		final double[] adblTSYCoupon = new double[] {0.0000, 0.0000, 0.0000, 0.0000, 0.00375, 0.00500, 0.0100, 0.01375,
			0.01375, 0.02875};
		double[] adblTSYYield = new double[] {0.00018, 0.00058, 0.00104, 0.00160, 0.00397, 0.00696, 0.01421, 0.01955,
			0.02529, 0.03568};

		DiscountCurve dc = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor, adblIRSRate, 0., "USD");

		Bond[] aTSYBond = CreateOnTheRunTSYBondSet (dtCurve, astrTSYTenor, adblTSYCoupon);

		/*
		 * Create the on-the-run treasury discount curve
		 */

		DiscountCurve dcTSY = BuildOnTheRunTSYDiscountCurve (dtCurve, aTSYBond, adblTSYYield);

		BondComponent bond = BondBuilder.CreateSimpleFixed (	// Simple Fixed Rate Bond
				"TEST",			// Name
				"USD",			// Currency
				0.0875,			// Bond Coupon
				2, 				// Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2010, 3, 17), // Effective
				JulianDate.CreateFromYMD (2015, 4, 1),	// Maturity
				null,		// Principal Schedule
				null);

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY, dcTSY, null, null,
			MakeTSYQuotes (astrTSYTenor, adblTSYYield), null);

		ValuationParams valParams = ValuationParams.CreateValParams (dtSettle, 0, "", Convention.DR_ACTUAL);

		double dblPrice = 1.1025;

		WorkoutInfo wi = bond.calcExerciseYieldFromPrice (valParams, cmp, null, dblPrice);

		org.drip.analytics.output.BondRVMeasures rvm = bond.standardMeasures (valParams, null, cmp, null, wi, dblPrice);

		PrintRVMeasures ("\tBase: ", rvm);

		DiscountCurve dcBumped = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor, adblIRSRate, 0.0001, "USD");

		cmp.setDiscountCurve (dcBumped);

		org.drip.analytics.output.BondRVMeasures rvmBumped = bond.standardMeasures (valParams, null, cmp, null, wi, dblPrice);

		PrintRVMeasures ("\tBumped: ", rvmBumped);
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		BondRVMeasuresSample();
	}
}
