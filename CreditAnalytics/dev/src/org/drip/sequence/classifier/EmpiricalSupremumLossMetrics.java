
package org.drip.sequence.classifier;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * EmpiricalSupremumLossMetrics implements Efron-Stein Metrics for the Empirical Loss Supremum Functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class EmpiricalSupremumLossMetrics extends org.drip.sequence.functional.EfronSteinMetrics {
	private org.drip.sequence.classifier.EmpiricalLossSupremum _funcELS = null;

	/**
	 * EmpiricalSupremumLossMetrics Constructor
	 * 
	 * @param funcELS Empirical Loss Supremum Function
	 * @param aSSAM Array of the Individual Single Sequence Metrics
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public EmpiricalSupremumLossMetrics (
		final org.drip.sequence.classifier.EmpiricalLossSupremum funcELS,
		final org.drip.sequence.metrics.SingleSequenceAgnosticMetrics[] aSSAM)
		throws java.lang.Exception
	{
		super (funcELS, aSSAM);

		if (null == (_funcELS = funcELS))
			throw new java.lang.Exception ("EmpiricalSupremumLossMetrics ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Empirical Loss Supremum Function
	 * 
	 * @return The Empirical Loss Supremum Function
	 */

	public org.drip.sequence.classifier.EmpiricalLossSupremum empiricalLossSupremum()
	{
		return _funcELS;
	}

	/**
	 * Retrieve the Variate-Sequence Dependent Variance Bound
	 * 
	 * @param adblVariate The Variate Sequence
	 * 
	 * @return The Variate-Sequence Dependent Variance Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Date Dependent Variance Bound cannot be Computed
	 */

	public double dataDependentVarianceBound (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		return _funcELS.evaluate (adblVariate) / adblVariate.length;
	}

	/**
	 * Compute the Lugosi Data-Dependent Variance Bound from the Sample and the Classifier Class Asymptotic
	 * 	Behavior: Source =>
	 * 
	 * 		G. Lugosi (2002): Pattern Classification and Learning Theory, in: L.Gyorfi, editor, Principles of
	 * 			Non-parametric Learning, 5-62, Springer, Wien.
	 * 
	 * @param adblVariate The Sample Variate Array
	 * 
	 * @return The Lugosi Data-Dependent Variance Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Lugosi Data-Dependent Variance Bound cannot be computed
	 */

	public double lugosiVarianceBound (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		org.drip.function.classifier.AbstractBinaryClassifier supClassifier = _funcELS.supremumClassifier
			(adblVariate);

		if (null == supClassifier)
			throw new java.lang.Exception
				("EmpiricalSupremumLossMetrics::lugosiVarianceBound => Cannot Find Supremum Classifier");

		org.drip.function.classifier.ClassAsymptoticSampleBound casb =
			_funcELS.classifierClass().asymptote();

		if (null == casb)
			throw new java.lang.Exception
				("EmpiricalSupremumLossMetrics::lugosiVarianceBound => Cannot Find Class Asymptote");

		return dataDependentVarianceBound (adblVariate) + casb.constant() + java.lang.Math.pow
			(adblVariate.length, casb.exponent());
	}
}
