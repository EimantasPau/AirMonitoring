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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

class EnvironmentalCentreServant extends EnvironmentalCentrePOA {
    private EnvironmentalCentreServer parent;
    ArrayList<CentreInfo> connectedCentres;
    ArrayList<Reading> centreReadings;
    ArrayList<Agency> agencies;
    ArrayList<Alarm> alarms;
    private ORB orb;
    private NamingContextExt nameService;


    public EnvironmentalCentreServant(EnvironmentalCentreServer parentGUI, ORB orb_val) {
        this.parent = parentGUI;
        connectedCentres = new ArrayList<>();
        centreReadings = new ArrayList<>();
        agencies = new ArrayList<>();
        alarms = new ArrayList<>();
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
    public Reading[] all_readings() {
        centreReadings.clear();
       for(int i = 0; i < connectedCentres.size(); i++){
           String centreName = connectedCentres.get(i).centre_name;
           try {
               RegionalCentre centreServant = RegionalCentreHelper.narrow(nameService.resolve_str(centreName));
               ArrayList<Reading> stationReadings = new ArrayList<>(Arrays.asList(centreServant.all_readings()));
               //check if we have any new readings
               if(stationReadings.size() != 0) {

                   Iterator<Reading> pulledIterator = stationReadings.iterator();

                   while(pulledIterator.hasNext()) {
                       Reading pulled_reading = pulledIterator.next();
                       if(!listContains(centreReadings, pulled_reading)) {
                           centreReadings.add(pulled_reading);
                       }
                   }
               }
           } catch (NotFound notFound) {
               notFound.printStackTrace();
           } catch (CannotProceed cannotProceed) {
               cannotProceed.printStackTrace();
           } catch (InvalidName invalidName) {
               invalidName.printStackTrace();
           }
       }
        return centreReadings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] get_readings(String centre_name) {
        try {
            RegionalCentre centre = RegionalCentreHelper.narrow(nameService.resolve_str(centre_name));
            return centre.all_readings();
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
    public CentreInfo[] connected_centres() {
        return connectedCentres.toArray(new CentreInfo[0]);
    }

    @Override
    public void register_regional_centre(CentreInfo info) {
        connectedCentres.add(info);
        parent.addToCentreList(info);
        System.out.println("New centre added. " + info.centre_name);
    }

    @Override
    public void unregister_regional_centre(CentreInfo info) {
        System.out.println("Unregistering.");
        for(CentreInfo centre: connectedCentres){
            if(centre.centre_name.equals(info.centre_name)){
                connectedCentres.remove(centre);
            }
        }
    }

    @Override
    public void raise_alarm(Alarm alarm) {
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run() {
                alarms.add(alarm);
                parent.alarmsListModel.addElement(alarm);
                String message = "Alarm triggered at regional centre:" + alarm.centre_name + "\n";
                for(int i = 0; i < alarm.alarm_readings.length; i++) {
                    message += (alarm.alarm_readings[i].station_name + "- Reading value:" +alarm.alarm_readings[i].reading_value + " - " + alarm.alarm_readings[i].date + "/" + alarm.alarm_readings[i].time + "\n");
                }
                //find agencies that are interested in readings from this region
                ArrayList<Agency> interestedAgencies = agencies.stream().filter(
                        a->a.region_of_interest.equals(alarm.centre_name))
                        .collect(Collectors.toCollection(ArrayList::new));

                //if we have any agencies registered, display a message for each of them
                if(interestedAgencies.size() > 0) {
                    for(Agency agency: interestedAgencies){
                        message += agency.name + " is registered for notifications.\n";
                        message += "Contact them on " + agency.contact_details + "\n";
                    }
                }
                JOptionPane.showMessageDialog(parent, message);
            }
        });

    }

    @Override
    public void register_agency(Agency agency) {
        agencies.add(agency);
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


public class EnvironmentalCentreServer extends JFrame {
    NamingContextExt nameService;
    EnvironmentalCentreServant servant;
    private String centreName;
    //main panel
    private JPanel panel;

    private JPanel centresPanel;
    private JPanel stationsPanel;
    private JPanel readingsPanel;
    private JPanel alarmsPanel;

    private JButton getCentreStations;
    private JButton getCentreReadings;
    private JButton getStationReadings;
    private JButton getAllReadings;
    private JButton registerAgency;
    private JButton getCurrentConnectedReadings;

    private JList<CentreInfo> centreList;
    private JList<StationInfo> stationList;
    private JList<Reading> readingsList;
    private JList<Alarm> alarmsList;
    DefaultListModel<CentreInfo> centreListModel;
    DefaultListModel<StationInfo> stationListModel;
    DefaultListModel<Reading> readingsListModel;
    DefaultListModel<Alarm> alarmsListModel;
    private JTextField agencyName;
    private JTextField regionOfInterest;
    private JTextField contactDetails;
    public EnvironmentalCentreServer(String[] args) {
        centreName = args[0];
        try {
            if(centreName == null) {
                System.out.print("Centre information invalid");
                return;
            }
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            servant = new EnvironmentalCentreServant(this, orb);

            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            EnvironmentalCentre cref = EnvironmentalCentreHelper.narrow(ref);

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

            // bind the Count object in the Naming service
            NameComponent[] sName = nameService.to_name(centreName);
            nameService.rebind(sName, cref);
            //initialize GUI
            GUI();
            //initialize listeners
            initListeners();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }

    public void GUI(){
        //GUI
        //main panel
        panel = new JPanel();
        centresPanel = new JPanel();
        stationsPanel = new JPanel();
        readingsPanel = new JPanel();
        alarmsPanel = new JPanel();

        //centre panel
        centresPanel.setLayout(new BoxLayout(centresPanel, BoxLayout.PAGE_AXIS));
        centresPanel.setPreferredSize(new Dimension(250, 200));

        centreListModel = new DefaultListModel<>();
        centreList = new JList<>(centreListModel);
        centreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        centreList.setVisibleRowCount(-1);
        CentreListCellRenderer centreRenderer = new CentreListCellRenderer();
        centreList.setCellRenderer(centreRenderer);
        JScrollPane centreListScroller = new JScrollPane(centreList);
        centreListScroller.setPreferredSize(new Dimension(250, 80));
        JLabel centreListTitle = new JLabel("Connected centres");
        getCentreStations = new JButton("Get stations at centre");

        centresPanel.add(centreListTitle);
        centresPanel.add(centreListScroller);

        //station panel
        stationsPanel.setLayout(new BoxLayout(stationsPanel, BoxLayout.PAGE_AXIS));
        stationsPanel.setPreferredSize(new Dimension(250, 200));

        stationListModel = new DefaultListModel<>();
        stationList = new JList<>(stationListModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setVisibleRowCount(-1);
        StationListCellRenderer stationRenderer = new StationListCellRenderer();
        stationList.setCellRenderer(stationRenderer);
        JScrollPane stationListScroller = new JScrollPane(stationList);
        stationListScroller.setPreferredSize(new Dimension(250, 80));
        JLabel stationListTitle = new JLabel("Stations for the centre");
        getStationReadings = new JButton("Get station readings");
        getAllReadings = new JButton("Get all readings");

        stationsPanel.add(stationListTitle);
        stationsPanel.add(stationListScroller);


        //readings panel
        readingsPanel.setLayout(new BoxLayout(readingsPanel, BoxLayout.PAGE_AXIS));
        readingsPanel.setPreferredSize(new Dimension(250, 200));

        readingsListModel = new DefaultListModel<>();
        readingsList = new JList<>(readingsListModel);
        readingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readingsList.setVisibleRowCount(-1);
        ReadingsListCellRenderer readingsRenderer = new ReadingsListCellRenderer();
        readingsList.setCellRenderer(readingsRenderer);
        JScrollPane readingsListScroller = new JScrollPane(readingsList);
        readingsListScroller.setPreferredSize(new Dimension(250, 80));
        JLabel readingsListTitle = new JLabel("Readings");

        readingsPanel.add(readingsListTitle);
        readingsPanel.add(readingsListScroller);

        //alarms panel
        alarmsPanel.setLayout(new BoxLayout(alarmsPanel, BoxLayout.PAGE_AXIS));
        alarmsPanel.setPreferredSize(new Dimension(250, 200));
        alarmsListModel = new DefaultListModel<>();
        alarmsList = new JList<>(alarmsListModel);
        alarmsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        alarmsList.setVisibleRowCount(-1);
        AlarmListCellRenderer alarmsRenderer = new AlarmListCellRenderer();
        alarmsList.setCellRenderer(alarmsRenderer);
        JScrollPane alarmsListScroller = new JScrollPane(alarmsList);
        alarmsListScroller.setPreferredSize(new Dimension(250, 80));
        JLabel alarmsListTitle = new JLabel("Confirmed alarms");

        alarmsPanel.add(alarmsListTitle);
        alarmsPanel.add(alarmsListScroller);

        getCentreReadings = new JButton("Get centre readings");
        getCurrentConnectedReadings = new JButton("Poll connected centre stations");
        panel.add(centresPanel);
        panel.add(stationsPanel);
        panel.add(readingsPanel);
        panel.add(alarmsPanel);
        panel.add(getStationReadings);
        panel.add(getCentreReadings);
        panel.add(getCurrentConnectedReadings);
        panel.add(getAllReadings);
        panel.add(getCentreStations);


        //register agency panel
        JPanel registerAgencyPanel = new JPanel();
        registerAgencyPanel.setLayout(new BoxLayout(registerAgencyPanel, BoxLayout.PAGE_AXIS));
        JLabel agencyNameLabel = new JLabel("Agency name");
        agencyName = new JTextField();
        JLabel regionOfInterestLabel = new JLabel("Region of interest");
        regionOfInterest = new JTextField();
        JLabel contactDetailsLabel = new JLabel("Contact info");
        contactDetails = new JTextField();
        registerAgency = new JButton("Register agency");
        registerAgencyPanel.add(agencyNameLabel);
        registerAgencyPanel.add(agencyName);
        registerAgencyPanel.add(regionOfInterestLabel);
        registerAgencyPanel.add(regionOfInterest);
        registerAgencyPanel.add(contactDetailsLabel);
        registerAgencyPanel.add(contactDetails);
        registerAgencyPanel.add(registerAgency);
        panel.add(registerAgencyPanel);


        getContentPane().add(panel, "Center");

        setSize(600, 700);
        setResizable(false);

        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        } );
        //orb.run();

    }

    public void initListeners() {
        getCentreStations.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stationListModel.clear();
                CentreInfo selectedCentre = centreList.getSelectedValue();
                try {
                    //find the servant matching the centre name
                    RegionalCentre centreServant = RegionalCentreHelper.narrow(nameService.resolve_str(selectedCentre.centre_name));
                    //retrieve the list of stations
                    StationInfo[] stationList = centreServant.connected_devices();
                    for(int i = 0; i < stationList.length; i++) {
                        stationListModel.addElement(stationList[i]);
                    }

                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }
            }
        });

        getCentreReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                CentreInfo selectedCentre = centreList.getSelectedValue();
                try {
                    RegionalCentre centreServant = RegionalCentreHelper.narrow(nameService.resolve_str(selectedCentre.centre_name));
                    Reading[] centreReadings = centreServant.all_readings();
                    for(int i = 0; i < centreReadings.length; i++) {
                        readingsListModel.addElement(centreReadings[i]);
                    }

                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }
            }
        });

        getStationReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                try {
                    RegionalCentre regionalCentreServant = RegionalCentreHelper.narrow(nameService.resolve_str(centreList.getSelectedValue().centre_name));
                    Reading[] stationReadings = regionalCentreServant.get_readings(stationList.getSelectedValue().station_name);
                    for(int i = 0; i < stationReadings.length; i++) {
                       readingsListModel.addElement(stationReadings[i]);
                    }
                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }

            }
        });

        getAllReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                Reading[] readings = servant.all_readings();
                for(int i = 0; i < readings.length; i++) {
                    readingsListModel.addElement(readings[i]);
                }
            }
        });
        registerAgency.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = agencyName.getText();
                String region = regionOfInterest.getText();
                String contact = contactDetails.getText();

                if(name.isEmpty() || region.isEmpty() || contact.isEmpty()){
                    return;
                }

                Agency agency = new Agency(name, region, contact);
                servant.register_agency(agency);
                agencyName.setText("");
                regionOfInterest.setText("");
                contactDetails.setText("");
            }
        });
        getCurrentConnectedReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                try {
                    RegionalCentre regionalCentreServant = RegionalCentreHelper.narrow(nameService.resolve_str(centreList.getSelectedValue().centre_name));
                    StationInfo[] stations = regionalCentreServant.connected_devices();
                    ArrayList<Reading> polled_readings = new ArrayList<>();
                    for(int i = 0; i < stations.length; i++) {
                        MonitoringStation station =MonitoringStationHelper.narrow(nameService.resolve_str(stations[i].station_name));
                        polled_readings.add(station.reading());
                    }
                   for(Reading r: polled_readings){
                        readingsListModel.addElement(r);
                    }


                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }

            }
        });


    }
    public void addToCentreList(CentreInfo info) {
        centreListModel.addElement(info);
    }

    public static void main(String args[]) {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EnvironmentalCentreServer(arguments).setVisible(true);
            }
        });
    }
}

class CentreListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof CentreInfo) {
            CentreInfo centre = (CentreInfo)value;
            setText(centre.centre_name);
        }
        return this;
    }
}


class StationListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof StationInfo) {
            StationInfo station = (StationInfo)value;
            setText(station.station_name);
        }
        return this;
    }
}

class ReadingsListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Reading) {
            Reading reading = (Reading)value;
            setText(reading.station_name + " - " + reading.reading_value + " - " + reading.date + "/" + reading.time);
        }
        return this;
    }
}

class AlarmListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Alarm) {
            Alarm alarm = (Alarm)value;

            String message = alarm.centre_name + " at stations ";
            for(int i=0; i<alarm.alarm_readings.length; i++){
                message += alarm.alarm_readings[i].station_name + ", ";
            }
            setText(message);
        }
        return this;
    }
}