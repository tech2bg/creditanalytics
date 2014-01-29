
package org.drip.regression.spline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * LagrangePolynomialStretchRegressor implements the local control basis spline regressor for the given basis
 *  spline. As part of the regression run, it executes the following:
 *  - Calibrate and compute the left and the right Jacobian.
 *  - Insert the Local Control Hermite, Cardinal, and Catmull-Rom knots.
 *  - Compute an intermediate value Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LagrangePolynomialStretchRegressor extends org.drip.regression.core.UnitRegressionExecutor {
	private boolean _bLocallyMonotone = false;
	private double _dblValue = java.lang.Double.NaN;
	private org.drip.spline.segment.Monotonocity _sm = null;
	private org.drip.quant.calculus.WengertJacobian _wj = null;
	private org.drip.spline.stretch.SingleSegmentSequence _sss = null;

	public LagrangePolynomialStretchRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName)
		throws java.lang.Exception
	{
		super (strName, strScenarioName);

		_sss = new org.drip.spline.stretch.SingleSegmentLagrangePolynomial (new double[] {1., 2., 3., 4.});
	}

	@Override public boolean preRegression()
	{
		try {
			return _sss.setup (1., new double[] {1., 2., 3., 4.}, null,
				org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
					org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public boolean execRegression()
	{
		try {
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblValue = _sss.responseValue (2.16)))
				return false;

			_bLocallyMonotone = _sss.isLocallyMonotone();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (null == (_wj = _sss.jackDResponseDCalibrationInput (2.16, 1))) return false;

		return null != (_sm = _sss.monotoneType (2.16));
	}

	@Override public boolean postRegression (
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		if (!rnvd.set ("LPSR_Value", "" + _dblValue)) return false;

		if (!rnvd.set ("LPSR_WJ", _wj.displayString())) return false;

		if (!rnvd.set ("LPSR_SM", _sm.toString())) return false;

		return rnvd.set ("LPSR_LocallyMonotone", "" + _bLocallyMonotone);
	}
}
