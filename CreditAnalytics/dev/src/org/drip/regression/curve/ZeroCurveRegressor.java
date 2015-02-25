
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
 * ZeroCurveRegressor implements the regression analysis set for the Zero Curve. The  regression tests do the
 *  consists of the following:
 *  - Build a discount curve, followed by the zero curve
 *  - Regressor #1: Compute zero curve discount factors
 *  - Regressor #2: Compute zero curve zero rates
 *
 * @author Lakshmi Krishnamurthy
 */

public class ZeroCurveRegressor implements org.drip.regression.core.RegressorSet {
	private org.drip.analytics.rates.ZeroCurve _zc = null;
	private java.lang.String _strRegressionScenario = "org.drip.analytics.curve.ZeroCurve";

	private java.util.List<org.drip.regression.core.UnitRegressor> _setRegressors = new
		java.util.ArrayList<org.drip.regression.core.UnitRegressor>();

	/**
	 * ZeroCurveRegressor constructor - Creates the base zero curve and initializes the regression objects
	 */

	public ZeroCurveRegressor()
	{
	}

	/*
	 * Setting up of the zero curve regressor set
	 */

	@Override public boolean setupRegressors()
	{
		/*
		 * Zero Curve Creation unit regressor - implements the pre-regression, the post-regression, and the
		 * 	actual regression functionality of the UnitRegressorExecutor class.
		 */

		try {
			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor
				("CreateZeroCurveFromPeriods", _strRegressionScenario)
			{
				private static final double s_dblZSpread = 0.01;

				private org.drip.analytics.date.JulianDate _dtStart = null;
				private org.drip.analytics.rates.ExplicitBootDiscountCurve _dc = null;

				private java.util.List<org.drip.analytics.cashflow.CompositePeriod> _lsCouponPeriod = new
					java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

				@Override public boolean preRegression()
				{
					if (null == (_dtStart = org.drip.analytics.date.DateUtil.CreateFromYMD (2010,
						org.drip.analytics.date.DateUtil.MAY, 12)))
						return false;

					final int NUM_DC_NODES = 5;
					final int NUM_PERIOD_NODES  = 40;
					double adblDate[] = new double[NUM_DC_NODES];
					double adblRate[] = new double[NUM_DC_NODES];

					for (int i = 0; i < NUM_DC_NODES; ++i) {
						adblDate[i] = _dtStart.addYears (2 * i + 1).julian();

						adblRate[i] = 0.05 + 0.001 * (NUM_DC_NODES - i);
					}

					if (null == (_dc = org.drip.state.creator.DiscountCurveBuilder.CreateDC (_dtStart, "CHF",
						null, adblDate, adblRate,
							org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD)))
						return false;

					try {
						org.drip.param.period.UnitCouponAccrualSetting ucas = new
							org.drip.param.period.UnitCouponAccrualSetting (2, "30/360", false, "30/360",
								false, "ZAR", false,
									org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

						org.drip.param.period.ComposableFixedUnitSetting cfus = new
							org.drip.param.period.ComposableFixedUnitSetting ("6M",
								org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
							null, s_dblZSpread, 0., "ZAR");

						org.drip.param.period.CompositePeriodSetting cps = new
							org.drip.param.period.CompositePeriodSetting (2, "6M", "ZAR", null, 1., null,
								null, null, null);

						java.util.List<java.lang.Double> lsStreamEdgeDate =
							org.drip.analytics.support.CompositePeriodBuilder.RegularEdgeDates (_dtStart,
								"6M", (NUM_PERIOD_NODES * 6) + "M", null);

						_lsCouponPeriod =
							org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit (
								lsStreamEdgeDate, cps, ucas, cfus);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}

				@Override public boolean execRegression()
				{
					try {
						if (null == (_zc = org.drip.state.creator.ZeroCurveBuilder.CreateZeroCurve (2,
							"30/360", _dc.currency(), true, _lsCouponPeriod, _lsCouponPeriod.get
								(_lsCouponPeriod.size() - 1).endDate(), _dtStart.addDays (2).julian(), _dc,
									null, s_dblZSpread, new
										org.drip.spline.params.SegmentCustomBuilderControl
											(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
							new org.drip.spline.basis.PolynomialFunctionSetParams (4),
								org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), new
									org.drip.spline.params.ResponseScalingShapeControl (true, new
										org.drip.function.deterministic1D.QuadraticRationalShapeControl (0.)),
											null))))
							return false;
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}
			});

			/*
			 * Get Zero Discount Factor unit regressor - implements the pre-regression, the post-regression,
			 *	and the actual regression functionality of the UnitRegressorExecutor class.
			 */

			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor ("getZeroDF",
				_strRegressionScenario)
			{
				private static final int NUM_DF_NODES = 30;

				private double _adblDate[] = new double[NUM_DF_NODES];
				private double _adblDiscFactor[] = new double[NUM_DF_NODES];

				@Override public boolean preRegression()
				{
					org.drip.analytics.date.JulianDate dtStart =
						org.drip.analytics.date.DateUtil.CreateFromYMD (2008,
							org.drip.analytics.date.DateUtil.SEPTEMBER, 25);

					for (int i = 0; i < NUM_DF_NODES; ++i)
						_adblDate[i] = dtStart.addMonths (6 * i + 6).julian();

					return true;
				}

				@Override public boolean execRegression()
				{
					try {
						for (int i = 0; i < NUM_DF_NODES; ++i)
							_adblDiscFactor[i] = _zc.df (_adblDate[i]);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}

				@Override public boolean postRegression (
					final org.drip.regression.core.RegressionRunDetail rnvd)
				{
					try {
						for (int i = 0; i < NUM_DF_NODES; ++i)
							rnvd.set ("ZeroDF[" + new org.drip.analytics.date.JulianDate (_adblDate[i]) +
								"]", "" + _adblDiscFactor[i]);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}
			});

			/*
			 * Get Zero Rate unit regressor - implements the pre-regression, the post-regression, and the
			 * 	actual regression functionality of the UnitRegressorExecutor class.
			 */

			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor ("getZeroRate",
				_strRegressionScenario)
			{
				private static final int NUM_DF_NODES = 30;

				private double _adblDate[] = new double[NUM_DF_NODES];
				private double _adblRate[] = new double[NUM_DF_NODES];

				@Override public boolean preRegression()
				{
					org.drip.analytics.date.JulianDate dtStart =
						org.drip.analytics.date.DateUtil.CreateFromYMD (2008,
							org.drip.analytics.date.DateUtil.SEPTEMBER, 25);

					for (int i = 0; i < NUM_DF_NODES; ++i)
						_adblDate[i] = dtStart.addMonths (6 * i + 6).julian();

					return true;
				}

				@Override public boolean execRegression()
				{
					try {
						for (int i = 0; i < NUM_DF_NODES; ++i)
							_adblRate[i] = _zc.zeroRate (_adblDate[i]);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}

				@Override public boolean postRegression (
					final org.drip.regression.core.RegressionRunDetail rnvd)
				{
					try {
						for (int i = 0; i < NUM_DF_NODES; ++i)
							rnvd.set ("ZeroRate[" + new org.drip.analytics.date.JulianDate (_adblDate[i]) +
								"]", "" + _adblRate[i]);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}
			});
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override public java.util.List<org.drip.regression.core.UnitRegressor> getRegressorSet()
	{
		return _setRegressors;
	}

	@Override public java.lang.String getSetName()
	{
		return _strRegressionScenario;
	}
}
