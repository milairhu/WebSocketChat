package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.Attempt;
import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    Attempt findByUserId(@Param("userId") long userId);

}
