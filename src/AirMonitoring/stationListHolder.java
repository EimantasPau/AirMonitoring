package AirMonitoring;


/**
* AirMonitoring/stationListHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

public final class stationListHolder implements org.omg.CORBA.portable.Streamable
{
  public AirMonitoring.StationInfo value[] = null;

  public stationListHolder ()
  {
  }

  public stationListHolder (AirMonitoring.StationInfo[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AirMonitoring.stationListHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AirMonitoring.stationListHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AirMonitoring.stationListHelper.type ();
  }

}
