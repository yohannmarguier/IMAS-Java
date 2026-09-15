package imasjava;

/**
 * One field a tolerant site left unset because of a tolerated refusal.
 *
 * <p>Recorded with the operation the traversal was performing, the field
 * path as the traversal knows it (relative to the enclosing context, not
 * the full Data Dictionary path), the refusal message and the status
 * code that {@link ToleranceChokepoint} judged to lie in the refusal band.
 */
public final class SkippedPath
{
   /**
    * The kind of root operation a tolerant site was performing when it
    * absorbed a refusal.
    */
   public enum Operation
   {
      READ, WRITE, DELETE
   }

   private final Operation operation;
   private final String    path;
   private final String    message;
   private final int       code;

   /**
    * @param operation the kind of root operation being performed
    * @param path      the field path as the traversal knows it, relative
    *                  to the enclosing context
    * @param message   the refusal message, carrying the full Data
    *                  Dictionary path
    * @param code      the status code, inside the refusal band
    */
   public SkippedPath(Operation operation, String path, String message, int code)
   {
      this.operation = operation;
      this.path = path;
      this.message = message;
      this.code = code;
   }

   public Operation getOperation()
   {
      return operation;
   }

   public String getPath()
   {
      return path;
   }

   public String getMessage()
   {
      return message;
   }

   public int getCode()
   {
      return code;
   }
}
