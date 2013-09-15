
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
 * LocalControlBasisSplineRegressor implements the local control basis spline regressor for the given basis
 *  spline. As part of the regression run, it executes the following:
 *  - Calibrate and compute the left and the right Jacobian.
 *  - Insert the Local Control Hermite, Cardinal, and Catmull-Rom knots.
 *  - Run Regressor for the C1 Local Control C1 Slope Insertion Bessel/Hermite Spline.
 *  - Compute an intermediate value Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LocalControlBasisSplineRegressor extends org.drip.regression.core.UnitRegressionExecutor {
	private org.drip.math.grid.MultiSegmentRegime _regime = null;
	private org.drip.math.grid.MultiSegmentRegime _regimeBesselHermite = null;
	private org.drip.math.grid.MultiSegmentRegime _regimeHermiteInsert = null;
	private org.drip.math.grid.MultiSegmentRegime _regimeCardinalInsert = null;
	private org.drip.math.grid.MultiSegmentRegime _regimeCatmullRomInsert = null;

	private final boolean DumpRNVD (
		final java.lang.String strRegimeName,
		final org.drip.math.grid.MultiSegmentRegime regime,
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		double dblX = 0.;
		double dblXMax = 4.;

		while (dblX <= dblXMax) {
			try {
				if (!rnvd.set (getName() + "_" + strRegimeName + "_" + dblX,
					org.drip.math.common.FormatUtil.FormatDouble (regime.response (dblX), 1, 2, 1.) + " | " +
						regime.monotoneType (dblX)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			if (!rnvd.set (getName() + "_" + strRegimeName + "_" + dblX + "_Jack",
				regime.jackDResponseDResponseInput (dblX).displayString()))
				return false;

			dblX += 0.5;
		}

		return true;
	}

	/**
	 * LocalControlBasisSplineRegressor constructor
	 * 
	 * @param strName Regressor Name
	 * @param strScenarioName Regression Scenario Name
	 * @param strBasisSpline Basis Spline
	 * @param bsp Basis Set Parameters
	 * @param iCk Continuity Ck
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public LocalControlBasisSplineRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName,
		final java.lang.String strBasisSpline,
		final org.drip.math.spline.BasisSetParams bsp,
		final int iCk)
		throws java.lang.Exception
	{
		super (strName, strScenarioName);

		double[] adblX = new double[] {0.00, 1.00,  2.00,  3.00,  4.00};
		int iNumSegment = adblX.length - 1;
		org.drip.math.segment.PredictorResponseBuilderParams[] aSBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumSegment];

		for (int i = 0; i < iNumSegment; ++i)
			aSBP[i] = new org.drip.math.segment.PredictorResponseBuilderParams (strBasisSpline, bsp, new
				org.drip.math.segment.DesignInelasticParams (iCk, 1), new
					org.drip.math.function.RationalShapeControl (1.));

		if (null == (_regime = org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeEstimator
			("SPLINE_REGIME", adblX, aSBP)))
			throw new java.lang.Exception ("LocalControlBasisSplineRegressor ctr: Cannot Construct Regime!");
	}

	@Override public boolean preRegression()
	{
		double[] adblY = new double[] {1.00, 4.00, 15.00, 40.00, 85.00};
		double[] adblDYDX = new double[] {1.00, 6.00, 17.00, 34.00, 57.00};

		org.drip.math.segment.PredictorResponseBuilderParams prbp = null;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aSEPLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[adblY.length - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aSEPRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[adblY.length - 1];

		for (int i = 0; i < adblY.length - 1; ++i) {
			try {
				aSEPLeft[i] = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblY[i], new
					double[] {adblDYDX[i]});

				aSEPRight[i] = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblY[i + 1],
					new double[] {adblDYDX[i + 1]});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		try {
			prbp = new org.drip.math.segment.PredictorResponseBuilderParams
				(org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.math.spline.PolynomialBasisSetParams (4), new
						org.drip.math.segment.DesignInelasticParams (2, 2), new
							org.drip.math.function.RationalShapeControl (1.));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		org.drip.math.segment.PredictorResponseBuilderParams[] aSBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[adblY.length - 1]; 

		for (int i = 0; i < adblY.length - 1; ++i)
			aSBP[i] = prbp;

		if (null == (_regimeBesselHermite = org.drip.math.grid.RegimeBuilder.CreateBesselCubicSplineRegime
			("BESSEL_REGIME", new double[] {0.00, 1.00,  2.00,  3.00,  4.00}, adblY, aSBP,
				org.drip.math.grid.RegimeCalibrationSetting.CALIBRATE)))
			return false;

		return _regime.setupHermite (aSEPLeft, aSEPRight, null,
			org.drip.math.grid.RegimeCalibrationSetting.CALIBRATE_JACOBIAN);
	}

	@Override public boolean execRegression()
	{
		try {
			if (null == (_regimeHermiteInsert = org.drip.math.grid.RegimeModifier.InsertKnot (_regime, 2.5,
				new org.drip.math.segment.PredictorOrdinateResponseDerivative (27.5, new double[] {25.5}),
					new org.drip.math.segment.PredictorOrdinateResponseDerivative (27.5, new double[]
						{25.5}))))
				return false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (null == (_regimeCardinalInsert = org.drip.math.grid.RegimeModifier.InsertCardinalKnot (_regime,
			2.5, 0.)))
			return false;

		return null != (_regimeCatmullRomInsert = org.drip.math.grid.RegimeModifier.InsertCatmullRomKnot
			(_regime, 2.5));
	}

	@Override public boolean postRegression (
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		return DumpRNVD ("LOCAL_NO_KNOT", _regime, rnvd) && DumpRNVD ("LOCAL_HERMITE_KNOT",
			_regimeHermiteInsert, rnvd) && DumpRNVD ("LOCAL_CARDINAL_KNOT", _regimeCardinalInsert, rnvd) &&
				DumpRNVD ("LOCAL_CATMULL_ROM_KNOT", _regimeCatmullRomInsert, rnvd) && DumpRNVD
					("LOCAL_C1_BESSEL_HERMITE", _regimeBesselHermite, rnvd);
	}
}
