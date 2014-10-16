
package org.drip.sample.bond;

/*
 * Credit Product imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.definition.*;
import org.drip.param.market.*;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.product.rates.GenericFixFloatComponent;
import org.drip.product.rates.GenericStream;
import org.drip.analytics.output.BondRVMeasures;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.product.credit.BondComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;
import org.drip.state.identifier.ForwardLabel;

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
 * BondRVMeasuresAPI is a Simple Bond RV Measures API Sample demonstrating the invocation and usage of Bond
 * 	RV Measures functionality. It shows the following:
 * 	- Create the discount/treasury curve from rates/treasury instruments.
 * 	- Compute the work-out date given the price.
 * 	- Compute and display the base RV measures to the work-out date.
 * 	- Compute and display the bumped RV measures to the work-out date.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BondRVMeasuresAPI {

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
				"USD",						// Fictitious Treasury Curve Name
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
	 * Put together a named map of treasury quotes
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CaseInsensitiveTreeMap<ProductQuote> MakeTSYQuotes (
		final String[] astrTSYTenor,
		final double[] adblTSYYield)
		throws Exception
	{
		CaseInsensitiveTreeMap<ProductQuote> mTSYQuotes = new CaseInsensitiveTreeMap<ProductQuote>();

		for (int i = 0; i < astrTSYTenor.length; ++i) {
			ProductMultiMeasureQuote cmmq = new ProductMultiMeasureQuote();

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
		final String strPrefix,
		final BondRVMeasures rv)
	{
		if (null == rv) return false;

		System.out.println (strPrefix + "ASW: " + FormatUtil.FormatDouble (rv._dblAssetSwapSpread, 0, 0, 10000.));

		System.out.println (strPrefix + "Bond Basis: " + FormatUtil.FormatDouble (rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Convexity: " + FormatUtil.FormatDouble (rv._dblConvexity, 0, 2, 1000000.));

		System.out.println (strPrefix + "Credit Basis: " + FormatUtil.FormatDouble (rv._dblCreditBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Discount Margin: " + FormatUtil.FormatDouble (rv._dblDiscountMargin, 0, 0, 10000.));

		System.out.println (strPrefix + "G Spread: " + FormatUtil.FormatDouble (rv._dblGSpread, 0, 0, 10000.));

		System.out.println (strPrefix + "I Spread: " + FormatUtil.FormatDouble (rv._dblISpread, 0, 0, 10000.));

		System.out.println (strPrefix + "Macaulay Duration: " + FormatUtil.FormatDouble (rv._dblMacaulayDuration, 0, 2, 1.));

		System.out.println (strPrefix + "Modified Duration: " + FormatUtil.FormatDouble (rv._dblModifiedDuration, 0, 2, 10000.));

		System.out.println (strPrefix + "OAS: " + FormatUtil.FormatDouble (rv._dblOASpread, 0, 0, 10000.));

		System.out.println (strPrefix + "PECS: " + FormatUtil.FormatDouble (rv._dblPECS, 0, 0, 10000.));

		System.out.println (strPrefix + "Price: " + FormatUtil.FormatDouble (rv._dblPrice, 0, 3, 100.));

		System.out.println (strPrefix + "TSY Spread: " + FormatUtil.FormatDouble (rv._dblTSYSpread, 0, 0, 10000.));

		try {
			System.out.println (strPrefix + "Workout Date: " + new JulianDate (rv._wi.date()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println (strPrefix + "Workout Factor: " + rv._wi.factor());

		System.out.println (strPrefix + "Workout Type: " + rv._wi.type());

		System.out.println (strPrefix + "Workout Yield: " + FormatUtil.FormatDouble (rv._wi.yield(), 0, 3, 100.));

		System.out.println (strPrefix + "Yield01: " + FormatUtil.FormatDouble (rv._dblYield01, 0, 2, 10000.));

		System.out.println (strPrefix + "Yield Basis: " + FormatUtil.FormatDouble (rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Yield Spread: " + FormatUtil.FormatDouble (rv._dblBondBasis, 0, 0, 10000.));

		System.out.println (strPrefix + "Z Spread: " + FormatUtil.FormatDouble (rv._dblZSpread, 0, 0, 10000.));

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

		/*
		 * Create the discount curve from rates instruments.
		 */

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
		 * Create the on-the-run treasury discount curve.
		 */

		DiscountCurve dcTSY = BuildOnTheRunTSYDiscountCurve (dtCurve, aTSYBond, adblTSYYield);

		BondComponent bond = BondBuilder.CreateSimpleFixed (	// Simple Fixed Rate Bond
				"TEST",			// Name
				"USD",			// Currency
				"",				// Credit Curve - Empty for now
				0.0875,			// Bond Coupon
				2, 				// Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2010, 3, 17), // Effective
				JulianDate.CreateFromYMD (2015, 4, 1),	// Maturity
				null,		// Principal Schedule
				null);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, dcTSY, null, null, null,
			MakeTSYQuotes (astrTSYTenor, adblTSYYield), null);

		ValuationParams valParams = ValuationParams.CreateValParams (dtSettle, 0, "", Convention.DR_ACTUAL);

		double dblPrice = 1.1025;

		/*
		 * Compute the work-out date given the price.
		 */

		WorkoutInfo wi = bond.calcExerciseYieldFromPrice (valParams, mktParams, null, dblPrice);

		/*
		 * Compute the base RV measures to the work-out date.
		 */

		org.drip.analytics.output.BondRVMeasures rvm = bond.standardMeasures (valParams, null, mktParams, null, wi, dblPrice);

		PrintRVMeasures ("\tBase: ", rvm);

		DiscountCurve dcBumped = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor, adblIRSRate, 0.0001, "USD");

		mktParams.setFundingCurve (dcBumped);

		/*
		 * Compute the bumped RV measures to the work-out date.
		 */

		org.drip.analytics.output.BondRVMeasures rvmBumped = bond.standardMeasures (valParams, null, mktParams, null, wi, dblPrice);

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
