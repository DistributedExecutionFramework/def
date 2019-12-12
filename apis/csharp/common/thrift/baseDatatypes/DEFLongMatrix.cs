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
public partial class DEFLongMatrix : TBase
{
  private string __id;
  private List<long> _values;
  private int _cols;
  private int _rows;

  public string _id
  {
    get
    {
      return __id;
    }
    set
    {
      __isset._id = true;
      this.__id = value;
    }
  }

  public List<long> Values
  {
    get
    {
      return _values;
    }
    set
    {
      __isset.values = true;
      this._values = value;
    }
  }

  public int Cols
  {
    get
    {
      return _cols;
    }
    set
    {
      __isset.cols = true;
      this._cols = value;
    }
  }

  public int Rows
  {
    get
    {
      return _rows;
    }
    set
    {
      __isset.rows = true;
      this._rows = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool _id;
    public bool values;
    public bool cols;
    public bool rows;
  }

  public DEFLongMatrix() {
    this.__id = "61a0d9e6-3f11-3990-b4e9-42e6d1cd7a02";
    this.__isset._id = true;
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
              _id = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.List) {
              {
                Values = new List<long>();
                TList _list0 = iprot.ReadListBegin();
                for( int _i1 = 0; _i1 < _list0.Count; ++_i1)
                {
                  long _elem2;
                  _elem2 = iprot.ReadI64();
                  Values.Add(_elem2);
                }
                iprot.ReadListEnd();
              }
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 3:
            if (field.Type == TType.I32) {
              Cols = iprot.ReadI32();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 4:
            if (field.Type == TType.I32) {
              Rows = iprot.ReadI32();
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
      TStruct struc = new TStruct("DEFLongMatrix");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (_id != null && __isset._id) {
        field.Name = "_id";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(_id);
        oprot.WriteFieldEnd();
      }
      if (Values != null && __isset.values) {
        field.Name = "values";
        field.Type = TType.List;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        {
          oprot.WriteListBegin(new TList(TType.I64, Values.Count));
          foreach (long _iter3 in Values)
          {
            oprot.WriteI64(_iter3);
          }
          oprot.WriteListEnd();
        }
        oprot.WriteFieldEnd();
      }
      if (__isset.cols) {
        field.Name = "cols";
        field.Type = TType.I32;
        field.ID = 3;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32(Cols);
        oprot.WriteFieldEnd();
      }
      if (__isset.rows) {
        field.Name = "rows";
        field.Type = TType.I32;
        field.ID = 4;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32(Rows);
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
    StringBuilder __sb = new StringBuilder("DEFLongMatrix(");
    bool __first = true;
    if (_id != null && __isset._id) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("_id: ");
      __sb.Append(_id);
    }
    if (Values != null && __isset.values) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Values: ");
      __sb.Append(Values);
    }
    if (__isset.cols) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Cols: ");
      __sb.Append(Cols);
    }
    if (__isset.rows) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Rows: ");
      __sb.Append(Rows);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}

