
package org.drip.service.bridge;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * CreditAnalyticsRequest contains the requests for the Credit Analytics server from the client. It contains
 * 	the following parameters:
 * 	- The GUID and the time-stamp of the request.
 * 	- The component that is being valued.
 * 	- The valuation, the pricer, and the quoting parameters.
 * 	- The market parameters assembled in the ComponentMarketParams.
 * 
 * Typical usage is: Client fills in the entities in the request, serializes them, and sends them to the
 * 	server, and receives a serialized response back from the server.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsRequest extends org.drip.service.stream.Serializer {
	private java.lang.String _strID = "";
	private java.lang.String _strTime = "";
	private org.drip.product.definition.FixedIncomeComponent _comp = null;
	private org.drip.param.pricer.PricerParams _pricerParams = null;
	private org.drip.param.valuation.ValuationParams _valParams = null;
	private org.drip.param.market.CurveSurfaceQuoteSet _mktParams = null;
	private org.drip.param.valuation.ValuationCustomizationParams _quotingParams = null;

	/**
	 * CreditAnalyticsRequest de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CreditAnalyticsRequest cannot be properly de-serialized
	 */

	public CreditAnalyticsRequest (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CreditAnalyticsRequest de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsRequest de-serializer: Empty state");

		java.lang.String strSerializedCreditAnalyticsRequest = strRawString.substring (0,
			strRawString.indexOf (objectTrailer()));

		if (null == strSerializedCreditAnalyticsRequest || strSerializedCreditAnalyticsRequest.isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsRequest de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedCreditAnalyticsRequest, fieldDelimiter());

		if (null == astrField || 8 > astrField.length)
			throw new java.lang.Exception ("CreditAnalyticsRequest de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate Request ID");

		_strID = new java.lang.String (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate comp params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_comp = null;
		else
			_comp = new org.drip.product.credit.BondComponent (astrField[2].getBytes());

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate valuation params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_valParams = null;
		else
			_valParams = new org.drip.param.valuation.ValuationParams (astrField[3].getBytes());

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate pricer params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_pricerParams = null;
		else
			_pricerParams = new org.drip.param.pricer.PricerParams (astrField[4].getBytes());

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate component market params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			_mktParams = null;
		else
			_mktParams = new org.drip.param.market.CurveSurfaceQuoteSet (astrField[5].getBytes());

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception
				("CreditAnalyticsRequest de-serializer: Cannot locate Quoting params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_quotingParams = null;
		else
			_quotingParams = new org.drip.param.valuation.ValuationCustomizationParams (astrField[6].getBytes());

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("CreditAnalyticsRequest de-serializer: Cannot locate time stamp");

		_strTime = astrField[7];
	}

	/**
	 * CreditAnalyticsRequest constructor
	 * 
	 * @param comp Component
	 * @param valParams Valuation Parameters
	 * @param pricerParams Price Parameters
	 * @param mktParams Component market Parameters
	 * @param quotingParams Quoting Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are not valid
	 */

	public CreditAnalyticsRequest (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
		throws java.lang.Exception
	{
		if (null == (_comp = comp) || null == (_valParams = valParams))
			throw new java.lang.Exception ("CreditAnalyticsRequest ctr: Invalid inputs");

		_mktParams = mktParams;
		_pricerParams = pricerParams;
		_quotingParams = quotingParams;

		_strTime = new java.util.Date().toString();

		_strID = org.drip.quant.common.StringUtil.GUID();
	}

	/**
	 * Retrieve the Request ID
	 * 
	 * @return The Request ID
	 */

	public java.lang.String getID()
	{
		return _strID;
	}

	/**
	 * Retrieve the Time Snap
	 * 
	 * @return The Time Snap
	 */

	public java.lang.String getTimeSnap()
	{
		return _strTime;
	}

	/**
	 * Retrieve the Component
	 * 
	 * @return The Component
	 */

	public org.drip.product.definition.FixedIncomeComponent getComponent()
	{
		return _comp;
	}

	/**
	 * Retrieve the Valuation Parameters
	 * 
	 * @return The Valuation Parameters
	 */

	public org.drip.param.valuation.ValuationParams getValuationParams()
	{
		return _valParams;
	}

	/**
	 * Retrieve the Pricer Parameters
	 * 
	 * @return The Pricer Parameters
	 */

	public org.drip.param.pricer.PricerParams getPricerParams()
	{
		return _pricerParams;
	}

	/**
	 * Retrieve the Component Market Parameters
	 * 
	 * @return The Component Market Parameters
	 */

	public org.drip.param.market.CurveSurfaceQuoteSet csqs()
	{
		return _mktParams;
	}

	/**
	 * Retrieve the Quoting Parameters
	 * 
	 * @return The Quoting Parameters
	 */

	public org.drip.param.valuation.ValuationCustomizationParams getQuotingParams()
	{
		return _quotingParams;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "(";
	}

	@Override public java.lang.String objectTrailer()
	{
		return ")";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_strID + fieldDelimiter());

		if (null == _comp)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_comp.serialize()) + fieldDelimiter());

		if (null == _valParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_valParams.serialize()) + fieldDelimiter());

		if (null == _pricerParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_pricerParams.serialize()) + fieldDelimiter());

		if (null == _mktParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_mktParams.serialize()) + fieldDelimiter());

		if (null == _quotingParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_quotingParams.serialize()) + fieldDelimiter());

		sb.append (_strTime);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab) {
		try {
			return new CreditAnalyticsRequest (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final org.drip.product.credit.BondComponent MakeBond()
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().julian();

		double[] adblDate = new double[3];
		double[] adblPutDate = new double[3];
		double[] adblCallDate = new double[3];
		double[] adblPutFactor = new double[3];
		double[] adblCallFactor = new double[3];
		double[] adblCouponFactor = new double[3];
		double[] adblNotionalFactor = new double[3];
		adblPutFactor[0] = 0.80;
		adblPutFactor[1] = 0.90;
		adblPutFactor[2] = 1.00;
		adblCallFactor[0] = 1.20;
		adblCallFactor[1] = 1.10;
		adblCallFactor[2] = 1.00;
		adblPutDate[0] = dblStart + 30.;
		adblPutDate[1] = dblStart + 396.;
		adblPutDate[2] = dblStart + 761.;
		adblCallDate[0] = dblStart + 1126.;
		adblCallDate[1] = dblStart + 1492.;
		adblCallDate[2] = dblStart + 1857.;

		for (int i = 0; i < 3; ++i) {
			adblCouponFactor[i] = 1 - 0.1 * i;
			adblNotionalFactor[i] = 1 - 0.05 * i;
			adblDate[i] = dblStart + 365. * (i + 1);
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0402);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.product.params.PeriodGenerator bpgp = new
			org.drip.product.params.PeriodGenerator (dblStart + 3653., dblStart, dblStart + 3653.,
				dblStart + 182., dblStart, 2, "30/360", "30/360", null, null, null, null, null, null, null,
					null, "IGNORE", false, "USD", "USD");

		if (!bpgp.validate()) {
			System.out.println ("Cannot validate BPGP!");

			System.exit (125);
		}

		org.drip.product.credit.BondComponent bond = new org.drip.product.credit.BondComponent();

		if (!bond.setTreasuryBenchmark (new org.drip.product.params.TsyBmkSet ("USD5YON", new
			java.lang.String[] {"USD3YON", "USD7YON"}))) {
			System.out.println ("Cannot initialize bond TSY params!");

			System.exit (126);
		}

		if (!bond.setCouponSetting (new org.drip.product.params.CouponSetting
			(org.drip.product.params.FactorSchedule.CreateFromDateFactorArray (adblDate, adblCouponFactor),
				"FLOATER", 0.05, java.lang.Double.NaN, java.lang.Double.NaN))) {
			System.out.println ("Cannot initialize bond Coupon params!");

			System.exit (127);
		}

		if (!bond.setNotionalSetting (new org.drip.product.params.NotionalSetting
			(org.drip.product.params.FactorSchedule.CreateFromDateFactorArray (adblDate, adblNotionalFactor),
				1., org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_START, false))) {
			System.out.println ("Cannot initialize bond Notional params!");

			System.exit (128);
		}

		if (!bond.setFixings (mmFixings)) {
			System.out.println ("Cannot initialize bond Fixings!");

			System.exit (130);
		}

		if (!bond.setCurrencySet (org.drip.product.params.CurrencySet.Create ("USD"))) {
			System.out.println ("Cannot initialize bond currency params!");

			System.exit (131);
		}

		if (!bond.setIdentifierSet (new org.drip.product.params.IdentifierSet ("US07942381EZ", "07942381E",
			"IBM-US07942381EZ", "IBM"))) {
			System.out.println ("Cannot initialize bond Identifier params!");

			System.exit (132);
		}

		if (!bond.setMarketConvention (new org.drip.product.params.QuoteConvention (new
			org.drip.param.valuation.ValuationCustomizationParams ("30/360", 2, true, null, "DKK", false,
				null, null), "REGULAR", dblStart + 2, 1., 3, "USD",
					org.drip.analytics.daycount.Convention.DR_FOLL))) {
			System.out.println ("Cannot initialize bond IR Valuation params!");

			System.exit (133);
		}

		if (!bond.setRatesSetting (new org.drip.product.params.RatesSetting ("USD", "USD", "USD", "USD"))) {
			System.out.println ("Cannot initialize Bond Rates Valuation params!");

			System.exit (153);
		}

		if (!bond.setCreditSetting (new org.drip.product.params.CreditSetting (30, java.lang.Double.NaN,
			true, "IBMSUB", false))) {
			System.out.println ("Cannot initialize bond Credit Valuation params!");

			System.exit (134);
		}

		if (!bond.setTerminationSetting (new org.drip.product.params.TerminationSetting (false, false,
			false))) {
			System.out.println ("Cannot initialize bond CFTE params!");

			System.exit (135);
		}

		if (!bond.setPeriodSet (bpgp)) {
			System.out.println ("Cannot initialize bond Period Generation params!");

			System.exit (136);
		}

		return bond;
	}

	private static org.drip.param.market.CurveSurfaceQuoteSet MakeCSQS (
		final org.drip.product.definition.FixedIncomeComponent fic)
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().julian();

		double[] adblDate = new double[3];
		double[] adblRate = new double[3];
		double[] adblRateTSY = new double[3];
		double[] adblHazardRate = new double[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRate[i] = 0.015 * (i + 1);
			adblRateTSY[i] = 0.01 * (i + 1);
			adblHazardRate[i] = 0.01 * (i + 1);
		}

		org.drip.analytics.rates.ExplicitBootDiscountCurve dc =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABC", null, adblDate, adblRate,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.rates.ExplicitBootDiscountCurve dcTSY =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCTSY", null, adblDate, adblRateTSY,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.definition.ExplicitBootCreditCurve cc =
			org.drip.state.creator.CreditCurveBuilder.CreateCreditCurve
				(org.drip.analytics.date.JulianDate.Today(), "ABCSOV", "USD", adblDate, adblHazardRate,
						0.40);

		org.drip.param.market.ProductMultiMeasureQuote cqTSY2ON = new
			org.drip.param.market.ProductMultiMeasureQuote();

		cqTSY2ON.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103.,
			java.lang.Double.NaN), false);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapTSYQuotes = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		mapTSYQuotes.put ("TSY2ON", cqTSY2ON);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.param.market.ProductMultiMeasureQuote cqBond = new
			org.drip.param.market.ProductMultiMeasureQuote();

		cqBond.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 100.,
			java.lang.Double.NaN), true);

		return org.drip.param.creator.MarketParamsBuilder.Create (dc, null, dcTSY, cc, fic.name(),
			cqBond, mapTSYQuotes, mmFixings);
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.support.Logger.Init ("c:\\DRIP\\Config.xml");

		org.drip.analytics.daycount.Convention.Init ("c:\\DRIP\\Config.xml");

		org.drip.product.credit.BondComponent bond = MakeBond();

		org.drip.param.valuation.ValuationParams valParams =
			org.drip.param.valuation.ValuationParams.CreateValParams
				(org.drip.analytics.date.JulianDate.Today(), 2, "USD", 3);

		org.drip.param.pricer.PricerParams pricerParams = new org.drip.param.pricer.PricerParams (7, new
			org.drip.param.definition.CalibrationParams ("Price", 1, new org.drip.param.valuation.WorkoutInfo
				(org.drip.analytics.date.JulianDate.Today().julian(), 0.04, 1.,
					org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY)), false, 1, false);

		org.drip.param.market.CurveSurfaceQuoteSet mktParams = MakeCSQS (bond);

		org.drip.param.valuation.ValuationCustomizationParams quotingParams = new
			org.drip.param.valuation.ValuationCustomizationParams ("30/360", 2, true, null, "USD", false,
				null, null);

		CreditAnalyticsRequest cre = new CreditAnalyticsRequest (bond, valParams, pricerParams, mktParams,
			quotingParams);

		byte[] abCRE = cre.serialize();

		java.lang.String strCRE = new java.lang.String (abCRE);

		System.out.println (strCRE);

		CreditAnalyticsRequest creDeser = new CreditAnalyticsRequest (abCRE);

		System.out.println (new java.lang.String (creDeser.serialize()));
	}
}
