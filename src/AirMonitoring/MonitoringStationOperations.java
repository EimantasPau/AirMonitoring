package AirMonitoring;


/**
* AirMonitoring/MonitoringStationOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

public interface MonitoringStationOperations 
{
  AirMonitoring.Reading reading ();
  AirMonitoring.Reading[] reading_log ();
  void take_reading ();
  void set_info (AirMonitoring.StationInfo info);
  AirMonitoring.StationInfo get_info ();
  void turn_on ();
  void turn_off ();
  void reset ();
} // interface MonitoringStationOperations
