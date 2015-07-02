/*
 * Copyright 2010-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.pte;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.base.Charsets;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Style;

/**
 * @author Andreas Schildbach
 */
public class ShProvider extends AbstractHafasProvider
{
	private static final String API_BASE = "http://nah.sh.hafas.de/bin/";

	public ShProvider()
	{
		super(NetworkId.SH, API_BASE + "stboard.exe/dn", API_BASE + "ajax-getstop.exe/dn", API_BASE + "query.exe/dn", 10, Charsets.UTF_8);
		setStyles(STYLES);
	}

	@Override
	protected Product intToProduct(final int value)
	{
		if (value == 1)
			return Product.HIGH_SPEED_TRAIN;
		if (value == 2)
			return Product.HIGH_SPEED_TRAIN;
		if (value == 4)
			return Product.HIGH_SPEED_TRAIN;
		if (value == 8)
			return Product.REGIONAL_TRAIN;
		if (value == 16)
			return Product.SUBURBAN_TRAIN;
		if (value == 32)
			return Product.BUS;
		if (value == 64)
			return Product.FERRY;
		if (value == 128)
			return Product.SUBWAY;
		if (value == 256)
			return Product.TRAM;
		if (value == 512)
			return Product.ON_DEMAND;

		throw new IllegalArgumentException("cannot handle: " + value);
	}

	@Override
	protected void setProductBits(final StringBuilder productBits, final Product product)
	{
		if (product == Product.HIGH_SPEED_TRAIN)
		{
			productBits.setCharAt(0, '1'); // Hochgeschwindigkeitszug
			productBits.setCharAt(1, '1'); // IC/EC
			productBits.setCharAt(2, '1'); // Fernverkehrszug
		}
		else if (product == Product.REGIONAL_TRAIN)
		{
			productBits.setCharAt(3, '1'); // Regionalverkehrszug
		}
		else if (product == Product.SUBURBAN_TRAIN)
		{
			productBits.setCharAt(4, '1'); // S-Bahn
		}
		else if (product == Product.SUBWAY)
		{
			productBits.setCharAt(7, '1'); // U-Bahn
		}
		else if (product == Product.TRAM)
		{
			productBits.setCharAt(8, '1'); // Stadtbahn
		}
		else if (product == Product.BUS)
		{
			productBits.setCharAt(5, '1'); // Bus
		}
		else if (product == Product.ON_DEMAND)
		{
			productBits.setCharAt(9, '1'); // Anruf-Sammel-Taxi
		}
		else if (product == Product.FERRY)
		{
			productBits.setCharAt(6, '1'); // Schiff
		}
		else if (product == Product.CABLECAR)
		{
		}
		else
		{
			throw new IllegalArgumentException("cannot handle: " + product);
		}
	}

	private static final String[] PLACES = { "Hamburg", "Kiel", "Lübeck", "Flensburg", "Neumünster" };

	@Override
	protected String[] splitStationName(final String name)
	{
		for (final String place : PLACES)
			if (name.startsWith(place + " ") || name.startsWith(place + "-"))
				return new String[] { place, name.substring(place.length() + 1) };

		return super.splitStationName(name);
	}

	@Override
	protected String[] splitPOI(final String poi)
	{
		final Matcher m = P_SPLIT_NAME_FIRST_COMMA.matcher(poi);
		if (m.matches())
			return new String[] { m.group(1), m.group(2) };

		return super.splitStationName(poi);
	}

	@Override
	protected String[] splitAddress(final String address)
	{
		final Matcher m = P_SPLIT_NAME_FIRST_COMMA.matcher(address);
		if (m.matches())
			return new String[] { m.group(1), m.group(2) };

		return super.splitStationName(address);
	}

	@Override
	public NearbyLocationsResult queryNearbyLocations(final EnumSet<LocationType> types, final Location location, final int maxDistance,
			final int maxLocations) throws IOException
	{
		if (location.type == LocationType.STATION && location.hasId())
			return nearbyStationsById(location.id, maxDistance);
		else
			throw new IllegalArgumentException("cannot handle: " + location);
	}

