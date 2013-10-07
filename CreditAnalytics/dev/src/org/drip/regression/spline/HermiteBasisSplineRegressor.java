
package org.drip.regression.spline;
/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * HermiteBasisSplineRegressor implements the Hermite basis spline regressor for the given basis spline. As
 *  part of the regression run, it executes the following:
 *  - Calibrate and compute the left and the right Jacobian.
 *  - Reset right node and re-run calibration.
 *  - Compute an intermediate value Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HermiteBasisSplineRegressor extends org.drip.regression.spline.BasisSplineRegressor {
	private java.lang.String _strName = "";
	private org.drip.math.segment.PredictorResponse _seg1 = null;
	private org.drip.math.segment.PredictorResponse _seg2 = null;
	private org.drip.math.calculus.WengertJacobian _wjLeft = null;
	private org.drip.math.calculus.WengertJacobian _wjRight = null;
	private org.drip.math.calculus.WengertJacobian _wjValue = null;

	/**
	 * Creates an instance of Hermite BasisSplineRegressor
	 * 
	 * @param strName Regressor Name
	 * @param strScenarioName Regressor Scenario Name
	 * @param iNumBasis Number of Basis Functions
	 * @param iCk Ck
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final org.drip.regression.spline.BasisSplineRegressor CreateHermiteSplineRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName,
		final int iNumBasis,
		final int iCk)
	{
		try {
			org.drip.math.function.AbstractUnivariate[] aAU =
				org.drip.math.spline.BasisSetBuilder.PolynomialBasisSet (new
					org.drip.math.spline.PolynomialBasisSetParams (iNumBasis));

			return null == aAU ? null : new HermiteBasisSplineRegressor (strName, strScenarioName, aAU, iCk);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private HermiteBasisSplineRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName,
		final org.drip.math.function.AbstractUnivariate[] aAU,
		final int iCk)
		throws java.lang.Exception
	{
		super (strName, strScenarioName, aAU, iCk);

		org.drip.math.segment.DesignInelasticParams segParams = new
			org.drip.math.segment.DesignInelasticParams (iCk, 1);

		org.drip.math.segment.ResponseScalingShapeController rssc = new
			org.drip.math.segment.ResponseScalingShapeController (true, new
				org.drip.math.function.RationalShapeControl (1.));

		if (null == (_seg1 = org.drip.math.segment.LocalBasisPredictorResponse.Create (0.0, 1.0, aAU, rssc,
			segParams)) || null == (_seg2 = org.drip.math.segment.LocalBasisPredictorResponse.Create (1.0,
				2.0, aAU, rssc, segParams)))
			throw new java.lang.Exception ("HermiteBasisSplineRegressor ctr: Cant create the segments");
	}

	@Override public boolean execRegression()
	{
		try {
			return null != (_wjLeft = _seg1.jackDCoeffDEdgeParams (new
				org.drip.math.segment.CalibrationParams (new double[] {0., 1.}, new double[] {1., 4.},
					new double[] {1.}, new double[] {6.}, null))) && null != (_wjRight =
						_seg2.jackDCoeffDEdgeParams (new org.drip.math.segment.CalibrationParams (new
							double[] {0., 1.}, new double[] {4., 15.}, new double[] {6.}, new double[] {17.},
								null))) && _seg2.calibrate (_seg1, 14.) && null != (_wjValue =
									_seg2.jackDResponseDEdgeParams (1.5));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public boolean postRegression (
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		try {
			if (!rnvd.set (_strName + "_Seg1_0_0", "" + _seg1.responseValue (0.))) return false;

			if (!rnvd.set (_strName + "_Seg1_1_0", "" + _seg1.responseValue (1.))) return false;

			if (!rnvd.set (_strName + "_Seg1_Jack", _wjLeft.displayString()));

			if (!rnvd.set (_strName + "_Seg1_Head_Jack", _seg1.jackDCoeffDEdgeParams().displayString()));

			if (!rnvd.set (_strName + "_Seg1_Monotone", _seg1.monotoneType().toString()));

			if (!rnvd.set (_strName + "_Seg2_1_0", "" + _seg2.responseValue (1.))) return false;

			if (!rnvd.set (_strName + "_Seg2_2_0", "" + _seg2.responseValue (2.))) return false;

			if (!rnvd.set (_strName + "_Seg2_Jack", _wjRight.displayString()));

			if (!rnvd.set (_strName + "_Seg2_Head_Jack", _seg2.jackDCoeffDEdgeParams().displayString()));

			if (!rnvd.set (_strName + "_Seg2_Monotone", _seg2.monotoneType().toString()));

			return rnvd.set (_strName + "_Seg2_Value_Jack", _wjValue.displayString());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
