
package org.drip.sample.ccbs;

import org.drip.market.otc.*;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * OTCFloatFloatDefinitions contains all the pre-fixed Definitions of the OTC Cross-Currency Float-Float Swap
 * 	Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCCrossCurrencyDefinitions {
	public static final void main (
		String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\tL -> R:");

		System.out.println ("\t\t\tReference Currency");

		System.out.println ("\t\t\tReference Tenor");

		System.out.println ("\t\t\tQuote Basis on Reference");

		System.out.println ("\t\t\tDerived Currency");

		System.out.println ("\t\t\tDerived Tenor");

		System.out.println ("\t\t\tQuote Basis on Derived");

		System.out.println ("\t\t\tFixing Setting Type");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("AUD"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("CAD"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("CHF"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("CLP"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("DKK"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("EUR"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("GBP"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("JPY"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("MXN"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("NOK"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("PLN"));

		System.out.println ("\t\t" + CrossFloatConventionContainer.ConventionFromJurisdiction ("SEK"));

		System.out.println ("\t--------------------------------------------------------------------------------------------------------");
	}
}
