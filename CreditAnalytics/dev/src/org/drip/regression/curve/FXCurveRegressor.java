
package org.drip.regression.curve;

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
 * FXCurveRegressor implements the regression analysis set for the FX Curve. FXCurveRegressor implements 3
 * 	regression tests:
 * 	- #1: FX Basis and FX Curve Creation: Construct a FX forward Curve from an array of FX forward nodes and
 * 		the spot.
 * 	- #2: Imply the FX Forward given the domestic and foreign discount curves.
 * 	- #3a: Compute the domestic and foreign basis given the market FX forward.
 * 	- #3b: Build the domestic/foreign basis curve given the corresponding basis nodes.
 * 	- #3c: Imply the array of FX forward points/PIPs from the array of basis and domestic/foreign discount
 * 		curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FXCurveRegressor implements org.drip.regression.core.RegressorSet {
	private org.drip.product.definition.FXForward _fxfwd = null;
	private org.drip.analytics.definition.FXForwardCurve _fxForwardCurve = null;
	private java.lang.String _strRegressionScenario = "org.drip.analytics.curve.FXCurve";

	private java.util.List<org.drip.regression.core.UnitRegressor> _setRegressors = new
		java.util.ArrayList<org.drip.regression.core.UnitRegressor>();

	/**
	 * Do nothing FXCurveRegressor constructor
	 */

	public FXCurveRegressor()
	{
	}

	/*
	 * FX Curve Regressor set setup
	 */

	@Override public boolean setupRegressors()
	{
		/*
		 * FXBasis and FXCurve Creation unit regressor - implements the pre-regression, the post-regression,
		 * 	and the actual regression functionality of the UnitRegressorExecutor class.
		 */

		try {
			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor
				("FXBasisAndFXCurveCreation", _strRegressionScenario)
			{
				private static final int NUM_FX_NODES = 5;

				private double _dblFXSpot = 1.25;
				double[] _adblFXFwd = new double[NUM_FX_NODES];
				double[] _adblNodes = new double[NUM_FX_NODES];
				boolean[] _abIsPIP = new boolean[NUM_FX_NODES];
				private org.drip.product.params.CurrencyPair _cp = null;

				@Override public boolean preRegression()
				{
					try {
						_cp = new org.drip.product.params.CurrencyPair ("EUR", "USD", "USD", 10000.);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					java.util.Random rand = new java.util.Random();

					org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

					for (int i = 0; i < NUM_FX_NODES; ++i) {
						_abIsPIP[i] = false;

						_adblFXFwd[i] = _dblFXSpot - (i + 1) * 0.01 * rand.nextDouble();

						_adblNodes[i] = dtToday.addYears (i + 1).julian();
					}

					return true;
				}

				@Override public boolean execRegression()
				{
					org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

					if (null == (_fxfwd = org.drip.product.creator.FXForwardBuilder.CreateFXForward (_cp,
						dtToday, "18M")))
						return false;

					try {
						if (null == (_fxForwardCurve =
							org.drip.state.creator.FXForwardCurveBuilder.CreateFXForwardCurve (_cp, dtToday,
								_dblFXSpot, _adblNodes, _adblFXFwd, _abIsPIP)))
							return false;
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}
			});

			/*
			 * FX Forward Test unit regressor - implements the pre-regression, the post-regression, and the
			 * 	actual regression functionality of the UnitRegressorExecutor class.
			 */

			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor ("FXFwdTest",
				_strRegressionScenario)
			{
				private double _dblFXFwd = java.lang.Double.NaN;
				private double _dblFXFwdPIP = java.lang.Double.NaN;
				private org.drip.param.valuation.ValuationParams _valParams = null;
				private org.drip.analytics.rates.ExplicitBootDiscountCurve _dcEUR = null;
				private org.drip.analytics.rates.ExplicitBootDiscountCurve _dcUSD = null;

				@Override public boolean preRegression()
				{
					org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

					if (null == (_dcUSD = org.drip.state.creator.DiscountCurveBuilder.CreateFromFlatRate
						(dtToday, "USD", null, 0.05)))
						return false;

					if (null == (_dcEUR = org.drip.state.creator.DiscountCurveBuilder.CreateFromFlatRate
						(dtToday, "EUR", null, 0.04)))
						return false;

					if (null == (_valParams = org.drip.param.valuation.ValuationParams.CreateValParams
						(dtToday, 0, "USD", org.drip.analytics.daycount.Convention.DATE_ROLL_ACTUAL)))
						return false;

					return true;
				}

				@Override public boolean execRegression()
				{
					try {
						_dblFXFwd = _fxfwd.imply (_valParams, _dcEUR, _dcUSD, 1.4, false);

						_dblFXFwdPIP = _fxfwd.imply (_valParams, _dcEUR, _dcUSD, 1.4, true);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					return true;
				}

				@Override public boolean postRegression (
					final org.drip.regression.core.RegressionRunDetail rnvd)
				{
					rnvd.set ("FXFwd", org.drip.quant.common.FormatUtil.FormatDouble (_dblFXFwd, 1, 4, 1));

					rnvd.set ("FXFwdPIP", org.drip.quant.common.FormatUtil.FormatDouble (_dblFXFwdPIP, 1, 1,
						1));

					return true;
				}
			});

			/*
			 * FXBasis Test unit regressor - implements the pre-regression, the post-regression, and the
			 * 	actual regression functionality of the UnitRegressorExecutor class.
			 */

			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor ("FXBasisTest",
				_strRegressionScenario)
			{
				private static final int NUM_FX_NODES = 5;

				private double _dblFXSpot = 1.26;
				private double _dblFXFwdMarket = 1.26;
				private double[] _adblFullEURBasis = null;
				private double[] _adblFullUSDBasis = null;
				private double[] _adblFXFwdFromEURBasis = null;
				private double[] _adblFXFwdFromUSDBasis = null;
				private double[] _adblBootstrappedEURBasis = null;
				private double[] _adblBootstrappedUSDBasis = null;
				private double _dblDCEURBasis = java.lang.Double.NaN;
				private double _dblDCUSDBasis = java.lang.Double.NaN;
				private double[] _adblNodes = new double[NUM_FX_NODES];
				private org.drip.analytics.rates.ExplicitBootDiscountCurve _dcEUR = null;
				private org.drip.analytics.rates.ExplicitBootDiscountCurve _dcUSD = null;
				private org.drip.param.valuation.ValuationParams _valParams = null;
				private org.drip.analytics.definition.FXBasisCurve _fxEURBasisCurve = null;
				private org.drip.analytics.definition.FXBasisCurve _fxUSDBasisCurve = null;

				@Override public boolean preRegression()
				{
					org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

					if (null == (_dcUSD = org.drip.state.creator.DiscountCurveBuilder.CreateFromFlatRate
						(dtToday, "USD", null, 0.05)))
						return false;

					if (null == (_dcEUR = org.drip.state.creator.DiscountCurveBuilder.CreateFromFlatRate
						(dtToday, "EUR", null, 0.04)))
						return false;

					if (null == (_valParams = org.drip.param.valuation.ValuationParams.CreateValParams
						(dtToday, 0, "USD", org.drip.analytics.daycount.Convention.DATE_ROLL_ACTUAL)))
						return false;

						for (int i = 0; i < NUM_FX_NODES; ++i)
							_adblNodes[i] = dtToday.addYears (i + 1).julian();

					return true;
				}

				@Override public boolean execRegression()
				{
					try {
						_dblDCEURBasis = _fxfwd.discountCurveBasis (_valParams, _dcEUR, _dcUSD, _dblFXSpot,
							_dblFXFwdMarket, false);

						_dblDCUSDBasis = _fxfwd.discountCurveBasis (_valParams, _dcEUR, _dcUSD, _dblFXSpot,
							_dblFXFwdMarket, true);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					if (null == (_adblFullUSDBasis = _fxForwardCurve.zeroBasis (_valParams, _dcEUR,
						_dcUSD, true)) || 0 == _adblFullUSDBasis.length)
						return false;

					if (null == (_adblFullEURBasis = _fxForwardCurve.zeroBasis (_valParams, _dcEUR,
						_dcUSD, false)) || 0 == _adblFullEURBasis.length)
						return false;

					if (null == (_adblBootstrappedUSDBasis = _fxForwardCurve.bootstrapBasis (_valParams,
						_dcEUR, _dcUSD, true)) || 0 == _adblBootstrappedUSDBasis.length)
						return false;

					if (null == (_adblBootstrappedEURBasis = _fxForwardCurve.bootstrapBasis (_valParams,
						_dcEUR, _dcUSD, false)) || 0 == _adblBootstrappedEURBasis.length)
						return false;

					try {
						org.drip.analytics.date.JulianDate dtToday =
							org.drip.analytics.date.JulianDate.Today();

						org.drip.product.params.CurrencyPair cp = new org.drip.product.params.CurrencyPair
							("EUR", "USD", "USD", 10000.);

						_fxUSDBasisCurve = org.drip.state.creator.FXBasisCurveBuilder.CreateFXBasisCurve (cp,
							dtToday, _dblFXSpot, _adblNodes, _adblFullUSDBasis, false);

						_fxEURBasisCurve = org.drip.state.creator.FXBasisCurveBuilder.CreateFXBasisCurve (cp,
							dtToday, _dblFXSpot, _adblNodes, _adblFullEURBasis, false);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					if (null == (_adblFXFwdFromUSDBasis = _fxUSDBasisCurve.fxForward (_valParams, _dcEUR,
						_dcUSD, true, false)) || 0 == _adblFXFwdFromUSDBasis.length)
						return false;

					if (null == (_adblFXFwdFromEURBasis = _fxEURBasisCurve.fxForward (_valParams, _dcEUR,
						_dcUSD, false, false)) || 0 == _adblFXFwdFromEURBasis.length)
						return false;

					return true;
				}

				@Override public boolean postRegression (
					final org.drip.regression.core.RegressionRunDetail rnvd)
				{
					rnvd.set ("EURBasis", org.drip.quant.common.FormatUtil.FormatDouble (_dblDCEURBasis, 1,
						2, 10000.));

					rnvd.set ("USDBasis", org.drip.quant.common.FormatUtil.FormatDouble (_dblDCUSDBasis, 1,
						2, 10000.));

					for (int i = 0; i < _adblFullUSDBasis.length; ++i) {
						rnvd.set ("FullUSDBasis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblFullUSDBasis[i], 1, 4,
								10000.));

						rnvd.set ("FullEURBasis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblFullEURBasis[i], 1, 4,
								10000.));

						rnvd.set ("BootstrapUSDBasis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblBootstrappedUSDBasis[i], 1,
								0, 10000.));

						rnvd.set ("BootstrapEURBasis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblBootstrappedEURBasis[i], 1,
								0, 10000.));

						rnvd.set ("FXFwd from USD Basis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblFXFwdFromUSDBasis[i], 1, 4,
								1.));

						rnvd.set ("FXFwd from EUR Basis{" + (i + 1) + "Y}",
							org.drip.quant.common.FormatUtil.FormatDouble (_adblFXFwdFromEURBasis[i], 1, 4,
								1.));

						if (!org.drip.quant.common.NumberUtil.WithinTolerance (_adblFullUSDBasis[i],
							-_adblFullEURBasis[i]))
							return false;

						if (!org.drip.quant.common.NumberUtil.WithinTolerance (_adblBootstrappedUSDBasis[i],
							-_adblBootstrappedEURBasis[i]))
							return false;

						if (!org.drip.quant.common.NumberUtil.WithinTolerance (_adblFXFwdFromUSDBasis[i],
							_adblFXFwdFromEURBasis[i]))
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
