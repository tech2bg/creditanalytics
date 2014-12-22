
package org.drip.analytics.rates;

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

public abstract class DiscountCurve implements org.drip.analytics.rates.DiscountFactorEstimator,
	org.drip.analytics.definition.Curve {
	private static final int NUM_DF_QUADRATURES = 5;

	protected java.lang.String _strCurrency = "";
	protected double _dblEpochDate = java.lang.Double.NaN;
	protected org.drip.analytics.rates.TurnListDiscountFactor _tldf = null;
	protected org.drip.analytics.input.CurveConstructionInputSet _ccis = null;

	private org.drip.param.valuation.CollateralizationParams _collatParams = null;

	protected DiscountCurve (
		final double dblEpochDate,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate))
			throw new java.lang.Exception ("DiscountCurve ctr: Invalid Inputs");

		_tldf = tldf;
		_collatParams = collatParams;
	}

	@Override public org.drip.state.identifier.LatentStateLabel label()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_strCurrency);
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

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return _collatParams;
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
		return turnAdjust (epoch().julian(), dblFinishDate);
	}

	@Override public double df (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DiscountCurve::df got null for date");

		return df (dt.julian());
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

		return effectiveDF (dt1.julian(), dt2.julian());
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

		return forward (dtStart.addTenor (strTenor1).julian(), dtStart.addTenor
			(strTenor2).julian());
	}

	@Override public double zero (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::zero => Invalid date");

		org.drip.analytics.date.JulianDate dtStart = epoch();

		return forward (dtStart.julian(), dtStart.addTenor (strTenor).julian());
	}

	@Override public double libor (
		final double dblDt1,
		final double dblDt2,
		final double dblDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDt1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDt2) || dblDt1 == dblDt2 || !org.drip.quant.common.NumberUtil.IsValid (dblDCF) || 0. ==
				dblDCF)
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid input dates");

		return ((df (dblDt1) / df (dblDt2)) - 1.) / dblDCF;
	}

	@Override public double libor (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDt1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDt2) || dblDt1 == dblDt2)
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid input dates");

		return libor (dblDt1, dblDt2, org.drip.analytics.daycount.Convention.YearFraction
			(dblDt1, dblDt2, "Act/360", false, null, ""));
	}

	@Override public double libor (
		final double dblDate,
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountCurve::libor => Invalid Inputs");

		double dblEndDate = new org.drip.analytics.date.JulianDate (dblDate).addTenor (strTenor).julian();

		return ((df (dblDate) / df (dblEndDate)) - 1.) / org.drip.analytics.daycount.Convention.YearFraction
			(dblDate, dblEndDate, "Act/360", false, null, "");
	}

	@Override public double libor (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("DiscountCurve::libor => Invalid Inputs");

		return libor (dt.julian(), strTenor);
	}

	@Override public double liborDV01 (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve::liborDV01 => Invalid Dates");

		java.lang.String strCurrency = currency();

		org.drip.analytics.date.JulianDate dtStart = epoch().addDays (2);

		org.drip.param.period.UnitCouponAccrualSetting ucasFixed = new
			org.drip.param.period.UnitCouponAccrualSetting (2, "Act/360", false, "Act/360", false,
				strCurrency, true,
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC);

		org.drip.param.period.ComposableFloatingUnitSetting cfusFloating = new
			org.drip.param.period.ComposableFloatingUnitSetting ("3M",
				org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
					org.drip.state.identifier.ForwardLabel.Standard (strCurrency + "-3M"),
						org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE, 0.);

		org.drip.param.period.ComposableFixedUnitSetting cfusFixed = new
			org.drip.param.period.ComposableFixedUnitSetting ("6M",
				org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null, 0., 0.,
					strCurrency);

		org.drip.param.period.CompositePeriodSetting cpsFloating = new
			org.drip.param.period.CompositePeriodSetting (4, "3M", strCurrency, null, -1., null, null, null,
				null);

		org.drip.param.period.CompositePeriodSetting cpsFixed = new
			org.drip.param.period.CompositePeriodSetting (2, "6M", strCurrency, null, 1., null, null, null,
				null);

		java.util.List<java.lang.Double> lsFixedStreamEdgeDate =
			org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtStart, new
				org.drip.analytics.date.JulianDate (dblDate), "6M", null,
					org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

		java.util.List<java.lang.Double> lsFloatingStreamEdgeDate =
			org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtStart, new
				org.drip.analytics.date.JulianDate (dblDate), "3M", null,
					org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

		org.drip.product.rates.Stream floatingStream = new org.drip.product.rates.Stream
			(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
				(lsFloatingStreamEdgeDate, cpsFloating, cfusFloating));

		org.drip.product.rates.Stream fixedStream = new org.drip.product.rates.Stream
			(org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit (lsFixedStreamEdgeDate,
				cpsFixed, ucasFixed, cfusFixed));

		org.drip.product.rates.FixFloatComponent irs = new org.drip.product.rates.FixFloatComponent
			(fixedStream, floatingStream, null);

		org.drip.param.market.LatentStateFixingsContainer lsfc = new
			org.drip.param.market.LatentStateFixingsContainer();

		lsfc.add (dtStart.addDays (2), irs.forwardLabel().get (0), 0.);

		org.drip.param.market.CurveSurfaceQuoteSet csqs = org.drip.param.creator.MarketParamsBuilder.Create
			(this, null, null, null, null, null, null, lsfc);

		return irs.measureValue (org.drip.param.valuation.ValuationParams.CreateValParams (dtStart, 0, "",
			org.drip.analytics.daycount.Convention.DATE_ROLL_ACTUAL), null, csqs, null, "FixedDV01");
	}

	@Override public double estimateManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == strManifestMeasure || strManifestMeasure.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountCurve::estimateManifestMeasure => Invalid input");

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = calibComp();

		if (null == aCalibComp)
			throw new java.lang.Exception
				("DiscountCurve::estimateManifestMeasure => Calib Components not available");

		org.drip.spline.params.SegmentCustomBuilderControl sbp = new
			org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

		int iNumComponent = aCalibComp.length;
		double[] adblDate = new double[iNumComponent];
		double[] adblQuote = new double[iNumComponent];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumComponent - 1];

		if (0 == iNumComponent)
			throw new java.lang.Exception
				("DiscountCurve::estimateManifestMeasure => Calib Components not available");

		for (int i = 0; i < iNumComponent; ++i) {
			if (0 != i) aSBP[i - 1] = sbp;

			if (null == aCalibComp[i])
				throw new java.lang.Exception
					("DiscountCurve::estimateManifestMeasure => Cannot locate a component");

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestMeasure =
				manifestMeasure (aCalibComp[i].primaryCode());

			if (null == mapManifestMeasure || !mapManifestMeasure.containsKey (strManifestMeasure))
				throw new java.lang.Exception
					("DiscountCurve::estimateManifestMeasure => Cannot locate the manifest measure");

			adblQuote[i] = mapManifestMeasure.get (strManifestMeasure);

			adblDate[i] = aCalibComp[i].maturityDate().julian();
		}

		org.drip.spline.stretch.MultiSegmentSequence regime =
			org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
				("DISC_CURVE_REGIME", adblDate, adblQuote, aSBP, null,
					org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
						org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE);

		if (null == regime)
			throw new java.lang.Exception
				("DiscountCurve::estimateManifestMeasure => Cannot create Interp Stretch");

		return regime.responseValue (dblDate);
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.input.CurveConstructionInputSet ccis)
	{
		if (null == ccis) return false;

		_ccis = ccis;
		return true;
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
		final org.drip.state.identifier.ForwardLabel fri);

	/**
	 * Retrieve the Latent State Quantification Metric
	 * 
	 * @return The Latent State Quantification Metric
	 */

	public abstract java.lang.String latentStateQuantificationMetric();

	/**
	 * Retrieve the Manifest Measure Jacobian of the Discount Factor to the given date
	 * 
	 * @param dblDate Date
	 * @param strManifestMeasure Manifest Measure
	 * 
	 * @return The Manifest Measure Jacobian of the Discount Factor to the given date
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final double dblDate,
		final java.lang.String strManifestMeasure);

	/**
	 * Retrieve the Manifest Measure Jacobian of the Discount Factor to the given date
	 * 
	 * @param dt Date
	 * @param strManifestMeasure Manifest Measure
	 * 
	 * @return The Manifest Measure Jacobian of the Discount Factor to the given date
	 */

	public org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strManifestMeasure)
	{
		if (null == dt) return null;

		return jackDDFDManifestMeasure (dt.julian(), strManifestMeasure);
	}

	/**
	 * Retrieve the Manifest Measure Jacobian of the Discount Factor to the date implied by the given Tenor
	 * 
	 * @param strTenor Tenor
	 * @param strManifestMeasure Manifest Measure
	 * 
	 * @return The Manifest Measure Jacobian of the Discount Factor to the date implied by the given Tenor
	 */

	public org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final java.lang.String strTenor,
		final java.lang.String strManifestMeasure)
	{
		if (null == strTenor || strTenor.isEmpty()) return null;

		try {
			return jackDDFDManifestMeasure (epoch().addTenor (strTenor), strManifestMeasure);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the Jacobian of PV at the given date to the Manifest Measure of each component in the
	 * 	calibration set to the DF
	 * 
	 * @param dblDate Date for which the Jacobian is needed
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian compJackDPVDManifestMeasure (
		final double dblDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = calibComp();

		if (null == aCalibComp || 0 == aCalibComp.length) return null;

		int iNumParameters = 0;
		int iNumComponents = aCalibComp.length;
		org.drip.quant.calculus.WengertJacobian wjCompPVDF = null;

		org.drip.param.valuation.ValuationParams valParams =
			org.drip.param.valuation.ValuationParams.CreateSpotValParams (dblDate);

		org.drip.param.market.CurveSurfaceQuoteSet csqs =
			org.drip.param.creator.MarketParamsBuilder.Create (this, null, null, null, null, null,
				null, null == _ccis ? null : _ccis.fixing());

		for (int i = 0; i < iNumComponents; ++i) {
			org.drip.quant.calculus.WengertJacobian wjCompDDirtyPVDManifestMeasure =
				aCalibComp[i].jackDDirtyPVDManifestMeasure (valParams, null, csqs, null);

			if (null == wjCompDDirtyPVDManifestMeasure) return null;

			iNumParameters = wjCompDDirtyPVDManifestMeasure.numParameters();

			if (null == wjCompPVDF) {
				try {
					wjCompPVDF = new org.drip.quant.calculus.WengertJacobian (iNumComponents,
						iNumParameters);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			for (int k = 0; k < iNumParameters; ++k) {
				if (!wjCompPVDF.accumulatePartialFirstDerivative (i, k,
					wjCompDDirtyPVDManifestMeasure.firstDerivative (0, k)))
					return null;
			}
		}

		return wjCompPVDF;
	}

	/**
	 * Calculate the Jacobian of PV at the given date to the Manifest Measure of each component in the
	 * 	calibration set to the DF
	 * 
	 * @param dt Date for which the Jacobian is needed
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian compJackDPVDManifestMeasure (
		final org.drip.analytics.date.JulianDate dt)
	{
		return null == dt ? null : compJackDPVDManifestMeasure (dt.julian());
	}

	/**
	 * Retrieve the Jacobian of the Forward Rate to the Manifest Measure between the given dates
	 * 
	 * @param dblDate1 Date 1
	 * @param dblDate2 Date 2
	 * @param strManifestMeasure Manifest Measure
	 * @param dblElapsedYear The Elapsed Year (in the appropriate Day Count) between dates 1 and 2
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final double dblDate1,
		final double dblDate2,
		final java.lang.String strManifestMeasure,
		final double dblElapsedYear)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2) || dblDate1 == dblDate2)
			return null;

		org.drip.quant.calculus.WengertJacobian wjDDFDManifestMeasureDate1 = jackDDFDManifestMeasure
			(dblDate1, strManifestMeasure);

		if (null == wjDDFDManifestMeasureDate1) return null;

		int iNumQuote = wjDDFDManifestMeasureDate1.numParameters();

		if (0 == iNumQuote) return null;

		org.drip.quant.calculus.WengertJacobian wjDDFDManifestMeasureDate2 = jackDDFDManifestMeasure
			(dblDate2, strManifestMeasure);

		if (null == wjDDFDManifestMeasureDate2 || iNumQuote != wjDDFDManifestMeasureDate2.numParameters())
			return null;

		double dblDF1 = java.lang.Double.NaN;
		double dblDF2 = java.lang.Double.NaN;
		org.drip.quant.calculus.WengertJacobian wjDForwardDManifestMeasure = null;

		try {
			dblDF1 = df (dblDate1);

			dblDF2 = df (dblDate2);

			wjDForwardDManifestMeasure = new org.drip.quant.calculus.WengertJacobian (1, iNumQuote);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblDForwardDManifestMeasure1iScale = 1. / dblDF2;
		double dblDForwardDManifestMeasure2iScale = dblDF1 / (dblDF2 * dblDF2);
		double dblInverseAnnualizedTenorLength = 1. / dblElapsedYear;

		for (int i = 0; i < iNumQuote; ++i) {
			double dblDForwardDQManifestMeasurei = ((wjDDFDManifestMeasureDate1.firstDerivative (0, i) *
				dblDForwardDManifestMeasure1iScale) - (wjDDFDManifestMeasureDate2.firstDerivative (0, i) *
					dblDForwardDManifestMeasure2iScale)) * dblInverseAnnualizedTenorLength;

			if (!wjDForwardDManifestMeasure.accumulatePartialFirstDerivative (0, i,
				dblDForwardDQManifestMeasurei))
				return null;
		}

		return wjDForwardDManifestMeasure;
	}

	/**
	 * Retrieve the Jacobian of the Forward Rate to the Manifest Measure between the given dates
	 * 
	 * @param dt1 Julian Date 1
	 * @param dt2 Julian Date 2
	 * @param strManifestMeasure Manifest Measure
	 * @param dblElapsedYear The Elapsed Year (in the appropriate Day Count) between dates 1 and 2
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final java.lang.String strManifestMeasure,
		final double dblElapsedYear)
	{
		if (null == dt1 || null == dt2) return null;

		return jackDForwardDManifestMeasure (dt1.julian(), dt2.julian(), strManifestMeasure,
			dblElapsedYear);
	}

	/**
	 * Retrieve the Jacobian of the Forward Rate to the Manifest Measure at the given date
	 * 
	 * @param dt Given Julian Date
	 * @param strTenor Tenor
	 * @param strManifestMeasure Manifest Measure
	 * @param dblElapsedYear The Elapsed Year (in the appropriate Day Count) implied by the Tenor
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strTenor,
		final java.lang.String strManifestMeasure,
		final double dblElapsedYear)
	{
		if (null == dt || null == strTenor || strTenor.isEmpty()) return null;

		return jackDForwardDManifestMeasure (dt.julian(), dt.addTenor (strTenor).julian(),
			strManifestMeasure, dblElapsedYear);
	}

	/**
	 * Retrieve the Jacobian for the Zero Rate to the given date
	 * 
	 * @param dblDate Date
	 * @param strManifestMeasure Manifest Measure
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian zeroRateJack (
		final double dblDate,
		final java.lang.String strManifestMeasure)
	{
		double dblEpochDate = epoch().julian();

		return jackDForwardDManifestMeasure (dblEpochDate, dblDate, strManifestMeasure, (dblDate -
			dblEpochDate) / 365.25);
	}

	/**
	 * Retrieve the Jacobian for the Zero Rate to the given date
	 * 
	 * @param dt Julian Date
	 * @param strManifestMeasure Manifest Measure
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian zeroRateJack (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strManifestMeasure)
	{
		return null == dt? null : zeroRateJack (dt.julian(), strManifestMeasure);
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
		if (null == strLatentStateQuantificationMetric ||
			(!org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE.equalsIgnoreCase
				(strLatentStateQuantificationMetric) && !
					org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR.equalsIgnoreCase
			(strLatentStateQuantificationMetric)))
			return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		boolean bFirstCashFlow = true;

		if (0 == iNumComp) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapCanonicalTruthness = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		if (org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR.equalsIgnoreCase
			(strLatentStateQuantificationMetric))
			mapCanonicalTruthness.put (_dblEpochDate, 1.);

		for (org.drip.product.definition.CalibratableFixedIncomeComponent cc : aCC) {
			if (null == cc) continue;

			java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod = cc.couponPeriods();

			if (null == lsCouponPeriod || 0 == lsCouponPeriod.size()) continue;

			for (org.drip.analytics.cashflow.CompositePeriod cpnPeriod : lsCouponPeriod) {
				if (null == cpnPeriod) continue;

				double dblPay = cpnPeriod.payDate();

				if (dblPay >= _dblEpochDate) {
					try {
						if (org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR.equalsIgnoreCase
							(strLatentStateQuantificationMetric))
							mapCanonicalTruthness.put (dblPay, df (dblPay));
						else if (org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE.equalsIgnoreCase
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
