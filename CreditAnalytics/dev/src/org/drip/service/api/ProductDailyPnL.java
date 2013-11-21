
package org.drip.service.api;

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
	private double _dbl1DReturn = java.lang.Double.NaN;
	private double _dbl1DRollDown = java.lang.Double.NaN;
	private double _dbl1MRollDown = java.lang.Double.NaN;
	private double _dbl3MRollDown = java.lang.Double.NaN;
	private double _dbl1DCurveShift = java.lang.Double.NaN;

	/**
	 * ProductDailyPnL constructor
	 * 
	 * @param dbl1DReturn 1D Return PnL
	 * @param dbl1DCarry 1D Carry PnL
	 * @param dbl1DRollDown 1D Roll Down PnL
	 * @param dbl1DCurveShift 1D Curve Shift PnL
	 * @param dbl1MCarry 1M Carry PnL
	 * @param dbl1MRollDown 1M Roll Down PnL
	 * @param dbl3MCarry 3M Carry PnL
	 * @param dbl3MRollDown 3M Roll Down PnL
	 * @param dblDV01 DV01
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ProductDailyPnL (
		final double dbl1DReturn,
		final double dbl1DCarry,
		final double dbl1DRollDown,
		final double dbl1DCurveShift,
		final double dbl1MCarry,
		final double dbl1MRollDown,
		final double dbl3MCarry,
		final double dbl3MRollDown,
		final double dblDV01)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dbl1DReturn = dbl1DReturn) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCarry = dbl1DCarry) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dbl1DRollDown = dbl1DRollDown) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dbl1DCurveShift = dbl1DCurveShift) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dbl1MCarry = dbl1MCarry) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dbl1MRollDown = dbl1MRollDown) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dbl3MCarry = dbl3MCarry) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dbl3MRollDown =
										dbl3MRollDown) || !org.drip.quant.common.NumberUtil.IsValid (_dblDV01
											= dblDV01))
			throw new java.lang.Exception ("ProductDailyPnL ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the 1D Return
	 * 
	 * @return The 1D Return
	 */

	public double return1D()
	{
		return _dbl1DReturn;
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
	 * Retrieve the Array of Metrics
	 * 
	 * @return The Array of Metrics
	 */

	public double[] toArray()
	{
		java.util.List<java.lang.Double> lsPnLMetric = new java.util.ArrayList<java.lang.Double>();

		lsPnLMetric.add (_dbl1DReturn);

		lsPnLMetric.add (_dbl1DCarry);

		lsPnLMetric.add (_dbl1DRollDown);

		lsPnLMetric.add (_dbl1DCurveShift);

		lsPnLMetric.add (_dbl1MCarry);

		lsPnLMetric.add (_dbl1MRollDown);

		lsPnLMetric.add (_dbl3MCarry);

		lsPnLMetric.add (_dbl3MRollDown);

		lsPnLMetric.add (_dblDV01);

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
