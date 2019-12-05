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
import java.util.*;

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

class EdgeCenterDistance{
	int centerId;
	int distance;
	public EdgeCenterDistance(int centerId, int distance) {
		this.centerId = centerId;
		this.distance = distance;
	}
}
class CompareDistances implements Comparator<EdgeCenterDistance>{
    @Override
    public int compare(EdgeCenterDistance t1, EdgeCenterDistance t2){
        if(t1.distance < t2.distance)
            return 1;
        else if(t1.distance > t2.distance)
            return -1;
        else
            return 0;
    }
}
public class MobilityKClosest extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	
	private int maxX = 50;
	private int maxY = 50;
	private int[] connectedMobileDevices;
	public MobilityKClosest(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
	}

    private NodeList datacenterList;
    private NodeList mobiledevicesList;

	private int getCloseBestDatacenter(Double x_pos2, Double y_pos2) {
		int k = 3;
		PriorityQueue<EdgeCenterDistance> pQueue = new PriorityQueue<EdgeCenterDistance>(new CompareDistances()); 
		// Trace closest edge datacenter
		int closestDatacenterIndex = Integer.MAX_VALUE;
		//int distToClosestDatacenter = Integer.MAX_VALUE;
		int numberOfDatacenters = SimSettings.getInstance().getNumOfEdgeDatacenters();
		int[] distances = new int[connectedMobileDevices.length];
		for (int j = 0; j < numberOfDatacenters; j++) {
			Node datacenterNode = datacenterList.item(j);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
			// Find pythagorean distance from datacenter
			int distToDatacenter = (int) Math.sqrt(Math.pow((x_pos2 - x_pos), 2) + Math.pow((y_pos2 - y_pos), 2));

			EdgeCenterDistance edgeCenterDistance = new EdgeCenterDistance(j, distToDatacenter);
			pQueue.add(edgeCenterDistance);
			/*if (distToDatacenter < distToClosestDatacenter) {
				closestDatacenterIndex = j;
				distToClosestDatacenter = distToDatacenter;
			}*/
		}
		int numMobile = Integer.MAX_VALUE;
		for(int i=0; i<k ;i++) {
			EdgeCenterDistance ed = pQueue.poll();
			if(connectedMobileDevices[ed.centerId] < numMobile) {
				closestDatacenterIndex = ed.centerId;
				numMobile = connectedMobileDevices[ed.centerId];
			}
		}
		connectedMobileDevices[closestDatacenterIndex]++;

		return closestDatacenterIndex;
	}

	@Override
	public void initialize() {
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		int lengthDataCenters = SimSettings.getInstance().getNumOfEdgeDatacenters();
		ExponentialDistribution[] expRngList = new ExponentialDistribution[lengthDataCenters];
		connectedMobileDevices = new int[lengthDataCenters];
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
		mobiledevicesList = clientdoc.getElementsByTagName("Client");
		for (int i = 0; i < numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());

			Node clientNode = mobiledevicesList.item(i);
			Element clientElement = (Element) clientNode;
            NodeList Timelist = clientElement.getElementsByTagName("Time");
            
            for(int l=0; l < Timelist.getLength(); l++){
                Node Timenode = Timelist.item(l);
                Element Time = (Element) Timenode;
                double time = Double.parseDouble(Time.getElementsByTagName("time").item(0).getTextContent());
                Element location = (Element) Time.getElementsByTagName("Location").item(0);
                Double X_pos = Double.parseDouble(location.getElementsByTagName("X_Pos").item(0).getTextContent());
                Double Y_pos = Double.parseDouble(location.getElementsByTagName("Y_Pos").item(0).getTextContent());
                

                int closestDatacenterIndex = getCloseBestDatacenter(X_pos, Y_pos);

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
