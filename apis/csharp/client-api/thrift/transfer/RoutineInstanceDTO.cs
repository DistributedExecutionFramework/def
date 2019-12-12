/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.IO;
using Thrift;
using Thrift.Collections;
using System.Runtime.Serialization;
using Thrift.Protocol;
using Thrift.Transport;


#if !SILVERLIGHT
[Serializable]
#endif
public partial class RoutineInstanceDTO : TBase
{
  private string _routineId;
  private Dictionary<string, ResourceDTO> _inParameters;
  private List<string> _missingParameters;

  public string RoutineId
  {
    get
    {
      return _routineId;
    }
    set
    {
      __isset.routineId = true;
      this._routineId = value;
    }
  }

  public Dictionary<string, ResourceDTO> InParameters
  {
    get
    {
      return _inParameters;
    }
    set
    {
      __isset.inParameters = true;
      this._inParameters = value;
    }
  }

  public List<string> MissingParameters
  {
    get
    {
      return _missingParameters;
    }
    set
    {
      __isset.missingParameters = true;
      this._missingParameters = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool routineId;
    public bool inParameters;
    public bool missingParameters;
  }

  public RoutineInstanceDTO() {
  }

  public void Read (TProtocol iprot)
  {
    iprot.IncrementRecursionDepth();
    try
    {
      TField field;
      iprot.ReadStructBegin();
      while (true)
      {
        field = iprot.ReadFieldBegin();
        if (field.Type == TType.Stop) { 
          break;
        }
        switch (field.ID)
        {
          case 1:
            if (field.Type == TType.String) {
              RoutineId = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.Map) {
              {
                InParameters = new Dictionary<string, ResourceDTO>();
                TMap _map33 = iprot.ReadMapBegin();
                for( int _i34 = 0; _i34 < _map33.Count; ++_i34)
                {
                  string _key35;
                  ResourceDTO _val36;
                  _key35 = iprot.ReadString();
                  _val36 = new ResourceDTO();
                  _val36.Read(iprot);
                  InParameters[_key35] = _val36;
                }
                iprot.ReadMapEnd();
              }
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 3:
            if (field.Type == TType.List) {
              {
                MissingParameters = new List<string>();
                TList _list37 = iprot.ReadListBegin();
                for( int _i38 = 0; _i38 < _list37.Count; ++_i38)
                {
                  string _elem39;
                  _elem39 = iprot.ReadString();
                  MissingParameters.Add(_elem39);
                }
                iprot.ReadListEnd();
              }
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          default: 
            TProtocolUtil.Skip(iprot, field.Type);
            break;
        }
        iprot.ReadFieldEnd();
      }
      iprot.ReadStructEnd();
    }
    finally
    {
      iprot.DecrementRecursionDepth();
    }
  }

  public void Write(TProtocol oprot) {
    oprot.IncrementRecursionDepth();
    try
    {
      TStruct struc = new TStruct("RoutineInstanceDTO");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (RoutineId != null && __isset.routineId) {
        field.Name = "routineId";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(RoutineId);
        oprot.WriteFieldEnd();
      }
      if (InParameters != null && __isset.inParameters) {
        field.Name = "inParameters";
        field.Type = TType.Map;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        {
          oprot.WriteMapBegin(new TMap(TType.String, TType.Struct, InParameters.Count));
          foreach (string _iter40 in InParameters.Keys)
          {
            oprot.WriteString(_iter40);
            InParameters[_iter40].Write(oprot);
          }
          oprot.WriteMapEnd();
        }
        oprot.WriteFieldEnd();
      }
      if (MissingParameters != null && __isset.missingParameters) {
        field.Name = "missingParameters";
        field.Type = TType.List;
        field.ID = 3;
        oprot.WriteFieldBegin(field);
        {
          oprot.WriteListBegin(new TList(TType.String, MissingParameters.Count));
          foreach (string _iter41 in MissingParameters)
          {
            oprot.WriteString(_iter41);
          }
          oprot.WriteListEnd();
        }
        oprot.WriteFieldEnd();
      }
      oprot.WriteFieldStop();
      oprot.WriteStructEnd();
    }
    finally
    {
      oprot.DecrementRecursionDepth();
    }
  }

  public override string ToString() {
    StringBuilder __sb = new StringBuilder("RoutineInstanceDTO(");
    bool __first = true;
    if (RoutineId != null && __isset.routineId) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("RoutineId: ");
      __sb.Append(RoutineId);
    }
    if (InParameters != null && __isset.inParameters) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("InParameters: ");
      __sb.Append(InParameters);
    }
    if (MissingParameters != null && __isset.missingParameters) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("MissingParameters: ");
      __sb.Append(MissingParameters);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}

