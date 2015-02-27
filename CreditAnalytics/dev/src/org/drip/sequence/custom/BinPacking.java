
package org.drip.sequence.custom;

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
 * BinPacking contains Variance Bounds of the critical measures of the standard operations research bin
 * 	packing problem.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BinPacking {

	private static final boolean UpdateBin (
		java.util.Map<java.lang.Integer, java.lang.Double> mapBin,
		final double dblVariate)
	{
		for (java.util.Map.Entry<java.lang.Integer, java.lang.Double> meBin : mapBin.entrySet()) {
			double dblBinCapacity = meBin.getValue();

			if (dblBinCapacity > dblVariate) {
				meBin.setValue (dblBinCapacity - dblVariate);

				return true;
			}
		}

		return false;
	}

	public static final org.drip.sequence.functional.BoundedMultivariateRandom MinimumNumberOfBins()
	{
		org.drip.sequence.functional.BoundedMultivariateRandom funcMinBins = new
			org.drip.sequence.functional.BoundedMultivariateRandom() {
			@Override public double evaluate (
				final double[] adblVariate)
				throws java.lang.Exception
			{
				java.util.Map<java.lang.Integer, java.lang.Double> mapBin = new
					java.util.HashMap<java.lang.Integer, java.lang.Double>();

				int iLastEntry = -1;
				int iNumVariate = adblVariate.length;

				for (int i = 0; i < iNumVariate; ++i) {
					if (0 == i || !UpdateBin (mapBin, adblVariate[i]))
						mapBin.put (++iLastEntry, 1. - adblVariate[i]);
				}

				return mapBin.size();
			}

			@Override public double targetVarianceBound (
				final int iTargetVariateIndex)
				throws java.lang.Exception
			{
				return 1.;
			}
		};

		return funcMinBins;
	}
}
