
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
 * Calibrates the component using the Newton-Raphson method
 *
 * @author Lakshmi Krishnamurthy
 */

public class NewtonRaphsonCalibrator implements ComponentCalibrator {
	// IR Stoch Calib

	private double _dblIRInit = 0.03;
	private int _iNumIRIterations = 50;
	private double _dblIRIncr = 0.0001;
	private double _dblIRDiffTol = 0.000001;

	// Hazard Stoch Calib

	private double _dblHazardInit = 0.03;
	private int _iNumHazardIterations = 50;
	private double _dblHazardIncr = 0.0001;
	private double _dblHazardDiffTol = 0.000001;

	private static final boolean SetNode (
		final org.drip.analytics.definition.Curve curve,
		final int iInstr,
		final boolean bFlat,
		final double dblIR)
	{
		if (!bFlat) return curve.setNodeValue (iInstr, dblIR);

		return curve.setFlatValue (dblIR);
	}

	/**
	 * Constructs an empty NewtonRaphsonCalibrator
	 */

	public NewtonRaphsonCalibrator()
	{
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
			System.out.println ("Invalid params into NewtonRaphsonCalibrator.bootstrapHazardRate!");

			return false;
		}

		double dblQ = java.lang.Double.NaN;
		double dblHazardPrev = _dblHazardInit;
		double dblDHazardDQ = java.lang.Double.NaN;
		int iNumHazardIterations = _iNumHazardIterations;
		org.drip.param.pricer.PricerParams pricerParams = null;

