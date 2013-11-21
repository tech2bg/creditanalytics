
package org.drip.spline.bspline;

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
 * BasisHatPairGenerator implements the generation functionality behind the hat basis function pair.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisHatPairGenerator {
	public static final org.drip.spline.bspline.TensionBasisHat[] GenerateHyperbolicTensionMonic (
		final double[] adblPredictorOrdinate,
		final double dblTension)
	{
		if (null == adblPredictorOrdinate) return null;

		int iNumPredictorOrdinate = adblPredictorOrdinate.length;

		if (3 == iNumPredictorOrdinate) {
			try {
				return new org.drip.spline.bspline.TensionBasisHat[] {new
					org.drip.spline.bspline.ExponentialTensionLeftHat (dblTension, adblPredictorOrdinate[0],
						adblPredictorOrdinate[1]), new org.drip.spline.bspline.ExponentialTensionRightHat
							(dblTension, adblPredictorOrdinate[1], adblPredictorOrdinate[2])};
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
