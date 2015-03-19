
package org.drip.sample.matrix;

import org.drip.quant.common.*;
import org.drip.quant.eigen.*;
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
 * Eigenization demonstrates how to generate the eigenvalue and eigenvector for the Input Matrix.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Eigenization {

	private static final void EigenRun (
		final QREigenComponentExtractor qrece)
	{
		double dblCorr1 = 0.5 * Math.random();

		double dblCorr2 = 0.5 * Math.random();

		double[][] aadblA = {
			{     1.0, dblCorr1,      0.0},
			{dblCorr1,      1.0, dblCorr2},
			{     0.0, dblCorr2,      1.0}
		};

		EigenOutput eo = qrece.eigenize (aadblA);

		if (null == eo) return;

		System.out.println ("\n\t|----------------------------------------|");

		System.out.println ("\t|-----------" + FormatUtil.FormatDouble (dblCorr1, 1, 4, 1.) + " ||| " + FormatUtil.FormatDouble (dblCorr2, 1, 4, 1.) + " ---------|");

		System.out.println ("\t|----------------------------------------|");

		for (int i = 0; i < aadblA.length; ++i) {
			java.lang.String strDump = "\t[" + FormatUtil.FormatDouble (eo.eigenvalue()[i], 1, 4, 1.) + "] => ";

			for (int j = 0; j < aadblA.length; ++j)
				strDump += FormatUtil.FormatDouble (eo.eigenvector()[i][j], 1, 4, 1.) + " | ";

			System.out.println (strDump);
		}

		EigenComponent ec = qrece.principalComponent (aadblA);

		double[] adblEigenvector = ec.eigenvector();

		java.lang.String strDump = "[" + FormatUtil.FormatDouble (ec.eigenvalue(), 1, 4, 1.) + "] => ";

		for (int i = 0; i < adblEigenvector.length; ++i)
			strDump += FormatUtil.FormatDouble (adblEigenvector[i], 1, 4, 1.) + " | ";

		System.out.println ("\t" + strDump);

		System.out.println ("\t|----------------------------------------|");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		QREigenComponentExtractor qrece = new QREigenComponentExtractor (
			50,
			0.00001
		);

		int iNumRun = 10;

		for (int iRun = 0; iRun < iNumRun; ++iRun)
			EigenRun (qrece);
	}
}
