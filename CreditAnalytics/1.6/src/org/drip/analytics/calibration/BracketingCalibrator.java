
package org.drip.analytics.calibration;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * Calibrates the component using the bracketing method
 *
 * @author Lakshmi Krishnamurthy
 */

public class BracketingCalibrator implements ComponentCalibrator {
	/**
	 * Bracketing using Secant Method
	 */

	public static int BRACKET_SECANT = 0;

	/**
	 * Bracketing using Bisection Method
	 */

	public static int BRACKET_BISECTION = 1;

	/**
	 * Bracketing using Composite (Secant/Bisection) Method
	 */

	public static int BRACKET_COMPOSITE = 2;

	private int _iMaxIterations = 100;
	private double _dblCalibFloor = 0.0001;
	private double _dblCalibCeiling = 1.;
	private double _dblRelTolerance = 1.e-06;
	private int _iBracketMethod = BRACKET_COMPOSITE;

	private double getNextRoot (
		final double dblLeft,
		final double dblRight,
		final double dblValueLeft,
		final double dblValueRight)
	{
		if (BRACKET_SECANT == _iBracketMethod)
			return dblLeft + ((dblLeft - dblRight) / (dblValueRight - dblValueLeft) * dblValueLeft);

		if (BRACKET_BISECTION == _iBracketMethod) return 0.5 * (dblLeft + dblRight);

		return 0.5 * (dblLeft + ((dblLeft - dblRight) / (dblValueRight - dblValueLeft) * dblValueLeft) + 0.5
			* (dblLeft + dblRight));
	}

	/**
	 * Constructs an empty BracketingCalibrator
	 */

	public BracketingCalibrator() {
	}

	/**
	 * Constructs a BracketingCalibrator instance from the bracketing parameters
	 * 
	 * @param iBracketMethod One of BRACKET_SECANT, BRACKET_BISECTION, BRACKET_COMPOSITE
	 * @param iMaxIterations Maximum number of iterations
	 * @param dblCalibFloor Floor below which the calibration is deemed to have failed
	 * @param dblCalibCeiling Ceiling above which the calibration is deemed to have failed
	 * @param dblRelTolerance The Relative tolerance to determine the vicinity of the error
	 * 
	 * @throws java.lang.Exception Thrown if the component cannot be calibrated
	 */

