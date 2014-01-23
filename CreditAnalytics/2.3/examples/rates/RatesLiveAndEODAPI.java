
package org.drip.sample.rates;

/*
 * General imports
 */

import java.util.*;

/*
 * Credit Product imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.product.definition.CalibratableComponent;

/*
 * Credit Analytics API Import
 */

import org.drip.service.api.CreditAnalytics;

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
 * RatesLiveAndEODAPI contains the sample API demonstrating the usage of the Rates Live and EOD functions. It
 * 	does the following:
 * 	- Pulls all the closing rates curve names (of any type, incl.TSY) that exist for a given date.
 * 	- Load the full IR curve created from all the single currency rate quotes (except TSY) for the given
 * 		currency and date.
 * 	- Calculate the discount factor to an arbitrary date using the constructed curve.
 * 	- Retrieve the components and their quotes that went into constructing the curve, and display them.
 * 	- Load all the rates curves available between the dates for the currency specified, and step through.
 * 	- Load all the Cash quotes available between the dates for the currency specified, and step through.
 * 	- Load all the EDF quotes available between the dates for the currency specified, and step through.
 * 	- Load all the IRS quotes available between the dates for the currency specified, and step through.
 * 	- Load all the TSY quotes available between the dates for the currency specified, and step through.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesLiveAndEODAPI {

	/**
	 * Sample API demonstrating the creation/usage of rates curve
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static final void RatesCurveAPISample()
		throws Exception
	{
		/*
		 * Pulls all the closing rates curve names (of any type, incl.TSY) that exist for a given date.
		 */

		JulianDate dt1 = JulianDate.CreateFromYMD (2011, 12, 16); // Date of interest

		Set<String> setstrIRCurves = CreditAnalytics.GetEODIRCurveNames (dt1); // Set of IR curves

		for (String strIRCurveName : setstrIRCurves)
			System.out.println ("\t" + strIRCurveName);

		/*
		 * Load the full IR curve created from all the single currency rate quotes (except TSY) for the given
		 * 		currency and date.
		 */

		DiscountCurve dc = CreditAnalytics.LoadEODFullIRCurve ("EUR", dt1);

		// Calculate the discount factor to an arbitrary date using the constructed curve.

		System.out.println ("DF (2021, 1, 14): " + dc.df (JulianDate.CreateFromYMD (2021, 1, 14)));

		// Retrieve the components whose quotes went into constructing the curve

		CalibratableComponent[] aCC = dc.calibComp();

		// Display the component named codes and the corresponding quotes

		for (int i = 0; i < aCC.length; ++i)
			System.out.println (aCC[i].getPrimaryCode() + " => " + dc.manifestMeasure (aCC[i].getPrimaryCode()));

		JulianDate dt2 = JulianDate.CreateFromYMD (2012, 1, 17);

		/*
		 * Load all the rates curves available between the dates for the currency specified.
		 */

		Map<JulianDate, DiscountCurve> mapDC = CreditAnalytics.LoadEODFullIRCurves ("EUR", dt1, dt2);

		// Navigate through them, and display the 3Y IRS rate

		for (Map.Entry<JulianDate, DiscountCurve> meDC : mapDC.entrySet()) {
			JulianDate dt = meDC.getKey();

			DiscountCurve dcEOD = meDC.getValue();

			System.out.println (dt + "[IRS.3Y] => " + dcEOD.manifestMeasure ("IRS.3Y"));
		}

		/*
		 *  Load the closing rates curve built from cash (money market) instruments for the given date and
		 * 		currency
		 */

		DiscountCurve dcCash = CreditAnalytics.LoadEODIRCashCurve ("EUR", dt1);

		// Discount factor for the Closing Cash curve

		System.out.println ("DF (2021, 1, 14): " + dcCash.df (JulianDate.CreateFromYMD (2021, 1, 14)));

		// Display the component named codes and the corresponding quotes

		CalibratableComponent[] aCCCash = dcCash.calibComp();

		for (int i = 0; i < aCCCash.length; ++i)
			System.out.println (aCCCash[i].getPrimaryCode() + " => " + (int) (10000. * dcCash.manifestMeasure (aCCCash[i].getPrimaryCode())));

		/*
		 * Load the cash curves available between the dates for the currency specified.
		 */

		Map<JulianDate, DiscountCurve> mapCashDC = CreditAnalytics.LoadEODIRCashCurves ("EUR", dt1, dt2);

		// Navigate through them, and display the 3M Cash rate

		for (Map.Entry<JulianDate, DiscountCurve> meCashDC : mapCashDC.entrySet()) {
			JulianDate dt = meCashDC.getKey();

			DiscountCurve dcEOD = meCashDC.getValue();

			System.out.println (dt + "[3M] => " + (int) (10000. * dcEOD.manifestMeasure ("3M")));
		}

		/*
		 *  Load the closing rates curve built from EDF (futures) instruments for the given date and currency
		 */

		DiscountCurve dcEDF = CreditAnalytics.LoadEODEDFCurve ("EUR", dt1);

		// Discount factor for the Closing EDF curve

		if (null != dcEDF) System.out.println ("DF (2021, 1, 14): " + dcEDF.df (JulianDate.CreateFromYMD (2021, 1, 14)));

		// Display the component named codes and the corresponding quotes

		if (null != dcEDF) {
			CalibratableComponent[] aCCEDF = dcEDF.calibComp();

			for (int i = 0; i < aCCEDF.length; ++i)
				System.out.println (aCCEDF[i].getPrimaryCode() + " => " +
					(int) (10000. * dcEDF.manifestMeasure (aCCEDF[i].getPrimaryCode())));
		}

		/*
		 * Load the EDF curves available between the dates for the currency specified.
		 */

		Map<JulianDate, DiscountCurve> mapEDFDC = CreditAnalytics.LoadEODEDFCurves ("EUR", dt1, dt2);

		// Navigate through them, and display the EDZ3 EDF rate

		for (Map.Entry<JulianDate, DiscountCurve> meEDFDC : mapEDFDC.entrySet()) {
			JulianDate dt = meEDFDC.getKey();

			DiscountCurve dcEOD = meEDFDC.getValue();

			System.out.println (dt + "[EDZ3] => " + (int) (10000. * dcEOD.manifestMeasure ("EDZ3")));
		}

		/*
		 *  Load the closing rates curve built from IRS (swap) instruments for the given date and currency
		 */

		DiscountCurve dcIRS = CreditAnalytics.LoadEODIRSwapCurve ("EUR", dt1);

		// Discount factor for the Closing IRS curve

		System.out.println ("DF (2021, 1, 14): " + dcIRS.df (JulianDate.CreateFromYMD (2021, 1, 14)));

		// Display the component named codes and the corresponding quotes

		CalibratableComponent[] aCCIRS = dcIRS.calibComp();

		for (int i = 0; i < aCCIRS.length; ++i)
			System.out.println (aCCIRS[i].getPrimaryCode() + " => " +
				(int) (10000. * dcIRS.manifestMeasure (aCCIRS[i].getPrimaryCode())));

		/*
		 * Load all the Closing IRS curves available between the dates for the currency specified.
		 */

		Map<JulianDate, DiscountCurve> mapIRSDC = CreditAnalytics.LoadEODIRSwapCurves ("EUR", dt1, dt2);

		// Navigate through them, and display the 3Y IRS rate

		for (Map.Entry<JulianDate, DiscountCurve> meIRSDC : mapIRSDC.entrySet()) {
			JulianDate dt = meIRSDC.getKey();

			DiscountCurve dcEOD = meIRSDC.getValue();

			System.out.println (dt + "[IRS.3Y] => " + dcEOD.manifestMeasure ("IRS.3Y"));
		}

		/*
		 * Pulls all the closing TSY curve names that exist for a given date.
		 */

		Set<String> setstrTSYCurves = CreditAnalytics.GetEODTSYCurveNames (dt1);

		for (String strTSYCurveName : setstrTSYCurves)
			System.out.println (strTSYCurveName);

		/*
		 *  Load the closing rates curve built from TSY bond instruments for the given date and currency
		 */

		DiscountCurve dcTSY = CreditAnalytics.LoadEODTSYCurve ("USD", dt1);

		// Discount factor for the Closing TSY curve

		System.out.println ("DF (2021, 1, 14): " + dcTSY.df (JulianDate.CreateFromYMD (2021, 1, 14)));

		// Display the component named codes and the corresponding quotes

		CalibratableComponent[] aCompTSY = dcTSY.calibComp();

		for (int i = 0; i < aCompTSY.length; ++i)
			System.out.println (aCompTSY[i].getPrimaryCode() + " => " + (int)
				(10000. * dcTSY.manifestMeasure (aCompTSY[i].getPrimaryCode())));

		/*
		 * Load all the Closing TSY curves available between the dates for the currency specified.
		 */

		Map<JulianDate, DiscountCurve> mapTSYDC = CreditAnalytics.LoadEODTSYCurves ("USD", dt1, dt2);

		// Navigate through them, and display the 5Y quote

		for (Map.Entry<JulianDate, DiscountCurve> meTSYDC : mapTSYDC.entrySet()) {
			JulianDate dt = meTSYDC.getKey();

			DiscountCurve dcTSYEOD = meTSYDC.getValue();

			System.out.println (dt + "[5Y] => " + (int) (10000. * dcTSYEOD.manifestMeasure ("5Y")));
		}
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		if (!CreditAnalytics.Init (strConfig)) {
			System.out.println ("Cannot fully init FI!");

			System.exit (306);
		}

		RatesCurveAPISample();
	}
}
