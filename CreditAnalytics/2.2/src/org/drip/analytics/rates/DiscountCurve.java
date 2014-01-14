
package org.drip.analytics.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * DiscountCurve is the stub for the discount curve functionality. It extends the both the Curve and the
 *  DiscountFactorEstimator instances by implementing their functions, and exposing the following:
 * 	- Forward Rate to a specific date/tenor, and effective rate between a date interval.
 * 	- Discount Factor to a specific date/tenor, and effective discount factor between a date interval.
 * 	- Zero Rate to a specific date/tenor.
 *  - Value Jacobian for Forward rate, discount factor, and zero rate.
 *  - Cross Jacobian between each of Forward rate, discount factor, and zero rate.
 *  - Quote Jacobian to Forward rate, discount factor, and zero rate.
 *  - QM (DF/Zero/Forward) to Quote Jacobian.
 *  - Latent State Quantification Metric, and the canonical truthness transformations.
 *  - Implied/embedded ForwardRateEstimator
 *  - Turns - set/unset/adjust.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class DiscountCurve extends org.drip.service.stream.Serializer implements
	org.drip.analytics.rates.DiscountFactorEstimator, org.drip.analytics.definition.Curve {
	private static final int NUM_DF_QUADRATURES = 5;

	/**
	 * Discount Latent State
	 */

	public static final java.lang.String LATENT_STATE_DISCOUNT = "LATENT_STATE_DISCOUNT";

	/**
	 * Discount Latent State Quantification Metric - Discount Factor
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_DISCOUNT_FACTOR =
		"QUANTIFICATION_METRIC_DISCOUNT_FACTOR";

	/**
	 * Discount Latent State Quantification Metric - Zero Rate
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_ZERO_RATE =
		"QUANTIFICATION_METRIC_ZERO_RATE";

	/**
	 * Discount Latent State Quantification Metric - Forward Rate
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_FORWARD_RATE =
		"QUANTIFICATION_METRIC_FORWARD_RATE";

	protected java.lang.String _strCurrency = "";
	protected double _dblEpochDate = java.lang.Double.NaN;
	protected org.drip.analytics.rates.TurnListDiscountFactor _tldf = null;
	protected org.drip.analytics.definition.CurveConstructionInputSet _ccis = null;

	protected DiscountCurve (
		final double dblEpochDate,
		final java.lang.String strCurrency,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate))
			throw new java.lang.Exception ("DiscountCurve ctr: Invalid Inputs");

		_tldf = tldf;
	}

	@Override public java.lang.String name()
	{
		return _strCurrency;
	}

	@Override public java.lang.String currency()
	{
		return _strCurrency;
	}

	@Override public org.drip.analytics.date.JulianDate epoch()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEpochDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Set the Discount Curve Turns'
	 * 
	 * @param tldf Turn List Discount Factor
	 * 
	 * @return TRUE => Valid Turn List Discount Factor Set
	 */

	public boolean setTurns (
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		return null != (_tldf = tldf);
	}

	/**
	 * Apply the Turns' DF Adjustment
	 * 
	 * @param dblStartDate Turn Start Date
	 * @param dblFinishDate Turn Finish Date
	 * 
	 * @return Turns' DF Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double turnAdjust (
		final double dblStartDate,
		final double dblFinishDate)
		throws java.lang.Exception
	{
		return null == _tldf ? 1. : _tldf.turnAdjust (dblStartDate, dblFinishDate);
	}

	/**
	 * Apply the Turns' DF Adjustment
	 * 
	 * @param dblFinishDate Turn Finish Date
	 * 
	 * @return Turns' DF Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	protected double turnAdjust (
		final double dblFinishDate)
		throws java.lang.Exception
	{
		return turnAdjust (epoch().getJulian(), dblFinishDate);
	}

	@Override public double df (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DiscountCurve::df got null for date");

		return df (dt.getJulian());
	}

	@Override public double df (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::df got bad tenor");

		return df (epoch().addTenor (strTenor));
	}

	@Override public double effectiveDF (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (dblDate1 == dblDate2) return df (dblDate1);

		int iNumQuadratures = 0;
		double dblEffectiveDF = 0.;
		double dblQuadratureWidth = (dblDate2 - dblDate1) / NUM_DF_QUADRATURES;

		for (double dblDate = dblDate1; dblDate <= dblDate2; dblDate += dblQuadratureWidth) {
			++iNumQuadratures;

			dblEffectiveDF += (df (dblDate) + df (dblDate + dblQuadratureWidth));
		}

		return dblEffectiveDF / (2. * iNumQuadratures);
	}

	@Override public double effectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (null == dt1 || null == dt2)
			throw new java.lang.Exception ("DiscountCurve::effectiveDF => Got null for date");

		return effectiveDF (dt1.getJulian(), dt2.getJulian());
	}

	@Override public double effectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::effectiveDF => Got bad tenor");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return effectiveDF (dtStart.addTenor (strTenor1), dtStart.addTenor (strTenor2));
	}

	@Override public double forward (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::forward => Invalid Date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return forward (dtStart.addTenor (strTenor1).getJulian(), dtStart.addTenor
			(strTenor2).getJulian());
	}

	@Override public double zero (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::zero => Invalid date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return forward (dtStart.getJulian(), dtStart.addTenor (strTenor).getJulian());
	}

	@Override public double libor (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDt1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDt2))
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid input dates");

		return ((df (dblDt1) / df (dblDt2)) - 1.) / org.drip.analytics.daycount.Convention.YearFraction
			(dblDt1, dblDt2, "Act/360", false, java.lang.Double.NaN, null, "");
	}

	@Override public double libor (
		final double dblDate)
		throws java.lang.Exception
	{
		return libor (epoch().getJulian(), dblDate);
	}

	@Override public double libor (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve.libor got empty date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return libor (dtStart.getJulian(), dtStart.addTenor (strTenor).getJulian());
	}

	@Override public double libor (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::libor got empty date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return libor (dtStart.addTenor (strTenor1).getJulian(), dtStart.addTenor
			(strTenor2).getJulian());
	}

	@Override public double liborDV01 (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve::liborDV01 got NaN for date");

		org.drip.analytics.date.JulianDate dtStart = epoch().addDays (2);

		java.lang.String strCurrency = currency();

		java.lang.String strIndex = strCurrency + "-LIBOR-6M";

		org.drip.product.definition.RatesComponent irs =
			org.drip.product.creator.RatesStreamBuilder.CreateIRS (dtStart, new
				org.drip.analytics.date.JulianDate (dblDate), 0., strCurrency, strIndex, strCurrency);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put (strIndex, 0.);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (dtStart, mIndexFixings);

		org.drip.param.market.ComponentMarketParamSet cmp = new org.drip.param.market.ComponentMarketParamSet
			(this, null, null, null, null, null, null, mmFixings);

		return irs.calcMeasureValue (org.drip.param.valuation.ValuationParams.CreateValParams (dtStart, 0,
			"", org.drip.analytics.daycount.Convention.DR_ACTUAL), null, cmp, null, "FixedDV01");
	}

	@Override public double estimateMeasure (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Invalid input");

		org.drip.product.definition.CalibratableComponent[] aCalibComp = calibComp();

		if (null == aCalibComp)
			throw new java.lang.Exception
				("DiscountCurve.estimateMeasure => Calib Components not available");

		org.drip.spline.params.SegmentCustomBuilderControl sbp = new
			org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4),
						org.drip.spline.params.SegmentDesignInelasticControl.Create (2, 2), null);

		int iNumComponent = aCalibComp.length;
		double[] adblDate = new double[iNumComponent];
		double[] adblQuote = new double[iNumComponent];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumComponent - 1];

		if (0 == iNumComponent)
			throw new java.lang.Exception
				("DiscountCurve.estimateMeasure => Calib Components not available");

		org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = lsmm();

		if (null == aLSMM || iNumComponent != aLSMM.length)
			throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Calib Quotes not available");

		for (int i = 0; i < iNumComponent; ++i) {
			if (0 != i) aSBP[i - 1] = sbp;

			if (null == aCalibComp[i])
				throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Cannot locate a component");

			adblQuote[i] = aLSMM[i].getMeasureQuoteValue();

			adblDate[i] = aCalibComp[i].getMaturityDate().getJulian();
		}

		org.drip.spline.stretch.MultiSegmentSequence regime =
			org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
				("DISC_CURVE_REGIME", adblDate, adblQuote, aSBP, null,
					org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
						org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE);

		if (null == regime)
			throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Cannot create Interp Stretch");

		return regime.responseValue (dblDate);
	}

	/**
	 * Retrieve the Forward Curve that might be implied by the Latent State of this Discount Curve Instance
	 * 	corresponding to the specified Floating Rate Index
	 * 
	 * @param fri The Floating Rate Index
	 * 
	 * @return The Forward Curve Implied by the Discount Curve Latent State
	 */

	public abstract org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator (
		final double dblDate,
		final org.drip.product.params.FloatingRateIndex fri);

	/**
	 * Retrieve the Latent State Quantification Metric
	 * 
	 * @return The Latent State Quantification Metric
	 */

	public abstract java.lang.String latentStateQuantificationMetric();

	/**
	 * Retrieve the Quote Jacobian of the Discount Factor to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The Quote Jacobian of the Discount Factor to the given date
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDDFDQuote (
		final double dblDate);

	/**
	 * Retrieve the Quote Jacobian of the Discount Factor to the given date
	 * 
	 * @param dt Date
	 * 
	 * @return The Quote Jacobian of the Discount Factor to the given date
	 */

	public org.drip.quant.calculus.WengertJacobian jackDDFDQuote (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		return jackDDFDQuote (dt.getJulian());
	}

	/**
	 * Retrieve the Quote Jacobian of the Discount Factor to the date implied by the given Tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return The Quote Jacobian of the Discount Factor to the date implied by the given Tenor
	 */

	public org.drip.quant.calculus.WengertJacobian jackDDFDQuote (
		final java.lang.String strTenor)
	{
		if (null == strTenor || strTenor.isEmpty()) return null;

		try {
			return jackDDFDQuote (epoch().addTenor (strTenor));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the Jacobian of PV at the given date for each component in the calibration set to the DF
	 * 
	 * @param dblDate Date for which the Jacobian is needed
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian compPVDFJack (
		final double dblDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibComp = calibComp();

		if (null == aCalibComp || 0 == aCalibComp.length) return null;

		int iNumParameters = 0;
		int iNumComponents = aCalibComp.length;
		org.drip.quant.calculus.WengertJacobian wjCompPVDF = null;

		org.drip.param.valuation.ValuationParams valParams =
			org.drip.param.valuation.ValuationParams.CreateSpotValParams (dblDate);

		org.drip.param.definition.ComponentMarketParams mktParams = new
			org.drip.param.market.ComponentMarketParamSet (this, null, null, null, null, null, null,
				_ccis.getFixing());

		for (int i = 0; i < iNumComponents; ++i) {
			org.drip.quant.calculus.WengertJacobian wjCompPVDFMicroJack = aCalibComp[i].calcPVDFMicroJack
				(valParams, null, mktParams, null);

			if (null == wjCompPVDFMicroJack) return null;

			if (null == wjCompPVDF) {
				try {
					wjCompPVDF = new org.drip.quant.calculus.WengertJacobian (iNumComponents, iNumParameters =
						wjCompPVDFMicroJack.numParameters());
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			for (int k = 0; k < iNumParameters; ++k) {
				if (!wjCompPVDF.accumulatePartialFirstDerivative (i, k,
					wjCompPVDFMicroJack.getFirstDerivative (0, k)))
					return null;
			}
		}

		return wjCompPVDF;
	}

	/**
	 * Calculate the Jacobian of PV at the given date for each component in the calibration set to the DF
	 * 
	 * @param dt Date for which the Jacobian is needed
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian compPVDFJack (
		final org.drip.analytics.date.JulianDate dt)
	{
		return null == dt ? null : compPVDFJack (dt.getJulian());
	}

	/**
	 * Retrieve the Jacobian for the Forward Rate between the given dates
	 * 
	 * @param dblDate1 Date 1
	 * @param dblDate2 Date 2
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian getForwardRateJack (
		final double dblDate1,
		final double dblDate2)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2) || dblDate1 == dblDate2)
			return null;

		org.drip.quant.calculus.WengertJacobian wj1 = jackDDFDQuote (dblDate1);

		if (null == wj1) return null;

		org.drip.quant.calculus.WengertJacobian wj2 = jackDDFDQuote (dblDate2);

		if (null == wj2) return null;

		int iNumDFNodes = wj2.numParameters();

		double dblDF1 = java.lang.Double.NaN;
		double dblDF2 = java.lang.Double.NaN;
		org.drip.quant.calculus.WengertJacobian wjForwardRate = null;

		try {
			dblDF1 = df (dblDate1);

			dblDF2 = df (dblDate2);

			wjForwardRate = new org.drip.quant.calculus.WengertJacobian (1, iNumDFNodes);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumDFNodes; ++i) {
			double dblDForwardDDFi = 365.25 * ((wj1.getFirstDerivative (0, i) / dblDF1) -
				(wj2.getFirstDerivative (0, i) / dblDF2)) / (dblDate2 - dblDate1);

			if (!wjForwardRate.accumulatePartialFirstDerivative (0, i, dblDForwardDDFi)) return null;
		}

		return wjForwardRate;
	}

	/**
	 * Retrieve the Jacobian for the Forward Rate between the given dates
	 * 
	 * @param dt1 Julian Date 1
	 * @param dt2 Julian Date 2
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian getForwardRateJack (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
	{
		if (null == dt1 || null == dt2) return null;

		return getForwardRateJack (dt1.getJulian(), dt2.getJulian());
	}

	/**
	 * Retrieve the Jacobian for the Zero Rate to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian getZeroRateJack (
		final double dblDate)
	{
		return getForwardRateJack (epoch().getJulian(), dblDate);
	}

	/**
	 * Retrieve the Jacobian for the Zero Rate to the given date
	 * 
	 * @param dt Julian Date
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian getZeroRateJack (
		final org.drip.analytics.date.JulianDate dt)
	{
		return getForwardRateJack (epoch(), dt);
	}

	/**
	 * Convert the inferred Formulation Constraint into a "Truthness" Entity
	 * 
	 * @param strLatentStateQuantificationMetric Latent State Quantification Metric
	 * 
	 * @return Map of the Truthness Entities
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> canonicalTruthness (
		final java.lang.String strLatentStateQuantificationMetric)
	{
		if (null == strLatentStateQuantificationMetric || (!QUANTIFICATION_METRIC_ZERO_RATE.equalsIgnoreCase
			(strLatentStateQuantificationMetric) && !QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
				(strLatentStateQuantificationMetric)))
			return null;

		org.drip.product.definition.CalibratableComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		boolean bFirstCashFlow = true;

		if (0 == iNumComp) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapCanonicalTruthness = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		if (QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase (strLatentStateQuantificationMetric))
			mapCanonicalTruthness.put (_dblEpochDate, 1.);

		for (org.drip.product.definition.CalibratableComponent cc : aCC) {
			if (null == cc) continue;

			java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod = cc.getCashFlowPeriod();

			if (null == lsCouponPeriod || 0 == lsCouponPeriod.size()) continue;

			for (org.drip.analytics.period.CashflowPeriod cpnPeriod : cc.getCashFlowPeriod()) {
				if (null == cpnPeriod) continue;

				double dblPay = cpnPeriod.getPayDate();

				if (dblPay >= _dblEpochDate) {
					try {
						if (QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
							(strLatentStateQuantificationMetric))
							mapCanonicalTruthness.put (dblPay, df (dblPay));
						else if (QUANTIFICATION_METRIC_ZERO_RATE.equalsIgnoreCase
							(strLatentStateQuantificationMetric)) {
							if (bFirstCashFlow) {
								bFirstCashFlow = false;

								mapCanonicalTruthness.put (_dblEpochDate, zero (dblPay));
							}

							mapCanonicalTruthness.put (dblPay, zero (dblPay));
						}
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				}
			}
		}

		return mapCanonicalTruthness;
	}
}
