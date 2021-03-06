/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.reducer.api.dto;


public enum ReduceStage implements org.apache.thrift.TEnum {
  SIMPLE(0),
  MASTER(1),
  FINISHED(2);

  private final int value;

  private ReduceStage(int value) {
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
  public static ReduceStage findByValue(int value) { 
    switch (value) {
      case 0:
        return SIMPLE;
      case 1:
        return MASTER;
      case 2:
        return FINISHED;
      default:
        return null;
    }
  }
}
