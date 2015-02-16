
package org.drip.sample.quant;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.discrete.*;
import org.drip.quant.function1D.*;
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
 * DiscreteBound demonstrates the Computation of the Probabilistic Bounds for a Discrete Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscreteBound {

	private static final void MarkovBound (
		final SampleSequenceAgnosticMetrics sm,
		final double dblLevel,
		final AbstractUnivariate au)
		throws Exception
	{
		System.out.println (
			(null == au ? "\tMarkov Base Bound        [" :  "\tMarkov Function Bound    [") +
			FormatUtil.FormatDouble (dblLevel, 1, 2, 1.) + "] : " +
			FormatUtil.FormatDouble (sm.markovUpperProbabilityBound (dblLevel, au), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (1. - dblLevel, 1, 4, 1.)
		);
	}

	private static final void ChebyshevBound (
		final SampleSequenceAgnosticMetrics sm,
		final double dblLevel)
		throws Exception
	{
		System.out.println (
			"\tChebyshev Bound          [" +
			FormatUtil.FormatDouble (dblLevel, 1, 2, 1.) + "] : " +
			FormatUtil.FormatDouble (sm.chebyshevBound (dblLevel).lower(), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (sm.chebyshevBound (dblLevel).upper(), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (1. - 2. * dblLevel, 1, 4, 1.)
		);
	}

	private static final void ChebyshevCantelliBound (
		final SampleSequenceAgnosticMetrics sm,
		final double dblLevel)
		throws Exception
	{
		System.out.println (
			"\tChebyshev Cantelli Bound [" +
			FormatUtil.FormatDouble (dblLevel, 1, 2, 1.) + "] : " +
			"        |" +
			FormatUtil.FormatDouble (sm.chebyshevCantelliBound (dblLevel).upper(), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (1. - dblLevel, 1, 4, 1.)
		);
	}

	private static final void CentralMomentBound (
		final SampleSequenceAgnosticMetrics sm,
		final double dblLevel,
		final int iMoment)
		throws Exception
	{
		System.out.println (
			"\tMoment #" + iMoment + " Bound          [" +
			FormatUtil.FormatDouble (dblLevel, 1, 2, 1.) + "] : " +
			FormatUtil.FormatDouble (sm.centralMomentBound (dblLevel, iMoment).lower(), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (sm.centralMomentBound (dblLevel, iMoment).upper(), 1, 4, 1.) + " |" +
			FormatUtil.FormatDouble (1. - 2. * dblLevel, 1, 4, 1.)
		);
	}

	private static final void MarkovBound (
		final SampleSequenceAgnosticMetrics sm,
		final AbstractUnivariate au)
		throws Exception
	{
		MarkovBound (sm, 0.20, au);

		MarkovBound (sm, 0.40, au);

		MarkovBound (sm, 0.59, au);

		MarkovBound (sm, 0.79, au);

		MarkovBound (sm, 0.99, au);
	}

	private static final void ChebyshevBound (
		final SampleSequenceAgnosticMetrics sm)
		throws Exception
	{
		ChebyshevBound (sm, 0.20);

		ChebyshevBound (sm, 0.25);

		ChebyshevBound (sm, 0.30);

		ChebyshevBound (sm, 0.35);

		ChebyshevBound (sm, 0.40);
	}

	private static final void ChebyshevCantelliBound (
		final SampleSequenceAgnosticMetrics sm)
		throws Exception
	{
		ChebyshevCantelliBound (sm, 0.20);

		ChebyshevCantelliBound (sm, 0.25);

		ChebyshevCantelliBound (sm, 0.30);

		ChebyshevCantelliBound (sm, 0.35);

		ChebyshevCantelliBound (sm, 0.40);
	}

	private static final void CentralMomentBound (
		final SampleSequenceAgnosticMetrics sm,
		final int iMoment)
		throws Exception
	{
		CentralMomentBound (sm, 0.20, iMoment);

		CentralMomentBound (sm, 0.25, iMoment);

		CentralMomentBound (sm, 0.30, iMoment);

		CentralMomentBound (sm, 0.35, iMoment);

		CentralMomentBound (sm, 0.40, iMoment);
	}

	private static final void SequenceGenerationRun (
		final SampleSequenceAgnosticMetrics sm)
		throws Exception
	{
		System.out.println ("\tExpectation                      : " + FormatUtil.FormatDouble (sm.empiricalExpectation(), 1, 4, 1.));

		System.out.println ("\tVariance                         : " + FormatUtil.FormatDouble (sm.empiricalVariance(), 1, 4, 1.));

		System.out.println ("\t---------------------------------------------------");

		MarkovBound (sm, new ExponentialTension (Math.E, 0.1));

		System.out.println ("\t---------------------------------------------------");

		MarkovBound (sm, new ExponentialTension (Math.E, 1.0));

		System.out.println ("\t---------------------------------------------------");

		MarkovBound (sm, new ExponentialTension (Math.E, 5.0));

		System.out.println ("\t---------------------------------------------------");

		MarkovBound (sm, null);

		System.out.println ("\t---------------------------------------------------");

		ChebyshevBound (sm);

		System.out.println ("\t---------------------------------------------------");

		ChebyshevCantelliBound (sm);

		System.out.println ("\t---------------------------------------------------");

		CentralMomentBound (sm, 3);

		System.out.println ("\t---------------------------------------------------");

		CentralMomentBound (sm, 4);

		System.out.println ("\t---------------------------------------------------");

		CentralMomentBound (sm, 5);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		/* System.out.println ("\t---------------------------------------------------");

		System.out.println ("\t|              BOUNDED GAUSSIAN RUN               |");

		System.out.println ("\t---------------------------------------------------");

		SequenceGenerationRun (new BoundedGaussian (0.5, 1., 0., 1.).sequence (50000));

		System.out.println ("\t---------------------------------------------------");

		System.out.println(); */

		System.out.println ("\t---------------------------------------------------");

		System.out.println ("\t|              BOUNDED UNIFORM RUN                |");

		System.out.println ("\t---------------------------------------------------");

		SequenceGenerationRun (new BoundedUniform (0., 1.).sequence (50000));

		System.out.println ("\t---------------------------------------------------");
	}
}
