
package org.drip.service.sample;

import java.util.HashMap;
import java.util.Map;

import org.drip.analytics.creator.DiscountCurveBuilder;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.daycount.DateAdjustParams;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.analytics.period.Period;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
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
import org.drip.product.rates.*;
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
 * BloombergSWPM contains the sample demonstrating the replication of Bloomberg's SWPM functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BloombergSWPM {
	private static final String FIELD_SEPARATOR = "    ";

	/*
	 * Sample demonstrating creation of discount curve from rates instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static DiscountCurve BuildBBGRatesCurve (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final double[] adblEDFRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
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

		JulianDate dtCashEffective = dtStart.addBusDays (1, strCurrency);

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = CashBuilder.CreateCash (dtCashEffective,
				new JulianDate (adblDate[i] = dtCashEffective.addTenor (astrCashTenor[i]).getJulian()),
				strCurrency);
		}

		// EDF Calibration

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtStart, adblEDFRate.length, "USD");

		for (int i = 0; i < adblEDFRate.length; ++i) {
			aCompCalib[astrCashTenor.length + i] = aEDF[i];
			astrCalibMeasure[astrCashTenor.length + i] = "Rate";
			adblRate[astrCashTenor.length + i] = java.lang.Double.NaN;
			adblCompCalibValue[astrCashTenor.length + i] = adblEDFRate[i] + dblBump;
		}

		// IRS Calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, strCurrency);

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + adblEDFRate.length + astrCashTenor.length] = "Rate";
			adblRate[i + adblEDFRate.length + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + adblEDFRate.length + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			aCompCalib[i + adblEDFRate.length + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtIRSEffective,
				new JulianDate (adblDate[i + astrCashTenor.length] = dtIRSEffective.addTenor (astrIRSTenor[i]).getJulian()),
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

		JulianDate dtValue = JulianDate.Today();

		JulianDate dtSettle = dtValue.addBusDays (2, "USD");

		System.out.println ("\n---- Valuation Details ----\n");

		System.out.println ("Trade Date  : " + dtValue);

		System.out.println ("Settle Date : " + dtSettle);

		/*
		 * Model the discount curve instrument quotes. Best pulled from Curves #23/#47 in the BBG SWPM "Curves" tab
		 */

		double dblCoupon = 0.01745756;
		double dblFixing = 0.0026410;
		double dblNotional = 10.e+06;
		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.0026410};
		double[] adblEDFRate = new double[] {0.0026410, 0.0026410};
		String[] astrIRSTenor = new String[] {    "4Y",      "5Y",      "6Y",
			     "7Y",	    "8Y",      "9Y",     "10Y",     "11Y",     "12Y",     "15Y",     "20Y"};
		double[] adblIRSRate = new double[] {0.0133440, 0.0174576, 0.0210107,
			0.0239764, 0.0263425, 0.0282876, 0.0299189, 0.0312560, 0.0324536, 0.0348415, 0.0367341};

		/*
		 * Build the Discount Curve
		 */

		DiscountCurve dc = BuildBBGRatesCurve (dtValue, astrCashTenor, adblCashRate, adblEDFRate, astrIRSTenor, adblIRSRate, 0., "USD");

		JulianDate dtEffective = dtValue.addBusDays (2, "USD");

		JulianDate dtMaturity = dtEffective.addTenor ("5Y");

		/*
		 * Build the Fixed Receive Stream
		 */

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, "USD");

		RatesComponent fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
			dblCoupon, 2, "30/360", "30/360", false, null, null, dap, dap, dap, dap, dap, null, dblNotional, "USD", "USD");

		/*
		 * Build the Floating Pay Stream
		 */

		RatesComponent floatStream = new FloatingStream (dtEffective.getJulian(),
			dtMaturity.getJulian(), 0., 4, "Act/360", "Act/360", "USD-LIBOR-3M", false, null, null,
				dap, dap, dap, dap, dap, null, null, -dblNotional, "USD", "USD");

		/*
		 * Build the Swap from the fixed and the floating streams
		 */

		IRSComponent swap = new IRSComponent (fixStream, floatStream);

		System.out.println ("\n---- Swap Details ----\n");

		System.out.println ("Effective: " + dtEffective);

		System.out.println ("Maturity:  " + dtMaturity);

		/*
		 * Set up the base market parameters, including base discount curves and the base fixings
		 */

		CaseInsensitiveTreeMap<Double> mapIndexFixing = new CaseInsensitiveTreeMap<Double>();

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing);

		Map<JulianDate, CaseInsensitiveTreeMap<Double>> mmFixings = new HashMap<JulianDate, CaseInsensitiveTreeMap<Double>>();

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings);

		/*
		 * Set up the valuation parameters
		 */

		ValuationParams valParams = new ValuationParams (dtValue, dtSettle, "USD");

		/*
		 * Generate the base scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapCalc = swap.value (valParams, null, cmp, null);

		double dblBasePV = mapSwapCalc.get ("PV");

		System.out.println ("\n---- Swap Output Measures ----\n");

		System.out.println ("Par Cpn      : " + FormatUtil.FormatDouble (mapSwapCalc.get ("FairPremium"), 1, 5, 100.));

		System.out.println ("Fixed DV01   : " + FormatUtil.FormatDouble (mapSwapCalc.get ("FixedDV01"), 0, 0, 0.0000001 * dblNotional));

		/*
		 * Set up the fixings bumped market parameters - these use base discount curve and the bumped fixing
		 */

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing + 0.0001);

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmpFixingsBumped = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings);

		/*
		 * Generate the fixing bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapFixingsBumpedCalc = swap.value (valParams, null, cmpFixingsBumped, null);

		double dblFixingsDV01 = mapSwapFixingsBumpedCalc.get ("PV") - dblBasePV;

		System.out.println ("Fixings DV01 : " + FormatUtil.FormatDouble (dblFixingsDV01, 0, 0, 0.001 * dblNotional));

		System.out.println ("Total DV01   : " + FormatUtil.FormatDouble (mapSwapCalc.get ("FixedDV01") * 0.0001 + dblFixingsDV01, 0, 0, 0.001 * dblNotional));

		/*
		 * Set up the rate flat bumped market parameters - these use the bumped base discount curve and the base fixing
		 */

		DiscountCurve dcBumped = BuildBBGRatesCurve (dtValue, astrCashTenor, adblCashRate, adblEDFRate, astrIRSTenor, adblIRSRate, 0.0001, "USD");

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing - 0.0001);

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmpRateBumped = ComponentMarketParamsBuilder.CreateComponentMarketParams (dcBumped, null, null, null, null, null, mmFixings);

		/*
		 * Generate the rate flat bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapRateBumpedCalc = swap.value (valParams, null, cmpRateBumped, null);

		System.out.println ("PV01         : " + FormatUtil.FormatDouble (mapSwapRateBumpedCalc.get ("PV") - dblBasePV, 0, 0, 0.001 * dblNotional));

		/*
		 * Generate the Swap's fixed cash flows
		 */

		System.out.println ("\n---- Fixed Cashflow ----\n");

		for (Period p : fixStream.getCouponPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 0, 2, dblCoupon * dblNotional) + FIELD_SEPARATOR
			);

		/*
		 * Generate the Swap's floating cash flows
		 */

		System.out.println ("\n---- Floating Cashflow ----\n");

		for (Period p : floatStream.getCouponPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR
			);
	}
}
