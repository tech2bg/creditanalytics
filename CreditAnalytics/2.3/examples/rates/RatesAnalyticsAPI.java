
package org.drip.sample.rates;

/*
 * Credit Product imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;

/*
 * Credit Analytics API Import
 */

import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * RatesAnalyticsAPI contains a demo of the Rates Analytics API Usage. It shows the following:
 * 	- Build a discount curve using: cash instruments only, EDF instruments only, IRS instruments only, or all
 * 		of them strung together.
 * 	- Re-calculate the component input measure quotes from the calibrated discount curve object.
 * 	- Compute the PVDF Wengert Jacobian across all the instruments used in the curve construction.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesAnalyticsAPI {

	/**
	 * Sample API demonstrating the creation/usage of discount curve
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static final void DiscountCurveAPISample()
		throws Exception
	{
		JulianDate dtStart = JulianDate.Today();

		double[] adblDF = new double[5];
		double[] adblDate = new double[5];
		double[] adblRate = new double[5];

		for (int i = 0; i < 5; ++i) {
			adblDate[i] = dtStart.addYears (2 * i + 2).getJulian();

			adblDF[i] = 1. - 2 * (i + 1) * 0.05;
			adblRate[i] = 0.05;
		}

		/*
		 * Build the discount curve from an array of dates and discount factors
		 */

		DiscountCurve dcFromDF = DiscountCurveBuilder.BuildFromDF (dtStart, "EUR", adblDate, adblDF,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		JulianDate dt = dtStart.addYears (10);

		System.out.println ("DCFromDF[" + dt.toString() + "]; DF=" + dcFromDF.df (dt) + "; Rate=" +
			dcFromDF.zero ("10Y"));

		/*
		 * Build the discount curve from an array of dates and forward rates
		 */

		DiscountCurve dcFromRate = DiscountCurveBuilder.CreateDC (dtStart, "EUR", adblDate, adblRate,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		System.out.println ("DCFromRate[" + dt.toString() + "]; DF=" + dcFromRate.df (dt) + "; Rate=" +
			dcFromRate.zero ("10Y"));

		/*
		 * Build the discount curve from a flat rate
		 */

		DiscountCurve dcFromFlatRate = DiscountCurveBuilder.CreateFromFlatRate (dtStart, "DKK", 0.04);

		System.out.println ("DCFromFlatRate[" + dt.toString() + "]; DF=" + dcFromFlatRate.df (dt) +
			"; Rate=" + dcFromFlatRate.zero ("10Y"));
	}

	/**
	 * Sample API demonstrating the creation of the discount curve from the rates input instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static void DiscountCurveFromRatesInstruments()
		throws Exception
	{
		int NUM_DC_INSTR = 30;
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblMaturity[] = new double[NUM_DC_INSTR];
		String astrCalibMeasure[] = new String[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[NUM_DC_INSTR];

		JulianDate dtStart = org.drip.analytics.date.JulianDate.CreateFromYMD (2011, 4, 6);

		// First 7 instruments - cash calibration

		JulianDate dtCashEffective = dtStart.addBusDays (1, "USD");

		adblMaturity[0] = dtCashEffective.addBusDays (1, "USD").getJulian(); // ON

		adblMaturity[1] = dtCashEffective.addBusDays (2, "USD").getJulian(); // 1D (TN)

		adblMaturity[2] = dtCashEffective.addBusDays (7, "USD").getJulian(); // 1W

		adblMaturity[3] = dtCashEffective.addBusDays (14, "USD").getJulian(); // 2W

		adblMaturity[4] = dtCashEffective.addBusDays (30, "USD").getJulian(); // 1M

		adblMaturity[5] = dtCashEffective.addBusDays (60, "USD").getJulian(); // 2M

		adblMaturity[6] = dtCashEffective.addBusDays (90, "USD").getJulian(); // 3M

		/*
		 * Cash Rate Quotes
		 */

		adblCompCalibValue[0] = .0013;
		adblCompCalibValue[1] = .0017;
		adblCompCalibValue[2] = .0017;
		adblCompCalibValue[3] = .0018;
		adblCompCalibValue[4] = .0020;
		adblCompCalibValue[5] = .0023;
		adblCompCalibValue[6] = .0026;

		for (int i = 0; i < 7; ++i) {
			adblRate[i] = 0.01;
			astrCalibMeasure[i] = "Rate";

			aCompCalib[i] = CashBuilder.CreateCash (dtCashEffective, // Effective
				new JulianDate (adblMaturity[i]).addBusDays (2, "USD"), // Maturity
				"USD");
		}

		// Next 8 instruments - EDF calibration

		adblCompCalibValue[7] = .0027;
		adblCompCalibValue[8] = .0032;
		adblCompCalibValue[9] = .0041;
		adblCompCalibValue[10] = .0054;
		adblCompCalibValue[11] = .0077;
		adblCompCalibValue[12] = .0104;
		adblCompCalibValue[13] = .0134;
		adblCompCalibValue[14] = .0160;

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtStart, 8, "USD");

		for (int i = 0; i < 8; ++i) {
			adblRate[i + 7] = 0.01;
			aCompCalib[i + 7] = aEDF[i];
			astrCalibMeasure[i + 7] = "Rate";

			adblMaturity[i + 7] = aEDF[i].getMaturityDate().getJulian();
		}

		// Final 15 instruments - IRS calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, "USD");

		adblMaturity[15] = dtIRSEffective.addTenor ("4Y").getJulian();

		adblMaturity[16] = dtIRSEffective.addTenor ("5Y").getJulian();

		adblMaturity[17] = dtIRSEffective.addTenor ("6Y").getJulian();

		adblMaturity[18] = dtIRSEffective.addTenor ("7Y").getJulian();

		adblMaturity[19] = dtIRSEffective.addTenor ("8Y").getJulian();

		adblMaturity[20] = dtIRSEffective.addTenor ("9Y").getJulian();

		adblMaturity[21] = dtIRSEffective.addTenor ("10Y").getJulian();

		adblMaturity[22] = dtIRSEffective.addTenor ("11Y").getJulian();

		adblMaturity[23] = dtIRSEffective.addTenor ("12Y").getJulian();

		adblMaturity[24] = dtIRSEffective.addTenor ("15Y").getJulian();

		adblMaturity[25] = dtIRSEffective.addTenor ("20Y").getJulian();

		adblMaturity[26] = dtIRSEffective.addTenor ("25Y").getJulian();

		adblMaturity[27] = dtIRSEffective.addTenor ("30Y").getJulian();

		adblMaturity[28] = dtIRSEffective.addTenor ("40Y").getJulian();

		adblMaturity[29] = dtIRSEffective.addTenor ("50Y").getJulian();

		adblCompCalibValue[15] = .0166;
		adblCompCalibValue[16] = .0206;
		adblCompCalibValue[17] = .0241;
		adblCompCalibValue[18] = .0269;
		adblCompCalibValue[19] = .0292;
		adblCompCalibValue[20] = .0311;
		adblCompCalibValue[21] = .0326;
		adblCompCalibValue[22] = .0340;
		adblCompCalibValue[23] = .0351;
		adblCompCalibValue[24] = .0375;
		adblCompCalibValue[25] = .0393;
		adblCompCalibValue[26] = .0402;
		adblCompCalibValue[27] = .0407;
		adblCompCalibValue[28] = .0409;
		adblCompCalibValue[29] = .0409;

		for (int i = 0; i < 15; ++i) {
			astrCalibMeasure[i + 15] = "Rate";
			adblRate[i + 15] = 0.01;

			aCompCalib[i + 15] = RatesStreamBuilder.CreateIRS (dtIRSEffective,
				new JulianDate (adblMaturity[i + 15]),
				0., "USD", "USD-LIBOR-6M", "USD");
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		DiscountCurve dc = RatesScenarioCurveBuilder.NonlinearBuild (dtStart, "USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib, adblCompCalibValue, astrCalibMeasure, null);

		/*
		 * Re-calculate the component input measure quotes from the calibrated discount curve object
		 */

		for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (astrCalibMeasure[i] + "[" + i + "] = " +
				FormatUtil.FormatDouble (aCompCalib[i].calcMeasureValue (new ValuationParams (dtStart, dtStart, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, astrCalibMeasure[i]), 1, 5, 1.) + " | " + FormatUtil.FormatDouble (adblCompCalibValue[i], 1, 5, 1.));

		/* for (int i = 0; i < 100; ++i) {
			org.drip.analytics.date.JulianDate dt = dtStart.addDays (90 * i);

			System.out.println ("DF[" + dt + "] = " + dc.df (dt));
		} */

		for (int i = 0; i < aCompCalib.length; ++i) {
			WengertJacobian wjComp = aCompCalib[i].calcPVDFMicroJack
				(new ValuationParams (dtStart, dtStart, "USD"),
				null,
				ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
				null);

			System.out.println ("PV/DF Micro Jack[" + aCompCalib[i].getComponentName() + "]=" +
				(null == wjComp ? null : wjComp.displayString()));
		}
	}

	/*
	 * Sample demonstrating creation of discount curve from cash instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static void DiscountCurveFromCash()
		throws Exception
	{
		int NUM_CASH_INSTR = 7;
		double adblRate[] = new double[NUM_CASH_INSTR];
		double adblMaturity[] = new double[NUM_CASH_INSTR];
		String astrCalibMeasure[] = new String[NUM_CASH_INSTR];
		double adblCompCalibValue[] = new double[NUM_CASH_INSTR];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[NUM_CASH_INSTR];

		JulianDate dtStart = JulianDate.CreateFromYMD (2011, 4, 6);

		// First 7 instruments - cash calibration

		JulianDate dtCashEffective = dtStart.addBusDays (1, "USD");

		adblMaturity[0] = dtCashEffective.addBusDays (1, "USD").getJulian(); // ON

		adblMaturity[1] = dtCashEffective.addBusDays (2, "USD").getJulian(); // 1D (TN)

		adblMaturity[2] = dtCashEffective.addBusDays (7, "USD").getJulian(); // 1W

		adblMaturity[3] = dtCashEffective.addBusDays (14, "USD").getJulian(); // 2W

		adblMaturity[4] = dtCashEffective.addBusDays (30, "USD").getJulian(); // 1M

		adblMaturity[5] = dtCashEffective.addBusDays (60, "USD").getJulian(); // 2M

		adblMaturity[6] = dtCashEffective.addBusDays (90, "USD").getJulian(); // 3M

		/*
		 * Cash Rate Quotes
		 */

		adblCompCalibValue[0] = .0013;
		adblCompCalibValue[1] = .0017;
		adblCompCalibValue[2] = .0017;
		adblCompCalibValue[3] = .0018;
		adblCompCalibValue[4] = .0020;
		adblCompCalibValue[5] = .0023;
		adblCompCalibValue[6] = .0026;

		for (int i = 0; i < NUM_CASH_INSTR; ++i) {
			adblRate[i] = 0.01;
			astrCalibMeasure[i] = "Rate";

			aCompCalib[i] = CashBuilder.CreateCash (dtCashEffective, new JulianDate (adblMaturity[i]), "USD");
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		DiscountCurve dc = RatesScenarioCurveBuilder.NonlinearBuild (dtStart, "USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib, adblCompCalibValue, astrCalibMeasure, null);

		/*
		 * Re-calculate the component input measure quotes from the calibrated discount curve object
		 */

		for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (astrCalibMeasure[i] + "[" + i + "] = " +
				FormatUtil.FormatDouble (aCompCalib[i].calcMeasureValue (new ValuationParams (dtStart, dtStart, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, astrCalibMeasure[i]), 1, 5, 1.) + " | " + FormatUtil.FormatDouble (adblCompCalibValue[i], 1, 5, 1.));

		org.drip.quant.calculus.WengertJacobian wjPVDF = dc.compPVDFJack (dtStart);

		System.out.println ("PV/DF Micro Jack[04/06/11]=" + (null == wjPVDF ? null : wjPVDF.displayString()));
	}

	/*
	 * Sample demonstrating creation of discount cure from EDF instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static void DiscountCurveFromEDF()
		throws Exception
	{
		int NUM_DC_INSTR = 8;
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblMaturity[] = new double[NUM_DC_INSTR];
		String astrCalibMeasure[] = new String[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[NUM_DC_INSTR];

		JulianDate dtStart = JulianDate.CreateFromYMD (2011, 4, 6);

		// Next 8 instruments - EDF calibration

		adblCompCalibValue[0] = .0027;
		adblCompCalibValue[1] = .0032;
		adblCompCalibValue[2] = .0041;
		adblCompCalibValue[3] = .0054;
		adblCompCalibValue[4] = .0077;
		adblCompCalibValue[5] = .0104;
		adblCompCalibValue[6] = .0134;
		adblCompCalibValue[7] = .0160;

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtStart, 8, "USD");

		for (int i = 0; i < NUM_DC_INSTR; ++i) {
			adblRate[i] = 0.01;
			aCompCalib[i] = aEDF[i];
			astrCalibMeasure[i] = "Rate";

			adblMaturity[i + 7] = aEDF[i].getMaturityDate().getJulian();
		}

		DiscountCurve dc = RatesScenarioCurveBuilder.NonlinearBuild (dtStart, "USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib, adblCompCalibValue, astrCalibMeasure, null);

		/*
		 * Re-calculate the component input measure quotes from the calibrated discount curve object
		 */

		for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (astrCalibMeasure[i] + "[" + i + "] = " +
				FormatUtil.FormatDouble (aCompCalib[i].calcMeasureValue (new ValuationParams (dtStart, dtStart, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, astrCalibMeasure[i]), 1, 5, 1.) + " | " + FormatUtil.FormatDouble (adblCompCalibValue[i], 1, 5, 1.));

		WengertJacobian wjPVDF = dc.compPVDFJack (dtStart);

		System.out.println ("PV/DF Micro Jack[04/06/11]=" + (null == wjPVDF ? null : wjPVDF.displayString()));
	}

	/*
	 * Sample demonstrating creation of discount cure from swap instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static void DiscountCurveFromIRS()
		throws Exception
	{
		int NUM_DC_INSTR = 15;
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblMaturity[] = new double[NUM_DC_INSTR];
		String astrCalibMeasure[] = new String[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[NUM_DC_INSTR];

		JulianDate dtStart = JulianDate.CreateFromYMD (2011, 4, 6);

		// Final 15 instruments - IRS calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, "USD");

		adblMaturity[0] = dtIRSEffective.addTenor ("4Y").getJulian();

		adblMaturity[1] = dtIRSEffective.addTenor ("5Y").getJulian();

		adblMaturity[2] = dtIRSEffective.addTenor ("6Y").getJulian();

		adblMaturity[3] = dtIRSEffective.addTenor ("7Y").getJulian();

		adblMaturity[4] = dtIRSEffective.addTenor ("8Y").getJulian();

		adblMaturity[5] = dtIRSEffective.addTenor ("9Y").getJulian();

		adblMaturity[6] = dtIRSEffective.addTenor ("10Y").getJulian();

		adblMaturity[7] = dtIRSEffective.addTenor ("11Y").getJulian();

		adblMaturity[8] = dtIRSEffective.addTenor ("12Y").getJulian();

		adblMaturity[9] = dtIRSEffective.addTenor ("15Y").getJulian();

		adblMaturity[10] = dtIRSEffective.addTenor ("20Y").getJulian();

		adblMaturity[11] = dtIRSEffective.addTenor ("25Y").getJulian();

		adblMaturity[12] = dtIRSEffective.addTenor ("30Y").getJulian();

		adblMaturity[13] = dtIRSEffective.addTenor ("40Y").getJulian();

		adblMaturity[14] = dtIRSEffective.addTenor ("50Y").getJulian();

		adblCompCalibValue[0] = .0166;
		adblCompCalibValue[1] = .0206;
		adblCompCalibValue[2] = .0241;
		adblCompCalibValue[3] = .0269;
		adblCompCalibValue[4] = .0292;
		adblCompCalibValue[5] = .0311;
		adblCompCalibValue[6] = .0326;
		adblCompCalibValue[7] = .0340;
		adblCompCalibValue[8] = .0351;
		adblCompCalibValue[9] = .0375;
		adblCompCalibValue[10] = .0393;
		adblCompCalibValue[11] = .0402;
		adblCompCalibValue[12] = .0407;
		adblCompCalibValue[13] = .0409;
		adblCompCalibValue[14] = .0409;

		for (int i = 0; i < NUM_DC_INSTR; ++i) {
			adblRate[i] = 0.01;
			astrCalibMeasure[i] = "Rate";

			aCompCalib[i] = RatesStreamBuilder.CreateIRS (dtIRSEffective, new JulianDate (adblMaturity[i]), 0., "USD", "USD-LIBOR-6M", "USD");
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		DiscountCurve dc = RatesScenarioCurveBuilder.NonlinearBuild (dtStart, "USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib, adblCompCalibValue, astrCalibMeasure, null);

		/*
		 * Re-calculate the component input measure quotes from the calibrated discount curve object
		 */

		for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (astrCalibMeasure[i] + "[" + i + "] = " +
				FormatUtil.FormatDouble (aCompCalib[i].calcMeasureValue (new ValuationParams (dtStart, dtStart, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, astrCalibMeasure[i]), 1, 5, 1.) + " | " + FormatUtil.FormatDouble (adblCompCalibValue[i], 1, 5, 1.));

		/* WengertJacobian wjQuoteDF = dc.compQuoteDFJacobian (dtStart);

		System.out.println ("Quote/DF Micro Jack[04/06/11]=" + (null == wjQuoteDF ? null :
			wjQuoteDF.displayString())); */

		WengertJacobian wjPVDF = dc.compPVDFJack (dtStart);

		System.out.println ("PV/Zero Micro Jack[04/06/11]=" + (null == wjPVDF ? null : wjPVDF.displayString()));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		DiscountCurveFromRatesInstruments();

		// DiscountCurveFromIRS();

		// DiscountCurveAPISample();
	}
}
