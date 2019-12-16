/*
 * Title:        EdgeCloudSim - Crawdad mobility implementation on sample app 2
 * 
 * Description: 
 * MobilityModel implements an observed mobility model where the
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

public class MobilityCrawdad extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private int maxX = 450;
	private int maxY = 370;

	public MobilityCrawdad(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
	}

    private NodeList datacenterList;
    private NodeList mobiledevicesList;

	private int getClosestDatacenter(Double x_pos2, Double y_pos2) {

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
			int distToDatacenter = (int) Math.sqrt(Math.pow((x_pos2 - x_pos), 2) + Math.pow((y_pos2 - y_pos), 2));

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
		ExponentialDistribution[] expRngList = new ExponentialDistribution[SimSettings.getInstance()
				.getNumOfEdgeDatacenters()];

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
		Document clientdoc = SimSettings.getInstance().getMobileDevicesDocument();
		mobiledevicesList = clientdoc.getElementsByTagName("Time");
		for (int i = 0; i < mobiledevicesList.getLength(); i++) {
			
			Node clientNode = mobiledevicesList.item(i);
			Element clientElement = (Element) clientNode;
            NodeList clientList = clientElement.getElementsByTagName("Client");
            Element Time = (Element) clientNode;
            double time = Double.parseDouble(Time.getElementsByTagName("time").item(0).getTextContent());
            for(int l=0; l < clientList.getLength(); l++){
                Element client = (Element) Time.getElementsByTagName("Client").item(l);
                int node = Integer.parseInt(client.getElementsByTagName("Node").item(0).getTextContent());
                if(node > numberOfMobileDevices) {
                	break;
                }
                if(treeMapArray.get(node)==null) {
                	treeMapArray.add(node, new TreeMap<Double, Location>());
                }
    			treeMapArray.add(node, new TreeMap<Double, Location>());
                Double X_pos = Double.parseDouble(client.getElementsByTagName("X_Pos").item(0).getTextContent());
                Double Y_pos = Double.parseDouble(client.getElementsByTagName("Y_Pos").item(0).getTextContent());
                int closestDatacenterIndex = getClosestDatacenter(X_pos, Y_pos);
                System.out.println(closestDatacenterIndex);
				Node datacenterNode = datacenterList.item(closestDatacenterIndex);
				Element datacenterElement = (Element) datacenterNode;
				Element location_d = (Element) datacenterElement.getElementsByTagName("location").item(0);
				//String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
				//int placeTypeIndex = Integer.parseInt(attractiveness);
				int wlan_id = Integer.parseInt(location_d.getElementsByTagName("wlan_id").item(0).getTextContent());
				int x_pos = Integer.parseInt(location_d.getElementsByTagName("x_pos").item(0).getTextContent());
				int y_pos = Integer.parseInt(location_d.getElementsByTagName("y_pos").item(0).getTextContent());
				TreeMap<Double, Location> treeMap = treeMapArray.get(i);
				treeMap.put(time, new Location(0, wlan_id, x_pos, y_pos));
				treeMapArray.get(node).put(time,
						new Location(0, wlan_id, x_pos, y_pos));
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
}