/*
 * Title:        EdgeCloudSim - Nomadic Mobility model implementation
 * 
 * Description: 
 * MobilityModel implements basic nomadic mobility model where the
 * place of the devices are changed from time to time instead of a
 * continuous location update.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.mobility;

import java.lang.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class Nomadic2DMobility extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private List<TreeMap<Double, Location>> mobileTreeMapArray;

	private int minX = 550;
	private int minY = 1450;
	private int maxX = 1000;
	private int maxY = 1820;

	public Nomadic2DMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}

	private NodeList datacenterList;

	private int getClosestDatacenter(int mobileX, int mobileY) {

		// Trace closest edge datacenter
		int closestDatacenterIndex = 0;
		int distToClosestDatacenter = Integer.MAX_VALUE; // Will be initialized in case of first comparison
		int numberOfDatacenters = SimSettings.getInstance().getNumOfEdgeDatacenters();

		for (int j = 0; j < numberOfDatacenters; j++) {
			Node datacenterNode = datacenterList.item(j);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());

			// Find pythagorean distance from datacenter
			int distToDatacenter = (int) Math.sqrt(Math.pow((mobileX - x_pos), 2) + Math.pow((mobileY - y_pos), 2));

			if (distToDatacenter < distToClosestDatacenter) {
				closestDatacenterIndex = j;
				distToClosestDatacenter = distToDatacenter;
			}
		}

		return closestDatacenterIndex;
	}

	@Override
	public void initialize() {
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		mobileTreeMapArray = new ArrayList<TreeMap<Double, Location>>();
		Random rand = new Random();
		ExponentialDistribution[] expRngList = new ExponentialDistribution[SimSettings.getInstance()
				.getNumOfEdgeDatacenters()];

		// create random number generator for each edge
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);

			expRngList[i] = new ExponentialDistribution(
					SimSettings.getInstance().getMobilityLookUpTable()[placeTypeIndex]);
		}

		// initialize tree maps and position of mobile devices
		for (int i = 0; i < numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());
			mobileTreeMapArray.add(i, new TreeMap<Double, Location>());

			// Generate random x,y coord for location of mobile node
			int randX = rand.nextInt(maxX);
			int randY = rand.nextInt(maxY);

			// Trace closest edge datacenter
			int closestDatacenterIndex = getClosestDatacenter(randX, randY);

			// Insert initial position of mobile devices
			Node datacenterNode = datacenterList.item(closestDatacenterIndex);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);
			int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
			//System.out.println("this one: "+x_pos+"     "+y_pos+"     " + wlan_id);
			// start locating user shortly after the simulation started (e.g. 10 seconds)
			treeMapArray.get(i).put(SimSettings.CLIENT_ACTIVITY_START_TIME,
					new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
			mobileTreeMapArray.get(i).put(SimSettings.CLIENT_ACTIVITY_START_TIME,
					new Location(placeTypeIndex, wlan_id, randX, randY));
		}

		for (int i = 0; i < numberOfMobileDevices; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);

			while (treeMap.lastKey() < SimSettings.getInstance().getSimulationTime()) {
				//boolean placeFound = false;
				int currentLocationId = treeMap.lastEntry().getValue().getServingWlanId();
				double waitingTime = expRngList[currentLocationId].sample();

				/*int randX = SimUtils.getRandomNumber(0, maxX - 1);
				int randY = SimUtils.getRandomNumber(0, maxY - 1);*/
				
				int randX = minX + rand.nextInt(maxX-minX+1);	//	Zero inclusive, bound exclusive
				int randY = minY + rand.nextInt(maxY-minY+1);

				int closestDatacenterIndex = getClosestDatacenter(randX, randY);

				Node datacenterNode = datacenterList.item(closestDatacenterIndex);
				Element datacenterElement = (Element) datacenterNode;
				Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
				String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
				int placeTypeIndex = Integer.parseInt(attractiveness);
				int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
				int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
				int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());

				treeMap.put(treeMap.lastKey() + waitingTime, new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
				mobileTreeMapArray.get(i).put(treeMap.lastKey() + waitingTime,
						new Location(placeTypeIndex, wlan_id, x_pos, y_pos));

				/*
				 * while (placeFound == false) { int newDatacenterId =
				 * SimUtils.getRandomNumber(0,
				 * SimSettings.getInstance().getNumOfEdgeDatacenters() - 1); if (newDatacenterId
				 * != currentLocationId) { placeFound = true; Node datacenterNode =
				 * datacenterList.item(newDatacenterId); Element datacenterElement = (Element)
				 * datacenterNode; Element location = (Element)
				 * datacenterElement.getElementsByTagName("location").item(0); String
				 * attractiveness = location.getElementsByTagName("attractiveness").item(0)
				 * .getTextContent(); int placeTypeIndex = Integer.parseInt(attractiveness); int
				 * wlan_id = Integer
				 * .parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
				 * int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).
				 * getTextContent()); int y_pos =
				 * Integer.parseInt(location.getElementsByTagName("y_pos").item(0).
				 * getTextContent());
				 * 
				 * treeMap.put(treeMap.lastKey() + waitingTime, new Location(placeTypeIndex,
				 * wlan_id, x_pos, y_pos)); } }
				 */
				}
		}

	}

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);

		Entry<Double, Location> e = treeMap.floorEntry(time);

		if (e == null) {
			SimLogger.printLine(
					"impossible is occured! no location is found for the device '" + deviceId + "' at " + time);
			System.exit(0);
		}

		return e.getValue();
	}

	@Override
	public Location getMobileLocation(int deviceId, double time) {
		TreeMap<Double, Location> mobileTreeMap = mobileTreeMapArray.get(deviceId);

		Entry<Double, Location> e = mobileTreeMap.floorEntry(time);

		if (e == null) {
			SimLogger.printLine(
					"impossible is occured! no location is found for the device '" + deviceId + "' at " + time);
			System.exit(0);
		}

		return e.getValue();
	}


}
