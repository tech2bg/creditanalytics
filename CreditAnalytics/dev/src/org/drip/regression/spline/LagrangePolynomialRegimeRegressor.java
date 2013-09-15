
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
 *  - Compute an intermediate value Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LagrangePolynomialRegimeRegressor extends org.drip.regression.core.UnitRegressionExecutor {
	private boolean _bLocallyMonotone = false;
	private double _dblValue = java.lang.Double.NaN;
	private org.drip.math.grid.SingleSegmentRegime _sss = null;
	private org.drip.math.segment.Monotonocity _sm = null;
	private org.drip.math.calculus.WengertJacobian _wj = null;

	public LagrangePolynomialRegimeRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName)
		throws java.lang.Exception
	{
		super (strName, strScenarioName);

		_sss = new org.drip.math.grid.LagrangePolynomialRegime (new double[] {1., 2., 3., 4.});
	}

	@Override public boolean preRegression()
	{
		try {
			return _sss.setup (1., new double[] {1., 2., 3., 4.}, new
				org.drip.math.grid.RegimeCalibrationSetting
					(org.drip.math.grid.RegimeCalibrationSetting.BOUNDARY_CONDITION_FLOATING,
						org.drip.math.grid.RegimeCalibrationSetting.CALIBRATE));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public boolean execRegression()
	{
		try {
			if (!org.drip.math.common.NumberUtil.IsValid (_dblValue = _sss.response (2.16))) return false;

			_bLocallyMonotone = _sss.isLocallyMonotone();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (null == (_wj = _sss.jackDResponseDResponseInput (2.16))) return false;

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