	@Override
	protected NearbyLocationsResult nearbyStationsById(final String id, final int maxDistance) throws IOException
	{
		final StringBuilder uri = new StringBuilder(stationBoardEndpoint);
		uri.append("?near=Anzeigen");
		uri.append("&distance=").append(maxDistance != 0 ? maxDistance / 1000 : 50);
		uri.append("&input=").append(normalizeStationId(id));
		return htmlNearbyStations(uri.toString());
	}

	protected static final Map<String, Style> STYLES = new HashMap<String, Style>();

	static {
		// Busse Kiel
		putKielBusStyle("1", new Style(Style.parseColor("#7288af"), Style.WHITE));
		putKielBusStyle("2", new Style(Style.parseColor("#50bbb4"), Style.WHITE));
		putKielBusStyle("5", new Style(Style.parseColor("#f39222"), Style.WHITE));
		putKielBusStyle("6", new Style(Style.parseColor("#aec436"), Style.WHITE));
		putKielBusStyle("8", new Style(Style.parseColor("#bcb261"), Style.WHITE));
		putKielBusStyle("9", new Style(Style.parseColor("#c99c7d"), Style.WHITE));
		putKielBusStyle("11", new Style(Style.parseColor("#f9b000"), Style.WHITE));
		putKielBusStyle("22", new Style(Style.parseColor("#8ea48a"), Style.WHITE));
		putKielBusStyle("31", new Style(Style.parseColor("#009ee3"), Style.WHITE));
		putKielBusStyle("32", new Style(Style.parseColor("#009ee3"), Style.WHITE));
		putKielBusStyle("33", new Style(Style.parseColor("#009ee3"), Style.WHITE));
		putKielBusStyle("34", new Style(Style.parseColor("#009ee3"), Style.WHITE));
		putKielBusStyle("41", new Style(Style.parseColor("#8ba5d6"), Style.WHITE));
		putKielBusStyle("42", new Style(Style.parseColor("#8ba5d6"), Style.WHITE));
		putKielBusStyle("50", new Style(Style.parseColor("#00a138"), Style.WHITE));
		putKielBusStyle("51", new Style(Style.parseColor("#00a138"), Style.WHITE));
		putKielBusStyle("52", new Style(Style.parseColor("#00a138"), Style.WHITE));
		putKielBusStyle("60S", new Style(Style.parseColor("#92b4af"), Style.WHITE));
		putKielBusStyle("60", new Style(Style.parseColor("#92b4af"), Style.WHITE));
		putKielBusStyle("61", new Style(Style.parseColor("#9d1380"), Style.WHITE));
		putKielBusStyle("62", new Style(Style.parseColor("#9d1380"), Style.WHITE));
		putKielBusStyle("71", new Style(Style.parseColor("#777e6f"), Style.WHITE));
		putKielBusStyle("72", new Style(Style.parseColor("#777e6f"), Style.WHITE));
		putKielBusStyle("81", new Style(Style.parseColor("#00836e"), Style.WHITE));
		putKielBusStyle("91", new Style(Style.parseColor("#947e62"), Style.WHITE));
		putKielBusStyle("92", new Style(Style.parseColor("#947e62"), Style.WHITE));
		putKielBusStyle("100", new Style(Style.parseColor("#d40a11"), Style.WHITE));
		putKielBusStyle("101", new Style(Style.parseColor("#d40a11"), Style.WHITE));
		putKielBusStyle("300", new Style(Style.parseColor("#cf94c2"), Style.WHITE));
		putKielBusStyle("501", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
		putKielBusStyle("502", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
		putKielBusStyle("503", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
		putKielBusStyle("503S", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
		putKielBusStyle("512", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
		putKielBusStyle("512S", new Style(Style.parseColor("#0f3f93"), Style.WHITE));
	}

	private static void putKielBusStyle(String name, Style style) {
		STYLES.put("AK|B" + name, style);
		STYLES.put("KIEL|B" + name, style);
	}
}
