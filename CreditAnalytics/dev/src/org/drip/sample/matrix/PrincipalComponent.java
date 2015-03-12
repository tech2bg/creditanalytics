
package org.drip.sample.matrix;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.linearalgebra.*;
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
 * PrincipalComponent demonstrates how to generate the Principal eigenvalue and eigenvector for the Input
 *  Matrix.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PrincipalComponent {

	private static final void PrincipalComponentRun (
		final PowerIterationComponentExtractor pice)
		throws Exception
	{
		double dblCorr1 = 0.5 * Math.random();

		double dblCorr2 = 0.5 * Math.random();

		double[][] aadblA = {
			{     1.0, dblCorr1,      0.0},
			{dblCorr1,      1.0, dblCorr2},
			{     0.0, dblCorr2,      1.0}
		};

		EigenComponent ec = pice.principalComponent (aadblA);

		double[] adblEigenvector = ec.eigenvector();

		java.lang.String strDump = "[" + FormatUtil.FormatDouble (ec.eigenvalue(), 1, 4, 1.) + "] => ";

		for (int i = 0; i < adblEigenvector.length; ++i)
			strDump += FormatUtil.FormatDouble (adblEigenvector[i], 1, 4, 1.) + " | ";

		System.out.println ("\tPrincipal Component => " + strDump);
	}

	public static final void main (
		final String[] astrArg)
		throws Exception
	{
		CreditAnalytics.Init ("");

		PowerIterationComponentExtractor pice = new PowerIterationComponentExtractor (
			30,
			0.001,
			false
		);

		for (int i = 0; i < 100; ++i)
			PrincipalComponentRun (pice);
	}
}
