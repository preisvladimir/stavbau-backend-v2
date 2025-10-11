package cz.stavbau.backend.users.repo;
import cz.stavbau.backend.common.simple.IdNameView;
import cz.stavbau.backend.users.model.User; import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional; import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

}
