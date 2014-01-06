
package org.drip.sample.rates;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.LinearRationalShapeControl;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.pchip.*;
import org.drip.spline.stretch.*;

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
 * This sample illustrates using the Hagan and West (2006) Estimator. It provides the following
 *  functionality:
 * 	- Ensure that the estimated regime is monotone an convex.
 * 	- If need be, enforce positivity.
 * 	- Apply segment-by-segment range bounds as needed.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HaganWestForwardInterpolator {
	private static void DisplayOP (
		final MonotoneConvexHaganWest mchw,
		final MultiSegmentSequence mss,
		final double[] adblTime)
		throws Exception
	{
		double dblTimeBegin = 0.;
		double dblTimeFinish = 30.;
		double dblTimeDelta = 3.00;
		double dblTime = dblTimeBegin;

		while (dblTime <= dblTimeFinish) {
			System.out.println ("\t\tResponse[" +
				FormatUtil.FormatDouble (dblTime, 2, 2, 1.) + "]: " +
				FormatUtil.FormatDouble (mchw.evaluate (dblTime), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (mss.responseValue (dblTime), 1, 6, 1.));

			dblTime += dblTimeDelta;
		}

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t\tPositivity enforced? " + mchw.enforcePositivity());

		System.out.println ("\t----------------------------------------------------------------");

		dblTime = dblTimeBegin;

		while (dblTime <= dblTimeFinish) {
			System.out.println ("\t\tPositivity Enforced Response[" +
				FormatUtil.FormatDouble (dblTime, 2, 2, 1.) + "]: " +
				FormatUtil.FormatDouble (mchw.evaluate (dblTime), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (mss.responseValue (dblTime), 1, 6, 1.));

			dblTime += dblTimeDelta;
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblTime = new double[] {0., 0.10, 1.0, 4.0, 9.0, 20.0, 30.0};
		double[] adblForwardRate = new double[] {1.008, 1.073, 1.221, 1.878, 2.226, 2.460};

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (
			false,
			new LinearRationalShapeControl (dblShapeControllerTension));

		int iK = 2;
		int iCurvaturePenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl sdic = SegmentDesignInelasticControl.Create (
			iK,
			iCurvaturePenaltyDerivativeOrder);

		double dblKLKTension = 1.;

		SegmentCustomBuilderControl scbc = new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (dblKLKTension),
				sdic,
				rssc);

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblForwardRate.length];

		for (int i = 0; i < adblForwardRate.length; ++i)
			aSCBC[i] = scbc;

		MultiSegmentSequence mssLinear = LocalControlStretchBuilder.CreateMonotoneConvexStretch (
			"MSS_LINEAR",
			adblTime,
			adblForwardRate,
			aSCBC,
			null,
			MultiSegmentSequence.CALIBRATE,
			false,
			false,
			false);

		MultiSegmentSequence mssHarmonic = LocalControlStretchBuilder.CreateMonotoneConvexStretch (
			"MSS_HARMONIC",
			adblTime,
			adblForwardRate,
			aSCBC,
			null,
			MultiSegmentSequence.CALIBRATE,
			true,
			false,
			false);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     MONOTONE CONVEX HAGAN WEST WITH LINEAR FORWARD STATE");

		System.out.println ("\t----------------------------------------------------------------");

		DisplayOP (MonotoneConvexHaganWest.Create (adblTime, adblForwardRate, false), mssLinear, adblTime);

		System.out.println ("\n\n\t----------------------------------------------------------------");

		System.out.println ("\t     MONOTONE CONVEX HAGAN WEST WITH HARMONIC FORWARD STATE");

		System.out.println ("\t----------------------------------------------------------------");

		DisplayOP (MonotoneConvexHaganWest.Create (adblTime, adblForwardRate, true), mssHarmonic, adblTime);
	}
}
