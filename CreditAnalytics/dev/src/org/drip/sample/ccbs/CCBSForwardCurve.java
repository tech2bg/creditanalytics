
package org.drip.sample.ccbs;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.cashflow.FloatingStream;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.fx.*;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.sample.forward.IBOR;
import org.drip.spline.params.SegmentCustomBuilderControl;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * CCBSForwardCurve demonstrates the setup and construction of the Forward Curve from the CCBS Quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CCBSForwardCurve {

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final CurrencyPair cp,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {

			/*
			 * The Reference 6M Leg
			 */

			List<CashflowPeriod> lsFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsReference = new FloatingStream (
				strCurrency,
				null == cp ? null : new FXMTMSetting (cp, false),
				0.,
				-1.,
				null,
				lsFloatPeriods,
				ForwardLabel.Create (strCurrency + "-LIBOR-6M"),
				false
			);

			/*
			 * The Derived Leg
			 */

			List<CashflowPeriod> lsDerivedFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				null == cp ? null : new FXMTMSetting (cp, false),
				0.,
				1.,
				null,
				lsDerivedFloatPeriods,
				ForwardLabel.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				false
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);

			aFFC[i].setPrimaryCode (strCurrency + "_6M::" + iTenorInMonths + "M::" + astrTenor[i]);
		}

		return aFFC;
	}

	private static final ComponentPair[] MakeCCSP (
		final JulianDate dtValue,
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final CurrencyPair cp,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFCReference = MakexM6MBasisSwap (
			dtValue,
			strReferenceCurrency,
			cp,
			astrTenor,
			3);

		FloatFloatComponent[] aFFCDerived = MakexM6MBasisSwap (
			dtValue,
			strDerivedCurrency,
			null,
			astrTenor,
			3);

		ComponentPair[] aCCSP = new ComponentPair[astrTenor.length];

		for (int i = 0; i < aCCSP.length; ++i)
			aCCSP[i] = new ComponentPair (
				strDerivedCurrency + strReferenceCurrency + "_" + astrTenor[i],
				aFFCReference[i],
				aFFCDerived[i]
			);

		return aCCSP;
	}

	public static final void ForwardCurveReferenceComponentBasis (
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final JulianDate dtValue,
		final DiscountCurve dcReference,
		final ForwardCurve fc6MReference,
		final ForwardCurve fc3MReference,
		final DiscountCurve dcDerived,
		final ForwardCurve fc6MDerived,
		final double dblRefDerFX,
		final SegmentCustomBuilderControl scbc,
		final String[] astrTenor,
		final double[] adblCrossCurrencyBasis,
		final boolean bBasisOnDerivedLeg)
		throws Exception
	{
		ComponentPair[] aCCSP = MakeCCSP (
			dtValue,
			strReferenceCurrency,
			strDerivedCurrency,
			CurrencyPair.FromCode (strDerivedCurrency + "/" + strReferenceCurrency),
			astrTenor,
			3);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcReference);

		mktParams.setFundingCurve (dcDerived);

		mktParams.setForwardCurve (fc3MReference);

		mktParams.setForwardCurve (fc6MReference);

		mktParams.setForwardCurve (fc6MDerived);

		mktParams.setFXCurve (FXLabel.Standard (CurrencyPair.FromCode (strDerivedCurrency + "/" + strReferenceCurrency)), new FlatUnivariate (dblRefDerFX));

		mktParams.setFXCurve (FXLabel.Standard (CurrencyPair.FromCode (strReferenceCurrency + "/" + strDerivedCurrency)), new FlatUnivariate (1. / dblRefDerFX));

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strReferenceCurrency);

		LinearLatentStateCalibrator llsc = new LinearLatentStateCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		LatentStateStretchSpec stretchSpec = CCBSStretchBuilder.ForwardStretch (
			"FLOATFLOAT",
			aCCSP,
			valParams,
			mktParams,
			adblCrossCurrencyBasis,
			bBasisOnDerivedLeg
		);

		ForwardCurve fc3MDerived = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			llsc,
			new LatentStateStretchSpec[] {stretchSpec},
			ForwardLabel.Create (strDerivedCurrency + "-LIBOR-3M"),
			valParams,
			null,
			MarketParamsBuilder.Create (dcDerived, fc6MDerived, null, null, null, null, null, null),
			null,
			dcDerived.forward (dtValue.julian(), dtValue.addTenor ("3M").julian())
		);

		CurveSurfaceQuoteSet mktParamsDerived = MarketParamsBuilder.Create
			(dcDerived, fc3MDerived, null, null, null, null, null, null);

		mktParamsDerived.setForwardCurve (fc6MDerived);

		mktParams.setForwardCurve (fc3MDerived);

		System.out.println ("\t----------------------------------------------------------------");

		if (bBasisOnDerivedLeg)
			System.out.println ("\t     RECOVERY OF THE CCBS REFERENCE COMPONENT DERIVED BASIS");
		else
			System.out.println ("\t     RECOVERY OF THE CCBS REFERENCE COMPONENT REFERENCE BASIS");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i) {
			CalibratableFixedIncomeComponent rc = aCCSP[i].derivedComponent();

			CaseInsensitiveTreeMap<Double> mapOP = aCCSP[i].value (valParams, null, mktParams, null);

			System.out.println ("\t[" + rc.effective() + " - " + rc.maturity() + "] = " +
				FormatUtil.FormatDouble (mapOP.get (bBasisOnDerivedLeg ? "ReferenceCompDerivedBasis" : "ReferenceCompReferenceBasis"), 1, 3, 1.) +
					" | " + FormatUtil.FormatDouble (adblCrossCurrencyBasis[i], 1, 3, 10000.) + " | " +
						FormatUtil.FormatDouble (fc3MDerived.forward (rc.maturity()), 1, 4, 100.) + "%");
		}

		IBOR.ForwardJack (
			dtValue,
			"---- CCBS DERIVED QUOTE FORWARD CURVE SENSITIVITY ---",
			fc3MDerived,
			"PV"
		);
	}
}
