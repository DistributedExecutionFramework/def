/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2019-07-30")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReduceJobDTO implements org.apache.thrift.TBase<ReduceJobDTO, ReduceJobDTO._Fields>, java.io.Serializable, Cloneable, Comparable<ReduceJobDTO> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ReduceJobDTO");

  private static final org.apache.thrift.protocol.TField JOB_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("jobId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField JOB_FIELD_DESC = new org.apache.thrift.protocol.TField("job", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField START_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("startTime", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField FINISH_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("finishTime", org.apache.thrift.protocol.TType.I64, (short)5);
  private static final org.apache.thrift.protocol.TField MESSAGES_FIELD_DESC = new org.apache.thrift.protocol.TField("messages", org.apache.thrift.protocol.TType.LIST, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ReduceJobDTOStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ReduceJobDTOTupleSchemeFactory();

  public java.lang.String jobId; // required
  public JobDTO job; // required
  /**
   * 
   * @see ExecutionState
   */
  public ExecutionState state; // required
  public long startTime; // required
  public long finishTime; // required
  public java.util.List<java.lang.String> messages; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    JOB_ID((short)1, "jobId"),
    JOB((short)2, "job"),
    /**
     * 
     * @see ExecutionState
     */
    STATE((short)3, "state"),
    START_TIME((short)4, "startTime"),
    FINISH_TIME((short)5, "finishTime"),
    MESSAGES((short)6, "messages");

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
        case 1: // JOB_ID
          return JOB_ID;
        case 2: // JOB
          return JOB;
        case 3: // STATE
          return STATE;
        case 4: // START_TIME
          return START_TIME;
        case 5: // FINISH_TIME
          return FINISH_TIME;
        case 6: // MESSAGES
          return MESSAGES;
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
  private static final int __STARTTIME_ISSET_ID = 0;
  private static final int __FINISHTIME_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.JOB_ID, new org.apache.thrift.meta_data.FieldMetaData("jobId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Id")));
    tmpMap.put(_Fields.JOB, new org.apache.thrift.meta_data.FieldMetaData("job", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, JobDTO.class)));
    tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ExecutionState.class)));
    tmpMap.put(_Fields.START_TIME, new org.apache.thrift.meta_data.FieldMetaData("startTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.FINISH_TIME, new org.apache.thrift.meta_data.FieldMetaData("finishTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.MESSAGES, new org.apache.thrift.meta_data.FieldMetaData("messages", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ReduceJobDTO.class, metaDataMap);
  }

  public ReduceJobDTO() {
  }

  public ReduceJobDTO(
    java.lang.String jobId,
    JobDTO job,
    ExecutionState state,
    long startTime,
    long finishTime,
    java.util.List<java.lang.String> messages)
  {
    this();
    this.jobId = jobId;
    this.job = job;
    this.state = state;
    this.startTime = startTime;
    setStartTimeIsSet(true);
    this.finishTime = finishTime;
    setFinishTimeIsSet(true);
    this.messages = messages;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ReduceJobDTO(ReduceJobDTO other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetJobId()) {
      this.jobId = other.jobId;
    }
    if (other.isSetJob()) {
      this.job = new JobDTO(other.job);
    }
    if (other.isSetState()) {
      this.state = other.state;
    }
    this.startTime = other.startTime;
    this.finishTime = other.finishTime;
    if (other.isSetMessages()) {
      java.util.List<java.lang.String> __this__messages = new java.util.ArrayList<java.lang.String>(other.messages);
      this.messages = __this__messages;
    }
  }

  public ReduceJobDTO deepCopy() {
    return new ReduceJobDTO(this);
  }

  @Override
  public void clear() {
    this.jobId = null;
    this.job = null;
    this.state = null;
    setStartTimeIsSet(false);
    this.startTime = 0;
    setFinishTimeIsSet(false);
    this.finishTime = 0;
    this.messages = null;
  }

  public java.lang.String getJobId() {
    return this.jobId;
  }

  public ReduceJobDTO setJobId(java.lang.String jobId) {
    this.jobId = jobId;
    return this;
  }

  public void unsetJobId() {
    this.jobId = null;
  }

  /** Returns true if field jobId is set (has been assigned a value) and false otherwise */
  public boolean isSetJobId() {
    return this.jobId != null;
  }

  public void setJobIdIsSet(boolean value) {
    if (!value) {
      this.jobId = null;
    }
  }

  public JobDTO getJob() {
    return this.job;
  }

  public ReduceJobDTO setJob(JobDTO job) {
    this.job = job;
    return this;
  }

  public void unsetJob() {
    this.job = null;
  }

  /** Returns true if field job is set (has been assigned a value) and false otherwise */
  public boolean isSetJob() {
    return this.job != null;
  }

  public void setJobIsSet(boolean value) {
    if (!value) {
      this.job = null;
    }
  }

  /**
   * 
   * @see ExecutionState
   */
  public ExecutionState getState() {
    return this.state;
  }

  /**
   * 
   * @see ExecutionState
   */
  public ReduceJobDTO setState(ExecutionState state) {
    this.state = state;
    return this;
  }

  public void unsetState() {
    this.state = null;
  }

  /** Returns true if field state is set (has been assigned a value) and false otherwise */
  public boolean isSetState() {
    return this.state != null;
  }

  public void setStateIsSet(boolean value) {
    if (!value) {
      this.state = null;
    }
  }

  public long getStartTime() {
    return this.startTime;
  }

  public ReduceJobDTO setStartTime(long startTime) {
    this.startTime = startTime;
    setStartTimeIsSet(true);
    return this;
  }

  public void unsetStartTime() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __STARTTIME_ISSET_ID);
  }

  /** Returns true if field startTime is set (has been assigned a value) and false otherwise */
  public boolean isSetStartTime() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __STARTTIME_ISSET_ID);
  }

  public void setStartTimeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __STARTTIME_ISSET_ID, value);
  }

  public long getFinishTime() {
    return this.finishTime;
  }

  public ReduceJobDTO setFinishTime(long finishTime) {
    this.finishTime = finishTime;
    setFinishTimeIsSet(true);
    return this;
  }

  public void unsetFinishTime() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __FINISHTIME_ISSET_ID);
  }

  /** Returns true if field finishTime is set (has been assigned a value) and false otherwise */
  public boolean isSetFinishTime() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __FINISHTIME_ISSET_ID);
  }

  public void setFinishTimeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __FINISHTIME_ISSET_ID, value);
  }

  public int getMessagesSize() {
    return (this.messages == null) ? 0 : this.messages.size();
  }

  public java.util.Iterator<java.lang.String> getMessagesIterator() {
    return (this.messages == null) ? null : this.messages.iterator();
  }

  public void addToMessages(java.lang.String elem) {
    if (this.messages == null) {
      this.messages = new java.util.ArrayList<java.lang.String>();
    }
    this.messages.add(elem);
  }

  public java.util.List<java.lang.String> getMessages() {
    return this.messages;
  }

  public ReduceJobDTO setMessages(java.util.List<java.lang.String> messages) {
    this.messages = messages;
    return this;
  }

  public void unsetMessages() {
    this.messages = null;
  }

  /** Returns true if field messages is set (has been assigned a value) and false otherwise */
  public boolean isSetMessages() {
    return this.messages != null;
  }

  public void setMessagesIsSet(boolean value) {
    if (!value) {
      this.messages = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case JOB_ID:
      if (value == null) {
        unsetJobId();
      } else {
        setJobId((java.lang.String)value);
      }
      break;

    case JOB:
      if (value == null) {
        unsetJob();
      } else {
        setJob((JobDTO)value);
      }
      break;

    case STATE:
      if (value == null) {
        unsetState();
      } else {
        setState((ExecutionState)value);
      }
      break;

    case START_TIME:
      if (value == null) {
        unsetStartTime();
      } else {
        setStartTime((java.lang.Long)value);
      }
      break;

    case FINISH_TIME:
      if (value == null) {
        unsetFinishTime();
      } else {
        setFinishTime((java.lang.Long)value);
      }
      break;

    case MESSAGES:
      if (value == null) {
        unsetMessages();
      } else {
        setMessages((java.util.List<java.lang.String>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case JOB_ID:
      return getJobId();

    case JOB:
      return getJob();

    case STATE:
      return getState();

    case START_TIME:
      return getStartTime();

    case FINISH_TIME:
      return getFinishTime();

    case MESSAGES:
      return getMessages();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case JOB_ID:
      return isSetJobId();
    case JOB:
      return isSetJob();
    case STATE:
      return isSetState();
    case START_TIME:
      return isSetStartTime();
    case FINISH_TIME:
      return isSetFinishTime();
    case MESSAGES:
      return isSetMessages();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof ReduceJobDTO)
      return this.equals((ReduceJobDTO)that);
    return false;
  }

  public boolean equals(ReduceJobDTO that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_jobId = true && this.isSetJobId();
    boolean that_present_jobId = true && that.isSetJobId();
    if (this_present_jobId || that_present_jobId) {
      if (!(this_present_jobId && that_present_jobId))
        return false;
      if (!this.jobId.equals(that.jobId))
        return false;
    }

    boolean this_present_job = true && this.isSetJob();
    boolean that_present_job = true && that.isSetJob();
    if (this_present_job || that_present_job) {
      if (!(this_present_job && that_present_job))
        return false;
      if (!this.job.equals(that.job))
        return false;
    }

    boolean this_present_state = true && this.isSetState();
    boolean that_present_state = true && that.isSetState();
    if (this_present_state || that_present_state) {
      if (!(this_present_state && that_present_state))
        return false;
      if (!this.state.equals(that.state))
        return false;
    }

    boolean this_present_startTime = true;
    boolean that_present_startTime = true;
    if (this_present_startTime || that_present_startTime) {
      if (!(this_present_startTime && that_present_startTime))
        return false;
      if (this.startTime != that.startTime)
        return false;
    }

    boolean this_present_finishTime = true;
    boolean that_present_finishTime = true;
    if (this_present_finishTime || that_present_finishTime) {
      if (!(this_present_finishTime && that_present_finishTime))
        return false;
      if (this.finishTime != that.finishTime)
        return false;
    }

    boolean this_present_messages = true && this.isSetMessages();
    boolean that_present_messages = true && that.isSetMessages();
    if (this_present_messages || that_present_messages) {
      if (!(this_present_messages && that_present_messages))
        return false;
      if (!this.messages.equals(that.messages))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetJobId()) ? 131071 : 524287);
    if (isSetJobId())
      hashCode = hashCode * 8191 + jobId.hashCode();

    hashCode = hashCode * 8191 + ((isSetJob()) ? 131071 : 524287);
    if (isSetJob())
      hashCode = hashCode * 8191 + job.hashCode();

    hashCode = hashCode * 8191 + ((isSetState()) ? 131071 : 524287);
    if (isSetState())
      hashCode = hashCode * 8191 + state.getValue();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(startTime);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(finishTime);

    hashCode = hashCode * 8191 + ((isSetMessages()) ? 131071 : 524287);
    if (isSetMessages())
      hashCode = hashCode * 8191 + messages.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(ReduceJobDTO other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetJobId()).compareTo(other.isSetJobId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetJobId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.jobId, other.jobId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetJob()).compareTo(other.isSetJob());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetJob()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.job, other.job);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetState()).compareTo(other.isSetState());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetState()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, other.state);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetStartTime()).compareTo(other.isSetStartTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startTime, other.startTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetFinishTime()).compareTo(other.isSetFinishTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFinishTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.finishTime, other.finishTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMessages()).compareTo(other.isSetMessages());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMessages()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.messages, other.messages);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("ReduceJobDTO(");
    boolean first = true;

    sb.append("jobId:");
    if (this.jobId == null) {
      sb.append("null");
    } else {
      sb.append(this.jobId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("job:");
    if (this.job == null) {
      sb.append("null");
    } else {
      sb.append(this.job);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("state:");
    if (this.state == null) {
      sb.append("null");
    } else {
      sb.append(this.state);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("startTime:");
    sb.append(this.startTime);
    first = false;
    if (!first) sb.append(", ");
    sb.append("finishTime:");
    sb.append(this.finishTime);
    first = false;
    if (!first) sb.append(", ");
    sb.append("messages:");
    if (this.messages == null) {
      sb.append("null");
    } else {
      sb.append(this.messages);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (job != null) {
      job.validate();
    }
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

  private static class ReduceJobDTOStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ReduceJobDTOStandardScheme getScheme() {
      return new ReduceJobDTOStandardScheme();
    }
  }

  private static class ReduceJobDTOStandardScheme extends org.apache.thrift.scheme.StandardScheme<ReduceJobDTO> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ReduceJobDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // JOB_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.jobId = iprot.readString();
              struct.setJobIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // JOB
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.job = new JobDTO();
              struct.job.read(iprot);
              struct.setJobIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // STATE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.state = at.enfilo.def.transfer.dto.ExecutionState.findByValue(iprot.readI32());
              struct.setStateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // START_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.startTime = iprot.readI64();
              struct.setStartTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // FINISH_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.finishTime = iprot.readI64();
              struct.setFinishTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // MESSAGES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list78 = iprot.readListBegin();
                struct.messages = new java.util.ArrayList<java.lang.String>(_list78.size);
                java.lang.String _elem79;
                for (int _i80 = 0; _i80 < _list78.size; ++_i80)
                {
                  _elem79 = iprot.readString();
                  struct.messages.add(_elem79);
                }
                iprot.readListEnd();
              }
              struct.setMessagesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ReduceJobDTO struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.jobId != null) {
        oprot.writeFieldBegin(JOB_ID_FIELD_DESC);
        oprot.writeString(struct.jobId);
        oprot.writeFieldEnd();
      }
      if (struct.job != null) {
        oprot.writeFieldBegin(JOB_FIELD_DESC);
        struct.job.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.state != null) {
        oprot.writeFieldBegin(STATE_FIELD_DESC);
        oprot.writeI32(struct.state.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(START_TIME_FIELD_DESC);
      oprot.writeI64(struct.startTime);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(FINISH_TIME_FIELD_DESC);
      oprot.writeI64(struct.finishTime);
      oprot.writeFieldEnd();
      if (struct.messages != null) {
        oprot.writeFieldBegin(MESSAGES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.messages.size()));
          for (java.lang.String _iter81 : struct.messages)
          {
            oprot.writeString(_iter81);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ReduceJobDTOTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ReduceJobDTOTupleScheme getScheme() {
      return new ReduceJobDTOTupleScheme();
    }
  }

  private static class ReduceJobDTOTupleScheme extends org.apache.thrift.scheme.TupleScheme<ReduceJobDTO> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ReduceJobDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetJobId()) {
        optionals.set(0);
      }
      if (struct.isSetJob()) {
        optionals.set(1);
      }
      if (struct.isSetState()) {
        optionals.set(2);
      }
      if (struct.isSetStartTime()) {
        optionals.set(3);
      }
      if (struct.isSetFinishTime()) {
        optionals.set(4);
      }
      if (struct.isSetMessages()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetJobId()) {
        oprot.writeString(struct.jobId);
      }
      if (struct.isSetJob()) {
        struct.job.write(oprot);
      }
      if (struct.isSetState()) {
        oprot.writeI32(struct.state.getValue());
      }
      if (struct.isSetStartTime()) {
        oprot.writeI64(struct.startTime);
      }
      if (struct.isSetFinishTime()) {
        oprot.writeI64(struct.finishTime);
      }
      if (struct.isSetMessages()) {
        {
          oprot.writeI32(struct.messages.size());
          for (java.lang.String _iter82 : struct.messages)
          {
            oprot.writeString(_iter82);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ReduceJobDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.jobId = iprot.readString();
        struct.setJobIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.job = new JobDTO();
        struct.job.read(iprot);
        struct.setJobIsSet(true);
      }
      if (incoming.get(2)) {
        struct.state = at.enfilo.def.transfer.dto.ExecutionState.findByValue(iprot.readI32());
        struct.setStateIsSet(true);
      }
      if (incoming.get(3)) {
        struct.startTime = iprot.readI64();
        struct.setStartTimeIsSet(true);
      }
      if (incoming.get(4)) {
        struct.finishTime = iprot.readI64();
        struct.setFinishTimeIsSet(true);
      }
      if (incoming.get(5)) {
        {
          org.apache.thrift.protocol.TList _list83 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.messages = new java.util.ArrayList<java.lang.String>(_list83.size);
          java.lang.String _elem84;
          for (int _i85 = 0; _i85 < _list83.size; ++_i85)
          {
            _elem84 = iprot.readString();
            struct.messages.add(_elem84);
          }
        }
        struct.setMessagesIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

