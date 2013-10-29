
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
	private org.drip.spline.regime.MultiSegmentSequence _regime = null;
	private org.drip.spline.regime.MultiSegmentSequence _regimeBesselHermite = null;
	private org.drip.spline.regime.MultiSegmentSequence _regimeHermiteInsert = null;
	private org.drip.spline.regime.MultiSegmentSequence _regimeCardinalInsert = null;
	private org.drip.spline.regime.MultiSegmentSequence _regimeCatmullRomInsert = null;

	private final boolean DumpRNVD (
		final java.lang.String strRegimeName,
		final org.drip.spline.regime.MultiSegmentSequence regime,
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		double dblX = 0.;
		double dblXMax = 4.;

		while (dblX <= dblXMax) {
			try {
				if (!rnvd.set (getName() + "_" + strRegimeName + "_" + dblX,
					org.drip.quant.common.FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
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
		final org.drip.spline.basis.FunctionSetBuilderParams bsp,
		final int iCk)
		throws java.lang.Exception
	{
		super (strName, strScenarioName);

		double[] adblX = new double[] {0.00, 1.00,  2.00,  3.00,  4.00};
		int iNumSegment = adblX.length - 1;
		org.drip.spline.params.SegmentCustomBuilderControl[] aSBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumSegment];

		for (int i = 0; i < iNumSegment; ++i)
			aSBP[i] = new org.drip.spline.params.SegmentCustomBuilderControl (strBasisSpline, bsp,
				org.drip.spline.params.SegmentDesignInelasticControl.Create (iCk, 1), new
					org.drip.spline.params.ResponseScalingShapeControl (true, new
						org.drip.quant.function1D.QuadraticRationalShapeControl (1.)));

		if (null == (_regime = org.drip.spline.regime.MultiSegmentSequenceBuilder.CreateUncalibratedRegimeEstimator
			("SPLINE_REGIME", adblX, aSBP)))
			throw new java.lang.Exception ("LocalControlBasisSplineRegressor ctr: Cannot Construct Regime!");
	}

	@Override public boolean preRegression()
	{
		double[] adblY = new double[] {1.00, 4.00, 15.00, 40.00, 85.00};
		double[] adblDYDX = new double[] {1.00, 6.00, 17.00, 34.00, 57.00};

		org.drip.spline.params.SegmentCustomBuilderControl prbp = null;
		org.drip.spline.params.SegmentPredictorResponseDerivative[] aSEPLeft = new
			org.drip.spline.params.SegmentPredictorResponseDerivative[adblY.length - 1];
		org.drip.spline.params.SegmentPredictorResponseDerivative[] aSEPRight = new
			org.drip.spline.params.SegmentPredictorResponseDerivative[adblY.length - 1];

		for (int i = 0; i < adblY.length - 1; ++i) {
			try {
				aSEPLeft[i] = new org.drip.spline.params.SegmentPredictorResponseDerivative (adblY[i], new
					double[] {adblDYDX[i]});

				aSEPRight[i] = new org.drip.spline.params.SegmentPredictorResponseDerivative (adblY[i + 1],
					new double[] {adblDYDX[i + 1]});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		try {
			prbp = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.regime.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4),
						org.drip.spline.params.SegmentDesignInelasticControl.Create (2, 2), new
							org.drip.spline.params.ResponseScalingShapeControl (true, new
								org.drip.quant.function1D.QuadraticRationalShapeControl (1.)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		org.drip.spline.params.SegmentCustomBuilderControl[] aSBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[adblY.length - 1]; 

		for (int i = 0; i < adblY.length - 1; ++i)
			aSBP[i] = prbp;

		if (null == (_regimeBesselHermite =
			org.drip.spline.pchip.LocalControlRegimeBuilder.CreateBesselCubicSplineRegime ("BESSEL_REGIME",
				new double[] {0.00, 1.00,  2.00,  3.00,  4.00}, adblY, aSBP, null,
					org.drip.spline.regime.MultiSegmentSequence.CALIBRATE, true, true)))
			return false;

		return _regime.setupHermite (aSEPLeft, aSEPRight, null, null,
			org.drip.spline.regime.MultiSegmentSequence.CALIBRATE_JACOBIAN);
	}

	@Override public boolean execRegression()
	{
		try {
			if (null == (_regimeHermiteInsert = org.drip.spline.regime.MultiSegmentSequenceModifier.InsertKnot (_regime, 2.5,
				new org.drip.spline.params.SegmentPredictorResponseDerivative (27.5, new double[] {25.5}),
					new org.drip.spline.params.SegmentPredictorResponseDerivative (27.5, new double[]
						{25.5}))))
				return false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (null == (_regimeCardinalInsert = org.drip.spline.regime.MultiSegmentSequenceModifier.InsertCardinalKnot (_regime,
			2.5, 0.)))
			return false;

		return null != (_regimeCatmullRomInsert = org.drip.spline.regime.MultiSegmentSequenceModifier.InsertCatmullRomKnot
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
