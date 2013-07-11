
package org.drip.service.sample;

import java.util.Map;

import org.drip.analytics.creator.DiscountCurveBuilder;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.daycount.DateAdjustParams;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.analytics.period.Period;
import org.drip.math.common.FormatUtil;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.creator.RatesScenarioCurveBuilder;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.CashBuilder;
import org.drip.product.creator.EDFutureBuilder;
import org.drip.product.creator.RatesStreamBuilder;
import org.drip.product.definition.CalibratableComponent;
import org.drip.product.definition.RatesComponent;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

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
 * Demo of reproduction of the calculations in Bloomberg's SWPM screen
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BloombergSWPM {
	private static final String FIELD_SEPARATOR = "    ";

	private static DiscountCurve BuildBBGRatesCurve (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final double[] adblEDFRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblEDFRate.length + adblIRSRate.length;
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
			adblCompCalibValue[i] = adblCashRate[i];

			JulianDate dtEffective = dtStart.addBusDays (2, strCurrency);

			aCompCalib[i] = CashBuilder.CreateCash (dtEffective,
				new JulianDate (adblDate[i] = dtEffective.addTenor (astrCashTenor[i]).getJulian()),
				strCurrency);
		}

		// EDF Calibration

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtStart, adblEDFRate.length, "USD");

		for (int i = 0; i < adblEDFRate.length; ++i) {
			aCompCalib[astrCashTenor.length + i] = aEDF[i];
			astrCalibMeasure[astrCashTenor.length + i] = "Rate";
			adblRate[astrCashTenor.length + i] = java.lang.Double.NaN;
			adblCompCalibValue[astrCashTenor.length + i] = adblEDFRate[i];
		}

		// IRS Calibration

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + adblEDFRate.length + astrCashTenor.length] = "Rate";
			adblRate[i + adblEDFRate.length + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + adblEDFRate.length + astrCashTenor.length] = adblIRSRate[i];

			JulianDate dtEffective = dtStart.addBusDays (2, strCurrency);

			aCompCalib[i + adblEDFRate.length + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtEffective,
				new JulianDate (adblDate[i + astrCashTenor.length] = dtEffective.addTenor (astrIRSTenor[i]).getJulian()),
				0., strCurrency, strIndex, strCurrency);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		DiscountCurve dc = RatesScenarioCurveBuilder.CreateDiscountCurve (dtStart, strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib, adblCompCalibValue, astrCalibMeasure, null);

		/*
		 * Check: Re-calculate the input rates
		 */

		/* for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (aCompCalib[i].getPrimaryCode() + " | " + " | " +
				FormatUtil.FormatDouble (dc.getDF (aCompCalib[i].getMaturityDate()), 1, 6, 1.)); */

		return dc;
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtStart = JulianDate.CreateFromYMD (2013, JulianDate.JULY, 11);

		/*
		 * This part is best modeled by Curve #23 in the SWPM "Curves" tab
		 */

		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.0026810};
		double[] adblEDFRate = new double[] {0.0026810, 0.0026810};
		String[] astrIRSTenor = new String[] {    "4Y",      "5Y",      "6Y",
			     "7Y",	    "8Y",      "9Y",     "10Y",     "11Y",     "12Y",     "15Y",     "20Y"};
		double[] adblIRSRate = new double[] {0.0119802, 0.0158492, 0.0192739,
			0.0221449, 0.0244588, 0.0264071, 0.028057, 0.0293805, 0.0306474, 0.0330706, 0.0349158};

		DiscountCurve dc = BuildBBGRatesCurve (dtStart, astrCashTenor, adblCashRate, adblEDFRate, astrIRSTenor, adblIRSRate, "USD");

		JulianDate dtEffective = JulianDate.CreateFromYMD (2013, JulianDate.JULY, 15);

		JulianDate dtMaturity = JulianDate.CreateFromYMD (2018, JulianDate.JULY, 15);

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, "USD");

		RatesComponent fixStream = new org.drip.product.rates.FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
			0.01584922, 2, "30/360", "30/360", false, null, null, dap, dap, dap, dap, dap, null, 10.e+06, "USD", "USD");

		RatesComponent floatStream = new org.drip.product.rates.FloatingStream (dtEffective.getJulian(),
			dtMaturity.getJulian(), 0., 4, "Act/360", "Act/360", "USD-LIBOR-3M", false, null, null,
				dap, dap, dap, dap, dap, null, null, -10.e+06, "USD", "USD");

		java.util.Map<java.lang.String, java.lang.Double> mapIndexFixing = new java.util.HashMap<java.lang.String, java.lang.Double>();

		mapIndexFixing.put ("USD-LIBOR-3M", 0.0026810);

		java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String, java.lang.Double>> mmFixings =
			new java.util.HashMap<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String, java.lang.Double>>();

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings);

		ValuationParams valParams = ValuationParams.CreateValParams (dtStart.addDays (2), 0, "", Convention.DR_ACTUAL);

		System.out.println ("\nFixed Cashflow\n--------");

		for (Period p : fixStream.getCouponPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 0, 2, 158492.2) + FIELD_SEPARATOR
			);

		System.out.println ("\n\nFloating Cashflow\n--------");

		for (Period p : floatStream.getCouponPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 0, 2, 166042.8) + FIELD_SEPARATOR
			);

		org.drip.product.rates.IRSComponent swap = new org.drip.product.rates.IRSComponent (fixStream, floatStream);

		Map<String, Double> mapSwapCalc = swap.value (valParams, null, cmp, null);

		System.out.println ("Par Cpn: " + org.drip.math.common.FormatUtil.FormatDouble (mapSwapCalc.get ("FairPremium"), 1, 4, 100.));
	}
}
