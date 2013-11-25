
package org.drip.sample.spline;

import org.drip.spline.bspline.*;
import org.drip.quant.common.FormatUtil;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * BasisMonicBSpline implements the comparison of the basis hat functions used in the construction of the
 *  monic basis B Splines. It demonstrates the following:
 * 	- Construction of the Linear Cubic Rational Raw Hat Functions
 * 	- Construction of the Quadratic Cubic Rational Raw Hat Functions
 * 	- Construction of the Corresponding Processed Tension Basis Hat Functions
 * 	- Construction of the Wrapping Monic Functions
 * 	- Estimation and Comparison of the Ordered Derivatives
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisMonicHatComparison {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CubicRationalLeftRaw crlr = new CubicRationalLeftRaw (CubicRationalLeftRaw.SHAPE_CONTROL_RATIONAL_LINEAR, 1., 1., 2.);

		CubicRationalRightRaw crrr = new CubicRationalRightRaw (CubicRationalRightRaw.SHAPE_CONTROL_RATIONAL_LINEAR, 1., 2., 3.);

		TensionProcessedBasisHat tpbhLeft = new TensionProcessedBasisHat (crlr, 2);

		TensionProcessedBasisHat tpbhRight = new TensionProcessedBasisHat (crrr, 2);

		SegmentMonicBasisFunction smbf = new SegmentMonicBasisFunction (tpbhLeft, tpbhRight);

		double dblX = crlr.left();

		while (dblX <= crrr.right()) {
			System.out.println ("Deriv[" + dblX + "] => " +
				FormatUtil.FormatDouble (smbf.calcDerivative (dblX, 1), 1, 5, 1.));

			System.out.println ("\tCubic Rational Left Deriv[" + dblX + "]  => " +
				FormatUtil.FormatDouble (crlr.calcDerivative (dblX, 3), 1, 5, 1.));

			System.out.println ("\tCubic Rational Right Deriv[" + dblX + "] => " +
				FormatUtil.FormatDouble (crrr.calcDerivative (dblX, 3), 1, 5, 1.));

			System.out.println ("\tTPBH Left Deriv[" + dblX + "]  => " +
				FormatUtil.FormatDouble (tpbhLeft.calcDerivative (dblX, 1), 1, 5, 1.));

			System.out.println ("\tTPBH Right Deriv[" + dblX + "] => " +
				FormatUtil.FormatDouble (tpbhRight.calcDerivative (dblX, 1), 1, 5, 1.));

			dblX += 0.5;
		}
	}
}
