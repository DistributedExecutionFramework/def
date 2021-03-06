/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.transfer.dto;


public enum ResourceType implements org.apache.thrift.TEnum {
  MEMORY(0),
  FILE(1),
  REDIS(2);

  private final int value;

  private ResourceType(int value) {
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
  public static ResourceType findByValue(int value) { 
    switch (value) {
      case 0:
        return MEMORY;
      case 1:
        return FILE;
      case 2:
        return REDIS;
      default:
        return null;
    }
  }
}
