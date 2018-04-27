package AirMonitoring;


/**
* AirMonitoring/EnvironmentalCentreOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

public interface EnvironmentalCentreOperations 
{
  AirMonitoring.Reading[] all_readings ();
  AirMonitoring.Reading[] get_readings (String centre_name);
  AirMonitoring.CentreInfo[] connected_centres ();
  void register_regional_centre (AirMonitoring.CentreInfo info);
  void unregister_regional_centre (AirMonitoring.CentreInfo info);
  void raise_alarm (AirMonitoring.Alarm alarm);
  void register_agency (AirMonitoring.Agency agency);
} // interface EnvironmentalCentreOperations