		try {
			pricerParams = new org.drip.param.pricer.PricerParams (pricerParamsIn._iUnitSize, new
				org.drip.param.definition.CalibrationParams (strMeasure, 0, null),
					pricerParamsIn._bSurvToPayDate, pricerParamsIn._iDiscretizationScheme);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (!SetNode (cc, iInstr, bFlat, dblHazardPrev)) {
			System.out.println ("Cannot set node at " + dblHazardPrev + " for " + comp.getComponentName() +
				"; flat? " + bFlat);

			return false;
		}

		try {
			dblQ = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!SetNode (cc, iInstr, bFlat, dblHazardPrev + _dblHazardIncr)) {
			System.out.println ("Cannot set node at " + (dblHazardPrev + _dblHazardIncr) + " for " +
				comp.getComponentName() + "; flat? " + bFlat);

			return false;
		}

		try {
			dblDHazardDQ = _dblHazardIncr / (comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure) - dblQ);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		double dblHazard = dblHazardPrev + dblDHazardDQ * (dblCalibValue - dblQ);

		if (java.lang.Double.isNaN (dblHazard) || java.lang.Double.NEGATIVE_INFINITY == dblHazard ||
			java.lang.Double.POSITIVE_INFINITY == dblHazard) {
			System.out.println (strMeasure + "[" + dblHazardPrev + "]: " + dblCalibValue);

			System.out.println ("Get " + dblHazard + " for hazard for " + comp.getComponentName() +
				" and calib value " + dblCalibValue + "!");

			return false;
		}

		if (!SetNode (cc, iInstr, bFlat, dblHazard)) {
			System.out.println ("Cannot set node at " + dblHazard + " for " + comp.getComponentName() +
				"; flat? " + bFlat);

			return false;
		}

		while (_dblHazardDiffTol < java.lang.Math.abs (dblHazard - dblHazardPrev)) {
			if (0 == --iNumHazardIterations) {
				System.out.println ("Cannot calib " + comp.getComponentName() + "[" + strMeasure + "] for " +
					dblCalibValue + " within " + _iNumHazardIterations + " iters!");

				return false;
			}

			dblHazardPrev = dblHazard;

			try {
				dblQ = comp.calcMeasureValue (valParams, pricerParams,
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc,
						dcTSY, dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			if (!SetNode (cc, iInstr, bFlat, dblHazardPrev + _dblHazardIncr)) {
				System.out.println ("Cannot set node at " + (dblHazardPrev + _dblHazardIncr) + " for " +
					comp.getComponentName() + "; flat? " + bFlat);

				return false;
			}

			try {
				dblDHazardDQ = _dblHazardIncr / (comp.calcMeasureValue (valParams, pricerParams,
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc,
						dcTSY, dcEDSF, cc, null, null, mmFixings), quotingParams, strMeasure) - dblQ);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			dblHazard = dblHazardPrev + dblDHazardDQ * (dblCalibValue - dblQ);

			if (java.lang.Double.isNaN (dblHazard) || java.lang.Double.NEGATIVE_INFINITY ==
				dblHazard || java.lang.Double.POSITIVE_INFINITY == dblHazard) {
				System.out.println ("Get " + dblHazard + " for hazard for " + comp.getComponentName() +
					" and calib value " + dblCalibValue + " within limit!");

				return false;
			}

			if (!SetNode (cc, iInstr, bFlat, dblHazard)) {
				System.out.println ("Cannot set node at " + dblHazardPrev + " for " + comp.getComponentName()
					+ "; flat? " + bFlat);

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
			System.out.println ("Invalid params into NewtonRaphsonCalibrator.bootstrapInterestRate!");

			return false;
		}

		double dblIRPrev = _dblIRInit;
		double dblQ = java.lang.Double.NaN;
		double dblDIRDQ = java.lang.Double.NaN;
		int iNumIRIterations = _iNumIRIterations;

		if (!SetNode (dc, iInstr, bFlat, dblIRPrev)) {
			System.out.println ("Cannot set IR = " + dblIRPrev + " for node " + iInstr + "; Flat?  " +
				bFlat);

			return false;
		}

		org.drip.param.pricer.PricerParams pricerParams = null;

		try {
			pricerParams = new org.drip.param.pricer.PricerParams (7, new
				org.drip.param.definition.CalibrationParams (strMeasure, 0, null), false,
					org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			dblQ = comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, null, null, null, mmFixings), quotingParams, strMeasure);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!SetNode (dc, iInstr, bFlat, dblIRPrev + _dblIRIncr)) {
			System.out.println ("Cannot set IR = " + (dblIRPrev + _dblIRIncr) + " for node " + iInstr +
				"; Flat?  " + bFlat);

			return false;
		}

		try {
			dblDIRDQ = _dblIRIncr / (comp.calcMeasureValue (valParams, pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, dcTSY,
					dcEDSF, null, null, null, mmFixings), quotingParams, strMeasure) - dblQ);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		double dblIR = dblIRPrev + dblDIRDQ * (dblCalibValue - dblQ);

		if (java.lang.Double.isNaN (dblIR) || java.lang.Double.POSITIVE_INFINITY == dblIR ||
			java.lang.Double.NEGATIVE_INFINITY == dblIR) {
			System.out.println ("Component Maturity: " + comp.getMaturityDate() + "; Node maturity: " +
				dc.getNodeDate (iInstr));

			System.out.println ("Get " + dblIR + " for IR for " + comp.getComponentName() + "[" +
				strMeasure + ", flat = " + bFlat + "] and calib value " + dblCalibValue +
					" within limit!");

			return false;
		}

		if (!SetNode (dc, iInstr, bFlat, dblIR)) {
			System.out.println ("Cannot set IR = " + dblIR + " for node " + iInstr + "; Flat?  " + bFlat);

			return false;
		}

		while (_dblIRDiffTol < java.lang.Math.abs (dblIR - dblIRPrev)) {
			if (0 == --iNumIRIterations) {
				System.out.println ("Cannot calib IR for " + comp.getComponentName() + " and " + strMeasure +
					" " + dblCalibValue + " within limit!");

				return false;
			}

			dblIRPrev = dblIR;

			try {
				dblQ = comp.calcMeasureValue (valParams, pricerParams, 
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc,
						dcTSY, dcEDSF, null, null, null, mmFixings), quotingParams, strMeasure);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			if (!SetNode (dc, iInstr, bFlat, dblIRPrev + _dblIRIncr)) {
				System.out.println ("Cannot set IR = " + (dblIRPrev + _dblIRIncr) + " for node " + iInstr +
					"; Flat?  " + bFlat);

				return false;
			}

			try {
				dblDIRDQ = _dblIRIncr / (comp.calcMeasureValue (valParams, pricerParams,
					org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc,
						dcTSY, dcEDSF, null, null, null, mmFixings), quotingParams, strMeasure) - dblQ);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			dblIR = dblIRPrev + dblDIRDQ * (dblCalibValue - dblQ);

			if (java.lang.Double.isNaN (dblIR) || java.lang.Double.NEGATIVE_INFINITY == dblIR ||
				java.lang.Double.POSITIVE_INFINITY == dblIR) {
				System.out.println ("Component Maturity: " + comp.getMaturityDate() + "; Node maturity: " +
					dc.getNodeDate (iInstr));

				System.out.println ("Get " + dblIR + " for IR for " + comp.getComponentName() + "[" +
					strMeasure + ", flat = " + bFlat + "] and calib value " + dblCalibValue +
						" within limit!");

				return false;
			}

			if (!SetNode (dc, iInstr, bFlat, dblIR)) {
				System.out.println ("Cannot set IR = " + dblIR + " for node " + iInstr + "; Flat?  " +
					bFlat);

				return false;
			}
		}

		return true;
	}
}
