
package org.drip.regression.curveJacobian;

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
 * CurveJacobianRegressionEngine implements the RegressionEngine for the curve Jacobian regression. It adds
 *  the CashJacobianRegressorSet, the EDFJacobianRegressorSet, the IRSJacobianRegressorSet, and the
 *  DiscountCurveJacobianRegressorSet, and launches the regression engine.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurveJacobianRegressionEngine extends org.drip.regression.core.RegressionEngine {

	/**
	 * CurveJacobianRegressionEngine constructor
	 * 
	 * @param iNumRuns Number of regression runs
	 * @param iRegressionDetail Detailed desired of the regression run
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CurveJacobianRegressionEngine (
		final int iNumRuns,
		final int iRegressionDetail)
		throws java.lang.Exception
	{
		super (iNumRuns, iRegressionDetail);
	}

	@Override public boolean initRegressionEnv()
	{
		if (!super.initRegressionEnv()) return false;

		org.drip.analytics.support.Logger.Init ("c:\\DRIP\\CreditAnalytics\\Config.xml");

		org.drip.analytics.daycount.Convention.Init ("c:\\DRIP\\CreditAnalytics\\Config.xml");

		return true;
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		CurveJacobianRegressionEngine cjre = new CurveJacobianRegressionEngine (10,
			org.drip.regression.core.RegressionEngine.REGRESSION_DETAIL_MODULE_UNIT_DECOMPOSED |
				org.drip.regression.core.RegressionEngine.REGRESSION_DETAIL_STATS);

		cjre.addRegressorSet (new org.drip.regression.curveJacobian.CashJacobianRegressorSet());

		cjre.addRegressorSet (new org.drip.regression.curveJacobian.EDFJacobianRegressorSet());

		cjre.addRegressorSet (new org.drip.regression.curveJacobian.IRSJacobianRegressorSet());

		cjre.addRegressorSet (new org.drip.regression.curveJacobian.DiscountCurveJacobianRegressorSet());

		cjre.launch();
	}
}
