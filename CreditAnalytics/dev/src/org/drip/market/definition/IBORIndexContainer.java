
package org.drip.market.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * IBORIndexContainer holds the definitions of the IBOR index definitions corresponding to the different
 *  jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IBORIndexContainer {
	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.IBORIndex>
			_mapJurisdictionIBORIndex = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.IBORIndex>();

	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.IBORIndex>
			_mapNamedIBORIndex = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.IBORIndex>();

	/**
	 * Initialize the IBOR Index Container with the Overnight Indexes
	 * 
	 * @return TRUE => The IBOR Index Container successfully initialized with the indexes
	 */

	public static final boolean Init()
	{
		try {
			org.drip.market.definition.IBORIndex iborAUD = new org.drip.market.definition.IBORIndex ("BBSW",
				"AUD", "Act/365", "AUD", 0, "1M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("AUD", iborAUD);

			_mapNamedIBORIndex.put ("AUDLIBOR", iborAUD);

			_mapNamedIBORIndex.put ("BBSW", iborAUD);

			org.drip.market.definition.IBORIndex iborCAD = new org.drip.market.definition.IBORIndex ("CDOR",
				"CAD", "Act/365", "CAD", 0, "1M", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("CAD", iborCAD);

			_mapNamedIBORIndex.put ("CADLIBOR", iborCAD);

			_mapNamedIBORIndex.put ("CDOR", iborCAD);

			org.drip.market.definition.IBORIndex iborCHF = new org.drip.market.definition.IBORIndex
				("CHFLIBOR", "CHF", "Act/360", "CHF", 2, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("CHF", iborCHF);

			_mapNamedIBORIndex.put ("CHFLIBOR", iborCHF);

			org.drip.market.definition.IBORIndex iborCZK = new org.drip.market.definition.IBORIndex
				("PRIBOR", "CZK", "Act/360", "CZK", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("CZK", iborCZK);

			_mapNamedIBORIndex.put ("CZKLIBOR", iborCZK);

			_mapNamedIBORIndex.put ("PRIBOR", iborCZK);

			org.drip.market.definition.IBORIndex iborDKK = new org.drip.market.definition.IBORIndex ("CIBOR",
				"DKK", "Act/360", "DKK", 2, "1W", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("DKK", iborDKK);

			_mapNamedIBORIndex.put ("CIBOR", iborDKK);

			_mapNamedIBORIndex.put ("DKKLIBOR", iborDKK);

			org.drip.market.definition.IBORIndex iborEUR = new org.drip.market.definition.IBORIndex
				("EURIBOR", "EUR", "Act/360", "EUR", 2, "1W", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("EUR", iborEUR);

			_mapNamedIBORIndex.put ("EURIBOR", iborEUR);

			org.drip.market.definition.IBORIndex iborEUR2 = new org.drip.market.definition.IBORIndex
				("EURIBOR", "EUR", "Act/365", "EUR", 2, "1W", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapNamedIBORIndex.put ("EURIBOR2", iborEUR2);

			org.drip.market.definition.IBORIndex iborEUR3 = new org.drip.market.definition.IBORIndex
				("EURLIBOR", "EUR", "Act/360", "EUR", 2, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapNamedIBORIndex.put ("EURLIBOR", iborEUR3);

			org.drip.market.definition.IBORIndex iborEUR4 = new org.drip.market.definition.IBORIndex
				("EURLIBOR", "EUR", "Act/360", "EUR", 0, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapNamedIBORIndex.put ("EURLIBOR2", iborEUR4);

			org.drip.market.definition.IBORIndex iborGBP = new org.drip.market.definition.IBORIndex
				("GBPLIBOR", "GBP", "Act/365", "GBP", 0, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("GBP", iborGBP);

			_mapNamedIBORIndex.put ("GBPLIBOR", iborGBP);

			org.drip.market.definition.IBORIndex iborHKD = new org.drip.market.definition.IBORIndex ("HIBOR",
				"HKD", "Act/365", "HKD", 2, "1M", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("HKD", iborHKD);

			_mapNamedIBORIndex.put ("HIBOR", iborHKD);

			_mapNamedIBORIndex.put ("HKDLIBOR", iborHKD);

			org.drip.market.definition.IBORIndex iborHUF = new org.drip.market.definition.IBORIndex ("BUBOR",
				"HUF", "Act/360", "HUF", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("HUF", iborHUF);

			_mapNamedIBORIndex.put ("BUBOR", iborHUF);

			_mapNamedIBORIndex.put ("HUFLIBOR", iborHUF);

			org.drip.market.definition.IBORIndex iborIDR = new org.drip.market.definition.IBORIndex
				("IDRFIX", "IDR", "Act/360", "IDR", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("IDR", iborIDR);

			_mapNamedIBORIndex.put ("IDRFIX", iborIDR);

			_mapNamedIBORIndex.put ("IDRLIBOR", iborIDR);

			org.drip.market.definition.IBORIndex iborINR = new org.drip.market.definition.IBORIndex ("MIFOR",
				"INR", "Act/365", "INR", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("INR", iborINR);

			_mapNamedIBORIndex.put ("INRLIBOR", iborINR);

			_mapNamedIBORIndex.put ("MIFOR", iborINR);

			org.drip.market.definition.IBORIndex iborJPY = new org.drip.market.definition.IBORIndex
				("JPYLIBOR", "JPY", "Act/360", "JPY", 2, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("JPY", iborJPY);

			_mapNamedIBORIndex.put ("JPYLIBOR", iborJPY);

			org.drip.market.definition.IBORIndex iborJPYTIBOR = new org.drip.market.definition.IBORIndex
				("Japan TIBOR", "JPY", "Act/365", "JPY", 2, "1W", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapNamedIBORIndex.put ("JPYTIBOR", iborJPYTIBOR);

			_mapNamedIBORIndex.put ("TIBOR", iborJPYTIBOR);

			org.drip.market.definition.IBORIndex iborJPYEuroyen = new org.drip.market.definition.IBORIndex
				("Euroyen TIBOR", "JPY", "Act/360", "JPY", 2, "1W", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapNamedIBORIndex.put ("EUROYEN", iborJPYEuroyen);

			_mapNamedIBORIndex.put ("EURTIBOR", iborJPYEuroyen);

			org.drip.market.definition.IBORIndex iborNOK = new org.drip.market.definition.IBORIndex ("NIBOR",
				"NOK", "Act/360", "NOK", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("NOK", iborNOK);

			_mapNamedIBORIndex.put ("NIBOR", iborNOK);

			_mapNamedIBORIndex.put ("NOKLIBOR", iborNOK);

			org.drip.market.definition.IBORIndex iborNZD = new org.drip.market.definition.IBORIndex ("BBR",
				"NZD", "Act/365", "NZD", 0, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("NZD", iborNZD);

			_mapNamedIBORIndex.put ("BBR", iborNZD);

			_mapNamedIBORIndex.put ("NZDLIBOR", iborNZD);

			org.drip.market.definition.IBORIndex iborPLN = new org.drip.market.definition.IBORIndex ("WIBOR",
				"PLN", "Act/365", "PLN", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("PLN", iborPLN);

			_mapNamedIBORIndex.put ("PLNLIBOR", iborPLN);

			_mapNamedIBORIndex.put ("WIBOR", iborPLN);

			org.drip.market.definition.IBORIndex iborRMB = new org.drip.market.definition.IBORIndex
				("SHIBOR", "RMB", "Act/360", "RMB", 0, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("RMB", iborRMB);

			_mapNamedIBORIndex.put ("RMBLIBOR", iborRMB);

			_mapNamedIBORIndex.put ("SHIBOR", iborRMB);

			org.drip.market.definition.IBORIndex iborSEK = new org.drip.market.definition.IBORIndex
				("STIBOR", "SEK", "Act/360", "SEK", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("SEK", iborSEK);

			_mapNamedIBORIndex.put ("SEKLIBOR", iborSEK);

			_mapNamedIBORIndex.put ("STIBOR", iborSEK);

			org.drip.market.definition.IBORIndex iborSGD = new org.drip.market.definition.IBORIndex ("SIBOR",
				"SGD", "Act/365", "SGD", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("SGD", iborSGD);

			_mapNamedIBORIndex.put ("SGDLIBOR", iborSGD);

			_mapNamedIBORIndex.put ("SIBOR", iborSGD);

			_mapNamedIBORIndex.put ("SOR", iborSGD);

			org.drip.market.definition.IBORIndex iborSKK = new org.drip.market.definition.IBORIndex
				("BRIBOR", "SKK", "Act/360", "SKK", 2, "", "",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("SKK", iborSKK);

			_mapNamedIBORIndex.put ("BRIBOR", iborSKK);

			_mapNamedIBORIndex.put ("SKKLIBOR", iborSKK);

			org.drip.market.definition.IBORIndex iborUSD = new org.drip.market.definition.IBORIndex
				("USDLIBOR", "USD", "Act/360", "USD", 2, "ON", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("USD", iborUSD);

			_mapNamedIBORIndex.put ("LIBOR", iborUSD);

			_mapNamedIBORIndex.put ("USDLIBOR", iborUSD);

			org.drip.market.definition.IBORIndex iborZAR = new org.drip.market.definition.IBORIndex ("JIBAR",
				"ZAR", "Act/365", "ZAR", 0, "1M", "12M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

			_mapJurisdictionIBORIndex.put ("ZAR", iborZAR);

			_mapNamedIBORIndex.put ("JIBAR", iborZAR);

			_mapNamedIBORIndex.put ("ZARLIBOR", iborZAR);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the IBOR Index from the Jurisdiction Name
	 * 
	 * @param strName The IBOR Index Jurisdiction Name
	 * 
	 * @return The IBOR Index
	 */

	public static final org.drip.market.definition.IBORIndex IndexFromJurisdiction (
		final java.lang.String strName)
	{
		return _mapJurisdictionIBORIndex.containsKey (strName) ? _mapJurisdictionIBORIndex.get (strName) :
			null;
	}

	/**
	 * Retrieve the IBOR Index from the Index Name
	 * 
	 * @param strName The IBOR Index Index Name
	 * 
	 * @return The IBOR Index
	 */

	public static final org.drip.market.definition.IBORIndex IndexFromName (
		final java.lang.String strName)
	{
		return _mapNamedIBORIndex.containsKey (strName) ? _mapNamedIBORIndex.get (strName) : null;
	}
}
