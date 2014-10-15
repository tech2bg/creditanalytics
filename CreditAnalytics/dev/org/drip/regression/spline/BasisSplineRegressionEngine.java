
package org.drip.regression.spline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * BasisSplineRegressionEngine implements the RegressionEngine class for the basis spline functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineRegressionEngine extends org.drip.regression.core.RegressionEngine {
	public BasisSplineRegressionEngine (
		final int iNumRuns,
		final int iRegressionDetail)
		throws java.lang.Exception
	{
		super (iNumRuns, iRegressionDetail);
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		BasisSplineRegressionEngine bsre = new BasisSplineRegressionEngine (12,
			org.drip.regression.core.RegressionEngine.REGRESSION_DETAIL_MODULE_UNIT_DECOMPOSED |
				org.drip.regression.core.RegressionEngine.REGRESSION_DETAIL_STATS);

		bsre.addRegressorSet (new org.drip.regression.spline.BasisSplineRegressorSet());

		bsre.launch();
	}
}
