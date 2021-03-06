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
public partial class LibraryInfoDTO : TBase
{
  private string _id;
  private LibraryType _libraryType;
  private string _storeDriver;
  private THashSet<string> _storedRoutines;

  public string Id
  {
    get
    {
      return _id;
    }
    set
    {
      __isset.id = true;
      this._id = value;
    }
  }

  /// <summary>
  /// 
  /// <seealso cref="LibraryType"/>
  /// </summary>
  public LibraryType LibraryType
  {
    get
    {
      return _libraryType;
    }
    set
    {
      __isset.libraryType = true;
      this._libraryType = value;
    }
  }

  public string StoreDriver
  {
    get
    {
      return _storeDriver;
    }
    set
    {
      __isset.storeDriver = true;
      this._storeDriver = value;
    }
  }

  public THashSet<string> StoredRoutines
  {
    get
    {
      return _storedRoutines;
    }
    set
    {
      __isset.storedRoutines = true;
      this._storedRoutines = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool id;
    public bool libraryType;
    public bool storeDriver;
    public bool storedRoutines;
  }

  public LibraryInfoDTO() {
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
              Id = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.I32) {
              LibraryType = (LibraryType)iprot.ReadI32();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 3:
            if (field.Type == TType.String) {
              StoreDriver = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 4:
            if (field.Type == TType.Set) {
              {
                StoredRoutines = new THashSet<string>();
                TSet _set0 = iprot.ReadSetBegin();
                for( int _i1 = 0; _i1 < _set0.Count; ++_i1)
                {
                  string _elem2;
                  _elem2 = iprot.ReadString();
                  StoredRoutines.Add(_elem2);
                }
                iprot.ReadSetEnd();
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
      TStruct struc = new TStruct("LibraryInfoDTO");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (Id != null && __isset.id) {
        field.Name = "id";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(Id);
        oprot.WriteFieldEnd();
      }
      if (__isset.libraryType) {
        field.Name = "libraryType";
        field.Type = TType.I32;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32((int)LibraryType);
        oprot.WriteFieldEnd();
      }
      if (StoreDriver != null && __isset.storeDriver) {
        field.Name = "storeDriver";
        field.Type = TType.String;
        field.ID = 3;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(StoreDriver);
        oprot.WriteFieldEnd();
      }
      if (StoredRoutines != null && __isset.storedRoutines) {
        field.Name = "storedRoutines";
        field.Type = TType.Set;
        field.ID = 4;
        oprot.WriteFieldBegin(field);
        {
          oprot.WriteSetBegin(new TSet(TType.String, StoredRoutines.Count));
          foreach (string _iter3 in StoredRoutines)
          {
            oprot.WriteString(_iter3);
          }
          oprot.WriteSetEnd();
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
    StringBuilder __sb = new StringBuilder("LibraryInfoDTO(");
    bool __first = true;
    if (Id != null && __isset.id) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Id: ");
      __sb.Append(Id);
    }
    if (__isset.libraryType) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("LibraryType: ");
      __sb.Append(LibraryType);
    }
    if (StoreDriver != null && __isset.storeDriver) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("StoreDriver: ");
      __sb.Append(StoreDriver);
    }
    if (StoredRoutines != null && __isset.storedRoutines) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("StoredRoutines: ");
      __sb.Append(StoredRoutines);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}

