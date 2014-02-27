
package org.drip.product.definition;

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
 * Component abstract class extends ComponentMarketParamRef and provides the following methods:
 *  - Get the component'sGet  initial notional, notional, and coupon.
 *  - Get the Effective date, Maturity date, First Coupon Date.
 *  - List the coupon periods.
 *  - Set the market curves - discount, TSY, forward, Credit, and EDSF curves.
 *  - Retrieve the component's settlement parameters.
 *  - Value the component using standard/custom market parameters.
 *  - Retrieve the component's named measures and named measure values.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class Component extends org.drip.service.stream.Serializer implements
	org.drip.product.definition.ComponentMarketParamRef {
	protected double getMeasure (
		final java.lang.String strMeasure,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalc)
		throws java.lang.Exception
	{
		if (null == strMeasure || strMeasure.isEmpty() || null == mapCalc || null == mapCalc.entrySet())
			throw new java.lang.Exception ("Component.getMeasure => Invalid Inputs");

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCalc.entrySet()) {
			if (null != me.getKey() && me.getKey().equalsIgnoreCase (strMeasure)) return me.getValue();
		}

		throw new java.lang.Exception ("Component.getMeasure => Invalid Measure: " + strMeasure);
	}

	protected boolean adjustPVDFMicroJackForCashSettle (
		final double dblSettleDate,
		final double dblPV,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack)
	{
		org.drip.quant.calculus.WengertJacobian wjCashSettleDFDF = dc.jackDDFDManifestMeasure
			(dblSettleDate, "Rate");

		if (null == wjCashSettleDFDF) return false;

		double dblDFCashSettle = java.lang.Double.NaN;

		int iNumParameters = wjCashSettleDFDF.numParameters();

		try {
			dblDFCashSettle = dc.df (dblSettleDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!wjPVDFMicroJack.scale (1. / dblDFCashSettle)) return false;

		double dblSettleJackAdjust = -1. * dblPV / dblDFCashSettle / dblDFCashSettle;

		for (int k = 0; k < iNumParameters; ++k) {
			if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, dblSettleJackAdjust *
				wjCashSettleDFDF.getFirstDerivative (0, k)))
				return false;
		}

		return true;
	}

	/**
	 * Get the Initial Notional for the Component
	 * 
	 * @return Initial Notional
	 * 
	 * @throws java.lang.Exception Thrown if Initial Notional cannot be computed
	 */

	public abstract double getInitialNotional()
		throws java.lang.Exception;

	/**
	 * Get the Notional for the Component at the given date
	 * 
	 * @param dblDate Double date input
	 * 
	 * @return Component Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional cannot be computed
	 */

	public abstract double getNotional (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Get the time-weighted Notional for the Component between 2 dates
	 * 
	 * @param dblDate1 Double date first
	 * @param dblDate2 Double date second
	 * 
	 * @return Component Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional cannot be computed
	 */

	public abstract double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception;

	/**
	 * Get the component's coupon at the given date
	 * 
	 * @param dblValue Valuation Date
	 * @param mktParams Component Market Parameters
	 * 
	 * @return Component's coupon
	 * 
	 * @throws java.lang.Exception Thrown if Component's coupon cannot be calculated
	 */

	public abstract double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception;

	/**
	 * Get the Effective Date
	 * 
	 * @return Effective Date
	 */

	public abstract org.drip.analytics.date.JulianDate getEffectiveDate();

	/**
	 * Get the Maturity Date
	 * 
	 * @return Maturity Date
	 */

	public abstract org.drip.analytics.date.JulianDate getMaturityDate();

	/**
	 * Get the First Coupon Date
	 * 
	 * @return First Coupon Date
	 */

	public abstract org.drip.analytics.date.JulianDate getFirstCouponDate();

	/**
	 * Set the component's IR, treasury, and credit curve names
	 * 
	 * @param strIR IR curve name
	 * @param strIRTSY Treasury Curve Name
	 * @param strCC Credit Curve Name
	 * 
	 * @return Success (True), Failure (false)
	 */

	public abstract boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC);

	/**
	 * Get the Component's Cash Flow Periods
	 * 
	 * @return List of the Component's Cash Flow Periods
	 */

	public abstract java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod();

	/**
	 * Get the component cash settlement parameters
	 * 
	 * @return Cash settlement Parameters
	 */

	public abstract org.drip.param.valuation.CashSettleParams getCashSettleParams();

	/**
	 * Generate a full list of the component measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams);

	/**
	 * Retrieve the ordered set of the measure names whose values will be calculated
	 * 
	 * @return Set of Measure Names
	 */

	public abstract java.util.Set<java.lang.String> getMeasureNames();

	/**
	 * Retrieve the Instrument's Imputed Tenor
	 * 
	 * @return The Instrument's Imputed Tenor
	 */

	public java.lang.String tenor()
	{
		double dblNumDays = getMaturityDate().getJulian() - getEffectiveDate().getJulian();

		if (365. > dblNumDays) return ((int) (0.5 + (dblNumDays / 30.))) + "M";

		 return ((int) (0.5 + (dblNumDays / 365.))) + "Y";
	}

	/**
	 * Calculate the value of the given component measure
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * @param strMeasure Measure String
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Double measure value
	 * 
	 * @throws java.lang.Exception Thrown if the measure cannot be calculated
	 */

	public double calcMeasureValue (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final java.lang.String strMeasure)
		throws java.lang.Exception
	{
		return getMeasure (strMeasure, value (valParams, pricerParams, mktParams, quotingParams));
	}

	/**
	 * Generate a full list of the component measures for the set of scenario market parameters present in
	 * 	the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return ComponentOutput object
	 */

	public org.drip.analytics.output.ComponentMeasures calcMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.MarketParams mpc,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == mpc || null == mpc.getScenCMP (this, "Base")) return null;

		org.drip.analytics.output.ComponentMeasures compOp = new
			org.drip.analytics.output.ComponentMeasures();

		long lStart = System.nanoTime();

		if (null == (compOp._mBase = value (valParams, pricerParams, mpc.getScenCMP (this, "Base"),
			quotingParams)))
			return null;

		if (null != mpc.getScenCMP (this, "FlatCreditBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCreditBumpUp = value
				(valParams, pricerParams, mpc.getScenCMP (this, "FlatCreditBumpUp"), quotingParams);

			if (null != mapCreditBumpUp && null != mapCreditBumpUp.entrySet()) {
				compOp._mFlatCreditDelta = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCreditBumpUp.entrySet())
				{
					if (null == me || null == me.getKey()) continue;

					compOp._mFlatCreditDelta.put (me.getKey(), me.getValue() - compOp._mBase.get
						(me.getKey()));
				}

				if (null != mpc.getScenCMP (this, "FlatCreditBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCreditBumpDn = value
						(valParams, pricerParams, mpc.getScenCMP (this, "FlatCreditBumpDn"), quotingParams);

					if (null != mapCreditBumpUp && null != mapCreditBumpDn.entrySet()) {
						compOp._mFlatCreditGamma = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapCreditBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp._mFlatCreditGamma.put (me.getKey(), me.getValue() + mapCreditBumpUp.get
								(me.getKey()) - 2. * compOp._mBase.get (me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.getScenCMP (this, "RRBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRRBumpUp = value (valParams,
				pricerParams, mpc.getScenCMP (this, "RRBumpUp"), quotingParams);

			compOp._mRRDelta = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			if (null != mapRRBumpUp && null != mapRRBumpUp.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapRRBumpUp.entrySet()) {
					if (null == me || null == me.getKey()) continue;

					compOp._mRRDelta.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()));
				}

				if (null != mpc.getScenCMP (this, "RRBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRRBumpDn = value
						(valParams, pricerParams, mpc.getScenCMP (this, "RRBumpDn"), quotingParams);

					if (null != mapRRBumpDn && null != mapRRBumpDn.entrySet()) {
						compOp._mRRGamma = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapRRBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp._mRRGamma.put (me.getKey(), me.getValue() + mapRRBumpUp.get (me.getKey())
								- 2. * compOp._mBase.get (me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.getScenCMP (this, "IRCreditBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRBumpUp = value (valParams,
				pricerParams, mpc.getScenCMP (this, "IRCreditBumpUp"), quotingParams);

			if (null != mapIRBumpUp && null != mapIRBumpUp.entrySet()) {
				compOp._mFlatIRDelta = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapIRBumpUp.entrySet()) {
					if (null == me || null == me.getKey()) continue;

					compOp._mFlatIRDelta.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()));
				}

				if (null != mpc.getScenCMP (this, "IRCreditBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRBumpDn = value
						(valParams, pricerParams, mpc.getScenCMP (this, "IRCreditBumpDn"), quotingParams);

					if (null != mapIRBumpDn && null != mapIRBumpDn.entrySet()) {
						compOp._mFlatIRGamma = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapIRBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp._mFlatIRGamma.put (me.getKey(), me.getValue() + mapIRBumpUp.get
								(me.getKey()) - 2. * compOp._mBase.get (me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.getCreditTenorCMP (this, true)) {
			compOp._mmTenorCreditDelta = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
				mapCCTenorUpCMP = mpc.getCreditTenorCMP (this, true);

			if (null != mapCCTenorUpCMP && null != mapCCTenorUpCMP.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentMarketParams>
					meTenorUpMP : mapCCTenorUpCMP.entrySet()) {
					if (null == meTenorUpMP || null == meTenorUpMP.getValue()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorUp = value
						(valParams, pricerParams, meTenorUpMP.getValue(), quotingParams);

					if (null == mapCCTenorUp || null == mapCCTenorUp.entrySet()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcUp = new
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
						mapCCTenorUp.entrySet()) {
						if (null == me || null == me.getKey()) continue;

						mapCalcUp.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()));
					}

					compOp._mmTenorCreditDelta.put (meTenorUpMP.getKey(), mapCalcUp);
				}

				if (null != mpc.getCreditTenorCMP (this, false)) {
					compOp._mmTenorCreditGamma = new
						org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
						mapCCTenorDnCMP = mpc.getCreditTenorCMP (this, false);

					if (null != mapCCTenorDnCMP && null != mapCCTenorDnCMP.entrySet()) {
						for (java.util.Map.Entry<java.lang.String,
							org.drip.param.definition.ComponentMarketParams> meTenorDnMP :
								mapCCTenorDnCMP.entrySet()) {
							if (null == meTenorDnMP || null == meTenorDnMP.getValue()) continue;

							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorDn =
								value (valParams, pricerParams, meTenorDnMP.getValue(), quotingParams);

							if (null == mapCCTenorDn || null == mapCCTenorDn.entrySet()) continue;

							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcDn = new
									org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

							for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
								mapCCTenorDn.entrySet()) {
								if (null == me || null == me.getKey()) continue;

								mapCalcDn.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()) +
									compOp._mmTenorCreditDelta.get (meTenorDnMP.getKey()).get (me.getKey()));
							}

							compOp._mmTenorCreditGamma.put (meTenorDnMP.getKey(), mapCalcDn);
						}
					}
				}
			}
		}

		if (null != mpc.getIRTenorCMP (this, true)) {
			compOp._mmTenorIRDelta = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
				mapIRTenorUpCMP = mpc.getIRTenorCMP (this, true);

			if (null != mapIRTenorUpCMP && null != mapIRTenorUpCMP.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentMarketParams>
					meTenorUpMP : mapIRTenorUpCMP.entrySet()) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorUp = value
						(valParams, pricerParams, meTenorUpMP.getValue(), quotingParams);

					if (null == mapCCTenorUp || null == mapCCTenorUp.entrySet()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcUp = new
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
						mapCCTenorUp.entrySet()) {
						if (null == me || null == me.getKey()) continue;

						mapCalcUp.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()));
					}

					compOp._mmTenorIRDelta.put (meTenorUpMP.getKey(), mapCalcUp);
				}
			}

			if (null != mpc.getIRTenorCMP (this, false)) {
				compOp._mmTenorIRGamma = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
					mapIRTenorDnCMP = mpc.getIRTenorCMP (this, false);

				if (null != mapIRTenorDnCMP & null != mapIRTenorDnCMP.entrySet()) {
					for (java.util.Map.Entry<java.lang.String,
						org.drip.param.definition.ComponentMarketParams> meTenorDnMP :
							mapIRTenorDnCMP.entrySet()) {
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorDn = value
							(valParams, pricerParams, meTenorDnMP.getValue(), quotingParams);

						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcDn = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

						if (null == mapCalcDn || null == mapCalcDn.entrySet()) continue;

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapCCTenorDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							mapCalcDn.put (me.getKey(), me.getValue() - compOp._mBase.get (me.getKey()) +
								compOp._mmTenorIRDelta.get (meTenorDnMP.getKey()).get (me.getKey()));
						}

						compOp._mmTenorIRGamma.put (meTenorDnMP.getKey(), mapCalcDn);
					}
				}
			}
		}

		compOp._dblCalcTime = (System.nanoTime() - lStart) * 1.e-09;

		return compOp;
	}

	/**
	 * Generate a full list of custom measures for the set of scenario market parameters present in
	 * 	the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param strCustomScenName Custom Scenario Name
	 * @param quotingParams Quoting Parameters
	 * @param mapBaseOP Base OP from used to calculate the desired delta measure. If null, the base OP will
	 * 			be generated.
	 * 
	 * @return Custom Scenarios Measures output set
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calcCustomScenarioMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.MarketParams mpc,
		final java.lang.String strCustomScenName,
		final org.drip.param.valuation.QuotingParams quotingParams,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBaseOP)
	{
		if (null == strCustomScenName || strCustomScenName.isEmpty() || null == valParams || null == mpc ||
			null == mpc.getScenCMP (this, strCustomScenName))
			return null;

		if (null == mapBaseOP) {
			org.drip.param.definition.ComponentMarketParams cmpBase = mpc.getScenCMP (this, "Base");

			if (null == cmpBase) return null;

			if (null == (mapBaseOP = value (valParams, pricerParams, cmpBase, quotingParams))) return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCustomOP = value (valParams,
			pricerParams, mpc.getScenCMP (this, strCustomScenName), quotingParams);

		if (null == mapCustomOP) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCustomOPDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (null != mapCustomOP && null != mapCustomOP.entrySet()) {
			for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCustomOP.entrySet()) {
				if (null == me || null == me.getKey()) continue;

				mapCustomOPDelta.put (me.getKey(), me.getValue() - mapBaseOP.get (me.getKey()));
			}
		}

		return mapCustomOPDelta;
	}
}
