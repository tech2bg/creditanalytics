
package org.drip.service.bridge;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 *  Class captures the requests for the Credit Analytics server from the client, formats them, and sends them
 *  	to the Credit Analytics Stub.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsProxy {
	private static final double PRICE_DOWNSHIFT_RATE = 0.250;
	private static final java.lang.String DISPLAY_GAP = "   ";
	private static final double PRICE_DOWNSHIFT_AMPLITUDE = 0.075;

	private static org.drip.analytics.definition.DiscountCurve MakeDC (
		final org.drip.analytics.date.JulianDate dtStart)
	{
		int NUM_DC_INSTR = 30;
		double adblDate[] = new double[NUM_DC_INSTR];
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		java.lang.String astrCalibMeasure[] = new java.lang.String[NUM_DC_INSTR];
		org.drip.product.definition.CalibratableComponent aCompCalib[] = new
			org.drip.product.definition.CalibratableComponent[NUM_DC_INSTR];

		adblDate[0] = dtStart.addDays (3).getJulian(); // ON

		adblDate[1] = dtStart.addDays (4).getJulian(); // 1D (TN)

		adblDate[2] = dtStart.addDays (9).getJulian(); // 1W

		adblDate[3] = dtStart.addDays (16).getJulian(); // 2W

		adblDate[4] = dtStart.addDays (32).getJulian(); // 1M

		adblDate[5] = dtStart.addDays (62).getJulian(); // 2M

		adblDate[6] = dtStart.addDays (92).getJulian(); // 3M

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

			try {
				aCompCalib[i] = org.drip.product.creator.CashBuilder.CreateCash (dtStart.addDays (2), new
					org.drip.analytics.date.JulianDate (adblDate[i]), "USD");
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.date.JulianDate dtEDFStart = dtStart;
		adblCompCalibValue[7] = .0027;
		adblCompCalibValue[8] = .0032;
		adblCompCalibValue[9] = .0041;
		adblCompCalibValue[10] = .0054;
		adblCompCalibValue[11] = .0077;
		adblCompCalibValue[12] = .0104;
		adblCompCalibValue[13] = .0134;
		adblCompCalibValue[14] = .0160;

		org.drip.product.definition.CalibratableComponent[] aEDF =
			org.drip.product.creator.EDFutureBuilder.GenerateEDPack (dtStart, 8, "USD");

		for (int i = 0; i < 8; ++i) {
			aCompCalib[i + 7] = aEDF[i];
			astrCalibMeasure[i + 7] = "Rate";
			adblRate[i + 7] = 0.01;

			adblDate[i + 7] = dtEDFStart.addDays ((i + 1) * 91).getJulian();
		}

		adblDate[15] = dtStart.addDays ((int)(365.25 * 4 + 2)).getJulian(); // 4Y

		adblDate[16] = dtStart.addDays ((int)(365.25 * 5 + 2)).getJulian(); // 5Y

		adblDate[17] = dtStart.addDays ((int)(365.25 * 6 + 2)).getJulian(); // 6Y

		adblDate[18] = dtStart.addDays ((int)(365.25 * 7 + 2)).getJulian(); // 7Y

		adblDate[19] = dtStart.addDays ((int)(365.25 * 8 + 2)).getJulian(); // 8Y

		adblDate[20] = dtStart.addDays ((int)(365.25 * 9 + 2)).getJulian(); // 9Y

		adblDate[21] = dtStart.addDays ((int)(365.25 * 10 + 2)).getJulian(); // 10Y

		adblDate[22] = dtStart.addDays ((int)(365.25 * 11 + 2)).getJulian(); // 11Y

		adblDate[23] = dtStart.addDays ((int)(365.25 * 12 + 2)).getJulian(); // 12Y

		adblDate[24] = dtStart.addDays ((int)(365.25 * 15 + 2)).getJulian(); // 15Y

		adblDate[25] = dtStart.addDays ((int)(365.25 * 20 + 2)).getJulian(); // 20Y

		adblDate[26] = dtStart.addDays ((int)(365.25 * 25 + 2)).getJulian(); // 25Y

		adblDate[27] = dtStart.addDays ((int)(365.25 * 30 + 2)).getJulian(); // 30Y

		adblDate[28] = dtStart.addDays ((int)(365.25 * 40 + 2)).getJulian(); // 40Y

		adblDate[29] = dtStart.addDays ((int)(365.25 * 50 + 2)).getJulian(); // 50Y

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

			try {
				aCompCalib[i + 15] = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dtStart.addDays
					(2), new org.drip.analytics.date.JulianDate (adblDate[i + 15]), 0., "USD",
						"USD-LIBOR-3M", "USD");
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-3M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (dtStart.addDays (2), mIndexFixings);

		return org.drip.param.creator.RatesScenarioCurveBuilder.CreateDiscountCurve (dtStart, "USD",
			org.drip.analytics.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib,
				adblCompCalibValue, astrCalibMeasure, mmFixings);
	}

	public static org.drip.analytics.definition.CreditCurve MakeCC (
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.analytics.definition.DiscountCurve dc)
	{
		double[] adblQuotes = new double[5];
		java.lang.String[] astrCalibMeasure = new java.lang.String[5];
		org.drip.product.definition.CreditDefaultSwap[] aCDS = new
			org.drip.product.definition.CreditDefaultSwap[5];

		for (int i = 0; i < 5; ++i) {
			aCDS[i] = org.drip.product.creator.CDSBuilder.CreateSNAC (dtStart, (i + 1) + "Y", 0.01, "CORP");

			adblQuotes[i] = 100.;
			astrCalibMeasure[i] = "FairPremium";
		}

		return org.drip.param.creator.CreditScenarioCurveBuilder.CreateCreditCurve ("CORP", dtStart, aCDS,
			dc, adblQuotes, astrCalibMeasure, 0.4, false);
	}

	private static final org.drip.product.credit.BondComponent TestJSON (
		final int iTenorInYears)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> mapBondParams = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String>();

		mapBondParams.put ("assetId", "111384AC7 CORP");

		mapBondParams.put ("daysToSettle", "3");

		mapBondParams.put ("currency", "USD");

		mapBondParams.put ("subInstrumentType", " ");

		mapBondParams.put ("hols", "US");

		mapBondParams.put ("ticker", "BDVU");

		mapBondParams.put ("description", "BDVU 11 3/8 09/01/12");

		mapBondParams.put ("shortName", "BDVU-RSTR11/12");

		mapBondParams.put ("country", "US");

		mapBondParams.put ("dataSource", "BGn");

		mapBondParams.put ("instrumentType", "GLOBAL");

		mapBondParams.put ("amtOutstanding", "0");

		mapBondParams.put ("firstCpnDate", "2008-03-01");

		mapBondParams.put ("maturity", "20" + iTenorInYears + "-09-01");

		mapBondParams.put ("nextParCallDate", " ");

		mapBondParams.put ("baseIndex", " ");

		mapBondParams.put ("frequency", "2");

		mapBondParams.put ("amtIssue", "300000000");

		mapBondParams.put ("callable", "Y");

		mapBondParams.put ("putable", "N");

		mapBondParams.put ("maturityType", "CALLABLE");

		mapBondParams.put ("coupon", "6.500");

		mapBondParams.put ("couponFloor", " ");

		mapBondParams.put ("inflationLinked", "N");

		mapBondParams.put ("dayCount", "30/360");

		mapBondParams.put ("couponType", "DEFAULTED");

		mapBondParams.put ("issuer", "BROADVIEW NETWORKS HOLDI");

		mapBondParams.put ("issueDate", "2007-11-14");

		mapBondParams.put ("cusip", "111384AC7");

		mapBondParams.put ("isin", "US111384AC72");

		mapBondParams.put ("callDaysNotice", "30");

		mapBondParams.put ("putDaysNotice", " ");

		mapBondParams.put ("issuerIndustry", "INDUSTRIAL");

		mapBondParams.put ("ratingSP", "NR");

		mapBondParams.put ("calcType", "** IN DEFAULT **");

		mapBondParams.put ("series", " ");

		mapBondParams.put ("called", "N");

		mapBondParams.put ("defaulted", "Y");

		mapBondParams.put ("onTheRun", "N");

		mapBondParams.put ("redempValue", "100");

		mapBondParams.put ("refIndex", " ");

		mapBondParams.put ("quotedMargin", " ");

		mapBondParams.put ("refixFreq", " ");

		mapBondParams.put ("resetIndex", " ");

		mapBondParams.put ("marketSector", "Corp");

		mapBondParams.put ("marketIndex", "GLOBAL");

		mapBondParams.put ("callFeature", "Anytime");

		mapBondParams.put ("putFeature", " ");

		mapBondParams.put ("callDiscrete", "N");

		mapBondParams.put ("putFeature", " ");

		mapBondParams.put ("announceDt", "2007-10-16");

		mapBondParams.put ("firstSettleDt", "2007-11-14");

		mapBondParams.put ("intAccDt", "2007-09-01");

		mapBondParams.put ("startAccDt", "2007-09-01");

		mapBondParams.put ("sinkable", "N");

		mapBondParams.put ("redempCrncy", "USD");

		mapBondParams.put ("tradeCrncy", "USD");

		mapBondParams.put ("cpnCrncy", "USD");

		mapBondParams.put ("floater", "N");

		mapBondParams.put ("finalMaturityDt", "20" + iTenorInYears + "-09-01");

		mapBondParams.put ("perpetual", "N");

		/*
		  "optionSchedule" : {
		    "2009-09-01" : 105.688,
		    "2010-09-01" : 102.844,
		    "2011-09-01" : 100
		  },
		   */

		org.drip.product.creator.BondProductBuilder bpb =
			org.drip.product.creator.BondProductBuilder.CreateFromJSONMap (mapBondParams, null);

		return null == bpb ? null : org.drip.product.creator.BondBuilder.CreateBondFromParams
			(bpb.getTSYParams(), bpb.getIdentifierParams(), bpb.getCouponParams(), bpb.getCurrencyParams(),
				bpb.getFloaterParams(), bpb.getMarketConvention(), bpb.getRatesValuationParams(),
					bpb.getCRValuationParams(), bpb.getCFTEParams(), bpb.getPeriodGenParams(),
						bpb.getNotionalParams());
	}

	private static org.drip.param.market.ComponentMarketParamSet MakeCMP (
		final org.drip.analytics.date.JulianDate dtStart,
		final double dblDownShift)
	{
		double dblStart = dtStart.getJulian();

		double[] adblDate = new double[3];
		double[] adblRateTSY = new double[3];
		double[] adblRateEDSF = new double[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRateTSY[i] = 0.01 * (i + 1);
			adblRateEDSF[i] = 0.0125 * (i + 1);
		}

		org.drip.analytics.definition.DiscountCurve dc = MakeDC (dtStart);

		org.drip.analytics.definition.DiscountCurve dcTSY =
			org.drip.analytics.creator.DiscountCurveBuilder.CreateDC (dtStart, "ABCTSY", adblDate,
				adblRateTSY,
					org.drip.analytics.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.definition.DiscountCurve dcEDSF =
			org.drip.analytics.creator.DiscountCurveBuilder.CreateDC (dtStart, "ABCEDSF", adblDate,
				adblRateEDSF,
					org.drip.analytics.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.definition.CreditCurve cc = MakeCC (dtStart, dc);

		org.drip.param.market.ComponentMultiMeasureQuote cqTSY2ON = new
			org.drip.param.market.ComponentMultiMeasureQuote();

		try {
			cqTSY2ON.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103.,
				java.lang.Double.NaN), false);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote> mapTSYQuotes
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

		mapTSYQuotes.put ("TSY2ON", cqTSY2ON);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (dtStart.addDays (2), mIndexFixings);

		org.drip.param.market.ComponentMultiMeasureQuote cqBond = new
			org.drip.param.market.ComponentMultiMeasureQuote();

		try {
			cqBond.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 100. - dblDownShift,
				java.lang.Double.NaN), false);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		try {
			return new org.drip.param.market.ComponentMarketParamSet (dc, null, dcTSY, dcEDSF, cc, cqBond,
				mapTSYQuotes, mmFixings);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final org.drip.service.bridge.CreditAnalyticsRequest CreateRequest (
		final int iTenorInYears)
	{
		org.drip.param.pricer.PricerParams pricerParams = null;
		org.drip.service.bridge.CreditAnalyticsRequest cre = null;
		org.drip.param.valuation.QuotingParams quotingParams = null;

		org.drip.analytics.date.JulianDate dtStart = org.drip.analytics.date.JulianDate.Today();

		org.drip.product.credit.BondComponent bond = TestJSON (iTenorInYears);

		org.drip.param.valuation.ValuationParams valParams =
			org.drip.param.valuation.ValuationParams.CreateValParams (dtStart, 0, "USD", 3);

		if (null == valParams) return null;

		try {
			pricerParams = new org.drip.param.pricer.PricerParams (7, new
				org.drip.param.definition.CalibrationParams ("Price", 1, new
					org.drip.param.valuation.WorkoutInfo (dtStart.getJulian(), 0.04, 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY)), false, 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.param.market.ComponentMarketParamSet cmp = MakeCMP (dtStart, (1. + PRICE_DOWNSHIFT_AMPLITUDE
			* (new java.util.Random().nextDouble() - 0.5)) * iTenorInYears * PRICE_DOWNSHIFT_RATE);

		try {
			quotingParams = new org.drip.param.valuation.QuotingParams ("30/360", 2, true, null, "USD",
				false);

			cre = new org.drip.service.bridge.CreditAnalyticsRequest (bond, valParams, pricerParams, cmp,
				quotingParams);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return cre;
	}

	private static final boolean PrintRVMeasures (
		final java.lang.String strPrefix,
		final org.drip.analytics.output.BondRVMeasures rv)
	{
		if (null == rv) return false;

		System.out.println (strPrefix + "ASW: " + org.drip.math.common.FormatUtil.FormatDouble
			(rv._dblAssetSwapSpread, 0, 0, 1.));

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

	private static final boolean PrintRun (
		final java.util.Map<org.drip.analytics.date.JulianDate, org.drip.analytics.output.BondRVMeasures>
			mapRVM)
	{
		System.out.println (" MATURITY" + DISPLAY_GAP + "YIELD" + DISPLAY_GAP + "Z-S" + DISPLAY_GAP + "T-S" +
			DISPLAY_GAP + "CRD" + DISPLAY_GAP + " PRICE");

		System.out.println (" ------- " + DISPLAY_GAP + "-----" + DISPLAY_GAP + "---" + DISPLAY_GAP + "---" +
			DISPLAY_GAP +  "---" + DISPLAY_GAP + " -----");

		for (java.util.Map.Entry<org.drip.analytics.date.JulianDate,
			org.drip.analytics.output.BondRVMeasures> me : mapRVM.entrySet()) {
			org.drip.analytics.output.BondRVMeasures rv = me.getValue();

			System.out.println (me.getKey() + DISPLAY_GAP + org.drip.math.common.FormatUtil.FormatDouble
				(rv._wi._dblYield, 0, 2, 100.) + DISPLAY_GAP + org.drip.math.common.FormatUtil.FormatDouble
					(rv._dblZSpread, 0, 0, 10000.) + DISPLAY_GAP +
						org.drip.math.common.FormatUtil.FormatDouble (rv._dblTSYSpread, 0, 0, 10000.) +
							DISPLAY_GAP + org.drip.math.common.FormatUtil.FormatDouble (rv._dblCreditBasis,
								0, 0, 10000.) + DISPLAY_GAP + org.drip.math.common.FormatUtil.FormatDouble
									(rv._dblPrice, 0, 3, 100.));
		}

		return true;
	}

	private static final boolean SendRequest (
		final java.util.Map<org.drip.analytics.date.JulianDate, org.drip.analytics.output.BondRVMeasures>
			mapRVM,
		final java.net.Socket socket,
		final int iTenorInYears)
	{
		try {
			java.io.ObjectOutputStream out = new java.io.ObjectOutputStream (socket.getOutputStream());

			java.io.ObjectInputStream in = new java.io.ObjectInputStream (socket.getInputStream());

			org.drip.service.bridge.CreditAnalyticsRequest creRequest = CreateRequest (iTenorInYears);

			if (null == creRequest) return false;

			System.out.println ("\t---Sending Request " + creRequest.getID() + " @ " +
				creRequest.getTimeSnap());

			out.writeObject (creRequest.serialize());

			out.flush();

			byte[] abStatus = (byte[]) in.readObject();

			if (null == abStatus || 0 == abStatus.length) return false;

			org.drip.service.bridge.CreditAnalyticsResponse creStatus = new
				org.drip.service.bridge.CreditAnalyticsResponse (abStatus);

			System.out.println ("\t---Received Response " + creStatus.getType() + " for request " +
				creStatus.getRequestID() + " @ " + creStatus.getTimeSnap());

			byte[] abResponse = (byte[]) in.readObject();

			if (null == abResponse || 0 == abResponse.length) return false;

			org.drip.service.bridge.CreditAnalyticsResponse creResponse = new
				org.drip.service.bridge.CreditAnalyticsResponse (abResponse);

			System.out.println ("\t---Received Response " + creResponse.getType() + " for request " +
				creResponse.getRequestID() + " @ " + creResponse.getTimeSnap());

			org.drip.analytics.output.BondRVMeasures rv = new org.drip.analytics.output.BondRVMeasures
				(creResponse.getSerializedMsg());

			try {
				mapRVM.put (new org.drip.analytics.date.JulianDate (rv._wi._dblDate), rv);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			System.out.println ("\t---RV Measures:\n\t--------------- ");

			PrintRVMeasures ("\t\t\t", rv);

			System.out.println ("\t---\n\t---\n\n ");

			in.close();

			out.close();

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.service.env.EnvManager.InitEnv ("c:\\DRIP\\CreditAnalytics\\Config.xml");

		final java.util.Map<org.drip.analytics.date.JulianDate, org.drip.analytics.output.BondRVMeasures>
			mapRVM = new java.util.TreeMap<org.drip.analytics.date.JulianDate,
				org.drip.analytics.output.BondRVMeasures>();

		for (int i = 16; i <= 70; ++i) {
			java.net.Socket socket = org.drip.param.config.ConfigLoader.ConnectToAnalServer
				("c:\\DRIP\\CreditAnalytics\\Config.xml");

			SendRequest (mapRVM, socket, i);

			java.lang.Thread.sleep (1500);
		}

		PrintRun (mapRVM);
	}
}
