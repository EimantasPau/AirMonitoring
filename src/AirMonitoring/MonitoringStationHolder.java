package AirMonitoring;

/**
* AirMonitoring/MonitoringStationHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 10 April 2018 13:29:09 o'clock BST
*/

public final class MonitoringStationHolder implements org.omg.CORBA.portable.Streamable
{
  public AirMonitoring.MonitoringStation value = null;

  public MonitoringStationHolder ()
  {
  }

  public MonitoringStationHolder (AirMonitoring.MonitoringStation initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AirMonitoring.MonitoringStationHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AirMonitoring.MonitoringStationHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AirMonitoring.MonitoringStationHelper.type ();
  }

}
