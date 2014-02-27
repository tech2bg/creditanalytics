
package org.drip.service.api;

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
 * ProductDailyPnL contains the following daily measures computed:
 * 	- 1D Carry, Roll Down, Curve Shift, and Full Return PnL
 * 	- 3D Carry and Roll Down PnL
 * 	- 3M Carry and Roll Down PnL
 * 	- Current DV01
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ProductDailyPnL {
	private double _dblDV01 = java.lang.Double.NaN;
	private double _dbl1DCarry = java.lang.Double.NaN;
	private double _dbl1MCarry = java.lang.Double.NaN;
	private double _dbl3MCarry = java.lang.Double.NaN;
	private double _dbl1DFixedDCF = java.lang.Double.NaN;
	private double _dbl1MFixedDCF = java.lang.Double.NaN;
	private double _dbl3MFixedDCF = java.lang.Double.NaN;
	private double _dbl1DRollDown = java.lang.Double.NaN;
	private double _dbl1MRollDown = java.lang.Double.NaN;
	private double _dbl3MRollDown = java.lang.Double.NaN;
	private double _dbl1DCurveShift = java.lang.Double.NaN;
	private double _dbl1DCleanReturn = java.lang.Double.NaN;
	private double _dbl1DDirtyReturn = java.lang.Double.NaN;
	private double _dbl1DTotalReturn = java.lang.Double.NaN;
	private double _dbl1DFloatingDCF = java.lang.Double.NaN;
	private double _dbl1MFloatingDCF = java.lang.Double.NaN;
	private double _dbl3MFloatingDCF = java.lang.Double.NaN;
	private double _dblPeriodFixedRate = java.lang.Double.NaN;
	private double _dblBaselineSwapRate = java.lang.Double.NaN;
	private double _dbl1DRolldownSwapRate = java.lang.Double.NaN;
	private double _dbl1MRolldownSwapRate = java.lang.Double.NaN;
	private double _dbl3MRolldownSwapRate = java.lang.Double.NaN;
	private double _dbl1DCurveShiftSwapRate = java.lang.Double.NaN;
	private double _dblPeriodCurveFloatingRate = java.lang.Double.NaN;
	private double _dblPeriodProductFloatingRate = java.lang.Double.NaN;

	/**
	 * ProductDailyPnL constructor
	 * 
	 * @param dbl1DTotalReturn 1D Total Return PnL
	 * @param dbl1DCleanReturn 1D Clean Return PnL
	 * @param dbl1DDirtyReturn 1D Dirty Return PnL
	 * @param dbl1DCarry 1D Carry PnL
	 * @param dbl1DRollDown 1D Roll Down PnL
	 * @param dbl1DCurveShift 1D Curve Shift PnL
	 * @param dbl1MCarry 1M Carry PnL
	 * @param dbl1MRollDown 1M Roll Down PnL
	 * @param dbl3MCarry 3M Carry PnL
	 * @param dbl3MRollDown 3M Roll Down PnL
	 * @param dblDV01 DV01
	 * @param dblBaselineSwapRate Baseline Par Swap Rate
	 * @param dbl1DRolldownSwapRate 1D Curve Roll down implied Par Swap rate
	 * @param dbl1MRolldownSwapRate 1M Curve Roll down implied Par Swap rate
	 * @param dbl3MRolldownSwapRate 3M Curve Roll down implied Par Swap rate
	 * @param dbl1DCurveShiftSwapRate 1D Day-to-Day Curve Shift implied Par Swap rate
	 * @param dblPeriodFixedRate The Period Fixed Rate
	 * @param dblPeriodCurveFloatingRate The Period Curve Floating Rate
	 * @param dblPeriodProductFloatingRate The Period Product Floating Rate
	 * @param dbl1DFixedDCF 1D Fixed Coupon DCF
	 * @param dbl1DFloatingDCF 1D Floating Coupon DCF
	 * @param dbl1MFixedDCF 1M Fixed Coupon DCF
	 * @param dbl1MFloatingDCF 1M Floating Coupon DCF
	 * @param dbl3MFixedDCF 3M Fixed Coupon DCF
	 * @param dbl3MFloatingDCF 3M Floating Coupon DCF
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ProductDailyPnL (
		final double dbl1DTotalReturn,
		final double dbl1DCleanReturn,
		final double dbl1DDirtyReturn,
		final double dbl1DCarry,
		final double dbl1DRollDown,
		final double dbl1DCurveShift,
		final double dbl1MCarry,
		final double dbl1MRollDown,
		final double dbl3MCarry,
		final double dbl3MRollDown,
		final double dblDV01,
		final double dblBaselineSwapRate,
		final double dbl1DRolldownSwapRate,
		final double dbl1MRolldownSwapRate,
		final double dbl3MRolldownSwapRate,
		final double dbl1DCurveShiftSwapRate,
		final double dblPeriodFixedRate,
		final double dblPeriodCurveFloatingRate,
		final double dblPeriodProductFloatingRate,
		final double dbl1DFixedDCF,
		final double dbl1DFloatingDCF,
		final double dbl1MFixedDCF,
		final double dbl1MFloatingDCF,
		final double dbl3MFixedDCF,
		final double dbl3MFloatingDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dbl1DTotalReturn = dbl1DTotalReturn) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCleanReturn = dbl1DCleanReturn) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dbl1DDirtyReturn = dbl1DDirtyReturn) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCarry = dbl1DCarry) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dbl1DRollDown = dbl1DRollDown) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCurveShift = dbl1DCurveShift) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dbl1MCarry = dbl1MCarry) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dbl1MRollDown =
										dbl1MRollDown) || !org.drip.quant.common.NumberUtil.IsValid
											(_dbl3MCarry = dbl3MCarry) ||
												!org.drip.quant.common.NumberUtil.IsValid (_dbl3MRollDown =
													dbl3MRollDown) ||
														!org.drip.quant.common.NumberUtil.IsValid (_dblDV01 =
															dblDV01) ||
																!org.drip.quant.common.NumberUtil.IsValid
																	(_dblBaselineSwapRate =
																		dblBaselineSwapRate) ||
																			!org.drip.quant.common.NumberUtil.IsValid
			(_dbl1DRolldownSwapRate = dbl1DRolldownSwapRate) || !org.drip.quant.common.NumberUtil.IsValid
				(_dbl1MRolldownSwapRate = dbl1MRolldownSwapRate) || !org.drip.quant.common.NumberUtil.IsValid
					(_dbl3MRolldownSwapRate = dbl3MRolldownSwapRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCurveShiftSwapRate =
							dbl1DCurveShiftSwapRate) || !org.drip.quant.common.NumberUtil.IsValid
								(_dblPeriodFixedRate = dblPeriodFixedRate) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblPeriodCurveFloatingRate =
										dblPeriodCurveFloatingRate) ||
											!org.drip.quant.common.NumberUtil.IsValid
												(_dblPeriodProductFloatingRate =
													dblPeriodProductFloatingRate) ||
														!org.drip.quant.common.NumberUtil.IsValid
															(_dbl1DFixedDCF = dbl1DFixedDCF) ||
																!org.drip.quant.common.NumberUtil.IsValid
																	(_dbl1DFloatingDCF = dbl1DFloatingDCF) ||
																		!org.drip.quant.common.NumberUtil.IsValid
			(_dbl1MFixedDCF = dbl1MFixedDCF) || !org.drip.quant.common.NumberUtil.IsValid (_dbl1MFloatingDCF
				= dbl1MFloatingDCF) || !org.drip.quant.common.NumberUtil.IsValid (_dbl3MFixedDCF =
					dbl3MFixedDCF) || !org.drip.quant.common.NumberUtil.IsValid (_dbl3MFloatingDCF =
						dbl3MFloatingDCF))
			throw new java.lang.Exception ("ProductDailyPnL ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the 1D Clean Return
	 * 
	 * @return The 1D Clean Return
	 */

	public double returnClean1D()
	{
		return _dbl1DCleanReturn;
	}

	/**
	 * Retrieve the 1D Dirty Return
	 * 
	 * @return The 1D Dirty Return
	 */

	public double returnDirty1D()
	{
		return _dbl1DDirtyReturn;
	}

	/**
	 * Retrieve the 1D Total Return
	 * 
	 * @return The 1D Total Return
	 */

	public double returnTotal1D()
	{
		return _dbl1DTotalReturn;
	}

	/**
	 * Retrieve the 1D Carry
	 * 
	 * @return The 1D Carry
	 */

	public double carry1D()
	{
		return _dbl1DCarry;
	}

	/**
	 * Retrieve the 1D Roll Down
	 * 
	 * @return The 1D Roll Down
	 */

	public double rollDown1D()
	{
		return _dbl1DRollDown;
	}

	/**
	 * Retrieve the 1D Curve Shift
	 * 
	 * @return The 1D Curve Shift
	 */

	public double curveShift1D()
	{
		return _dbl1DCurveShift;
	}

	/**
	 * Retrieve the 1M Carry
	 * 
	 * @return The 1M Carry
	 */

	public double carry1M()
	{
		return _dbl1MCarry;
	}

	/**
	 * Retrieve the 1M Roll Down
	 * 
	 * @return The 1M Roll Down
	 */

	public double rollDown1M()
	{
		return _dbl1MRollDown;
	}

	/**
	 * Retrieve the 3M Carry
	 * 
	 * @return The 3M Carry
	 */

	public double carry3M()
	{
		return _dbl3MCarry;
	}

	/**
	 * Retrieve the 3M Roll Down
	 * 
	 * @return The 3M Roll Down
	 */

	public double rollDown3M()
	{
		return _dbl3MRollDown;
	}

	/**
	 * Retrieve the DV01
	 * 
	 * @return The DV01
	 */

	public double DV01()
	{
		return _dblDV01;
	}

	/**
	 * Retrieve the Baseline Swap Rate
	 * 
	 * @return The Baseline Swap Rate
	 */

	public double baselineSwapRate()
	{
		return _dblBaselineSwapRate;
	}

	/**
	 * Retrieve the 1D Roll Down Swap Rate
	 * 
	 * @return The 1D Roll Down Swap Rate
	 */

	public double rolldownSwapRate1D()
	{
		return _dbl1DRolldownSwapRate;
	}

	/**
	 * Retrieve the 1M Roll Down Swap Rate
	 * 
	 * @return The 1M Roll Down Swap Rate
	 */

	public double rolldownSwapRate1M()
	{
		return _dbl1MRolldownSwapRate;
	}

	/**
	 * Retrieve the 3M Roll Down Swap Rate
	 * 
	 * @return The 3M Roll Down Swap Rate
	 */

	public double rolldownSwapRate3M()
	{
		return _dbl3MRolldownSwapRate;
	}

	/**
	 * Retrieve the 1D Curve Shift Swap Rate
	 * 
	 * @return The 1D Curve Shift Swap Rate
	 */

	public double curveShiftSwapRate1D()
	{
		return _dbl1DCurveShiftSwapRate;
	}

	/**
	 * Retrieve the Period Fixed Rate
	 * 
	 * @return The Period Fixed Rate
	 */

	public double periodFixedRate()
	{
		return _dblPeriodFixedRate;
	}

	/**
	 * Retrieve the Period Curve Floating Rate
	 * 
	 * @return The Period Curve Floating Rate
	 */

	public double periodCurveFloatingRate()
	{
		return _dblPeriodCurveFloatingRate;
	}

	/**
	 * Retrieve the Period Product Floating Rate
	 * 
	 * @return The Period Product Floating Rate
	 */

	public double periodProductFloatingRate()
	{
		return _dblPeriodProductFloatingRate;
	}

	/**
	 * Retrieve the Period 1D Fixed DCF
	 * 
	 * @return The Period 1D Fixed DCF
	 */

	public double fixed1DDCF()
	{
		return _dbl1DFixedDCF;
	}

	/**
	 * Retrieve the Period 1D Floating DCF
	 * 
	 * @return The Period 1D Floating DCF
	 */

	public double floating1DDCF()
	{
		return _dbl1DFloatingDCF;
	}

	/**
	 * Retrieve the Period 1M Fixed DCF
	 * 
	 * @return The Period 1M Fixed DCF
	 */

	public double fixed1MDCF()
	{
		return _dbl1MFixedDCF;
	}

	/**
	 * Retrieve the Period 1M Floating DCF
	 * 
	 * @return The Period 1M Floating DCF
	 */

	public double floating1MDCF()
	{
		return _dbl1MFloatingDCF;
	}

	/**
	 * Retrieve the Period 3M Fixed DCF
	 * 
	 * @return The Period 3M Fixed DCF
	 */

	public double fixed3MDCF()
	{
		return _dbl3MFixedDCF;
	}

	/**
	 * Retrieve the Period 3M Floating DCF
	 * 
	 * @return The Period 3M Floating DCF
	 */

	public double floating3MDCF()
	{
		return _dbl3MFloatingDCF;
	}

	/**
	 * Retrieve the Array of Metrics
	 * 
	 * @return The Array of Metrics
	 */

	public double[] toArray()
	{
		java.util.List<java.lang.Double> lsPnLMetric = new java.util.ArrayList<java.lang.Double>();

		lsPnLMetric.add (_dbl1DTotalReturn);

		lsPnLMetric.add (_dbl1DCleanReturn);

		lsPnLMetric.add (_dbl1DDirtyReturn);

		lsPnLMetric.add (_dbl1DCarry);

		lsPnLMetric.add (_dbl1DRollDown);

		lsPnLMetric.add (_dbl1DCurveShift);

		lsPnLMetric.add (_dbl1MCarry);

		lsPnLMetric.add (_dbl1MRollDown);

		lsPnLMetric.add (_dbl3MCarry);

		lsPnLMetric.add (_dbl3MRollDown);

		lsPnLMetric.add (_dblDV01);

		lsPnLMetric.add (_dblBaselineSwapRate);

		lsPnLMetric.add (_dbl1DRolldownSwapRate);

		lsPnLMetric.add (_dbl1MRolldownSwapRate);

		lsPnLMetric.add (_dbl3MRolldownSwapRate);

		lsPnLMetric.add (_dbl1DCurveShiftSwapRate);

		lsPnLMetric.add (_dblPeriodFixedRate);

		lsPnLMetric.add (_dblPeriodCurveFloatingRate);

		lsPnLMetric.add (_dblPeriodProductFloatingRate);

		lsPnLMetric.add (_dbl1DFixedDCF);

		lsPnLMetric.add (_dbl1DFloatingDCF);

		lsPnLMetric.add (_dbl1MFixedDCF);

		lsPnLMetric.add (_dbl1MFloatingDCF);

		lsPnLMetric.add (_dbl3MFixedDCF);

		lsPnLMetric.add (_dbl3MFloatingDCF);

		int i = 0;

		double[] adblSPCA = new double[lsPnLMetric.size()];

		for (double dbl : lsPnLMetric)
			adblSPCA[i++] = dbl;

		return adblSPCA;
	}

	@Override public java.lang.String toString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		boolean bStart = true;

		for (double dbl : toArray()) {
			if (bStart)
				bStart = false;
			else
				sb.append (",");

			sb.append (dbl);
		}

		return sb.toString();
	}
}
