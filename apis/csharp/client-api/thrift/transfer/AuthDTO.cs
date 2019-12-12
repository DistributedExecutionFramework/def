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
public partial class AuthDTO : TBase
{
  private string _userId;
  private string _token;

  public string UserId
  {
    get
    {
      return _userId;
    }
    set
    {
      __isset.userId = true;
      this._userId = value;
    }
  }

  public string Token
  {
    get
    {
      return _token;
    }
    set
    {
      __isset.token = true;
      this._token = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool userId;
    public bool token;
  }

  public AuthDTO() {
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
              UserId = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.String) {
              Token = iprot.ReadString();
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
      TStruct struc = new TStruct("AuthDTO");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (UserId != null && __isset.userId) {
        field.Name = "userId";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(UserId);
        oprot.WriteFieldEnd();
      }
      if (Token != null && __isset.token) {
        field.Name = "token";
        field.Type = TType.String;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(Token);
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
    StringBuilder __sb = new StringBuilder("AuthDTO(");
    bool __first = true;
    if (UserId != null && __isset.userId) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("UserId: ");
      __sb.Append(UserId);
    }
    if (Token != null && __isset.token) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Token: ");
      __sb.Append(Token);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}

