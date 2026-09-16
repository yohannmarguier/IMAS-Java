package imasjava;

import java.util.Objects;

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
      READ("REFUSED READ: "),
      WRITE("REFUSED WRITE: "),
      DELETE("REFUSED DELETE: ");

      private final String refusalPrefix;

      Operation(String refusalPrefix)
      {
         this.refusalPrefix = refusalPrefix;
      }

      /**
       * @return the prefix of the one line a tolerated refusal reports on
       *         standard output, spelled to match IMAS-Cpp
       */
      public String getRefusalPrefix()
      {
         return refusalPrefix;
      }
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

   @Override
   public boolean equals(Object other)
   {
      if (this == other)
      {
         return true;
      }
      if (!(other instanceof SkippedPath))
      {
         return false;
      }
      SkippedPath that = (SkippedPath) other;
      return code == that.code
            && operation == that.operation
            && Objects.equals(path, that.path)
            && Objects.equals(message, that.message);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(operation, path, message, code);
   }
}
