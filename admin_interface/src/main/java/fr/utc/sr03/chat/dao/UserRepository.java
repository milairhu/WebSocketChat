package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    User findByEmailAndPassword(@Param("email") String mail, @Param("password") String password);
    List<User> findByIsAdmin(@Param("isAdmin") boolean isAdmin);
    List<User> findByIsDeactivated(@Param("isDeactivated") boolean isDeactivated);
    User findByEmail(@Param("email") String mail);

    Page<User> findByFirstNameContainingIgnoreCaseOrFamilyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(PageRequest page,
                                                                                @Param("firstName")String firstName,
                                                                                @Param("familyName")String familyName,
                                                                                @Param("Email")String email); //Pour filtrer les utilisateurs

    // Requete créée manuellement
    Page<User> findAllByIsDeactivated(PageRequest pageRequest, @Param("isDeactivated") boolean isDeactivated);

    //Trouve les chats du user
    @Query("SELECT c FROM ChatUser cu JOIN Chat c ON cu.chatId = c.id WHERE cu.userId = :userId")
    List<Chat> findUserInvitations(@Param("userId") long userId);

    //Delete ChatUsers when user is deleted
    @Transactional
    @Modifying //Necessaire car modification sur la table
    @Query("DELETE FROM ChatUser cu WHERE cu.userId = :userId")
    void deleteUserChats(@Param("userId") long userId);

}
