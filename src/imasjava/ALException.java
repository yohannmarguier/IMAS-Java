package imasjava;

/**
 * Signals a failure reported through the Java High-Level Interface.
 *
 * <p>A root operation that fails throws an {@code ALException}. When the
 * failure originates in the Access Layer C ABI, the exception carries the
 * status code and the underlying message exactly as the Access Layer
 * reported them, unformatted; {@link #getCode()} is {@code 0} when the
 * exception was raised from Java itself rather than the C ABI, in which case
 * {@link #getRawMessage()} returns the same text as {@link #getMessage()}.
 *
 * <p>{@link #getMessage()} keeps returning the formatted text it has always
 * returned, so existing code that catches {@code ALException} and reads
 * {@link #getMessage()} is unaffected by the carried status.
 */
public class ALException extends Exception
{
   private final int    code;
   private final String rawMessage;

   /**
    * Constructs an exception not raised from the Access Layer C ABI. The
    * carried status code is {@code 0} and the raw message is the same as
    * the message returned by {@link #getMessage()}.
    *
    * @param message the exception message, returned unchanged by {@link #getMessage()}
    */
   public ALException(String message)
   {
      this(message, 0, message);
   }

   /**
    * Constructs an exception carrying the Access Layer status code and the
    * underlying message unformatted.
    *
    * @param message    the formatted message, returned unchanged by {@link #getMessage()}
    * @param code       the Access Layer status code
    * @param rawMessage the underlying message, unformatted
    */
   public ALException(String message, int code, String rawMessage)
   {
      super(message);
      this.code = code;
      this.rawMessage = rawMessage;
   }

   /**
    * @return the Access Layer status code carried by this exception, or
    *         {@code 0} if it was not raised from the C ABI
    */
   public int getCode()
   {
      return code;
   }

   /**
    * @return the underlying message, unformatted
    */
   public String getRawMessage()
   {
      return rawMessage;
   }
}
