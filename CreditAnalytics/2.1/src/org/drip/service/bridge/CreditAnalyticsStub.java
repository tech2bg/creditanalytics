
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

		org.drip.param.definition.Quote qPrice = GetQuote (bond, cmp, mpc, "Price");

		if (null == qPrice) return null;

		double dblAskPrice = 0.01 * qPrice.getQuote ("ASK");

		if (!org.drip.math.common.NumberUtil.IsValid (dblAskPrice)) return null;

		org.drip.param.valuation.WorkoutInfo wi = bond.calcExerciseYieldFromPrice (valParams, cmp, null,
			dblAskPrice);

		return null == wi ? null : bond.standardMeasures (valParams, null, cmp, null, wi, dblAskPrice);
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
