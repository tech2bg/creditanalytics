
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
 * FunctionClass implements the Class that holds the Set of Classifier Functions. Class-Specific Bounds and
 *  other Parameters are also maintained.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FunctionClass {
	private org.drip.function.classifier.ClassAsymptoticSampleBound _asymptote = null;
	private org.drip.function.classifier.AbstractBinaryClassifier[] _aClassifier = null;

	/**
	 * FunctionClass Constructor
	 * 
	 * @param aClassifier Array of Classifiers belonging to the Function Class
	 * @param asymptote Asymptotic Bounds Behavior of the Function Class
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FunctionClass (
		final org.drip.function.classifier.AbstractBinaryClassifier[] aClassifier,
		final org.drip.function.classifier.ClassAsymptoticSampleBound asymptote)
		throws java.lang.Exception
	{
		if (null == (_aClassifier = aClassifier) || 0 == _aClassifier.length)
			throw new java.lang.Exception ("FunctionClass ctr => Invalid Inputs!");

		_asymptote = asymptote;
	}

	/**
	 * Retrieve the Array of Classifiers
	 * 
	 * @return The Array of Classifiers
	 */

	public org.drip.function.classifier.AbstractBinaryClassifier[] classifiers()
	{
		return _aClassifier;
	}

	/**
	 * Retrieve the Class Asymptotic Bounds Behavior
	 * 
	 * @return The Class Asymptotic Bounds Behavior
	 */

	public org.drip.function.classifier.ClassAsymptoticSampleBound asymptote()
	{
		return _asymptote;
	}
}
