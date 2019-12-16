# EdgeCloudSim

EdgeCloudSim provides a simulation environment specific to Edge Computing scenarios where it is possible to conduct experiments that considers both computational and networking resources. EdgeCloudSim is based on CloudSim but adds considerable functionality so that it can be efficiently used for Edge Computing scenarios. EdgeCloudSim is an open source tool and any contributions are welcome. If you want to contribute EdgeCloudSim, please check below feature list and the [contributing guidelines](/CONTRIBUTING.md). If you want to use EdgeCloudSim in your research work, please cite our paper [[3]](https://onlinelibrary.wiley.com/doi/abs/10.1002/ett.3493).

The original repository can be found at https://github.com/CagataySonmez/EdgeCloudSim

# Compiling and Running EdgeCloudSim
Clone the repository: https://github.com/abhaumick/EdgeCloudSim/tree/Crawdad-Mobility

Follow the following steps to install and compile. 
https://github.com/CagataySonmez/EdgeCloudSim/wiki/EdgeCloudSim-in-Eclipse:-step-by-step-installation-&-running-sample-application

Sample app 5 can be used to run Nomadic 2D Mobility model. In scripts/sample_app5/config there are multiple xml files for setting parameters for the simulation. In src\edu\boun\edgecloudsim\applications\sample_app5\MainApp.java set the file names for edge_devices.xml and mobile_devices.xml. "mobile_devices_new{1,2,3,4}" represent 4 datasets taken from Crawdad Kth walker database for the city of stockholm.

Use MATLAB to plot and analyze results as generated in output folder selected in the main app. scripts to generate plots are provided in scripts\sample_app5\matlab

Sample app 6 can be used to analyze pedestrian mobility. By default Best K-Closest Edge Selection policy is selected. Run and compile using the same steps as for sample app 5.
To change to closest Selection policy make the following changes :
  Under src\edu\boun\edgecloudsim\applications\sample_app6\sampleScenarioFactory.java 
       under public MobilityModel getMobilityModel()
        line 57:  change MobilityKclosest to MobilityCrawdad


For further doubts contact a.bhaumick@gmail.com, kgoel62@gmail.com
