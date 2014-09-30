
package org.drip.analytics.output;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ComposedPeriodMetrics holds the results of the compounded Composed period metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposedPeriodMetrics {
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblCompoundedNominalAmount = java.lang.Double.NaN;
	private java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> _lsCPM = null;

	/**
	 * Instance of ComposedPeriodMetrics from the Composable Metrics list and the compounding Rule
	 * 
	 * @param lsCPM List of the Composable Period Metrics
	 * @param iAccrualCompoundingRule Accrual Compounding Rule
	 * 
	 * @return Instance of ComposedPeriodMetrics
	 */

	public static final ComposedPeriodMetrics Create (
		final java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM,
		final int iAccrualCompoundingRule)
	{
		if (null == lsCPM || 0 == lsCPM.size() ||
			!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule (iAccrualCompoundingRule))
			return null;

		double dblDCF = 0.;
		double dblCompoundedNominalAmount = java.lang.Double.NaN;

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			iAccrualCompoundingRule)
			dblCompoundedNominalAmount = 0.;
		else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			iAccrualCompoundingRule)
			dblCompoundedNominalAmount = 1.;

		for (org.drip.analytics.output.ComposablePeriodMetrics cpm : lsCPM) {
			double dblPeriodDCF = cpm.dcf();

			dblDCF += dblPeriodDCF;

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				iAccrualCompoundingRule)
				dblCompoundedNominalAmount += cpm.rate() * dblPeriodDCF;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				iAccrualCompoundingRule)
				dblCompoundedNominalAmount *= (1. + cpm.rate() * dblPeriodDCF);
		}

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			iAccrualCompoundingRule)
			dblCompoundedNominalAmount -= 1.;

		return new ComposedPeriodMetrics (dblDCF, dblCompoundedNominalAmount, lsCPM);
	}

	private ComposedPeriodMetrics (
		final double dblDCF,
		final double dblCompoundedNominalAmount,
		final java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM)
	{
		_lsCPM = lsCPM;
		_dblDCF = dblDCF;
		_dblCompoundedNominalAmount = dblCompoundedNominalAmount;
	}

	/**
	 * Retrieve the Composite DCF
	 * 
	 * @return The Composite DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}

	/**
	 * Retrieve the Compounded Nominal Amount
	 * 
	 * @return The Compounded Nominal Amount
	 */

	public double compoundedNominalAmount()
	{
		return _dblCompoundedNominalAmount;
	}

	/**
	 * Retrieve the Compounded Rate
	 * 
	 * @return The Compounded Rate
	 */

	public double compoundedRate()
	{
		return _dblCompoundedNominalAmount / _dblDCF;
	}

	/**
	 * Retrieve the List of Composable Period Metrics
	 * 
	 * @return The List of Composable Period Metrics
	 */

	public java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> composableMetrics()
	{
		return _lsCPM;
	}
}
