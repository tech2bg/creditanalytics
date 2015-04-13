
package org.drip.spline.tension;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * This class implements the basic framework and the family of C2 Tension Splines outlined in Koch and Lyche
 * 	(1989), Koch and Lyche (1993), and Kvasov (2000) Papers.
 * 
 * Currently, this class exposes functions to create monic and quadratic tension B Spline Basis Function Set.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class KochLycheKvasovBasis {

	/**
	 * Generate the Monic BSpline Basis Function Set
	 * 
	 * @param dblTension The Tension Parameter
	 * 
	 * @return The Monic BSpline Basis Function Set
	 */

	public static final org.drip.function.deterministic.R1ToR1[] GenerateMonicBSplineSet (
		final double dblTension)
	{
		try {
			return new org.drip.function.deterministic.R1ToR1[] {new
				org.drip.spline.tension.KLKHyperbolicTensionPhy (dblTension), new
					org.drip.spline.tension.KLKHyperbolicTensionPsy (dblTension)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the Quadratic BSpline Basis Function Set
	 * 
	 * @param dblTension The Tension Parameter
	 * 
	 * @return The Quadratic BSpline Basis Function Set
	 */

	public static final org.drip.function.deterministic.R1ToR1[] GenerateQuadraticBSplineSet (
		final double dblTension)
	{
		try {
			return new org.drip.function.deterministic.R1ToR1[] {new
				org.drip.spline.tension.KLKHyperbolicTensionPhy (dblTension), new
					org.drip.spline.tension.KLKHyperbolicTensionPsy (dblTension)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
