package AirMonitoring;


/**
* AirMonitoring/EnvironmentalCentreOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 10 April 2018 13:29:09 o'clock BST
*/

public interface EnvironmentalCentreOperations 
{
  AirMonitoring.Reading[] readings ();
  AirMonitoring.CentreInfo[] connectedCentres ();
  void connectedCentres (AirMonitoring.CentreInfo[] newConnectedCentres);
  void registerRegionalCentre (AirMonitoring.CentreInfo info);
} // interface EnvironmentalCentreOperations
