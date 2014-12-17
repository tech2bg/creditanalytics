
package org.drip.sample.forward;

import org.drip.market.definition.*;
import org.drip.service.api.CreditAnalytics;

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
 * IBORIndexDefinition demonstrates the functionality to retrieve the IBOR settings for the various
 *  Jurisdictions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IBORIndexDefinition {
	private static final void DisplayNameOvernightSetting (
		final String strName)
	{
		IBORIndex index = IBORIndexContainer.IndexFromName (strName);

		String strLongestMaturity = index.longestMaturity();

		String strShortestMaturity = index.shortestMaturity();

		System.out.println ("\t[" +
			index.currency() + "] => " +
			index.dayCount() + " | " +
			index.spotLag() + " | " +
			(strShortestMaturity.isEmpty() ? "  " : strShortestMaturity) + " | " + 
			(strLongestMaturity.isEmpty() ? "   " : strLongestMaturity) + " | " + 
			index.name()
		);
	}

	public static final void main (
		String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayNameOvernightSetting ("CHFLIBOR");

		DisplayNameOvernightSetting ("EURIBOR");

		DisplayNameOvernightSetting ("EURLIBOR");

		DisplayNameOvernightSetting ("GBPLIBOR");

		DisplayNameOvernightSetting ("JPYLIBOR");

		DisplayNameOvernightSetting ("USDLIBOR");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayNameOvernightSetting ("AUDLIBOR");

		DisplayNameOvernightSetting ("CADLIBOR");

		DisplayNameOvernightSetting ("CZKLIBOR");

		DisplayNameOvernightSetting ("DKKLIBOR");

		DisplayNameOvernightSetting ("HKDLIBOR");

		DisplayNameOvernightSetting ("HUFLIBOR");

		DisplayNameOvernightSetting ("IDRLIBOR");

		DisplayNameOvernightSetting ("INRLIBOR");

		DisplayNameOvernightSetting ("NOKLIBOR");

		DisplayNameOvernightSetting ("NZDLIBOR");

		DisplayNameOvernightSetting ("PLNLIBOR");

		DisplayNameOvernightSetting ("RMBLIBOR");

		DisplayNameOvernightSetting ("SGDLIBOR");

		DisplayNameOvernightSetting ("SEKLIBOR");

		DisplayNameOvernightSetting ("SKKLIBOR");

		DisplayNameOvernightSetting ("ZARLIBOR");
	}
}
