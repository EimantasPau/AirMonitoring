package AirMonitoring;


/**
* AirMonitoring/Set_of_readingsHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Thursday, 29 March 2018 17:19:50 o'clock BST
*/

public final class Set_of_readingsHolder implements org.omg.CORBA.portable.Streamable
{
  public AirMonitoring.Reading value[] = null;

  public Set_of_readingsHolder ()
  {
  }

  public Set_of_readingsHolder (AirMonitoring.Reading[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AirMonitoring.Set_of_readingsHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AirMonitoring.Set_of_readingsHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AirMonitoring.Set_of_readingsHelper.type ();
  }

}
