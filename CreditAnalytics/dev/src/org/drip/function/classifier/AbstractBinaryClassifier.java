
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
 * AbstractBinaryClassifier provides the Classification Outcome of 0/1 for a specified Variate. Default
 *  Implementation of the Derivatives come from the Underlying Black Box Objective Function.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class AbstractBinaryClassifier extends org.drip.function.deterministic.AbstractUnivariate {

	/**
	 * AbstractBinaryClassifier Constructor
	 * 
	 * @param dc Derivative Control
	 */

	public AbstractBinaryClassifier (
		final org.drip.quant.calculus.DerivativeControl dc)
	{
		super (dc);
	}

	/**
	 * Classify the Specified Variate onto a Binary Outcome
	 * 
	 * @param dblVariate The Variate
	 * 
	 * @return Classifier Outcome 0/1
	 * 
	 * @throws java.lang.Exception Thrown if the Classification cannot be performed
	 */

	public abstract short classify (
		final double dblVariate)
		throws java.lang.Exception;

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		return classify (dblVariate);
	}
}
