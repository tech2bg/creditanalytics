
package org.drip.math.segment;

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
 * This Class implements the Segment's Best Fit and the Curvature Penalizers.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BestFitCurvaturePenalizer {
	private org.drip.math.segment.LocalBasisEvaluator _lbe = null;
	private org.drip.math.segment.CurvaturePenaltyParams _cpp = null;
	private org.drip.math.segment.BestFitWeightedResponse _bfwr = null;

	/**
	 * BestFitCurvaturePenalizer constructor
	 * 
	 * @param cpp Curvature Penalty Parameters
	 * @param bfwr Best Fit Weighted Response
	 * @param lbe The Local Basis Evaluator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BestFitCurvaturePenalizer (
		final org.drip.math.segment.CurvaturePenaltyParams cpp,
		final org.drip.math.segment.BestFitWeightedResponse bfwr,
		final org.drip.math.segment.LocalBasisEvaluator lbe)
		throws java.lang.Exception
	{
		if (null == (_cpp = cpp) || null == (_lbe = lbe))
			throw new java.lang.Exception ("BestFitCurvaturePenalizer ctr: Invalid Inputs");

		_bfwr = bfwr;
	}

	/**
	 * Compute the Cross-Curvature Penalty for the given Basis Pair
	 * 
	 * @param iBasisIndexI I Basis Index (I is the Summation Index)
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return The Cross-Curvature Penalty for the given Basis Pair
	 * 
	 * @throws java.lang.Exception Thrown if the Cross-Curvature Penalty cannot be computed
	 */

	public double basisPairCurvaturePenalty (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		org.drip.math.function.AbstractUnivariate of = new org.drip.math.function.AbstractUnivariate (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws Exception
			{
				int iOrder = _cpp.derivativeOrder();

				return _lbe.localSpecificBasisDerivative (dblVariate, iOrder, iBasisIndexI) *
					_lbe.localSpecificBasisDerivative (dblVariate, iOrder, iBasisIndexR);
			}
		};

		return _cpp.amplitude() * org.drip.math.calculus.Integrator.Boole (of, 0., 1.);
	}

	/**
	 * Compute the Best Fit Cross-Product Penalty for the given Basis Pair
	 * 
	 * @param iBasisIndexI I Basis Index (I is the Summation Index)
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return The Best Fit Cross-Product Penalty for the given Basis Pair
	 * 
	 * @throws java.lang.Exception Thrown if the Best Fit Cross-Product Penalty cannot be computed
	 */

	public double basisBestFitPenalty (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		if (null == _bfwr) return 0.;

		int iNumPoint = _bfwr.numPoint();

		if (0 == iNumPoint) return 0.;

		double dblBasisPairFitnessPenalty = 0.;

		for (int i = 0; i < iNumPoint; ++i) {
			double dblPredictorOrdinate = _bfwr.predictorOrdinate (i);

			dblBasisPairFitnessPenalty += _bfwr.weight (i) * _lbe.localSpecificBasisResponse
				(dblPredictorOrdinate, iBasisIndexI) * _lbe.localSpecificBasisResponse (dblPredictorOrdinate,
					iBasisIndexR);
		}

		return dblBasisPairFitnessPenalty / iNumPoint;
	}

	/**
	 * Compute the Basis Pair Penalty Coefficient for the Best Fit and the Curvature Penalties
	 * 
	 * @param iBasisIndexI I Basis Index (I is the Summation Index)
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return The Basis Pair Penalty Coefficient for the Fitness and the Curvature Penalties
	 */

	public double basisPairConstraintCoefficient (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		return basisPairCurvaturePenalty (iBasisIndexI, iBasisIndexR) + basisBestFitPenalty (iBasisIndexI,
			iBasisIndexR);
	}

	/**
	 * Compute the Penalty Constraint for the Basis Pair
	 * 
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return Penalty Constraint for the Basis Pair
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double basisPairPenaltyConstraint (
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		if (null == _bfwr) return 0.;

		int iNumPoint = _bfwr.numPoint();

		if (0 == iNumPoint) return 0.;

		double dblBasisPairPenaltyConstraint = 0.;

		for (int i = 0; i < iNumPoint; ++i) {
			double dblPredictorOrdinate = _bfwr.predictorOrdinate (i);

			dblBasisPairPenaltyConstraint += _bfwr.weight (i) * _lbe.localSpecificBasisResponse
				(dblPredictorOrdinate, iBasisIndexR) * _bfwr.response (i);
		}

		return dblBasisPairPenaltyConstraint / iNumPoint;
	}
}
