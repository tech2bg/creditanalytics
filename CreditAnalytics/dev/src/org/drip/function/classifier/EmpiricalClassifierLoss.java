
package org.drip.function.classifier;

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
 * EmpiricalClassifierLoss Function computes the Empirical Loss of a Classification Operation resulting from
 *  the Use of a Classification Function in Conjunction with the corresponding Empirical Outcome.
 *
 * @author Lakshmi Krishnamurthy
 */

public class EmpiricalClassifierLoss extends org.drip.function.deterministic.AbstractUnivariate {
	private short _sEmpiricalOutcome = -1;
	private org.drip.function.classifier.AbstractBinaryClassifier _abe = null;

	/**
	 * EmpiricalClassifierLoss Constructor
	 * 
	 * @param abe The Abstract Binary Classifier Instance
	 * @param sEmpiricalOutcome The Empirical Outcome
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public EmpiricalClassifierLoss (
		final org.drip.function.classifier.AbstractBinaryClassifier abe,
		final short sEmpiricalOutcome)
		throws java.lang.Exception
	{
		super (null);

		if (null == (_abe = abe) || 0 > (_sEmpiricalOutcome = sEmpiricalOutcome))
			throw new java.lang.Exception ("EmpiricalClassifierLoss ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Empirical Outcome
	 * 
	 * @return The Empirical Outcome
	 */

	public short empiricalOutcome()
	{
		return _sEmpiricalOutcome;
	}

	/**
	 * Retrieve the Classifier Function
	 * 
	 * @return The Classifier Function
	 */

	public org.drip.function.classifier.AbstractBinaryClassifier classifier()
	{
		return _abe;
	}

	/**
	 * Compute the Loss for the specified Variate
	 * 
	 * @param dblVariate The Variate
	 * 
	 * @return Loss for the specified Variate
	 * 
	 * @throws java.lang.Exception Thrown if the Loss cannot be computed
	 */

	public short loss (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("EmpiricalClassifierLoss::loss => Invalid Inputs");

		return _sEmpiricalOutcome == _abe.classify (dblVariate) ? (short) 0 : 1;
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		return loss (dblVariate);
	}
}
