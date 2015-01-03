
package org.drip.regression.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * CreditAnalyticsRegressionEngine implements the RegressionEngine for the curve regression. It adds the
 * 	CreditCurveRegressor, DiscountCurveRegressor, FXCurveRegressor, and ZeroCurveRegressor, and launches the
 * 	regression engine.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsRegressionEngine extends org.drip.regression.core.RegressionEngine {

	/**
	 * Initialize the Credit Analytics Regression Engine
	 * 
	 * @param iNumRuns Number of runs to be initialized with
	 * @param iRegressionDetail Detail of the regression run
	 * 
	 * @throws Exception Thrown from the super
	 */

	public CreditAnalyticsRegressionEngine (
		final int iNumRuns,
		final int iRegressionDetail)
		throws java.lang.Exception
	{
		super (iNumRuns, iRegressionDetail);
	}

	@Override public boolean initRegressionEnv()
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		return super.initRegressionEnv();
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		CreditAnalyticsRegressionEngine care = new CreditAnalyticsRegressionEngine (10,
			org.drip.regression.core.RegressionEngine.REGRESSION_DETAIL_MODULE_UNIT_DECOMPOSED);

		/*
		 * Add the regressor sets: Refer to the implementation of the corresponding regressors
		 */

		care.addRegressorSet (new CreditCurveRegressor());

		care.addRegressorSet (new DiscountCurveRegressor());

		care.addRegressorSet (new FXCurveRegressor());

		care.addRegressorSet (new ZeroCurveRegressor());

		/*
		 * Launch regression - and that's it!
		 */

		care.launch();
	}
}
