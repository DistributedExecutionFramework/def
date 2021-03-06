/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package at.enfilo.def.communication.dto;


public enum TicketStatusDTO implements org.apache.thrift.TEnum {
  UNKNOWN(0),
  IN_PROGRESS(1),
  CANCELED(2),
  DONE(3),
  FAILED(4);

  private final int value;

  private TicketStatusDTO(int value) {
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
  public static TicketStatusDTO findByValue(int value) { 
    switch (value) {
      case 0:
        return UNKNOWN;
      case 1:
        return IN_PROGRESS;
      case 2:
        return CANCELED;
      case 3:
        return DONE;
      case 4:
        return FAILED;
      default:
        return null;
    }
  }
}
