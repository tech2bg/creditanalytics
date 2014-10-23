
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
 * DiscountCurveJacobianRegressorSet implements the regression analysis for the full discount curve (built
 *  from cash/future/swap) Sensitivity Jacobians. Specifically, it computes the PVDF micro-Jack.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscountCurveJacobianRegressorSet implements org.drip.regression.core.RegressorSet {
	private java.lang.String _strRegressionScenario =
		"org.drip.analytics.definition.IRSDiscountCurve.CompPVDFJacobian";

	private java.util.List<org.drip.regression.core.UnitRegressor> _setRegressors = new
		java.util.ArrayList<org.drip.regression.core.UnitRegressor>();

	@Override public java.util.List<org.drip.regression.core.UnitRegressor> getRegressorSet()
	{
		return _setRegressors;
	}

	@Override public boolean setupRegressors()
	{
		try {
			_setRegressors.add (new org.drip.regression.core.UnitRegressionExecutor ("DiscountCurveJacobian",
				_strRegressionScenario)
			{
				org.drip.analytics.date.JulianDate dtStart = null;
				org.drip.analytics.rates.DiscountCurve dcIRS = null;
				org.drip.quant.calculus.WengertJacobian wjPVDF = null;
				org.drip.quant.calculus.WengertJacobian aWJComp[] = null;
				org.drip.product.definition.CalibratableFixedIncomeComponent aCompCalib[] = null;

				org.drip.param.market.LatentStateFixingsContainer lsfc = new
					org.drip.param.market.LatentStateFixingsContainer();

				@Override public boolean preRegression()
				{
					int NUM_DC_INSTR = 15;
					double adblDate[] = new double[NUM_DC_INSTR];
					double adblRate[] = new double[NUM_DC_INSTR];
					double adblCompCalibValue[] = new double[NUM_DC_INSTR];
					aWJComp = new org.drip.quant.calculus.WengertJacobian[NUM_DC_INSTR];
					java.lang.String astrCalibMeasure[] = new java.lang.String[NUM_DC_INSTR];
					aCompCalib = new org.drip.product.definition.CalibratableFixedIncomeComponent[NUM_DC_INSTR];

					if (null == (dtStart = org.drip.analytics.date.JulianDate.CreateFromYMD (2011, 4, 6)))
						return false;

					adblDate[0] = dtStart.addDays ((int)(365.25 * 1 + 2)).julian(); // 4Y

					adblDate[1] = dtStart.addDays ((int)(365.25 * 2 + 2)).julian(); // 5Y

					adblDate[2] = dtStart.addDays ((int)(365.25 * 3 + 2)).julian(); // 6Y

					adblDate[3] = dtStart.addDays ((int)(365.25 * 7 + 2)).julian(); // 7Y

					adblDate[4] = dtStart.addDays ((int)(365.25 * 8 + 2)).julian(); // 8Y

					adblDate[5] = dtStart.addDays ((int)(365.25 * 9 + 2)).julian(); // 9Y

					adblDate[6] = dtStart.addDays ((int)(365.25 * 10 + 2)).julian(); // 10Y

					adblDate[7] = dtStart.addDays ((int)(365.25 * 11 + 2)).julian(); // 11Y

					adblDate[8] = dtStart.addDays ((int)(365.25 * 12 + 2)).julian(); // 12Y

					adblDate[9] = dtStart.addDays ((int)(365.25 * 15 + 2)).julian(); // 15Y

					adblDate[10] = dtStart.addDays ((int)(365.25 * 20 + 2)).julian(); // 20Y

					adblDate[11] = dtStart.addDays ((int)(365.25 * 25 + 2)).julian(); // 25Y

					adblDate[12] = dtStart.addDays ((int)(365.25 * 30 + 2)).julian(); // 30Y

					adblDate[13] = dtStart.addDays ((int)(365.25 * 40 + 2)).julian(); // 40Y

					adblDate[14] = dtStart.addDays ((int)(365.25 * 50 + 2)).julian(); // 50Y

					adblCompCalibValue[0] = .0166;
					adblCompCalibValue[1] = .0206;
					adblCompCalibValue[2] = .0241;
					adblCompCalibValue[3] = .0269;
					adblCompCalibValue[4] = .0292;
					adblCompCalibValue[5] = .0311;
					adblCompCalibValue[6] = .0326;
					adblCompCalibValue[7] = .0340;
					adblCompCalibValue[8] = .0351;
					adblCompCalibValue[9] = .0375;
					adblCompCalibValue[10] = .0393;
					adblCompCalibValue[11] = .0402;
					adblCompCalibValue[12] = .0407;
					adblCompCalibValue[13] = .0409;
					adblCompCalibValue[14] = .0409;
					org.drip.param.period.CompositePeriodSetting cpsFixed = null;
					org.drip.param.period.CompositePeriodSetting cpsFloating = null;
					org.drip.param.period.UnitCouponAccrualSetting ucasFixed = null;
					org.drip.param.period.ComposableFixedUnitSetting cfusFixed = null;
					org.drip.param.period.UnitCouponAccrualSetting ucasFloating = null;
					org.drip.param.period.ComposableFloatingUnitSetting cfusFloating = null;

					try {
						ucasFloating = new org.drip.param.period.UnitCouponAccrualSetting (4, "Act/360",
							false, "Act/360", false, "USD", true);

						ucasFixed = new org.drip.param.period.UnitCouponAccrualSetting (2, "Act/360", false,
							"Act/360", false, "USD", true);

						cfusFloating = new org.drip.param.period.ComposableFloatingUnitSetting ("3M",
							org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
								null, org.drip.state.identifier.ForwardLabel.Standard ("USD-LIBOR-3M"),
									org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
							null, 0.);

						cfusFixed = new org.drip.param.period.ComposableFixedUnitSetting ("6M",
							org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
								null, 0., 0., "USD");

						cpsFloating = new org.drip.param.period.CompositePeriodSetting (4, "3M", "USD", null,
							org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
							-1., null, null, null, null);

						cpsFixed = new org.drip.param.period.CompositePeriodSetting (2, "6M", "USD", null,
							org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
							1., null, null, null, null);
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return false;
					}

					for (int i = 0; i < NUM_DC_INSTR; ++i) {
						adblRate[i] = 0.01;
						astrCalibMeasure[i] = "Rate";

						try {
							java.util.List<java.lang.Double> lsFixedStreamEdgeDate =
								org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtStart,
									new org.drip.analytics.date.JulianDate (adblDate[i]), "6M", null,
										org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

							java.util.List<java.lang.Double> lsFloatingStreamEdgeDate =
								org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtStart,
									new org.drip.analytics.date.JulianDate (adblDate[i]), "3M", null,
										org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

							org.drip.product.rates.Stream floatingStream = new org.drip.product.rates.Stream
								(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
									(lsFloatingStreamEdgeDate, cpsFloating, ucasFloating, cfusFloating));

							org.drip.product.rates.Stream fixedStream = new org.drip.product.rates.Stream
								(org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit
									(lsFixedStreamEdgeDate, cpsFixed, ucasFixed, cfusFixed));


							aCompCalib[i] = new org.drip.product.rates.FixFloatComponent (fixedStream,
								floatingStream, null);
						} catch (java.lang.Exception e) {
							e.printStackTrace();

							return false;
						}
					}

					lsfc.add (dtStart.addDays (2), org.drip.state.identifier.ForwardLabel.Standard
						("USD-LIBOR-6M"), 0.0042);

					return null != (dcIRS = org.drip.param.creator.ScenarioDiscountCurveBuilder.NonlinearBuild
						(dtStart, "USD",
							org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
								aCompCalib, adblCompCalibValue, astrCalibMeasure, lsfc));
				}

				@Override public boolean execRegression()
				{
					for (int i = 0; i < aCompCalib.length; ++i) {
						try {
							if (null == (aWJComp[i] = aCompCalib[i].jackDDirtyPVDManifestMeasure (new
								org.drip.param.valuation.ValuationParams (dtStart, dtStart, "USD"), null,
									org.drip.param.creator.MarketParamsBuilder.Create (dcIRS, null, null,
										null, null, null, lsfc), null)))
								return false;
						} catch (java.lang.Exception e) {
							e.printStackTrace();

							return false;
						}
					}

					return null != (wjPVDF = dcIRS.compJackDPVDManifestMeasure (dtStart));
				}

				@Override public boolean postRegression (
					final org.drip.regression.core.RegressionRunDetail rnvd)
				{
					for (int i = 0; i < aCompCalib.length; ++i) {
						if (!rnvd.set ("PVDFMicroJack_" + aCompCalib[i].name(), aWJComp[i].displayString()))
							return false;
					}

					return rnvd.set ("CompPVDFJacobian", wjPVDF.displayString());
				}
			});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override public java.lang.String getSetName()
	{
		return _strRegressionScenario;
	}
}
