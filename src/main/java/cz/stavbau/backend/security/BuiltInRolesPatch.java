package cz.stavbau.backend.security;
import java.util.Set;
/**
 * TODO: Sloučit do existující BuiltInRoles – pouze ilustrační patch pro nové scopy.
 */
public final class BuiltInRolesPatch {
    public static final Set<String> INVOICE_SCOPES = Set.of(
        "invoices:read","invoices:write","invoices:delete","invoices:export"
    );
    public static final Set<String> FILE_SCOPES = Set.of(
        "files:read","files:write","files:delete","files:tag"
    );
    private BuiltInRolesPatch() {}
}
