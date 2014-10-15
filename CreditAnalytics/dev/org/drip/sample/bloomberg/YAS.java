
package org.drip.sample.bloomberg;

/*
 * Credit Product imports
 */

import org.drip.analytics.cashflow.GenericCouponPeriod;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.definition.*;
import org.drip.param.market.MultiSidedQuote;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.product.params.EmbeddedOptionSchedule;

/*
 * Credit Analytics API imports
 */

import org.drip.product.rates.GenericFixFloatComponent;
import org.drip.product.rates.GenericStream;
import org.drip.param.creator.*;
import org.drip.param.market.*;
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
 * YAS contains the sample demonstrating the replication of Bloomberg's YAS functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class YAS {
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

			aCompCalib[i] = DepositBuilder.CreateDeposit (
				dtCashEffective,
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

		return ScenarioDiscountCurveBuilder.NonlinearBuild (dtStart, strCurrency,
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
				"USD",						// Fictitious Treasury Curve Name
				"",						 	// Empty Credit Curve
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
			"USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aTSYBond,
			adblTSYYield,
			astrCalibMeasure,
			null);
	}

	/*
	 * Sample demonstrating creation of treasury quotes map
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
	 * Sample demonstrating generation of all the YAS measures
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void BondPricerSample()
		throws Exception
	{
		JulianDate dtCurve = JulianDate.Today();

		JulianDate dtSettle = dtCurve.addBusDays (3, "USD");

		double dblNotional = 1000000.;
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
				"",				// Empty Credit Curve
				0.054,			// Bond Coupon
				2, 				// Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2011, 4, 21), // Effective
				JulianDate.CreateFromYMD (2021, 4, 15),	// Maturity
				null,		// Principal Schedule
				null);

		double[] adblDate = new double[] {
			JulianDate.CreateFromYMD (2016, 3, 1).julian(),
			JulianDate.CreateFromYMD (2017, 3, 1).julian(),
			JulianDate.CreateFromYMD (2018, 3, 1).julian(),
			JulianDate.CreateFromYMD (2019, 3, 1).julian(),
			JulianDate.CreateFromYMD (2020, 3, 1).julian()
		};

		double[] adblFactor = new double[] {1.045, 1.03, 1.015, 1., 1.};

		EmbeddedOptionSchedule eos = new EmbeddedOptionSchedule (adblDate, adblFactor, false, 30, false, Double.NaN, "", Double.NaN);

		bond.setEmbeddedCallSchedule (eos);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, dcTSY, null, null, null, 
			MakeTSYQuotes (astrTSYTenor, adblTSYYield), null);

		System.out.println ("\n---- Valuation Details ----");

		System.out.println ("Trade Date   : " + dtCurve);

		System.out.println ("Cash Settle  : " + dtSettle);

		System.out.println ("\n--------");

		ValuationParams valParams = ValuationParams.CreateValParams (dtSettle, 0, "", Convention.DR_ACTUAL);

		double dblPrice = 0.97828;

		double dblAccrued = bond.calcAccrued (valParams.valueDate(), mktParams);

		WorkoutInfo wi = bond.calcExerciseYieldFromPrice (valParams, mktParams, null, dblPrice);

		double dblTSYSpread = bond.calcTSYSpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblGSpread = bond.calcGSpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblISpread = bond.calcISpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblZSpread = bond.calcZSpreadFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblASW = bond.calcASWFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblOAS = bond.calcOASFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblModDur = bond.calcModifiedDurationFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblMacDur = bond.calcMacaulayDurationFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblYield01 = bond.calcYield01FromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		double dblConvexity = bond.calcConvexityFromPrice (valParams, mktParams, null, wi.date(), wi.factor(), dblPrice);

		System.out.println ("Price          : " + FormatUtil.FormatDouble (dblPrice, 1, 3, 100.));

		System.out.println ("Yield          : " + FormatUtil.FormatDouble (wi.yield(), 1, 3, 100.));

		System.out.println ("Workout Date   : " + new JulianDate (wi.date()));

		System.out.println ("Workout Factor : " + FormatUtil.FormatDouble (wi.factor(), 1, 2, 100.));

		System.out.println ("\n--SPREAD AND YIELD CALCULATIONS--\n");

		System.out.println ("TSY Spread : " + FormatUtil.FormatDouble (dblTSYSpread, 1, 0, 10000.));

		System.out.println ("G Spread   : " + FormatUtil.FormatDouble (dblGSpread, 1, 0, 10000.));

		System.out.println ("I Spread   : " + FormatUtil.FormatDouble (dblISpread, 1, 0, 10000.));

		System.out.println ("Z Spread   : " + FormatUtil.FormatDouble (dblZSpread, 1, 0, 10000.));

		System.out.println ("ASW        : " + FormatUtil.FormatDouble (dblASW, 1, 0, 10000.));

		System.out.println ("OAS        : " + FormatUtil.FormatDouble (dblOAS, 1, 0, 10000.));

		System.out.println ("\n--RISK--\n");

		System.out.println ("Modified Duration : " + FormatUtil.FormatDouble (dblModDur, 1, 2, 10000.));

		System.out.println ("Macaulay Duration : " + FormatUtil.FormatDouble (dblMacDur, 1, 2, 1.));

		System.out.println ("Risk              : " + FormatUtil.FormatDouble (dblYield01 * 10000., 1, 2, 1.));

		System.out.println ("Convexity         : " + FormatUtil.FormatDouble (dblConvexity, 1, 2, 1000000.));

		System.out.println ("DV01              : " + FormatUtil.FormatDouble (dblYield01 * dblNotional, 1, 0, 1.));

		System.out.println ("\n--INVOICE--\n");

		System.out.println ("Face      : " + FormatUtil.FormatDouble (dblNotional, 1, 0, 1.));

		System.out.println ("Principal : " + FormatUtil.FormatDouble (dblPrice * dblNotional, 1, 2, 1.));

		System.out.println ("Accrued   : " + FormatUtil.FormatDouble (dblAccrued * dblNotional, 1, 2, 1.));

		System.out.println ("Total     : " + FormatUtil.FormatDouble ((dblPrice + dblAccrued) * dblNotional, 1, 2, 1.));

		System.out.println ("\nCashflow\n--------");

		for (GenericCouponPeriod p : bond.cashFlowPeriod())
			System.out.println (
				JulianDate.fromJulian (p.accrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.accrualEndDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.payDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.couponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.payDate()), 1, 4, 1.) + FIELD_SEPARATOR
			);
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		BondPricerSample();
	}
}
