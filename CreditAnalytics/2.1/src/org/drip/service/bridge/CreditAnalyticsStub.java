
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
 *  This class receives the requests from the analytics client, and invokes the CreditAnalytics
 *  	functionality, and sends the client the results.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsStub {
	private static final boolean s_bWhineOnError = false;

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
				(org.drip.analytics.date.JulianDate.Today().getJulian());
	}

	private static final org.drip.param.definition.ComponentMarketParams GetCMP (
		final org.drip.product.definition.Component comp,
		final org.drip.service.bridge.CreditAnalyticsRequest cre,
		final org.drip.param.definition.MarketParams mpc)
	{
		return (null != cre && null != cre.getCMP()) ? cre.getCMP() : mpc.getScenCMP (comp, "Base");
	}

	private static final org.drip.param.pricer.PricerParams GetPricerParams (
		final org.drip.service.bridge.CreditAnalyticsRequest cre)
	{
		return (null != cre && null != cre.getPricerParams()) ? cre.getPricerParams() :
			org.drip.param.pricer.PricerParams.MakeStdPricerParams();
	}

	private static final org.drip.param.valuation.QuotingParams GetQuotingParams (
		final org.drip.service.bridge.CreditAnalyticsRequest cre)
	{
		return (null != cre && null != cre.getQuotingParams()) ? cre.getQuotingParams() : null;
	}

	private static final org.drip.param.definition.Quote GetQuote (
		final org.drip.product.credit.BondComponent bond,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.definition.MarketParams mpc,
		final java.lang.String strMeasure)
	{
		org.drip.param.definition.ComponentQuote cq = (null != cmp && null != cmp.getComponentQuote()) ?
			cmp.getComponentQuote() : mpc.getCompQuote (bond.getIdentifierSet()._strID);

		return null == cq ? null : cq.getQuote (strMeasure);
	}

	private static final org.drip.analytics.output.BondRVMeasures ProcessRequest (
		final org.drip.service.bridge.CreditAnalyticsRequest cre,
		final org.drip.param.definition.MarketParams mpc)
	{
		if (null == cre) return null;

		org.drip.product.credit.BondComponent bond = (org.drip.product.credit.BondComponent)
			cre.getComponent();

		if (null == bond) return null;

		org.drip.param.valuation.ValuationParams valParams = GetValuationParams (cre);

		org.drip.param.definition.ComponentMarketParams cmp = GetCMP (bond, cre, mpc);

		if (null == cmp) return null;

		org.drip.param.pricer.PricerParams pricerParams = GetPricerParams (cre);

		org.drip.param.valuation.QuotingParams quotingParams = GetQuotingParams (cre);

		org.drip.param.definition.Quote qPrice = GetQuote (bond, cmp, mpc, "Price");

		if (null == qPrice) return null;

		double dblAskPrice = 0.01 * qPrice.getQuote ("ASK");

		if (!org.drip.math.common.NumberUtil.IsValid (dblAskPrice)) return null;

		double dblAssetSwapSpread = java.lang.Double.NaN;
		double dblBondBasis = java.lang.Double.NaN;
		double dblConvexity = java.lang.Double.NaN;
		double dblCreditBasis = java.lang.Double.NaN;
		double dblDiscountMargin = java.lang.Double.NaN;
		double dblDuration = java.lang.Double.NaN;
		double dblGSpread = java.lang.Double.NaN;
		double dblISpread = java.lang.Double.NaN;
		double dblOASpread = java.lang.Double.NaN;
		double dblPECS = java.lang.Double.NaN;
		double dblTSYSpread = java.lang.Double.NaN;
		double dblYield = java.lang.Double.NaN;
		double dblZSpread = java.lang.Double.NaN;
		org.drip.analytics.output.BondRVMeasures brvm = null;

		try {
			dblAssetSwapSpread = bond.calcASWFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblBondBasis = bond.calcBondBasisFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblConvexity = bond.calcConvexityFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblCreditBasis = bond.calcCreditBasisFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblDuration = bond.calcDurationFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblDiscountMargin = bond.calcDiscountMarginFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblGSpread = bond.calcGSpreadFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblISpread = bond.calcISpreadFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblOASpread = bond.calcOASFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblPECS = bond.calcPECSFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblYield = bond.calcYieldFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblTSYSpread = bond.calcTSYSpreadFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			dblZSpread = bond.calcZSpreadFromPrice (valParams, cmp, quotingParams, dblAskPrice);
		} catch (java.lang.Exception e) {
			if (s_bWhineOnError) e.printStackTrace();
		}

		try {
			brvm = new org.drip.analytics.output.BondRVMeasures (dblAskPrice, dblBondBasis, dblZSpread,
				dblGSpread, dblISpread, dblOASpread, dblTSYSpread, dblDiscountMargin, dblAssetSwapSpread,
					dblCreditBasis, dblPECS, dblDuration, dblConvexity, new
						org.drip.param.valuation.WorkoutInfo (bond.getMaturityDate().getJulian(), dblYield,
							1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return brvm;
	}

	private static final boolean run (
		final java.net.ServerSocket sockAS,
		final org.drip.param.definition.MarketParams mpc)
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

		org.drip.param.definition.MarketParams mpc = org.drip.service.env.EnvManager.PopulateMPC (stmt,
			org.drip.analytics.date.JulianDate.Today());

		java.net.ServerSocket sockAS = org.drip.param.config.ConfigLoader.InitAnalServer
			("c:\\DRIP\\CreditAnalytics\\Config.xml");

		System.out.println ("Ready ...");

		while (true)
			run (sockAS, mpc);
	}
}
