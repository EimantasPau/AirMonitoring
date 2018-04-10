import AirMonitoring.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


class RegionalCentreServant extends RegionalCentrePOA {
    ArrayList<StationInfo> connectedDevices;
    ArrayList<Reading> station_readings;
    private CentreInfo info;
    private ORB orb;
    private NamingContextExt nameService;
    public RegionalCentreServant(ORB orb_val) {
        connectedDevices = new ArrayList<>();
        station_readings = new ArrayList<>();
        try {
            orb = orb_val;
            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            nameService = NamingContextExtHelper.narrow(nameServiceObj);
            if (nameService == null) {
                System.out.println("nameService = null");
                return;
            }

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }


    @Override
    public StationInfo[] connectedDevices() {
        return connectedDevices.toArray(new StationInfo[0]);
    }

    @Override
    public void connectedDevices(StationInfo[] newConnectedDevices) {

    }

    @Override
    public CentreInfo centreInfo() {
        return info;
    }

    @Override
    public void setInfo(CentreInfo info) {
        this.info = info;
    }

    //returns an array of centreReadings from all stations connected to this centre
    @Override
    public Reading[] allReadings() {
        ArrayList<Reading> readings = new ArrayList<>();
        //loop through all connected stations
        for(StationInfo station : connectedDevices) {
            String station_name = station.station_name;
            try {
                //find the reference to the station
                MonitoringStation monitoringStation = MonitoringStationHelper.narrow(nameService.resolve_str(station_name));


                ArrayList<Reading> pulled_readings = new ArrayList<>(Arrays.asList(monitoringStation.reading_log()));
                //check if we have any new readings
                if(pulled_readings.size() != 0) {

                    Iterator<Reading> pulledIterator = pulled_readings.iterator();

                    while(pulledIterator.hasNext()) {
                        Reading pulled_reading = pulledIterator.next();
                            if(!listContains(station_readings, pulled_reading)) {
                                station_readings.add(pulled_reading);
                            }
                        }
                }
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        station_readings.addAll(readings);

        return station_readings.toArray(new Reading[0]);
    }


    @Override
    public Reading[] readings(String station_name) {
        try {
            MonitoringStation station = MonitoringStationHelper.narrow(nameService.resolve_str(station_name));
            return station.reading_log();
        } catch (NotFound notFound) {
            notFound.printStackTrace();
            return new Reading[0];
        } catch (CannotProceed cannotProceed) {
            cannotProceed.printStackTrace();
            return new Reading[0];
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();
            return new Reading[0];
        }
    }

    @Override
    public Reading[] alarms() {
        return new Reading[0];
    }

    @Override
    public void registerMonitoringStation(StationInfo info) {
        connectedDevices.add(info);
        System.out.println("New station added." + info.station_name);
    }

    public boolean listContains(ArrayList<Reading> list, Reading r) {
        for(Reading reading: list){
            if((reading.reading_value == r.reading_value) &&
                    (reading.station_name.equals(r.station_name)) &&
                    (reading.date == r.date) &&
                    (reading.time == r.time)){
                return true;
            }
        }
        return false;
    }
}

public class RegionalCentreServer {
    //program arguments
    private String centreName;
    private String location;
    private String environmentalCentre;

    public RegionalCentreServer(String[] args){


        if(args.length < 3) {
            System.out.println("Please provide all of the arguments to start the station.");
            return;
        }
        //get program args
        centreName = args[0];
        location = args[1];
        environmentalCentre = args[2];
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            RegionalCentreServant servant = new RegionalCentreServant(orb);

            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            RegionalCentre cref = RegionalCentreHelper.narrow(ref);

            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
            if (nameService == null) {
                System.out.println("nameService = null");
                return;
            }

            // bind the centre object in the Naming service
            NameComponent[] sName = nameService.to_name(centreName);
            nameService.rebind(sName, cref);

            //register with the environmental centre
            EnvironmentalCentre environmentalServant = EnvironmentalCentreHelper.narrow(nameService.resolve_str(environmentalCentre));
            CentreInfo newCentre = new CentreInfo(centreName, location, "someid");
            environmentalServant.registerRegionalCentre(newCentre);

            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }
    public static void main(String args[]) {
        new RegionalCentreServer(args);
    }
}


