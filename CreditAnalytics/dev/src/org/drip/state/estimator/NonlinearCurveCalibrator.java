
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * NonlinearCurveCalibrator calibrates the discount and credit/hazard curves from the components and their
 *  quotes.
 * 
 * NonlinearCurveCalibrator employs a set of techniques for achieving this calibration.
 * 	- It bootstraps the nodes in sequence to calibrate the curve.
 * 	- In conjunction with splining estimation techniques, it may also be used to perform dual sweep
 * 		calibration. The inner sweep achieves the calibration of the segment spline parameters, while the
 * 		outer sweep calibrates iteratively for the targeted boundary conditions.
 * 	- It may also be used to custom calibrate a single Interest Rate/Hazard Rate Node from the corresponding
 * 		Component
 * 
 * CurveCalibrator bootstraps/cooks both discount curves and credit curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NonlinearCurveCalibrator {
	class CreditCurveCalibrator extends org.drip.quant.function1D.AbstractUnivariate {
		private int _iInstr = -1;
		private boolean _bFlat = false;
		private java.lang.String _strMeasure = "";
		private double _dblCalibValue = java.lang.Double.NaN;
		private org.drip.product.definition.FixedIncomeComponent _comp = null;
		private org.drip.analytics.definition.ExplicitBootCreditCurve _cc = null;
		private org.drip.analytics.rates.DiscountCurve _dc = null;
		private org.drip.param.pricer.PricerParams _pricerParams = null;
		private org.drip.analytics.rates.DiscountCurve _dcTSY = null;
		private org.drip.param.valuation.ValuationParams _valParams = null;
		private org.drip.param.valuation.ValuationCustomizationParams _quotingParams = null;
		private java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

		public CreditCurveCalibrator (
			org.drip.analytics.definition.ExplicitBootCreditCurve cc,
			final org.drip.product.definition.FixedIncomeComponent comp,
			final int iInstr,
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.rates.DiscountCurve dc,
			final org.drip.analytics.rates.DiscountCurve dcTSY,
			final org.drip.param.pricer.PricerParams pricerParamsIn,
			final java.lang.String strMeasure,
			final double dblCalibValue,
			final java.util.Map<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
			final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
			final boolean bFlat)
			throws java.lang.Exception
		{
			super (null);

			_cc = cc;
			_dc = dc;
			_comp = comp;
			_bFlat = bFlat;
			_dcTSY = dcTSY;
			_iInstr = iInstr;
			_mmFixings = mmFixings;
			_valParams = valParams;
			_strMeasure = strMeasure;
			_dblCalibValue = dblCalibValue;
			_quotingParams = quotingParams;

			_pricerParams = new org.drip.param.pricer.PricerParams (pricerParamsIn._iUnitSize, new
				org.drip.param.definition.CalibrationParams (strMeasure, 0, null),
					pricerParamsIn._bSurvToPayDate, pricerParamsIn._iDiscretizationScheme);
		}

		@Override public double evaluate (
			final double dblRate)
			throws java.lang.Exception
		{
			if (!SetNode (_cc, _iInstr, _bFlat, dblRate))
				throw new java.lang.Exception ("Cannot set CC = " + dblRate + " for node #" + _iInstr);

			return _dblCalibValue - _comp.measureValue (_valParams, _pricerParams,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (_dc, _dcTSY,
					_cc, null, null, _mmFixings), _quotingParams, _strMeasure);
		}

		@Override public double integrate (
			final double dblBegin,
			final double dblEnd)
			throws java.lang.Exception
		{
			return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
		}
	}

	private static final boolean SetNode (
		final org.drip.analytics.definition.ExplicitBootCurve curve,
		final int iInstr,
		final boolean bFlat,
		final double dblValue)
	{
		if (!bFlat) return curve.setNodeValue (iInstr, dblValue);

		return curve.setFlatValue (dblValue);
	}

	private double calcCalibrationMetric (
		final org.drip.state.curve.NonlinearDiscountFactorDiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.product.definition.FixedIncomeComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String[] astrCalibMeasure,
		final double[] adblCalibValue,
		final double dblBump,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCalibLeftSlope)
		throws java.lang.Exception
	{
		if (!dc.initializeCalibrationRun (dblCalibLeftSlope))
			throw new java.lang.Exception
				("NonlinearCurveCalibrator::calcCalibrationMetric => Cannot initialize Calibration Run!");

		double[] adblNodeCalibOP = new double[aCalibComp.length];

		for (int i = 0; i < aCalibComp.length; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblNodeCalibOP[i] = calibrateIRNode (dc, dcTSY,
				aCalibComp[i], i, valParams, astrCalibMeasure[i], adblCalibValue[i] + dblBump, mmFixings,
					quotingParams, false, 0 == i ? java.lang.Double.NaN : adblNodeCalibOP[i - 1]))) {
				System.out.println ("\t\tCalibration failed for node #" + i);

				throw new java.lang.Exception
					("NonlinearCurveCalibrator::calcCalibrationMetric => Cannot calibrate node " + i +
						" for left slope " + dblCalibLeftSlope + "!");
			}
		}

		return dc.getCalibrationMetric();
	}

	private double calibrateIRCurve (
		final org.drip.state.curve.NonlinearDiscountFactorDiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.product.definition.FixedIncomeComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String[] astrCalibMeasure,
		final double[] adblCalibValue,
		final double dblBump,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
		throws java.lang.Exception
	{
		org.drip.quant.function1D.AbstractUnivariate ofIROuter = new
			org.drip.quant.function1D.AbstractUnivariate (null) {
			public double evaluate (
				final double dblShiftedLeftSlope)
				throws java.lang.Exception
			{
				return calcCalibrationMetric (dc, dcTSY, aCalibComp, valParams, astrCalibMeasure,
					adblCalibValue, dblBump, mmFixings, quotingParams, dblShiftedLeftSlope);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		org.drip.quant.solver1D.FixedPointFinderOutput rfop = new org.drip.quant.solver1D.FixedPointFinderBrent
			(0., ofIROuter, true).findRoot();

		if (null == rfop || !rfop.containsRoot())
			throw new java.lang.Exception ("NonlinearCurveCalibrator::calibrateIRCurve => Cannot get root!");

		return rfop.getRoot();
	}

	/**
	 * Construct an empty NonlinearCurveCalibrator
	 */

	public NonlinearCurveCalibrator()
	{
	}

	/**
	 * Calibrate a single Hazard Rate Node from the corresponding Component
	 * 
	 * @param cc The Credit Curve to be calibrated
	 * @param comp The Calibration Component
	 * @param iInstr The Calibration Instrument Index
	 * @param valParams Calibration Valuation Parameters
	 * @param dc The discount curve to be bootstrapped
	 * @param dcTSY The TSY discount curve
	 * @param pricerParamsIn Input Pricer Parameters
	 * @param strMeasure The Calibration Measure
	 * @param dblCalibValue The Value to be Calibrated to
	 * @param mmFixings Fixings Double Map
	 * @param quotingParams Quoting Parameters
	 * @param bFlat TRUE => Calibrate a Flat Curve across all Tenors
	 * 
	 * @return The successfully calibrated State Hazard Rate Point
	 * 
	 * @throws java.lang.Exception Thrown if the Bootstrapping is unsuccessful
	 */

	public boolean bootstrapHazardRate (
		org.drip.analytics.definition.ExplicitBootCreditCurve cc,
		final org.drip.product.definition.FixedIncomeComponent comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.param.pricer.PricerParams pricerParamsIn,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat)
	{
		if (null == cc || null == comp || null == valParams || null == dc || null == pricerParamsIn || null
			== strMeasure || strMeasure.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
				(dblCalibValue)) {
			System.out.println
				("NonlinearCurveCalibrator::bootstrapHazardRate => Invalid params into CurveCalibrator.bootstrapHazardRate!");

			return false;
		}

		try {
			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., new CreditCurveCalibrator (cc, comp,
					iInstr, valParams, dc, dcTSY, pricerParamsIn, strMeasure, dblCalibValue, mmFixings,
						quotingParams, bFlat), true).findRoot();

			return null != rfop && rfop.containsRoot();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate a single Interest Rate Node from the corresponding Component
	 * 
	 * @param dc The discount curve to be bootstrapped
	 * @param dcTSY The TSY discount curve
	 * @param comp The Calibration Component
	 * @param iInstr The Calibration Instrument Index
	 * @param valParams Calibration Valuation Parameters
	 * @param strMeasure The Calibration Measure
	 * @param dblCalibValue The Value to be Calibrated to
	 * @param mmFixings Fixings Double Map
	 * @param quotingParams Quoting Parameters
	 * @param bFlat TRUE => Calibrate a Flat Curve across all Tenors
	 * @param dblSearchStart State IR Start Point
	 * 
	 * @return The successfully calibrated State IR Point
	 * 
	 * @throws java.lang.Exception Thrown if the Bootstrapping is unsuccessful
	 */

	public double calibrateIRNode (
		final org.drip.analytics.rates.ExplicitBootDiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.product.definition.FixedIncomeComponent comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat,
		final double dblSearchStart)
		throws java.lang.Exception
	{
		if (null == dc || null == comp || null == valParams || null == strMeasure || strMeasure.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblCalibValue))
			throw new java.lang.Exception ("NonlinearCurveCalibrator::calibrateIRNode => Invalid inputs!");

		org.drip.quant.function1D.AbstractUnivariate ofIRNode = new
			org.drip.quant.function1D.AbstractUnivariate (null) {
			public double evaluate (
				final double dblValue)
				throws java.lang.Exception
			{
				if (!SetNode (dc, iInstr, bFlat, dblValue))
					throw new java.lang.Exception
						("NonlinearCurveCalibrator::calibrateIRNode => Cannot set Value = " + dblValue +
							" for node " + iInstr);

				return dblCalibValue - comp.measureValue (valParams, new
					org.drip.param.pricer.PricerParams (1, new org.drip.param.definition.CalibrationParams
						(strMeasure, 0, null), true, 0),
							org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
								(dc, dcTSY, null, null, null, mmFixings), quotingParams, strMeasure);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
			org.drip.quant.solver1D.FixedPointFinderBrent (0., ofIRNode, true).findRoot();

		if (null == rfop || !rfop.containsRoot())
			throw new java.lang.Exception
				("NonlinearCurveCalibrator::calibrateIRNode => Cannot calibrate IR segment for node #" +
					iInstr);

		return rfop.getRoot();
	}

	/**
	 * Boot-strap an interest rate curve from the set of calibration components
	 * 
	 * @param dc The discount curve to be bootstrapped
	 * @param dcTSY The TSY discount curve
	 * @param aCalibComp Array of the calibration components
	 * @param valParams Calibration Valuation Parameters
	 * @param astrCalibMeasure Array of Calibration Measures
	 * @param adblCalibValue Array of Calibration Values
	 * @param dblBump Amount to bump the Quotes by
	 * @param mmFixings Fixings Double Map
	 * @param quotingParams Quoting Parameters
	 * @param bFlat TRUE => Calibrate a Flat Curve across all Tenors
	 * 
	 * @return TRUE => Bootstrapping was successful
	 */

	public boolean bootstrapInterestRateSequence (
		final org.drip.analytics.rates.ExplicitBootDiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.product.definition.FixedIncomeComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String[] astrCalibMeasure,
		final double[] adblCalibValue,
		final double dblBump,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat)
	{
		if (null == dc || null == adblCalibValue || null == aCalibComp || null == astrCalibMeasure || 0 ==
			aCalibComp.length || adblCalibValue.length != aCalibComp.length || adblCalibValue.length !=
				astrCalibMeasure.length)
			return false;

		if (dc instanceof org.drip.state.curve.NonlinearDiscountFactorDiscountCurve)
			return bootstrapNonlinearInterestRateSequence
				((org.drip.state.curve.NonlinearDiscountFactorDiscountCurve) dc, dcTSY, aCalibComp,
					valParams, astrCalibMeasure, adblCalibValue, dblBump, mmFixings, quotingParams, bFlat);

		for (int i = 0; i < adblCalibValue.length; ++i) {
			try {
				if (!org.drip.quant.common.NumberUtil.IsValid (calibrateIRNode (dc, dcTSY, aCalibComp[i], i,
					valParams, astrCalibMeasure[i], adblCalibValue[i] + dblBump, mmFixings, quotingParams,
						false, java.lang.Double.NaN)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return true;
	}

	/**
	 * Boot-strap a non-linear interest rate curve from the set of calibration components
	 * 
	 * @param nldfdc The discount curve to be bootstrapped
	 * @param dcTSY The TSY discount curve
	 * @param aCalibComp Array of the calibration components
	 * @param valParams Calibration Valuation Parameters
	 * @param astrCalibMeasure Array of Calibration Measures
	 * @param adblCalibValue Array of Calibration Values
	 * @param dblBump Amount to bump the Quotes by
	 * @param mmFixings Fixings Double Map
	 * @param quotingParams Quoting Parameters
	 * @param bFlat TRUE => Calibrate a Flat Curve across all Tenors
	 * 
	 * @return TRUE => Bootstrapping was successful
	 */

	public boolean bootstrapNonlinearInterestRateSequence (
		final org.drip.state.curve.NonlinearDiscountFactorDiscountCurve nldfdc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.product.definition.FixedIncomeComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String[] astrCalibMeasure,
		final double[] adblCalibValue,
		final double dblBump,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final boolean bFlat)
	{
		if (null == nldfdc || null == adblCalibValue || null == aCalibComp || null == astrCalibMeasure || 0
			== aCalibComp.length || adblCalibValue.length != aCalibComp.length || adblCalibValue.length !=
				astrCalibMeasure.length)
			return false;

		try {
			calibrateIRCurve (nldfdc, dcTSY, aCalibComp, valParams, astrCalibMeasure, adblCalibValue,
				dblBump, mmFixings, quotingParams);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}
}
