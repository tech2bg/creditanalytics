
package org.drip.product.fx;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * FXForwardContract contains the FX forward product contract details - the effective date, the maturity
 *  date, the currency pair and the product code. It also exports a calibrator that computes the forward
 *  points from the discount curve. Finally additional functions serialize into and de-serialize out of byte
 *  arrays.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class FXForwardContract extends org.drip.product.definition.FXForward {
	private static final boolean s_bLog = false;

	private java.lang.String _strCode = "";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.CurrencyPair _ccyPair = null;

	/**
	 * Create an FXForwardContract from the currency pair, the effective and the maturity dates
	 * 
	 * @param ccyPair Currency Pair
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FXForwardContract (
		final org.drip.product.params.CurrencyPair ccyPair,
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity)
		throws java.lang.Exception
	{
		if (null == ccyPair || null == dtEffective || null == dtMaturity || dtEffective.julian() >=
			dtMaturity.julian())
			throw new java.lang.Exception ("FXForwardContract ctr: Invalid Inputs");

		_ccyPair = ccyPair;

		_dblMaturity = dtMaturity.julian();

		_dblEffective = dtEffective.julian();
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String[] secondaryCode()
	{
		java.lang.String strPrimaryCode = primaryCode();

		int iNumTokens = 0;
		java.lang.String astrCodeTokens[] = new java.lang.String[2];

		java.util.StringTokenizer stCodeTokens = new java.util.StringTokenizer (strPrimaryCode, ".");

		while (stCodeTokens.hasMoreTokens())
			astrCodeTokens[iNumTokens++] = stCodeTokens.nextToken();

		System.out.println (astrCodeTokens[0]);

		return new java.lang.String[] {astrCodeTokens[0]};
	}

	@Override public org.drip.analytics.date.JulianDate effectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.product.params.CurrencyPair currencyPair()
	{
		return _ccyPair;
	}

	@Override public double imply (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot,
		final boolean bFwdAsPIP)
		throws java.lang.Exception
	{
		if (null == valParams || null == dcNum || null == dcDenom ||
			!org.drip.quant.common.NumberUtil.IsValid (dblFXSpot))
			throw new java.lang.Exception ("FXForwardContract:: imply => Invalid params");

		double dblFXFwd = dblFXSpot * dcDenom.df (_dblMaturity) * dcNum.df (valParams.cashPayDate()) /
			dcNum.df (_dblMaturity) / dcDenom.df (valParams.cashPayDate());

		if (!bFwdAsPIP) return dblFXFwd;
		
		return (dblFXFwd - dblFXSpot) * _ccyPair.pipFactor();
	}

	@Override public double discountCurveBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot,
		final double dblMarketFXFwdPrice,
		final boolean bBasisOnDenom)
		throws java.lang.Exception
	{
		if (null == valParams || null == dcNum || null == dcDenom || !org.drip.quant.common.NumberUtil.IsValid
			(dblFXSpot))
			throw new java.lang.Exception ("FXForwardContract::calcDCBasis => Invalid params");

		return new FXBasisCalibrator (this).calibrateDCBasisFromFwdPriceNR (valParams, dcNum, dcDenom,
			dblFXSpot, dblMarketFXFwdPrice, bBasisOnDenom);
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblFXSpot)
	{
		if (null == valParams || null == dcNum || null == dcDenom || !org.drip.quant.common.NumberUtil.IsValid
			(dblFXSpot))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRes = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			mapRes.put ("FXFWD", imply (valParams, dcNum, dcDenom, dblFXSpot, false));

			mapRes.put ("FXOutright", imply (valParams, dcNum, dcDenom, dblFXSpot, false));

			mapRes.put ("Outright", imply (valParams, dcNum, dcDenom, dblFXSpot, false));

			mapRes.put ("FXFWDPIP", imply (valParams, dcNum, dcDenom, dblFXSpot, true));

			mapRes.put ("PIP", imply (valParams, dcNum, dcDenom, dblFXSpot, true));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return mapRes;
	}

	/**
	 * @author Lakshmi Krishnamurthy
	 *
	 * Calibrator for FXBasis - either bootstrapped or cumulative
	 */

	public class FXBasisCalibrator {
		private FXForwardContract _fxfwd = null;

		// DC Basis Calibration Stochastic Control

		private int _iNumIterations = 100;
		private double _dblBasisIncr = 0.0001;
		private double _dblBasisDiffTol = 0.0001;

		private final double calcFXFwd (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.rates.DiscountCurve dcNum,
			final org.drip.analytics.rates.DiscountCurve dcDenom,
			final double dblFXSpot,
			final double dblBump,
			final boolean bBasisOnDenom)
			throws java.lang.Exception {
			if (bBasisOnDenom)
				return _fxfwd.imply (valParams, dcNum, (org.drip.analytics.rates.DiscountCurve)
					dcDenom.parallelShiftQuantificationMetric (dblBump), dblFXSpot, false);

			return _fxfwd.imply (valParams, (org.drip.analytics.rates.DiscountCurve)
				dcNum.parallelShiftQuantificationMetric (dblBump), dcDenom, dblFXSpot, false);
		}

		/**
		 * Constructor: Construct the basis calibrator from the FXForward parent
		 * 
		 * @param fxfwd FXForward parent
		 * 
		 * @throws java.lang.Exception Thrown if parent is invalid
		 */

		public FXBasisCalibrator (
			final FXForwardContract fxfwd)
			throws java.lang.Exception
		{
			if (null == (_fxfwd = fxfwd))
				throw new java.lang.Exception ("FXForwardContract::FXBasisCalibrator ctr: Invalid Inputs");
		}

		/**
		 * Calibrate the discount curve basis from FXForward using Newton-Raphson methodology
		 * 
		 * @param valParams ValuationParams
		 * @param dcNum Discount Curve for the Numerator
		 * @param dcDenom Discount Curve for the Denominator
		 * @param dblFXSpot FXSpot value
		 * @param dblMarketFXFwdPrice FXForward market value
		 * @param bBasisOnDenom True - Basis is set on the denominator
		 * 
		 * @return Calibrated DC basis
		 * 
		 * @throws java.lang.Exception Thrown if cannot calibrate
		 */

		public double calibrateDCBasisFromFwdPriceNR (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.rates.DiscountCurve dcNum,
			final org.drip.analytics.rates.DiscountCurve dcDenom,
			final double dblFXSpot,
			final double dblMarketFXFwdPrice,
			final boolean bBasisOnDenom)
			throws java.lang.Exception
		{
			if (null == valParams || null == dcNum || null == dcDenom ||
				!org.drip.quant.common.NumberUtil.IsValid (dblMarketFXFwdPrice) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblFXSpot))
				throw new java.lang.Exception
					("FXForwardContract::calibrateDCBasisFromFwdPriceNR => bad inputs");

			double dblFXFwdBase = _fxfwd.imply (valParams, dcNum, dcDenom, dblFXSpot, false);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblFXFwdBase))
				throw new java.lang.Exception
					("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot imply FX Fwd Base!");

			double dblFXFwdBumped = calcFXFwd (valParams, dcNum, dcDenom, dblFXSpot, _dblBasisIncr,
				bBasisOnDenom);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblFXFwdBumped))
				throw new java.lang.Exception
					("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot imply FX Fwd for " +
						_dblBasisIncr + " shift!");

			double dblDBasisDFXFwd = _dblBasisIncr / (dblFXFwdBumped - dblFXFwdBase);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblDBasisDFXFwd))
				throw new java.lang.Exception
					("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot calculate Fwd/Basis Slope for 0 basis!");

			double dblBasisPrev = 0.;
			double dblBasis = dblDBasisDFXFwd * (dblMarketFXFwdPrice - dblFXFwdBase);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblBasis))
				throw new java.lang.Exception ("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Got " +
					dblBasis + " for FlatSpread for " + _fxfwd.primaryCode() + " and price " + dblFXFwdBase);

			while (_dblBasisDiffTol < java.lang.Math.abs (dblBasis - dblBasisPrev)) {
				if (0 == --_iNumIterations)
					throw new java.lang.Exception
						("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot calib Basis for " +
							_fxfwd.primaryCode() + " and price " + dblMarketFXFwdPrice + " within limit!");

				if (!org.drip.quant.common.NumberUtil.IsValid (dblFXFwdBase = calcFXFwd (valParams, dcNum,
					dcDenom, dblFXSpot, dblBasisPrev = dblBasis, bBasisOnDenom)))
					throw new java.lang.Exception
						("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot imply FX Fwd for " +
							dblBasis + " shift!");

				if (!org.drip.quant.common.NumberUtil.IsValid (dblFXFwdBumped = calcFXFwd (valParams, dcNum,
					dcDenom, dblFXSpot, dblBasis + _dblBasisIncr, bBasisOnDenom)))
					throw new java.lang.Exception
						("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot imply FX Fwd for " +
							(dblBasis + _dblBasisIncr) + " shift!");

				if (!org.drip.quant.common.NumberUtil.IsValid (dblDBasisDFXFwd = _dblBasisIncr /
					(dblFXFwdBumped - dblFXFwdBase)))
					throw new java.lang.Exception
						("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Cannot calculate Fwd/Basis Slope for "
							+ (dblBasis + _dblBasisIncr) + " basis!");

				if (s_bLog) System.out.println ("\tFXFwd[" + dblBasis + "]=" + dblFXFwdBase);

				dblBasis = dblBasisPrev + dblDBasisDFXFwd * (dblMarketFXFwdPrice - dblFXFwdBase);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblBasis))
					throw new java.lang.Exception
						("FXForwardContract::calibrateDCBasisFromFwdPriceNR => Got " + dblBasis +
							" for FlatSpread for " + _fxfwd.primaryCode() + " and price " + dblFXFwdBase);
			}

			return dblBasis;
		}
	}
}
