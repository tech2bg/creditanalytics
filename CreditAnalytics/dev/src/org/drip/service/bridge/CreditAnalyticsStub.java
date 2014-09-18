
package org.drip.service.bridge;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * CreditAnalyticsStub serves as a sample server that hosts Credit Analytics functionality. It receives
 *  requests from the analytics client as a serialized message, and invokes the CreditAnalytics
 *  functionality, and sends the client the serialized results.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsStub {
	private static boolean SendMessage (
		final java.io.ObjectOutputStream oos,
		final byte[] abMsg)
	{
		try {
			oos.writeObject (abMsg);

			oos.flush();

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static final org.drip.param.valuation.ValuationParams GetValuationParams (
		final org.drip.service.bridge.CreditAnalyticsRequest cre)
	{
		return (null != cre && null != cre.getValuationParams()) ? cre.getValuationParams() :
			org.drip.param.valuation.ValuationParams.CreateSpotValParams
				(org.drip.analytics.date.JulianDate.Today().julian());
	}

	private static final org.drip.param.market.CurveSurfaceQuoteSet csqs (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.service.bridge.CreditAnalyticsRequest cre,
		final org.drip.param.definition.ScenarioMarketParams mpc)
	{
		return (null != cre && null != cre.csqs()) ? cre.csqs() : mpc.getScenMarketParams (comp, "Base");
	}

	private static final org.drip.param.definition.Quote GetQuote (
		final org.drip.product.credit.BondComponent bond,
		final org.drip.param.market.CurveSurfaceQuoteSet mktParams,
		final org.drip.param.definition.ScenarioMarketParams mpc,
		final java.lang.String strMeasure)
	{
		org.drip.param.definition.ProductQuote cq = (null != mktParams && null != mktParams.productQuote (bond.name())) ?
			mktParams.productQuote (bond.name()) : mpc.getCompQuote (bond.getIdentifierSet()._strID);

		return null == cq ? null : cq.quote (strMeasure);
	}

	private static org.drip.analytics.rates.DiscountCurve MakeDC (
		final org.drip.analytics.date.JulianDate dtStart)
	{
		int NUM_DC_INSTR = 30;
		double adblDate[] = new double[NUM_DC_INSTR];
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		java.lang.String astrCalibMeasure[] = new java.lang.String[NUM_DC_INSTR];
		org.drip.product.definition.CalibratableFixedIncomeComponent aCompCalib[] = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[NUM_DC_INSTR];

		adblDate[0] = dtStart.addDays (3).julian(); // ON

		adblDate[1] = dtStart.addDays (4).julian(); // 1D (TN)

		adblDate[2] = dtStart.addDays (9).julian(); // 1W

		adblDate[3] = dtStart.addDays (16).julian(); // 2W

		adblDate[4] = dtStart.addDays (32).julian(); // 1M

		adblDate[5] = dtStart.addDays (62).julian(); // 2M

		adblDate[6] = dtStart.addDays (92).julian(); // 3M

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
				System.out.println ("Instr[" + i + "]: " + dtStart.addDays (2) + " => " + new
					org.drip.analytics.date.JulianDate (adblDate[i]));

				aCompCalib[i] = org.drip.product.creator.DepositBuilder.CreateDeposit (dtStart.addDays (2), new
					org.drip.analytics.date.JulianDate (adblDate[i]), null, "USD");
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

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aEDF =
			org.drip.product.creator.EDFutureBuilder.GenerateEDPack (dtStart, 8, "USD");

		for (int i = 0; i < 8; ++i) {
			aCompCalib[i + 7] = aEDF[i];
			astrCalibMeasure[i + 7] = "Rate";
			adblRate[i + 7] = 0.01;

			adblDate[i + 7] = dtEDFStart.addDays ((i + 1) * 91).julian();
		}

		adblDate[15] = dtStart.addDays ((int)(365.25 * 4 + 2)).julian(); // 4Y

		adblDate[16] = dtStart.addDays ((int)(365.25 * 5 + 2)).julian(); // 5Y

		adblDate[17] = dtStart.addDays ((int)(365.25 * 6 + 2)).julian(); // 6Y

		adblDate[18] = dtStart.addDays ((int)(365.25 * 7 + 2)).julian(); // 7Y

		adblDate[19] = dtStart.addDays ((int)(365.25 * 8 + 2)).julian(); // 8Y

		adblDate[20] = dtStart.addDays ((int)(365.25 * 9 + 2)).julian(); // 9Y

		adblDate[21] = dtStart.addDays ((int)(365.25 * 10 + 2)).julian(); // 10Y

		adblDate[22] = dtStart.addDays ((int)(365.25 * 11 + 2)).julian(); // 11Y

		adblDate[23] = dtStart.addDays ((int)(365.25 * 12 + 2)).julian(); // 12Y

		adblDate[24] = dtStart.addDays ((int)(365.25 * 15 + 2)).julian(); // 15Y

		adblDate[25] = dtStart.addDays ((int)(365.25 * 20 + 2)).julian(); // 20Y

		adblDate[26] = dtStart.addDays ((int)(365.25 * 25 + 2)).julian(); // 25Y

		adblDate[27] = dtStart.addDays ((int)(365.25 * 30 + 2)).julian(); // 30Y

		adblDate[28] = dtStart.addDays ((int)(365.25 * 40 + 2)).julian(); // 40Y

		adblDate[29] = dtStart.addDays ((int)(365.25 * 50 + 2)).julian(); // 50Y

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
				org.drip.product.rates.Stream fixStream = new org.drip.product.rates.Stream
					(org.drip.analytics.support.PeriodBuilder.BackwardPeriodSingleReset (dtStart.julian(),
						adblDate[i + 15], java.lang.Double.NaN, null, null, null, null, null, null, null,
							null, 2, "Act/360", false, "Act/360", false,
								org.drip.analytics.support.PeriodBuilder.NO_ADJUSTMENT, true, "USD", -1., null,
									0., "USD", "USD", null, null));

				org.drip.product.rates.Stream floatStream = new org.drip.product.rates.Stream
					(org.drip.analytics.support.PeriodBuilder.BackwardPeriodSingleReset (dtStart.julian(),
						adblDate[i + 15], java.lang.Double.NaN, null, null, null, null, null, null, null,
							null, 4, "Act/360", false, "Act/360", false,
								org.drip.analytics.support.PeriodBuilder.NO_ADJUSTMENT, true, "USD", -1.,
									null, 0., "USD", "USD", org.drip.state.identifier.ForwardLabel.Create
										("USD", "LIBOR", "3M"), null));

				aCompCalib[i + 15] = new org.drip.product.rates.FixFloatComponent (fixStream, floatStream,
					null);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.param.market.LatentStateFixingsContainer lsfc = new
			org.drip.param.market.LatentStateFixingsContainer();

		lsfc.add (dtStart.addDays (2), org.drip.state.identifier.ForwardLabel.Standard ("USD-LIBOR-6M"),
			0.0402);

		return org.drip.param.creator.ScenarioDiscountCurveBuilder.NonlinearBuild (dtStart, "USD",
			org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib,
				adblCompCalibValue, astrCalibMeasure, lsfc);
	}

	private static final org.drip.analytics.output.BondRVMeasures ProcessRequest (
		final org.drip.service.bridge.CreditAnalyticsRequest cre,
		final org.drip.param.definition.ScenarioMarketParams mpc)
	{
		if (null == cre) return null;

		org.drip.product.credit.BondComponent bond = (org.drip.product.credit.BondComponent)
			cre.getComponent();

		if (null == bond) return null;

		org.drip.param.valuation.ValuationParams valParams = GetValuationParams (cre);

		org.drip.param.market.CurveSurfaceQuoteSet mktParams = csqs (bond, cre, mpc);

		if (null == mktParams) return null;

		if (null == mktParams.fundingCurve (org.drip.state.identifier.FundingLabel.Standard
			(bond.payCurrency()[0]))) {
			try {
				if (!mktParams.setFundingCurve (MakeDC (new org.drip.analytics.date.JulianDate
					(valParams.valueDate()))))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.param.definition.Quote qPrice = GetQuote (bond, mktParams, mpc, "Price");

		if (null == qPrice) return null;

		double dblAskPrice = 0.01 * qPrice.getQuote ("ASK");

		if (!org.drip.quant.common.NumberUtil.IsValid (dblAskPrice)) return null;

		org.drip.param.valuation.WorkoutInfo wi = bond.calcExerciseYieldFromPrice (valParams, mktParams, null,
			dblAskPrice);

		return null == wi ? null : bond.standardMeasures (valParams, null, mktParams, null, wi, dblAskPrice);
	}

	private static final boolean run (
		final java.net.ServerSocket sockAS,
		final org.drip.param.definition.ScenarioMarketParams mpc)
		throws java.lang.Exception
	{
		java.net.Socket sockAC = sockAS.accept();

		System.out.println ("Connection from: " + sockAC.getInetAddress().getHostName());

		java.io.ObjectOutputStream out = new java.io.ObjectOutputStream (sockAC.getOutputStream());

		java.io.ObjectInputStream in = new java.io.ObjectInputStream (sockAC.getInputStream());

		byte[] abCommand = (byte[]) in.readObject();

		if (null == abCommand || 0 == abCommand.length) {
			org.drip.service.bridge.CreditAnalyticsResponse creFailureResponse = new
				org.drip.service.bridge.CreditAnalyticsResponse ("BAD_REQUEST",
					org.drip.service.bridge.CreditAnalyticsResponse.CAR_FAILURE, "Invalid Request");

			System.out.println ("\t---Sending Failure Response " + creFailureResponse.getRequestID() + " @ "
				+ creFailureResponse.getTimeSnap());

			SendMessage (out, creFailureResponse.serialize());

			return false;
		}

		org.drip.service.bridge.CreditAnalyticsRequest cre = null;

		try {
			cre = new org.drip.service.bridge.CreditAnalyticsRequest (abCommand);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			org.drip.service.bridge.CreditAnalyticsResponse creFailureResponse = new
				org.drip.service.bridge.CreditAnalyticsResponse ("BAD_REQUEST",
					org.drip.service.bridge.CreditAnalyticsResponse.CAR_FAILURE, "Invalid Request");

			System.out.println ("\t---Sending Failure Response " + creFailureResponse.getRequestID() + " @ "
				+ creFailureResponse.getTimeSnap());

			SendMessage (out, creFailureResponse.serialize());

			return false;
		}

		org.drip.service.bridge.CreditAnalyticsResponse creStatusResponse = new
			org.drip.service.bridge.CreditAnalyticsResponse (cre.getID(),
				org.drip.service.bridge.CreditAnalyticsResponse.CAR_SUCCESS, "InProgress");

		System.out.println ("\t---Sending Status Response " + creStatusResponse.getRequestID() + " @ " +
			creStatusResponse.getTimeSnap());

		if (!SendMessage (out, creStatusResponse.serialize())) return false;

		org.drip.analytics.output.BondRVMeasures bondRVM = ProcessRequest (cre, mpc);

		if (null == bondRVM) return false;

		org.drip.service.bridge.CreditAnalyticsResponse carFinalResponse = new
			org.drip.service.bridge.CreditAnalyticsResponse (cre.getID(),
				org.drip.service.bridge.CreditAnalyticsResponse.CAR_SUCCESS, "BondRVMeasures");

		System.out.println ("\t---Sending Final Response " + carFinalResponse.getRequestID() + " @ " +
			carFinalResponse.getTimeSnap());

		if (!carFinalResponse.setSerializedMsg (bondRVM.serialize())) return false;

		return SendMessage (out, carFinalResponse.serialize());
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		java.sql.Statement stmt = org.drip.service.env.EnvManager.InitEnv
			("c:\\DRIP\\CreditAnalytics\\Config.xml");

		org.drip.param.definition.ScenarioMarketParams mpc = org.drip.service.env.EnvManager.PopulateMPC (stmt,
			org.drip.analytics.date.JulianDate.Today());

		java.net.ServerSocket sockAS = org.drip.param.config.ConfigLoader.InitAnalServer
			("c:\\DRIP\\CreditAnalytics\\Config.xml");

		System.out.println ("Ready ...");

		while (true)
			run (sockAS, mpc);
	}
}
