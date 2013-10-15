
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * DiscountCurve is the stub for the discount curve functionality. It extends the Curve object by exposing
 * 	the following functions:
 * 	- Forward Rate to a specific date/tenor, and effective rate between a date interval.
 * 	- Discount Factor to a specific date/tenor, and effective discount factor between a date interval.
 * 	- Zero Rate to a specific date/tenor.
 *  - Generate scenario curves from the base discount curve (flat/parallel/custom)
 *  - Value Jacobian for Forward rate, discount factor, and zero rate.
 *  - Cross Jacobian between each of Forward rate, discount factor, and zero rate.
 *  - Quote Jacobian to Forward rate, discount factor, and zero rate.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class DiscountCurve extends org.drip.service.stream.Serializer implements
	org.drip.analytics.definition.Curve {
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
	protected org.drip.analytics.definition.CurveConstructionInputSet _ccis = null;

	protected DiscountCurve (
		final double dblEpochDate,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.math.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate))
			throw new java.lang.Exception ("DiscountCurve ctr: Invalid Inputs");
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
	 * Calculate the Discount Factor to the given Date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor cannot be calculated
	 */

	public abstract double df (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Calculate the discount factor to the given date
	 * 
	 * @param dt Date
	 * 
	 * @return Discount factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double df (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DiscountCurve::df got null for date");

		return df (dt.getJulian());
	}

	/**
	 * Calculate the Discount Factor to the given Tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return Discount factor
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor cannot be calculated
	 */

	public double df (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::df got bad tenor");

		return df (epoch().addTenor (strTenor));
	}

	/**
	 * Compute the time-weighted discount factor between 2 dates
	 * 
	 * @param dblDate1 First Date
	 * @param dblDate2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double effectiveDF (
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

	/**
	 * Compute the time-weighted discount factor between 2 dates
	 * 
	 * @param dt1 First Date
	 * @param dt2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double effectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (null == dt1 || null == dt2)
			throw new java.lang.Exception ("DiscountCurve::effectiveDF => Got null for date");

		return effectiveDF (dt1.getJulian(), dt2.getJulian());
	}

	/**
	 * Compute the time-weighted discount factor between 2 tenors
	 * 
	 * @param strTenor1 First Date
	 * @param strTenor2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double effectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::effectiveDF => Got bad tenor");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return effectiveDF (dtStart.addTenor (strTenor1), dtStart.addTenor (strTenor2));
	}

	/**
	 * Compute the Forward Rate between two Dates
	 * 
	 * @param dblDt1 First Date
	 * @param dblDt2 Second Date
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate1) || !org.drip.math.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("DiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().getJulian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	/**
	 * Compute the Forward Rate between two Tenors
	 * 
	 * @param strTenor1 Tenor Start
	 * @param strTenor2 Tenor End
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public double forward (
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

	/**
	 * Calculate the implied rate to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Implied rate
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve::zero => Invalid Date");

		return forward (epoch().getJulian(), dblDate);
	}

	/**
	 * Calculate the implied rate to the given tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return Implied rate
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double zero (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::zero => Invalid date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return forward (dtStart.getJulian(), dtStart.addTenor (strTenor).getJulian());
	}

	/**
	 * Compute the LIBOR between 2 dates
	 * 
	 * @param dblDt1 First Date
	 * @param dblDt2 Second Date
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public double libor (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDt1) || !org.drip.math.common.NumberUtil.IsValid
			(dblDt2))
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid input dates");

		double dblStartDate = epoch().getJulian();

		if (dblDt1 < dblStartDate || dblDt2 < dblStartDate)
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid input dates");

		return ((df (dblDt1) / df (dblDt2)) - 1.) / org.drip.analytics.daycount.Convention.YearFraction
			(dblDt1, dblDt2, "Act/360", false, java.lang.Double.NaN, null, "");
	}

	/**
	 * Calculate the LIBOR to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR cannot be calculated
	 */

	public double libor (
		final double dblDate)
		throws java.lang.Exception
	{
		return libor (epoch().getJulian(), dblDate);
	}

	/**
	 * Calculate the LIBOR to the given tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR cannot be calculated
	 */

	public double libor (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve.libor got empty date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return libor (dtStart.getJulian(), dtStart.addTenor (strTenor).getJulian());
	}

	/**
	 * Calculate LIBOR between 2 tenors
	 * 
	 * @param strTenor1 Tenor start
	 * @param strTenor2 Tenor end
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception
	 */

	public double libor (
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

	/**
	 * Calculate the LIBOR DV01 to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return LIBOR DV01
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR DV01 cannot be calculated
	 */

	public double liborDV01 (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
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

	/**
	 * Estimates the estimated calibrated measure value for the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The estimated calibrated measure value
	 * 
	 * @throws java.lang.Exception Thrown if the estimated calibrated measure value cannot be computed
	 */

	public double estimateMeasure (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Invalid input");

		org.drip.product.definition.CalibratableComponent[] aCalibComp = calibComp();

		if (null == aCalibComp)
			throw new java.lang.Exception
				("DiscountCurve.estimateMeasure => Calib Components not available");

		org.drip.math.segment.PredictorResponseBuilderParams sbp = new
			org.drip.math.segment.PredictorResponseBuilderParams
				(org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.math.spline.PolynomialBasisSetParams (4),
						org.drip.math.segment.DesignInelasticParams.Create (2, 2), null);

		int iNumComponent = aCalibComp.length;
		double[] adblDate = new double[iNumComponent];
		double[] adblQuote = new double[iNumComponent];
		org.drip.math.segment.PredictorResponseBuilderParams[] aSBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumComponent - 1];

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

		org.drip.math.regime.MultiSegmentRegime regime =
			org.drip.math.regime.RegimeBuilder.CreateCalibratedRegimeEstimator ("DISC_CURVE_REGIME",
				adblDate, adblQuote, aSBP,
					org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
						org.drip.math.regime.MultiSegmentRegime.CALIBRATE);

		if (null == regime)
			throw new java.lang.Exception ("DiscountCurve.estimateMeasure => Cannot create Interp Regime");

		return regime.responseValue (dblDate);
	}

	/**
	 * Retrieve the Jacobian for the DF to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The Jacobian
	 */

	public abstract org.drip.math.calculus.WengertJacobian dfJack (
		final double dblDate);

	/**
	 * Retrieve the Jacobian for the DF for the given date
	 * 
	 * @param dt Date
	 * 
	 * @return The Jacobian
	 */

	public org.drip.math.calculus.WengertJacobian dfJack (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		return dfJack (dt.getJulian());
	}

	/**
	 * Retrieve the Jacobian for the DF for the given tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return The Jacobian
	 */

	public org.drip.math.calculus.WengertJacobian dfJack (
		final java.lang.String strTenor)
	{
		if (null == strTenor || strTenor.isEmpty()) return null;

		try {
			return dfJack (epoch().addTenor (strTenor));
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

	public org.drip.math.calculus.WengertJacobian compPVDFJack (
		final double dblDate)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibComp = calibComp();

		if (null == aCalibComp || 0 == aCalibComp.length) return null;

		int iNumParameters = 0;
		int iNumComponents = aCalibComp.length;
		org.drip.math.calculus.WengertJacobian wjCompPVDF = null;

		org.drip.param.valuation.ValuationParams valParams =
			org.drip.param.valuation.ValuationParams.CreateSpotValParams (dblDate);

		org.drip.param.definition.ComponentMarketParams mktParams = new
			org.drip.param.market.ComponentMarketParamSet (this, null, null, null, null, null, null,
				_ccis.getFixing());

		for (int i = 0; i < iNumComponents; ++i) {
			org.drip.math.calculus.WengertJacobian wjCompPVDFMicroJack = aCalibComp[i].calcPVDFMicroJack
				(valParams, null, mktParams, null);

			if (null == wjCompPVDFMicroJack) return null;

			if (null == wjCompPVDF) {
				try {
					wjCompPVDF = new org.drip.math.calculus.WengertJacobian (iNumComponents, iNumParameters =
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

	public org.drip.math.calculus.WengertJacobian compPVDFJack (
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

	public org.drip.math.calculus.WengertJacobian getForwardRateJack (
		final double dblDate1,
		final double dblDate2)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.math.common.NumberUtil.IsValid (dblDate2) || dblDate1 == dblDate2)
			return null;

		org.drip.math.calculus.WengertJacobian wj1 = dfJack (dblDate1);

		if (null == wj1) return null;

		org.drip.math.calculus.WengertJacobian wj2 = dfJack (dblDate2);

		if (null == wj2) return null;

		int iNumDFNodes = wj2.numParameters();

		double dblDF1 = java.lang.Double.NaN;
		double dblDF2 = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wjForwardRate = null;

		try {
			dblDF1 = df (dblDate1);

			dblDF2 = df (dblDate2);

			wjForwardRate = new org.drip.math.calculus.WengertJacobian (1, iNumDFNodes);
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

	public org.drip.math.calculus.WengertJacobian getForwardRateJack (
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

	public org.drip.math.calculus.WengertJacobian getZeroRateJack (
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

	public org.drip.math.calculus.WengertJacobian getZeroRateJack (
		final org.drip.analytics.date.JulianDate dt)
	{
		return getForwardRateJack (epoch(), dt);
	}
}