	public BracketingCalibrator (
		final int iBracketMethod,
		final int iMaxIterations,
		final double dblCalibFloor,
		final double dblCalibCeiling,
		final double dblRelTolerance)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (_dblCalibFloor = dblCalibFloor) || java.lang.Double.isNaN
			(_dblCalibCeiling = dblCalibCeiling) || java.lang.Double.isNaN (_dblRelTolerance =
				dblRelTolerance))
			throw new java.lang.Exception ("Invalid params into Comp Calib ctr!");

		_iBracketMethod = iBracketMethod;
		_iMaxIterations = iMaxIterations;
	}

	@Override public boolean bootstrapHazardRate (
		org.drip.analytics.definition.CreditCurve cc,
		final org.drip.product.definition.Component comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final org.drip.param.pricer.PricerParams pricerParamsIn,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat)
	{
		if (null == cc || null == comp || null == valParams || null == dc || null == pricerParamsIn || null
			== strMeasure || strMeasure.isEmpty() || java.lang.Double.isNaN (dblCalibValue)) {
			System.out.println ("Invalid params into BracketingCalibrator.bootstrapHazardRate!");

			return false;
		}

		int iNumIterations = 0;
		double dblHazardLeft = _dblCalibFloor;
		double dblValue = java.lang.Double.NaN;
		double dblHazardRight = _dblCalibCeiling;
		double dblValueLeft = java.lang.Double.NaN;
		double dblValueRight = java.lang.Double.NaN;
		double dblHazardRate = java.lang.Double.NaN;
		org.drip.param.pricer.PricerParams pricerParams = null;

		try {
			pricerParams = new org.drip.param.pricer.PricerParams (pricerParamsIn._iUnitSize, new
				org.drip.param.definition.CalibrationParams (strMeasure, 0, null),
					pricerParamsIn._bSurvToPayDate, pricerParamsIn._iDiscretizationScheme);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		double dblAbsTolerance = java.lang.Math.abs (_dblRelTolerance * dblCalibValue);

		if (dblAbsTolerance < _dblRelTolerance) dblAbsTolerance = _dblRelTolerance;

		if (!bFlat) {
			if (!cc.setNodeValue (iInstr, dblHazardLeft)) {
				System.out.println (comp.getComponentName() + " can't set bootstrap node with hazard " +
					dblHazardLeft);

				return false;
			}
		} else {
			if (!cc.setFlatValue (dblHazardLeft)) {
				System.out.println (comp.getComponentName() + " can't set flat node with hazard " +
					dblHazardLeft);

				return false;
			}
		}

		try {
			dblValueLeft = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (java.lang.Double.isNaN (dblValueLeft) || java.lang.Double.POSITIVE_INFINITY == dblValueLeft ||
			java.lang.Double.NEGATIVE_INFINITY == dblValueLeft) {
			System.out.println ("Cant calib " + comp.getComponentName() + ": Haz=+" + dblHazardLeft +
				"; Val=NaN");

			return false;
		}

		if (java.lang.Math.abs (dblValueLeft) <= dblAbsTolerance) return true;

		if (!bFlat) {
			if (!cc.setNodeValue (iInstr, dblHazardRight)) {
				System.out.println (comp.getComponentName() + " can't set boostrap node with hazard " +
					dblHazardRight);

				return false;
			}
		} else {
			if (!cc.setFlatValue (dblHazardRight)) {
				System.out.println (comp.getComponentName() + " can't set flat node with hazard " +
					dblHazardRight);

				return false;
			}
		}

		try {
			dblValueRight = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
					(dc, dcTSY, dcEDSF, cc, null, null, mmFixings),
					quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (java.lang.Double.isNaN (dblValueRight) || java.lang.Double.POSITIVE_INFINITY == dblValueRight ||
			java.lang.Double.NEGATIVE_INFINITY == dblValueRight) {
			System.out.println ("Cant calib " + comp.getComponentName() + ": Haz=+" + dblHazardLeft +
				"; Val=NaN");

			return false;
		}

		if (java.lang.Math.abs (dblValueRight) <= dblAbsTolerance) return true;

		if (0 < dblValueLeft * dblValueRight) {
			System.out.println (comp.getComponentName() + " bracketing problem! Left[" + dblHazardRight +
				"]=" + dblValueRight + "; Right[=" + dblHazardRight + "]=" + dblValueLeft);

			return false;
		}

		try {
			dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight, dblValueLeft, dblValueRight);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!bFlat) {
			if (!cc.setNodeValue (iInstr, dblHazardRate)) {
				System.out.println (comp.getComponentName() + " can't set boostrap node with hazard " +
					dblHazardRate);

				return false;
			}
		} else {
			if (!cc.setFlatValue (dblHazardRate)) {
				System.out.println (comp.getComponentName() + " can't set flat node with hazard " +
					dblHazardRate);

				return false;
			}
		}

		try {
			dblValue = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
					(dc, dcTSY, dcEDSF, cc, null, null, mmFixings),
					quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		while (java.lang.Math.abs (dblValue) > dblAbsTolerance) {
			if (java.lang.Double.isNaN (dblValue) || java.lang.Double.POSITIVE_INFINITY == dblValue ||
				java.lang.Double.NEGATIVE_INFINITY == dblValue) {
				System.out.println ("Measure values at " + dblValue + " for " + comp.getComponentName() +
					" and hazard " + dblHazardRate);

				return false;
			}

			if (++iNumIterations >= _iMaxIterations) {
				System.out.println ("Cannot calibrate " + comp.getComponentName() + " within " +
					_iMaxIterations + " iterations!");

				return false;
			}

			if (dblValue < 0) {
				dblValueLeft = dblValue;
				dblHazardLeft = dblHazardRate;
			} else {
				dblValueRight = dblValue;
				dblHazardRight = dblHazardRate;
			}

			try {
				if (!bFlat) {
					if (!cc.setNodeValue (iInstr, dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight,
						dblValueLeft, dblValueRight))) {
						System.out.println (comp.getComponentName() + " can't set boostrap node with hazard "
							+ dblHazardRate);

						return false;
					}
				} else {
					if (!cc.setFlatValue (dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight,
						dblValueLeft, dblValueRight))) {
						System.out.println (comp.getComponentName() + " can't set flat node with hazard " +
							dblHazardRate);

						return false;
					}
				}

				dblValue = comp.calcMeasureValue (valParams, pricerParams,
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc,
						dcTSY, dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure) -
							dblCalibValue;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return true;
	}

	@Override public boolean bootstrapInterestRate (
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final org.drip.product.definition.Component comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat)
	{
		if (null == comp || null == valParams || null == dc || null == strMeasure || strMeasure.isEmpty() ||
			java.lang.Double.isNaN (dblCalibValue)) {
			System.out.println ("Invalid params into BracketingCalibrator.bootstrapInterestRate!");

			return false;
		}

		int iNumIterations = 0;
		double dblHazardLeft = _dblCalibFloor;
		double dblValue = java.lang.Double.NaN;
		double dblHazardRight = _dblCalibCeiling;
		double dblValueLeft = java.lang.Double.NaN;
		double dblValueRight = java.lang.Double.NaN;
		double dblHazardRate = java.lang.Double.NaN;
		org.drip.param.pricer.PricerParams pricerParams = null;

		try {
			pricerParams = new org.drip.param.pricer.PricerParams (7, new
				org.drip.param.definition.CalibrationParams (strMeasure, 0, null), false,
					org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		double dblAbsTolerance = java.lang.Math.abs (_dblRelTolerance * dblCalibValue);

		if (dblAbsTolerance < _dblRelTolerance) dblAbsTolerance = _dblRelTolerance;

		if (!bFlat) {
			if (!dc.setNodeValue (iInstr, dblHazardLeft)) {
				System.out.println ("Cannot set bootstrap node at " + dblHazardLeft + " for " +
					comp.getComponentName());

				return false;
			}
		} else {
			if (!dc.setFlatValue (dblHazardLeft)) {
				System.out.println ("Cannot set flat node at " + dblHazardLeft + " for " +
					comp.getComponentName());

				return false;
			}
		}

		try {
			dblValueLeft = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, null, null, null, mmFixings), quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (java.lang.Double.isNaN (dblValueLeft) || java.lang.Double.POSITIVE_INFINITY == dblValueLeft ||
			java.lang.Double.NEGATIVE_INFINITY == dblValueLeft) {
			System.out.println ("Measure values at " + dblValueLeft + " for " + comp.getComponentName() + 
				" and IR " + dblHazardLeft);

			return false;
		}

		if (java.lang.Math.abs (dblValueLeft) <= dblAbsTolerance) return true;

		if (!bFlat) {
			if (!dc.setNodeValue (iInstr, dblHazardRight)) {
				System.out.println ("Cannot set bootrstap node at " + dblHazardRight + " for " +
					comp.getComponentName());

				return false;
			}
		} else {
			if (!dc.setFlatValue (dblHazardRight)) {
				System.out.println ("Cannot set flat node at " + dblHazardRight + " for " +
					comp.getComponentName());

				return false;
			}
		}

		try {
			dblValueRight = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
					(dc, dcTSY, dcEDSF, null, null, null, mmFixings),
						quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (java.lang.Double.isNaN (dblValueRight) || java.lang.Double.POSITIVE_INFINITY == dblValueRight ||
			java.lang.Double.NEGATIVE_INFINITY == dblValueRight) {
			System.out.println ("Measure values at " + dblValueRight + " for " + comp.getComponentName() +
				" and IR " + dblHazardRight);

			return false;
		}

		if (java.lang.Math.abs (dblValueRight) <= dblAbsTolerance) return true;

		if (0 < dblValueLeft * dblValueRight) {
			System.out.println ("Calib: = " + dblCalibValue);

			System.out.println ("HazLeft = " + dblHazardLeft + "; Val Left = " + dblValueLeft);

			System.out.println ("HazRight = " + dblHazardRight + "; Val Right = " + dblValueRight);

			System.out.println ("Cannot bracket the root for " +
				((org.drip.product.definition.CalibratableComponent) comp).getPrimaryCode() + " for " +
					strMeasure + " = " + dblCalibValue);

			return false;
		}

		try {
			dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight, dblValueLeft, dblValueRight);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!bFlat) {
			if (!dc.setNodeValue (iInstr, dblHazardRate)) {
				System.out.println ("Cannot set bootrstap node at " + dblHazardRate + " for " +
					comp.getComponentName());

				return false;
			}
		} else {
			if (!dc.setFlatValue (dblHazardRate)) {
				System.out.println ("Cannot set flat node at " + dblHazardRate + " for " +
					comp.getComponentName());

				return false;
			}
		}

		try {
			dblValue = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
					(dc, dcTSY, dcEDSF, null, null, null, mmFixings),
					quotingParams, strMeasure) - dblCalibValue;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		while (java.lang.Math.abs (dblValue) > dblAbsTolerance) {
			if (java.lang.Double.isNaN (dblValue) || java.lang.Double.POSITIVE_INFINITY == dblValue ||
				java.lang.Double.NEGATIVE_INFINITY == dblValue) {
				System.out.println ("Measure values at " + dblValue + " for " + comp.getComponentName() +
					" and IR " + dblHazardRate);

				return false;
			}

			if (++iNumIterations >= _iMaxIterations) {
				System.out.println ("Cannot calibrate within " + _iMaxIterations + " iters!");

				return false;
			}

			if (dblValue < 0) {
				dblValueLeft = dblValue;
				dblHazardLeft = dblHazardRate;
			} else {
				dblValueRight = dblValue;
				dblHazardRight = dblHazardRate;
			}

			try {
				if (!bFlat) {
					if (!dc.setNodeValue (iInstr, dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight,
						dblValueLeft, dblValueRight))) {
						System.out.println ("Cannot set bootstrap node at " + dblHazardRate + " for " +
							comp.getComponentName());

						return false;
					}
				} else {
					if (!dc.setFlatValue (dblHazardRate = getNextRoot (dblHazardLeft, dblHazardRight,
						dblValueLeft, dblValueRight))) {
						System.out.println ("Cannot set flat node at " + dblHazardRate + " for " +
							comp.getComponentName());

						return false;
					}
				}

				dblValue = comp.calcMeasureValue (valParams, pricerParams,
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
						(dc, dcTSY, dcEDSF, null, null, null,
						mmFixings), quotingParams, strMeasure) - dblCalibValue;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
 		}

		return true;
	}
}
