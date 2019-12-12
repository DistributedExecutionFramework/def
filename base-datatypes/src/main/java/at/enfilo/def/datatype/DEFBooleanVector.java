/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.datatype;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-11-06")
public class DEFBooleanVector implements org.apache.thrift.TBase<DEFBooleanVector, DEFBooleanVector._Fields>, java.io.Serializable, Cloneable, Comparable<DEFBooleanVector> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DEFBooleanVector");

  private static final org.apache.thrift.protocol.TField _ID_FIELD_DESC = new org.apache.thrift.protocol.TField("_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("values", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new DEFBooleanVectorStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new DEFBooleanVectorTupleSchemeFactory();

  public java.lang.String _id; // optional
  public java.util.List<java.lang.Boolean> values; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    _ID((short)1, "_id"),
    VALUES((short)2, "values");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // _ID
          return _ID;
        case 2: // VALUES
          return VALUES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields._ID};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields._ID, new org.apache.thrift.meta_data.FieldMetaData("_id", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Id")));
    tmpMap.put(_Fields.VALUES, new org.apache.thrift.meta_data.FieldMetaData("values", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DEFBooleanVector.class, metaDataMap);
  }

  public DEFBooleanVector() {
    this._id = "29198299-1d34-3cd7-b7bc-e2ac1def2c80";

  }

  public DEFBooleanVector(
    java.util.List<java.lang.Boolean> values)
  {
    this();
    this.values = values;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DEFBooleanVector(DEFBooleanVector other) {
    if (other.isSet_id()) {
      this._id = other._id;
    }
    if (other.isSetValues()) {
      java.util.List<java.lang.Boolean> __this__values = new java.util.ArrayList<java.lang.Boolean>(other.values);
      this.values = __this__values;
    }
  }

  public DEFBooleanVector deepCopy() {
    return new DEFBooleanVector(this);
  }

  @Override
  public void clear() {
    this._id = "29198299-1d34-3cd7-b7bc-e2ac1def2c80";

    this.values = null;
  }

  public java.lang.String get_id() {
    return this._id;
  }

  public DEFBooleanVector set_id(java.lang.String _id) {
    this._id = _id;
    return this;
  }

  public void unset_id() {
    this._id = null;
  }

  /** Returns true if field _id is set (has been assigned a value) and false otherwise */
  public boolean isSet_id() {
    return this._id != null;
  }

  public void set_idIsSet(boolean value) {
    if (!value) {
      this._id = null;
    }
  }

  public int getValuesSize() {
    return (this.values == null) ? 0 : this.values.size();
  }

  public java.util.Iterator<java.lang.Boolean> getValuesIterator() {
    return (this.values == null) ? null : this.values.iterator();
  }

  public void addToValues(boolean elem) {
    if (this.values == null) {
      this.values = new java.util.ArrayList<java.lang.Boolean>();
    }
    this.values.add(elem);
  }

  public java.util.List<java.lang.Boolean> getValues() {
    return this.values;
  }

  public DEFBooleanVector setValues(java.util.List<java.lang.Boolean> values) {
    this.values = values;
    return this;
  }

  public void unsetValues() {
    this.values = null;
  }

  /** Returns true if field values is set (has been assigned a value) and false otherwise */
  public boolean isSetValues() {
    return this.values != null;
  }

  public void setValuesIsSet(boolean value) {
    if (!value) {
      this.values = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case _ID:
      if (value == null) {
        unset_id();
      } else {
        set_id((java.lang.String)value);
      }
      break;

    case VALUES:
      if (value == null) {
        unsetValues();
      } else {
        setValues((java.util.List<java.lang.Boolean>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case _ID:
      return get_id();

    case VALUES:
      return getValues();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case _ID:
      return isSet_id();
    case VALUES:
      return isSetValues();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof DEFBooleanVector)
      return this.equals((DEFBooleanVector)that);
    return false;
  }

  public boolean equals(DEFBooleanVector that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present__id = true && this.isSet_id();
    boolean that_present__id = true && that.isSet_id();
    if (this_present__id || that_present__id) {
      if (!(this_present__id && that_present__id))
        return false;
      if (!this._id.equals(that._id))
        return false;
    }

    boolean this_present_values = true && this.isSetValues();
    boolean that_present_values = true && that.isSetValues();
    if (this_present_values || that_present_values) {
      if (!(this_present_values && that_present_values))
        return false;
      if (!this.values.equals(that.values))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSet_id()) ? 131071 : 524287);
    if (isSet_id())
      hashCode = hashCode * 8191 + _id.hashCode();

    hashCode = hashCode * 8191 + ((isSetValues()) ? 131071 : 524287);
    if (isSetValues())
      hashCode = hashCode * 8191 + values.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(DEFBooleanVector other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSet_id()).compareTo(other.isSet_id());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSet_id()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this._id, other._id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetValues()).compareTo(other.isSetValues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.values, other.values);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("DEFBooleanVector(");
    boolean first = true;

    if (isSet_id()) {
      sb.append("_id:");
      if (this._id == null) {
        sb.append("null");
      } else {
        sb.append(this._id);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("values:");
    if (this.values == null) {
      sb.append("null");
    } else {
      sb.append(this.values);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DEFBooleanVectorStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DEFBooleanVectorStandardScheme getScheme() {
      return new DEFBooleanVectorStandardScheme();
    }
  }

  private static class DEFBooleanVectorStandardScheme extends org.apache.thrift.scheme.StandardScheme<DEFBooleanVector> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DEFBooleanVector struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // _ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct._id = iprot.readString();
              struct.set_idIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VALUES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.values = new java.util.ArrayList<java.lang.Boolean>(_list0.size);
                boolean _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readBool();
                  struct.values.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setValuesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DEFBooleanVector struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct._id != null) {
        if (struct.isSet_id()) {
          oprot.writeFieldBegin(_ID_FIELD_DESC);
          oprot.writeString(struct._id);
          oprot.writeFieldEnd();
        }
      }
      if (struct.values != null) {
        oprot.writeFieldBegin(VALUES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.BOOL, struct.values.size()));
          for (boolean _iter3 : struct.values)
          {
            oprot.writeBool(_iter3);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DEFBooleanVectorTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DEFBooleanVectorTupleScheme getScheme() {
      return new DEFBooleanVectorTupleScheme();
    }
  }

  private static class DEFBooleanVectorTupleScheme extends org.apache.thrift.scheme.TupleScheme<DEFBooleanVector> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DEFBooleanVector struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSet_id()) {
        optionals.set(0);
      }
      if (struct.isSetValues()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSet_id()) {
        oprot.writeString(struct._id);
      }
      if (struct.isSetValues()) {
        {
          oprot.writeI32(struct.values.size());
          for (boolean _iter4 : struct.values)
          {
            oprot.writeBool(_iter4);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DEFBooleanVector struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct._id = iprot.readString();
        struct.set_idIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.BOOL, iprot.readI32());
          struct.values = new java.util.ArrayList<java.lang.Boolean>(_list5.size);
          boolean _elem6;
          for (int _i7 = 0; _i7 < _list5.size; ++_i7)
          {
            _elem6 = iprot.readBool();
            struct.values.add(_elem6);
          }
        }
        struct.setValuesIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

