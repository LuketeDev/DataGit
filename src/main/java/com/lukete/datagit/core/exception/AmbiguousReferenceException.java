public class AmbiguousReferenceException extends DataGitException {

    public AmbiguousReferenceException(String ref) {
        super("Ambiguous reference: " + ref);
    }
}