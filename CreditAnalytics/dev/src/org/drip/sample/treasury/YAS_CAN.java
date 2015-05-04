
package org.drip.sample.treasury;

import org.drip.analytics.cashflow.CompositePeriod;
import org.drip.analytics.date.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.market.otc.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.credit.BondComponent;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.FixFloatComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.identifier.ForwardLabel;

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
 * YAS_CAN contains the sample demonstrating the replication of Bloomberg's Canadian Govvie CAD Bond YAS
 *  Functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class YAS_CAN {

	private static BondComponent TSYBond (
		final JulianDate dtEffective,
		final JulianDate dtMaturity,
		final int iFreq,
		final String strDayCount,
		final String strCurrency,
		final double dblCoupon)
		throws Exception
	{
		return BondBuilder.CreateSimpleFixed (
			"CAN " + FormatUtil.FormatDouble (dblCoupon, 1, 2, 100.) + " " + dtMaturity,
			strCurrency,
			"",
			dblCoupon,
			iFreq,
			strDayCount,
			dtEffective,
			dtMaturity,
			null,
			null
		);
	}

	private static final FixFloatComponent OTCIRS (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
	{
		FixedFloatSwapConvention ffConv = IBORFixedFloatContainer.ConventionFromJurisdiction (
			strCurrency,
			"ALL",
			strMaturityTenor,
			"MAIN"
		);

		return ffConv.createFixFloatComponent (
			dtSpot,
			strMaturityTenor,
			dblCoupon,
			0.,
			1.
		);
	}

	/*
	 * Sample demonstrating building of rates curve from cash/future/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static DiscountCurve BuildRatesCurveFromInstruments (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableFixedIncomeComponent aCompCalib[] = new CalibratableFixedIncomeComponent[iNumDCInstruments];

		// Cash Calibration

		JulianDate dtCashEffective = dtStart.addBusDays (
			1,
			strCurrency
		);

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = SingleStreamComponentBuilder.Deposit (
				dtCashEffective,
				new JulianDate (adblDate[i] = dtCashEffective.addTenor (astrCashTenor[i]).julian()),
				ForwardLabel.Create (strCurrency, astrCashTenor[i])
			);
		}

		// IRS Calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, strCurrency);

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			adblDate[i + astrCashTenor.length] = dtIRSEffective.addTenor (astrIRSTenor[i]).julian();

			aCompCalib[i + astrCashTenor.length] = OTCIRS (
				dtIRSEffective,
				strCurrency,
				astrIRSTenor[i],
				0.
			);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return ScenarioDiscountCurveBuilder.NonlinearBuild (
			dtStart,
			strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib,
			adblCompCalibValue,
			astrCalibMeasure,
			null
		);
	}

	private static final DiscountCurve FundingCurve (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.00276};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
			   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};

		return BuildRatesCurveFromInstruments (
			dtSpot,
			astrCashTenor,
			adblCashRate,
			astrIRSTenor,
			adblIRSRate,
			0.,
			strCurrency
		);
	}

	private static final void TSYMetrics (
		final BondComponent tsyBond,
		final double dblNotional,
		final JulianDate dtSettle,
		final CurveSurfaceQuoteSet mktParams,
		final double dblCleanPrice)
		throws Exception
	{
		double dblAccrued = tsyBond.accrued (
			dtSettle.julian(),
			null
		);

		double dblYield = tsyBond.yieldFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				tsyBond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblModifiedDuration = tsyBond.modifiedDurationFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				tsyBond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblRisk = tsyBond.yield01FromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				tsyBond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblConvexity = tsyBond.convexityFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				tsyBond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		JulianDate dtPreviousCouponDate = tsyBond.previousCouponDate (dtSettle);

		System.out.println();

		System.out.println ("\t\t" + tsyBond.name());

		System.out.println ("\tPrice             : " + FormatUtil.FormatDouble (dblCleanPrice, 1, 4, 100.));

		System.out.println ("\tYield             : " + FormatUtil.FormatDouble (dblYield, 1, 4, 100.) + "%");

		System.out.println ("\tSettle            :  " + dtSettle);

		System.out.println();

		System.out.println ("\tModified Duration : " + FormatUtil.FormatDouble (dblModifiedDuration, 1, 4, 10000.));

		System.out.println ("\tRisk              : " + FormatUtil.FormatDouble (dblRisk, 1, 4, 10000.));

		System.out.println ("\tConvexity         : " + FormatUtil.FormatDouble (dblConvexity * dblNotional, 1, 4, 1.));

		System.out.println ("\tDV01              : " + FormatUtil.FormatDouble (dblRisk * dblNotional, 1, 0, 1.));

		System.out.println();

		System.out.println ("\tPrevious Coupon Date :  " + dtPreviousCouponDate);

		System.out.println ("\tFace                 : " + FormatUtil.FormatDouble (dblNotional, 1, 2, 1.));

		System.out.println ("\tPrincipal            : " + FormatUtil.FormatDouble (dblCleanPrice * dblNotional, 1, 2, 1.));

		System.out.println ("\tAccrued              : " + FormatUtil.FormatDouble (dblAccrued * dblNotional, 1, 2, 1.));

		System.out.println ("\tTotal                : " + FormatUtil.FormatDouble ((dblCleanPrice + dblAccrued) * dblNotional, 1, 2, 1.));

		System.out.println ("\tAccrual Days         : " + (dtSettle.julian() - dtPreviousCouponDate.julian()));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.CreateFromYMD (
			2015,
			DateUtil.MAY,
			1
		);

		JulianDate dtEffective = DateUtil.CreateFromYMD (
			2012,
			DateUtil.JULY,
			30
		);

		JulianDate dtMaturity = DateUtil.CreateFromYMD (
			2023,
			DateUtil.JUNE,
			1
		);

		int iFreq = 2;
		String strDayCount = "DCAct_Act_UST";
		String strCurrency = "CAD";
		double dblCoupon = 0.015;
		double dblNotional = 1000000.;
		double dblCleanPrice = 1.002;

		BondComponent tsyBond = TSYBond (
			dtEffective,
			dtMaturity,
			iFreq,
			strDayCount,
			strCurrency,
			dblCoupon
		);

		System.out.println();

		System.out.println ("\tEffective : " + tsyBond.effectiveDate());

		System.out.println ("\tMaturity  : " + tsyBond.maturityDate());

		System.out.println();

		DiscountCurve dc = FundingCurve (
			dtSpot,
			strCurrency
		);

		TSYMetrics (
			tsyBond,
			dblNotional,
			dtSpot,
			MarketParamsBuilder.Create (
				dc,
				null,
				null,
				null,
				null,
				null,
				null
			),
			dblCleanPrice
		);

		System.out.println ("\n\tCashflow\n\t--------");

		for (CompositePeriod p : tsyBond.couponPeriods())
			System.out.println ("\t\t" +
				DateUtil.FromJulian (p.startDate()) + " | " +
				DateUtil.FromJulian (p.endDate()) + " | " +
				DateUtil.FromJulian (p.payDate()) + " | " +
				FormatUtil.FormatDouble (p.couponDCF(), 1, 4, 1.) + " ||"
			);
	}
}
