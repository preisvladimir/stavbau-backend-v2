package cz.stavbau.backend.dev;

import cz.stavbau.backend.features.companies.model.Company;
import cz.stavbau.backend.features.companies.repo.CompanyRepository;
import cz.stavbau.backend.identity.users.model.User;
import cz.stavbau.backend.identity.users.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

/** DEV ONLY: založí firmu + admin uživatele s heslem 'admin123'. */
//@Component
@Profile({"dev","default"}) // běž i bez explicitního profilu v lokálu; v prod to vypni
public class DevUserSeeder implements CommandLineRunner {

    private final CompanyRepository companies;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DevUserSeeder(CompanyRepository companies, UserRepository users, PasswordEncoder encoder) {
        this.companies = companies;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // 1) Firma
        UUID companyId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Company c = companies.findById(companyId).orElseGet(() -> {
            Company nc = new Company();
            try {
                var f = Company.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(nc, companyId);
            } catch (Exception ignored) {}
            nc.setObchodniJmeno("Preis Studio");
            nc.setIco("01820991");
            return companies.save(nc);
        });

        // 2) Uživatel
        UUID userId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        Optional<User> opt = users.findById(userId);
        User u = opt.orElseGet(() -> {
            User nu = new User();
            try {
                var f = User.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(nu, userId);
            } catch (Exception ignored) {}
            nu.setEmail("admin@stavbau.local");
            //nu.setCompanyId(c.getId());
            nu.setLocale("cs");
            return nu;
        });

        // vždy nastav/aktualizuj heslo (pro jistotu)
        u.setPasswordHash(encoder.encode("BoRiS&&1974"));
        // token rotace defaults
        if (u.getTokenVersion() == 0) u.setTokenVersion(0);
        u.setRefreshTokenId(null);

        users.save(u);
        System.out.println("[DEV] Seed OK: admin@stavbau.local / admin123 (company=" + c.getId() + ")");
    }
}
