
package org.drip.sample.matrix;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.common.NumberUtil;
import org.drip.quant.linearalgebra.Matrix;
import org.drip.service.api.CreditAnalytics;

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
 * GrahamSchmidtProcess illustrates the Graham Schmidt Orthogonalization and Orthonormalization.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GrahamSchmidtProcess {

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		double[][] aadblV = new double[][] {
			{3, 1, 4, 9},
			{2, 2, 6, 0},
			{1, 8, 3, 5},
			{7, 0, 4, 5}
		};

		double[][] aadblUOrthogonal = Matrix.GrahamSchmidtOrthogonalization (aadblV);

		NumberUtil.PrintMatrix ("ORTHOGONAL", aadblUOrthogonal);

		System.out.println (
			"ORTHOGONAL TEST: " +
			FormatUtil.FormatDouble (
				Matrix.DotProduct (
					aadblUOrthogonal[0],
					aadblUOrthogonal[1]
				),
			1, 1, 1.
			)
		);

		double[][] aadblUOrthonormal = Matrix.GrahamSchmidtOrthonormalization (aadblV);

		NumberUtil.PrintMatrix ("ORTHONORMAL", aadblUOrthonormal);

		System.out.println (
			"ORTHONORMAL TEST: " +
			FormatUtil.FormatDouble (
				Matrix.DotProduct (
					aadblUOrthonormal[0],
					aadblUOrthonormal[1]
				),
			1, 1, 1.
			)
		);
	}
}
