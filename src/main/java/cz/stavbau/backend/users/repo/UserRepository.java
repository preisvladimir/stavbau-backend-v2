package cz.stavbau.backend.users.repo;
import cz.stavbau.backend.users.model.User; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional; import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
