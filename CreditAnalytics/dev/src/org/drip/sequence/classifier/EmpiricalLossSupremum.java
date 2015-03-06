
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
 * EmpiricalLossSupremum contains the Implementation of the Bounded Empirical Classifier Loss dependent on
 *  Multivariate Random Variables where the Multivariate Function is a Linear Combination of Bounded
 * 	Univariate Functions acting on each Random Variate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class EmpiricalLossSupremum extends org.drip.sequence.functional.BoundedMultivariateRandom {

	class Supremum {
		int _iIndex = -1;
		double _dblValue = java.lang.Double.NaN;

		Supremum (
			int iIndex,
			double dblValue)
		{
			_iIndex = iIndex;
			_dblValue = dblValue;
		}
	}

	private short[] _asEmpiricalOutcome = null;
	private org.drip.function.classifier.FunctionClass _fcClassifier = null;

	private Supremum supremum (
		final double[] adblVariate)
	{
		org.drip.function.classifier.AbstractBinaryClassifier[] aClassifier = _fcClassifier.classifiers();

		int iSupremumIndex  = 0;
		int iNumClassifier = aClassifier.length;
		double dblClassifierEmpiricalLossSupremum = 0.;
		int iNumEmpiricalOutcome = _asEmpiricalOutcome.length;

		if (null == adblVariate || adblVariate.length != iNumEmpiricalOutcome) return null;

		for (int i = 0 ; i < iNumClassifier; ++i) {
			double dblClassifierEmpiricalLoss = 0.;

			for (int j = 0; j < iNumEmpiricalOutcome; ++j) {
				try {
					dblClassifierEmpiricalLoss += aClassifier[i].evaluate (adblVariate[j]) -
						_asEmpiricalOutcome[j];
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			if (dblClassifierEmpiricalLoss > dblClassifierEmpiricalLossSupremum) {
				iSupremumIndex = iNumClassifier;
				dblClassifierEmpiricalLossSupremum = dblClassifierEmpiricalLoss;
			}
		}

		return new Supremum (iSupremumIndex, dblClassifierEmpiricalLossSupremum / iNumEmpiricalOutcome);
	}

	/**
	 * EmpiricalLossSupremum Constructor
	 * 
	 * @param fcClassifier The Classifier Function Class
	 * @param asEmpiricalOutcome Array of the Empirical Outcomes
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public EmpiricalLossSupremum (
		final org.drip.function.classifier.FunctionClass fcClassifier,
		final short[] asEmpiricalOutcome)
		throws java.lang.Exception
	{
		if (null == (_fcClassifier = fcClassifier) || null == (_asEmpiricalOutcome = asEmpiricalOutcome))
			throw new java.lang.Exception ("EmpiricalLossSupremum ctr: Invalid Inputs");

		org.drip.function.classifier.AbstractBinaryClassifier[] aClassifier = _fcClassifier.classifiers();

		int iNumClassifier = aClassifier.length;
		int iNumEmpiricalOutcome = _asEmpiricalOutcome.length;

		if (0 == iNumEmpiricalOutcome)
			throw new java.lang.Exception ("EmpiricalLossSupremum ctr: Invalid Inputs");

		for (int i = 0; i < iNumClassifier; ++i) {
			if (null == aClassifier[i])
				throw new java.lang.Exception ("EmpiricalLossSupremum ctr: Invalid Inputs");
		}

		for (int i = 0; i < iNumEmpiricalOutcome; ++i) {
			if (0 > _asEmpiricalOutcome[i])
				throw new java.lang.Exception ("EmpiricalLossSupremum ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Classifier Function Class
	 * 
	 * @return The Classifier Function Class
	 */

	public org.drip.function.classifier.FunctionClass classifierClass()
	{
		return _fcClassifier;
	}

	/**
	 * Retrieve the Array of Empirical Outcomes
	 * 
	 * @return The Array of Empirical Outcomes
	 */

	public short[] empiricalOutcomes()
	{
		return _asEmpiricalOutcome;
	}

	/**
	 * Retrieve the Supremum Classifier for the specified Variate Sequence
	 * 
	 * @param adblVariate The Multivariate Sequence
	 * 
	 * @return The Supremum Classifier
	 */

	public org.drip.function.classifier.AbstractBinaryClassifier supremumClassifier (
		final double[] adblVariate)
	{
		Supremum sup = supremum (adblVariate);

		return null == sup ? null : _fcClassifier.classifiers()[sup._iIndex];
	}

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		Supremum sup = supremum (adblVariate);

		if (null == sup) throw new java.lang.Exception ("EmpiricalLossSupremum::evaluate => Invalid Inputs");

		return sup._dblValue;
	}

	@Override public double targetVariateVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return 1. / (_asEmpiricalOutcome.length * _asEmpiricalOutcome.length);
	}
}
