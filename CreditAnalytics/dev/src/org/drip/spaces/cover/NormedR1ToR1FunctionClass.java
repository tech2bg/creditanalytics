
package org.drip.spaces.cover;

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
 * NormedR1ToR1FunctionClass implements the Class F of f : R^1 -> R^1 Normed Function Spaces of all variants.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedR1ToR1FunctionClass extends org.drip.spaces.cover.GeneralizedNormedFunctionClass {

	/**
	 * Create R^1 -> R^1 Function Class for the specified Bound Predictor/Response Function Set
	 * 
	 * @param aR1ToR1 The R^1 -> R^1 Function Set
	 * @param dblPredictorSupport The Set Predictor Support
	 * @param dblResponseBound The Set Response Bound
	 * @param iPNorm The Norm
	 * 
	 * @return The R^1 -> R^1 Function Class for the specified Bound Predictor/Response Function Set
	 */

	public static final NormedR1ToR1FunctionClass BoundPredictorBoundResponse (
		final org.drip.function.deterministic.R1ToR1[] aR1ToR1,
		final double dblPredictorSupport,
		final double dblResponseBound,
		final int iPNorm)
	{
		if (null == aR1ToR1) return null;

		int iNumFunction = aR1ToR1.length;
		org.drip.spaces.function.NormedR1ToR1[] aR1ToR1FunctionSpace = new
			org.drip.spaces.function.NormedR1ToR1[iNumFunction];

		if (0 == iNumFunction) return null;

		try {
			org.drip.spaces.tensor.ContinuousRealUnidimensionalVector curvInput = new
				org.drip.spaces.tensor.ContinuousRealUnidimensionalVector (-0.5 * dblPredictorSupport, 0.5 *
					dblPredictorSupport);

			org.drip.spaces.tensor.ContinuousRealUnidimensionalVector curvOutput = new
				org.drip.spaces.tensor.ContinuousRealUnidimensionalVector (-0.5 * dblResponseBound, 0.5 *
					dblResponseBound);

			for (int i = 0; i < iNumFunction; ++i)
				aR1ToR1FunctionSpace[i] = new org.drip.spaces.function.NormedR1ContinuousToR1Continuous
					(aR1ToR1[i], curvInput, curvOutput, iPNorm);

			return new NormedR1ToR1FunctionClass (aR1ToR1FunctionSpace);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private NormedR1ToR1FunctionClass (
		final org.drip.spaces.function.NormedR1ToR1[] aR1ToR1FunctionSpace)
		throws java.lang.Exception
	{
		super (aR1ToR1FunctionSpace);
	}

	@Override public org.drip.spaces.cover.CoveringNumber agnosticCoveringNumber()
	{
		org.drip.spaces.function.GeneralizedNormedFunctionSpace[] aGNFS = functionSpaces();

		int iNumFunction = aGNFS.length;
		double dblResponseLowerBound = java.lang.Double.NaN;
		double dblResponseUpperBound = java.lang.Double.NaN;
		double dblPredictorLowerBound = java.lang.Double.NaN;
		double dblPredictorUpperBound = java.lang.Double.NaN;

		for (int i = 0; i < iNumFunction; ++i) {
			org.drip.spaces.function.NormedR1ToR1 r1Tor1 = (org.drip.spaces.function.NormedR1ToR1) aGNFS[i];

			org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsOutput =
				(org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace) r1Tor1.output();

			org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsInput = r1Tor1.input();

			if (!guvsInput.isPredictorBounded() || !guvsOutput.isPredictorBounded()) return null;

			double dblResponseLeftBound = guvsOutput.leftEdge();

			double dblResponseRightBound = guvsOutput.rightEdge();

			double dblPredictorLeftBound = guvsInput.leftEdge();

			double dblPredictorRightBound = guvsInput.rightEdge();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorLowerBound))
				dblPredictorLowerBound = dblPredictorLeftBound;
			else {
				if (dblPredictorLowerBound > dblPredictorLeftBound)
					dblPredictorLowerBound = dblPredictorLeftBound;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorUpperBound))
				dblPredictorUpperBound = dblPredictorRightBound;
			else {
				if (dblPredictorUpperBound < dblPredictorRightBound)
					dblPredictorUpperBound = dblPredictorRightBound;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblResponseLowerBound))
				dblResponseLowerBound = dblResponseLeftBound;
			else {
				if (dblResponseLowerBound > dblResponseLeftBound)
					dblResponseLowerBound = dblResponseLeftBound;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblResponseUpperBound))
				dblResponseUpperBound = dblResponseRightBound;
			else {
				if (dblResponseUpperBound < dblResponseRightBound)
					dblResponseUpperBound = dblResponseRightBound;
			}
		}

		double dblVariation = dblResponseUpperBound - dblResponseLowerBound;

		try {
			return new org.drip.spaces.cover.BoundedFunctionCoveringNumber (dblPredictorUpperBound -
				dblPredictorLowerBound, dblVariation, dblVariation);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
