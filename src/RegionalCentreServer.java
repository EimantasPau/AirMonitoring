import AirMonitoring.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;


class RegionalCentreServant extends RegionalCentrePOA {
    //to store the list of stations connected
    private ArrayList<StationInfo> connectedDevices;
    //to store all readings from the connected stations
    private ArrayList<Reading> station_readings;
    //to store alarming readings
    private ArrayList<Reading> alarms;
    //to store the details about the centre
    private CentreInfo info;
    private RegionalCentreServer parent;
    private ORB orb;
    private NamingContextExt nameService;
    //time interval for confirming alarms in minutes
    //alarms that happen within a minute of each other will be confirmed
    private int timePeriod = 1;
    public RegionalCentreServant(ORB orb_val, RegionalCentreServer parent) {
        this.parent = parent;
        connectedDevices = new ArrayList<>();
        station_readings = new ArrayList<>();
        alarms = new ArrayList<>();
        try {
            orb = orb_val;
            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt
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
    public StationInfo[] connected_devices() {
        return connectedDevices.toArray(new StationInfo[0]);
    }

    @Override
    public CentreInfo get_centre_info() {
        return info;
    }

    @Override
    public void set_info(CentreInfo info) {
        this.info = info;
    }

    //returns an array of all readings from all stations connected to this centre
    @Override
    public Reading[] all_readings() {
        //loop through all connected stations
        for(StationInfo station : connectedDevices) {
            String station_name = station.station_name;
            try {
                //find the reference to the station
                MonitoringStation monitoringStation = MonitoringStationHelper.narrow(nameService.resolve_str(station_name));
                //get the readings from the station
                ArrayList<Reading> pulled_readings = new ArrayList<>(Arrays.asList(monitoringStation.reading_log()));
                //check if we have any new readings by comparing the ones stored in the regional centre
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
        return station_readings.toArray(new Reading[0]);
    }


    @Override
    public Reading[] get_readings(String station_name) {
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
    public Reading[] get_current_readings() {
        ArrayList<Reading> currentReadings = new ArrayList<>();
        for(StationInfo s: connected_devices()){
            try {
                MonitoringStation station = MonitoringStationHelper.narrow(nameService.resolve_str(s.station_name));
                currentReadings.add(station.reading());
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
        return currentReadings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] alarms() {
        return alarms.toArray(new Reading[0]);
    }

    @Override
    public void register_monitoring_station(StationInfo info) {
        connectedDevices.add(info);
        System.out.println("New station added." + info.station_name);
    }

    @Override
    public void raise_alarm(Reading reading) {
        //Check if any other alarms were triggered within the same time period
        ArrayList<Reading> foundAlarms = alarms.stream().filter(
                r->!r.station_name.equals(reading.station_name) && r.date == reading.date && (r.time + timePeriod >= reading.time || r.time == reading.time))
                .collect(Collectors.toCollection(ArrayList::new));
        //add to alarms array
        if(!listContains(alarms, reading)) {
            alarms.add(reading);
            foundAlarms.add(reading);
        }
        //if true, alarm is confirmed
        if(foundAlarms.size() > 1){
            //create new alarm instance and raise the alarm to the environmental centre
            Alarm alarm = new Alarm(this.get_centre_info().centre_name, foundAlarms.toArray(new Reading[0]));
            parent.environmentalServant.raise_alarm(alarm);
        }
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
    private RegionalCentreServant servant;
    EnvironmentalCentre environmentalServant;

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
            servant = new RegionalCentreServant(orb, this);

            // get the 'stringified IOR'
            Object ref = rootpoa.servant_to_reference(servant);
            RegionalCentre cref = RegionalCentreHelper.narrow(ref);

            // Get a reference to the Naming service
            Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt
            NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
            if (nameService == null) {
                System.out.println("nameService = null");
                return;
            }

            // bind the centre object in the Naming service
            NameComponent[] sName = nameService.to_name(centreName);
            nameService.rebind(sName, cref);

            //register with the environmental centre
            environmentalServant = EnvironmentalCentreHelper.narrow(nameService.resolve_str(environmentalCentre));
            CentreInfo newCentre = new CentreInfo(centreName, location);
            servant.set_info(newCentre);
            environmentalServant.register_regional_centre(newCentre);

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


