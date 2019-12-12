/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.cloud.communication.dto;


public enum InstanceTypeDTO implements org.apache.thrift.TEnum {
  CLUSTER(0),
  WORKER(1),
  REDUCER(2);

  private final int value;

  private InstanceTypeDTO(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static InstanceTypeDTO findByValue(int value) { 
    switch (value) {
      case 0:
        return CLUSTER;
      case 1:
        return WORKER;
      case 2:
        return REDUCER;
      default:
        return null;
    }
  }
}
