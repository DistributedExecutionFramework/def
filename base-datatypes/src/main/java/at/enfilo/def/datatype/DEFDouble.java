/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.datatype;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-11-06")
public class DEFDouble implements org.apache.thrift.TBase<DEFDouble, DEFDouble._Fields>, java.io.Serializable, Cloneable, Comparable<DEFDouble> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DEFDouble");

  private static final org.apache.thrift.protocol.TField _ID_FIELD_DESC = new org.apache.thrift.protocol.TField("_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.DOUBLE, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new DEFDoubleStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new DEFDoubleTupleSchemeFactory();

  public java.lang.String _id; // optional
  public double value; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    _ID((short)1, "_id"),
    VALUE((short)2, "value");

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
        case 2: // VALUE
          return VALUE;
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
  private static final int __VALUE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields._ID};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields._ID, new org.apache.thrift.meta_data.FieldMetaData("_id", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Id")));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DEFDouble.class, metaDataMap);
  }

  public DEFDouble() {
    this._id = "6e8d4e97-38f8-31df-887d-8b193c2e50b3";

  }

  public DEFDouble(
    double value)
  {
    this();
    this.value = value;
    setValueIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DEFDouble(DEFDouble other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSet_id()) {
      this._id = other._id;
    }
    this.value = other.value;
  }

  public DEFDouble deepCopy() {
    return new DEFDouble(this);
  }

  @Override
  public void clear() {
    this._id = "6e8d4e97-38f8-31df-887d-8b193c2e50b3";

    setValueIsSet(false);
    this.value = 0.0;
  }

  public java.lang.String get_id() {
    return this._id;
  }

  public DEFDouble set_id(java.lang.String _id) {
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

  public double getValue() {
    return this.value;
  }

  public DEFDouble setValue(double value) {
    this.value = value;
    setValueIsSet(true);
    return this;
  }

  public void unsetValue() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __VALUE_ISSET_ID);
  }

  /** Returns true if field value is set (has been assigned a value) and false otherwise */
  public boolean isSetValue() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __VALUE_ISSET_ID);
  }

  public void setValueIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __VALUE_ISSET_ID, value);
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

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((java.lang.Double)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case _ID:
      return get_id();

    case VALUE:
      return getValue();

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
    case VALUE:
      return isSetValue();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof DEFDouble)
      return this.equals((DEFDouble)that);
    return false;
  }

  public boolean equals(DEFDouble that) {
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

    boolean this_present_value = true;
    boolean that_present_value = true;
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (this.value != that.value)
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

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(value);

    return hashCode;
  }

  @Override
  public int compareTo(DEFDouble other) {
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
    lastComparison = java.lang.Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("DEFDouble(");
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
    sb.append("value:");
    sb.append(this.value);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DEFDoubleStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DEFDoubleStandardScheme getScheme() {
      return new DEFDoubleStandardScheme();
    }
  }

  private static class DEFDoubleStandardScheme extends org.apache.thrift.scheme.StandardScheme<DEFDouble> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DEFDouble struct) throws org.apache.thrift.TException {
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
          case 2: // VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.value = iprot.readDouble();
              struct.setValueIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, DEFDouble struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct._id != null) {
        if (struct.isSet_id()) {
          oprot.writeFieldBegin(_ID_FIELD_DESC);
          oprot.writeString(struct._id);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldBegin(VALUE_FIELD_DESC);
      oprot.writeDouble(struct.value);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DEFDoubleTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DEFDoubleTupleScheme getScheme() {
      return new DEFDoubleTupleScheme();
    }
  }

  private static class DEFDoubleTupleScheme extends org.apache.thrift.scheme.TupleScheme<DEFDouble> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DEFDouble struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSet_id()) {
        optionals.set(0);
      }
      if (struct.isSetValue()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSet_id()) {
        oprot.writeString(struct._id);
      }
      if (struct.isSetValue()) {
        oprot.writeDouble(struct.value);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DEFDouble struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct._id = iprot.readString();
        struct.set_idIsSet(true);
      }
      if (incoming.get(1)) {
        struct.value = iprot.readDouble();
        struct.setValueIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

