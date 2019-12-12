/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
/**
 * NodeInfo Data Transfer Object.
 * Contains all relevant node information.
 */
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2019-07-30")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeInfoDTO implements org.apache.thrift.TBase<NodeInfoDTO, NodeInfoDTO._Fields>, java.io.Serializable, Cloneable, Comparable<NodeInfoDTO> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("NodeInfoDTO");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField CLUSTER_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("clusterId", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField NUMBER_OF_CORES_FIELD_DESC = new org.apache.thrift.protocol.TField("numberOfCores", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField LOAD_FIELD_DESC = new org.apache.thrift.protocol.TField("load", org.apache.thrift.protocol.TType.DOUBLE, (short)5);
  private static final org.apache.thrift.protocol.TField TIME_STAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timeStamp", org.apache.thrift.protocol.TType.I64, (short)6);
  private static final org.apache.thrift.protocol.TField PARAMETERS_FIELD_DESC = new org.apache.thrift.protocol.TField("parameters", org.apache.thrift.protocol.TType.MAP, (short)7);
  private static final org.apache.thrift.protocol.TField HOST_FIELD_DESC = new org.apache.thrift.protocol.TField("host", org.apache.thrift.protocol.TType.STRING, (short)8);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new NodeInfoDTOStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new NodeInfoDTOTupleSchemeFactory();

  public java.lang.String id; // required
  public java.lang.String clusterId; // required
  /**
   * 
   * @see NodeType
   */
  public NodeType type; // required
  public int numberOfCores; // required
  public double load; // required
  public long timeStamp; // required
  public java.util.Map<java.lang.String,java.lang.String> parameters; // required
  public java.lang.String host; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    CLUSTER_ID((short)2, "clusterId"),
    /**
     * 
     * @see NodeType
     */
    TYPE((short)3, "type"),
    NUMBER_OF_CORES((short)4, "numberOfCores"),
    LOAD((short)5, "load"),
    TIME_STAMP((short)6, "timeStamp"),
    PARAMETERS((short)7, "parameters"),
    HOST((short)8, "host");

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
        case 1: // ID
          return ID;
        case 2: // CLUSTER_ID
          return CLUSTER_ID;
        case 3: // TYPE
          return TYPE;
        case 4: // NUMBER_OF_CORES
          return NUMBER_OF_CORES;
        case 5: // LOAD
          return LOAD;
        case 6: // TIME_STAMP
          return TIME_STAMP;
        case 7: // PARAMETERS
          return PARAMETERS;
        case 8: // HOST
          return HOST;
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
  private static final int __NUMBEROFCORES_ISSET_ID = 0;
  private static final int __LOAD_ISSET_ID = 1;
  private static final int __TIMESTAMP_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Id")));
    tmpMap.put(_Fields.CLUSTER_ID, new org.apache.thrift.meta_data.FieldMetaData("clusterId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Id")));
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, NodeType.class)));
    tmpMap.put(_Fields.NUMBER_OF_CORES, new org.apache.thrift.meta_data.FieldMetaData("numberOfCores", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.LOAD, new org.apache.thrift.meta_data.FieldMetaData("load", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.TIME_STAMP, new org.apache.thrift.meta_data.FieldMetaData("timeStamp", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.PARAMETERS, new org.apache.thrift.meta_data.FieldMetaData("parameters", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.HOST, new org.apache.thrift.meta_data.FieldMetaData("host", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(NodeInfoDTO.class, metaDataMap);
  }

  public NodeInfoDTO() {
  }

  public NodeInfoDTO(
    java.lang.String id,
    java.lang.String clusterId,
    NodeType type,
    int numberOfCores,
    double load,
    long timeStamp,
    java.util.Map<java.lang.String,java.lang.String> parameters,
    java.lang.String host)
  {
    this();
    this.id = id;
    this.clusterId = clusterId;
    this.type = type;
    this.numberOfCores = numberOfCores;
    setNumberOfCoresIsSet(true);
    this.load = load;
    setLoadIsSet(true);
    this.timeStamp = timeStamp;
    setTimeStampIsSet(true);
    this.parameters = parameters;
    this.host = host;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public NodeInfoDTO(NodeInfoDTO other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetId()) {
      this.id = other.id;
    }
    if (other.isSetClusterId()) {
      this.clusterId = other.clusterId;
    }
    if (other.isSetType()) {
      this.type = other.type;
    }
    this.numberOfCores = other.numberOfCores;
    this.load = other.load;
    this.timeStamp = other.timeStamp;
    if (other.isSetParameters()) {
      java.util.Map<java.lang.String,java.lang.String> __this__parameters = new java.util.HashMap<java.lang.String,java.lang.String>(other.parameters);
      this.parameters = __this__parameters;
    }
    if (other.isSetHost()) {
      this.host = other.host;
    }
  }

  public NodeInfoDTO deepCopy() {
    return new NodeInfoDTO(this);
  }

  @Override
  public void clear() {
    this.id = null;
    this.clusterId = null;
    this.type = null;
    setNumberOfCoresIsSet(false);
    this.numberOfCores = 0;
    setLoadIsSet(false);
    this.load = 0.0;
    setTimeStampIsSet(false);
    this.timeStamp = 0;
    this.parameters = null;
    this.host = null;
  }

  public java.lang.String getId() {
    return this.id;
  }

  public NodeInfoDTO setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  public void unsetId() {
    this.id = null;
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return this.id != null;
  }

  public void setIdIsSet(boolean value) {
    if (!value) {
      this.id = null;
    }
  }

  public java.lang.String getClusterId() {
    return this.clusterId;
  }

  public NodeInfoDTO setClusterId(java.lang.String clusterId) {
    this.clusterId = clusterId;
    return this;
  }

  public void unsetClusterId() {
    this.clusterId = null;
  }

  /** Returns true if field clusterId is set (has been assigned a value) and false otherwise */
  public boolean isSetClusterId() {
    return this.clusterId != null;
  }

  public void setClusterIdIsSet(boolean value) {
    if (!value) {
      this.clusterId = null;
    }
  }

  /**
   * 
   * @see NodeType
   */
  public NodeType getType() {
    return this.type;
  }

  /**
   * 
   * @see NodeType
   */
  public NodeInfoDTO setType(NodeType type) {
    this.type = type;
    return this;
  }

  public void unsetType() {
    this.type = null;
  }

  /** Returns true if field type is set (has been assigned a value) and false otherwise */
  public boolean isSetType() {
    return this.type != null;
  }

  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }

  public int getNumberOfCores() {
    return this.numberOfCores;
  }

  public NodeInfoDTO setNumberOfCores(int numberOfCores) {
    this.numberOfCores = numberOfCores;
    setNumberOfCoresIsSet(true);
    return this;
  }

  public void unsetNumberOfCores() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __NUMBEROFCORES_ISSET_ID);
  }

  /** Returns true if field numberOfCores is set (has been assigned a value) and false otherwise */
  public boolean isSetNumberOfCores() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __NUMBEROFCORES_ISSET_ID);
  }

  public void setNumberOfCoresIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __NUMBEROFCORES_ISSET_ID, value);
  }

  public double getLoad() {
    return this.load;
  }

  public NodeInfoDTO setLoad(double load) {
    this.load = load;
    setLoadIsSet(true);
    return this;
  }

  public void unsetLoad() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __LOAD_ISSET_ID);
  }

  /** Returns true if field load is set (has been assigned a value) and false otherwise */
  public boolean isSetLoad() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __LOAD_ISSET_ID);
  }

  public void setLoadIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __LOAD_ISSET_ID, value);
  }

  public long getTimeStamp() {
    return this.timeStamp;
  }

  public NodeInfoDTO setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
    setTimeStampIsSet(true);
    return this;
  }

  public void unsetTimeStamp() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  /** Returns true if field timeStamp is set (has been assigned a value) and false otherwise */
  public boolean isSetTimeStamp() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  public void setTimeStampIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
  }

  public int getParametersSize() {
    return (this.parameters == null) ? 0 : this.parameters.size();
  }

  public void putToParameters(java.lang.String key, java.lang.String val) {
    if (this.parameters == null) {
      this.parameters = new java.util.HashMap<java.lang.String,java.lang.String>();
    }
    this.parameters.put(key, val);
  }

  public java.util.Map<java.lang.String,java.lang.String> getParameters() {
    return this.parameters;
  }

  public NodeInfoDTO setParameters(java.util.Map<java.lang.String,java.lang.String> parameters) {
    this.parameters = parameters;
    return this;
  }

  public void unsetParameters() {
    this.parameters = null;
  }

  /** Returns true if field parameters is set (has been assigned a value) and false otherwise */
  public boolean isSetParameters() {
    return this.parameters != null;
  }

  public void setParametersIsSet(boolean value) {
    if (!value) {
      this.parameters = null;
    }
  }

  public java.lang.String getHost() {
    return this.host;
  }

  public NodeInfoDTO setHost(java.lang.String host) {
    this.host = host;
    return this;
  }

  public void unsetHost() {
    this.host = null;
  }

  /** Returns true if field host is set (has been assigned a value) and false otherwise */
  public boolean isSetHost() {
    return this.host != null;
  }

  public void setHostIsSet(boolean value) {
    if (!value) {
      this.host = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((java.lang.String)value);
      }
      break;

    case CLUSTER_ID:
      if (value == null) {
        unsetClusterId();
      } else {
        setClusterId((java.lang.String)value);
      }
      break;

    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((NodeType)value);
      }
      break;

    case NUMBER_OF_CORES:
      if (value == null) {
        unsetNumberOfCores();
      } else {
        setNumberOfCores((java.lang.Integer)value);
      }
      break;

    case LOAD:
      if (value == null) {
        unsetLoad();
      } else {
        setLoad((java.lang.Double)value);
      }
      break;

    case TIME_STAMP:
      if (value == null) {
        unsetTimeStamp();
      } else {
        setTimeStamp((java.lang.Long)value);
      }
      break;

    case PARAMETERS:
      if (value == null) {
        unsetParameters();
      } else {
        setParameters((java.util.Map<java.lang.String,java.lang.String>)value);
      }
      break;

    case HOST:
      if (value == null) {
        unsetHost();
      } else {
        setHost((java.lang.String)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case CLUSTER_ID:
      return getClusterId();

    case TYPE:
      return getType();

    case NUMBER_OF_CORES:
      return getNumberOfCores();

    case LOAD:
      return getLoad();

    case TIME_STAMP:
      return getTimeStamp();

    case PARAMETERS:
      return getParameters();

    case HOST:
      return getHost();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case CLUSTER_ID:
      return isSetClusterId();
    case TYPE:
      return isSetType();
    case NUMBER_OF_CORES:
      return isSetNumberOfCores();
    case LOAD:
      return isSetLoad();
    case TIME_STAMP:
      return isSetTimeStamp();
    case PARAMETERS:
      return isSetParameters();
    case HOST:
      return isSetHost();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof NodeInfoDTO)
      return this.equals((NodeInfoDTO)that);
    return false;
  }

  public boolean equals(NodeInfoDTO that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_id = true && this.isSetId();
    boolean that_present_id = true && that.isSetId();
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (!this.id.equals(that.id))
        return false;
    }

    boolean this_present_clusterId = true && this.isSetClusterId();
    boolean that_present_clusterId = true && that.isSetClusterId();
    if (this_present_clusterId || that_present_clusterId) {
      if (!(this_present_clusterId && that_present_clusterId))
        return false;
      if (!this.clusterId.equals(that.clusterId))
        return false;
    }

    boolean this_present_type = true && this.isSetType();
    boolean that_present_type = true && that.isSetType();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }

    boolean this_present_numberOfCores = true;
    boolean that_present_numberOfCores = true;
    if (this_present_numberOfCores || that_present_numberOfCores) {
      if (!(this_present_numberOfCores && that_present_numberOfCores))
        return false;
      if (this.numberOfCores != that.numberOfCores)
        return false;
    }

    boolean this_present_load = true;
    boolean that_present_load = true;
    if (this_present_load || that_present_load) {
      if (!(this_present_load && that_present_load))
        return false;
      if (this.load != that.load)
        return false;
    }

    boolean this_present_timeStamp = true;
    boolean that_present_timeStamp = true;
    if (this_present_timeStamp || that_present_timeStamp) {
      if (!(this_present_timeStamp && that_present_timeStamp))
        return false;
      if (this.timeStamp != that.timeStamp)
        return false;
    }

    boolean this_present_parameters = true && this.isSetParameters();
    boolean that_present_parameters = true && that.isSetParameters();
    if (this_present_parameters || that_present_parameters) {
      if (!(this_present_parameters && that_present_parameters))
        return false;
      if (!this.parameters.equals(that.parameters))
        return false;
    }

    boolean this_present_host = true && this.isSetHost();
    boolean that_present_host = true && that.isSetHost();
    if (this_present_host || that_present_host) {
      if (!(this_present_host && that_present_host))
        return false;
      if (!this.host.equals(that.host))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetId()) ? 131071 : 524287);
    if (isSetId())
      hashCode = hashCode * 8191 + id.hashCode();

    hashCode = hashCode * 8191 + ((isSetClusterId()) ? 131071 : 524287);
    if (isSetClusterId())
      hashCode = hashCode * 8191 + clusterId.hashCode();

    hashCode = hashCode * 8191 + ((isSetType()) ? 131071 : 524287);
    if (isSetType())
      hashCode = hashCode * 8191 + type.getValue();

    hashCode = hashCode * 8191 + numberOfCores;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(load);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(timeStamp);

    hashCode = hashCode * 8191 + ((isSetParameters()) ? 131071 : 524287);
    if (isSetParameters())
      hashCode = hashCode * 8191 + parameters.hashCode();

    hashCode = hashCode * 8191 + ((isSetHost()) ? 131071 : 524287);
    if (isSetHost())
      hashCode = hashCode * 8191 + host.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(NodeInfoDTO other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetClusterId()).compareTo(other.isSetClusterId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClusterId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clusterId, other.clusterId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetType()).compareTo(other.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetNumberOfCores()).compareTo(other.isSetNumberOfCores());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNumberOfCores()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.numberOfCores, other.numberOfCores);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetLoad()).compareTo(other.isSetLoad());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLoad()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.load, other.load);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetTimeStamp()).compareTo(other.isSetTimeStamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimeStamp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeStamp, other.timeStamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetParameters()).compareTo(other.isSetParameters());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParameters()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.parameters, other.parameters);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetHost()).compareTo(other.isSetHost());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHost()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.host, other.host);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("NodeInfoDTO(");
    boolean first = true;

    sb.append("id:");
    if (this.id == null) {
      sb.append("null");
    } else {
      sb.append(this.id);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("clusterId:");
    if (this.clusterId == null) {
      sb.append("null");
    } else {
      sb.append(this.clusterId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("numberOfCores:");
    sb.append(this.numberOfCores);
    first = false;
    if (!first) sb.append(", ");
    sb.append("load:");
    sb.append(this.load);
    first = false;
    if (!first) sb.append(", ");
    sb.append("timeStamp:");
    sb.append(this.timeStamp);
    first = false;
    if (!first) sb.append(", ");
    sb.append("parameters:");
    if (this.parameters == null) {
      sb.append("null");
    } else {
      sb.append(this.parameters);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("host:");
    if (this.host == null) {
      sb.append("null");
    } else {
      sb.append(this.host);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class NodeInfoDTOStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public NodeInfoDTOStandardScheme getScheme() {
      return new NodeInfoDTOStandardScheme();
    }
  }

  private static class NodeInfoDTOStandardScheme extends org.apache.thrift.scheme.StandardScheme<NodeInfoDTO> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, NodeInfoDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.id = iprot.readString();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CLUSTER_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.clusterId = iprot.readString();
              struct.setClusterIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.type = at.enfilo.def.transfer.dto.NodeType.findByValue(iprot.readI32());
              struct.setTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // NUMBER_OF_CORES
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.numberOfCores = iprot.readI32();
              struct.setNumberOfCoresIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // LOAD
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.load = iprot.readDouble();
              struct.setLoadIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // TIME_STAMP
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.timeStamp = iprot.readI64();
              struct.setTimeStampIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // PARAMETERS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map144 = iprot.readMapBegin();
                struct.parameters = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map144.size);
                java.lang.String _key145;
                java.lang.String _val146;
                for (int _i147 = 0; _i147 < _map144.size; ++_i147)
                {
                  _key145 = iprot.readString();
                  _val146 = iprot.readString();
                  struct.parameters.put(_key145, _val146);
                }
                iprot.readMapEnd();
              }
              struct.setParametersIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 8: // HOST
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.host = iprot.readString();
              struct.setHostIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, NodeInfoDTO struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.id != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.id);
        oprot.writeFieldEnd();
      }
      if (struct.clusterId != null) {
        oprot.writeFieldBegin(CLUSTER_ID_FIELD_DESC);
        oprot.writeString(struct.clusterId);
        oprot.writeFieldEnd();
      }
      if (struct.type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.type.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(NUMBER_OF_CORES_FIELD_DESC);
      oprot.writeI32(struct.numberOfCores);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(LOAD_FIELD_DESC);
      oprot.writeDouble(struct.load);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(TIME_STAMP_FIELD_DESC);
      oprot.writeI64(struct.timeStamp);
      oprot.writeFieldEnd();
      if (struct.parameters != null) {
        oprot.writeFieldBegin(PARAMETERS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.parameters.size()));
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter148 : struct.parameters.entrySet())
          {
            oprot.writeString(_iter148.getKey());
            oprot.writeString(_iter148.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.host != null) {
        oprot.writeFieldBegin(HOST_FIELD_DESC);
        oprot.writeString(struct.host);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class NodeInfoDTOTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public NodeInfoDTOTupleScheme getScheme() {
      return new NodeInfoDTOTupleScheme();
    }
  }

  private static class NodeInfoDTOTupleScheme extends org.apache.thrift.scheme.TupleScheme<NodeInfoDTO> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, NodeInfoDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetId()) {
        optionals.set(0);
      }
      if (struct.isSetClusterId()) {
        optionals.set(1);
      }
      if (struct.isSetType()) {
        optionals.set(2);
      }
      if (struct.isSetNumberOfCores()) {
        optionals.set(3);
      }
      if (struct.isSetLoad()) {
        optionals.set(4);
      }
      if (struct.isSetTimeStamp()) {
        optionals.set(5);
      }
      if (struct.isSetParameters()) {
        optionals.set(6);
      }
      if (struct.isSetHost()) {
        optionals.set(7);
      }
      oprot.writeBitSet(optionals, 8);
      if (struct.isSetId()) {
        oprot.writeString(struct.id);
      }
      if (struct.isSetClusterId()) {
        oprot.writeString(struct.clusterId);
      }
      if (struct.isSetType()) {
        oprot.writeI32(struct.type.getValue());
      }
      if (struct.isSetNumberOfCores()) {
        oprot.writeI32(struct.numberOfCores);
      }
      if (struct.isSetLoad()) {
        oprot.writeDouble(struct.load);
      }
      if (struct.isSetTimeStamp()) {
        oprot.writeI64(struct.timeStamp);
      }
      if (struct.isSetParameters()) {
        {
          oprot.writeI32(struct.parameters.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter149 : struct.parameters.entrySet())
          {
            oprot.writeString(_iter149.getKey());
            oprot.writeString(_iter149.getValue());
          }
        }
      }
      if (struct.isSetHost()) {
        oprot.writeString(struct.host);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, NodeInfoDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(8);
      if (incoming.get(0)) {
        struct.id = iprot.readString();
        struct.setIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.clusterId = iprot.readString();
        struct.setClusterIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.type = at.enfilo.def.transfer.dto.NodeType.findByValue(iprot.readI32());
        struct.setTypeIsSet(true);
      }
      if (incoming.get(3)) {
        struct.numberOfCores = iprot.readI32();
        struct.setNumberOfCoresIsSet(true);
      }
      if (incoming.get(4)) {
        struct.load = iprot.readDouble();
        struct.setLoadIsSet(true);
      }
      if (incoming.get(5)) {
        struct.timeStamp = iprot.readI64();
        struct.setTimeStampIsSet(true);
      }
      if (incoming.get(6)) {
        {
          org.apache.thrift.protocol.TMap _map150 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.parameters = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map150.size);
          java.lang.String _key151;
          java.lang.String _val152;
          for (int _i153 = 0; _i153 < _map150.size; ++_i153)
          {
            _key151 = iprot.readString();
            _val152 = iprot.readString();
            struct.parameters.put(_key151, _val152);
          }
        }
        struct.setParametersIsSet(true);
      }
      if (incoming.get(7)) {
        struct.host = iprot.readString();
        struct.setHostIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

