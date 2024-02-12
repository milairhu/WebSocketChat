package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.Chat;
import fr.utc.sr03.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByOwnerId(@Param("ownerId") long ownerId);

    Chat findByTitle(@Param("title") String title);

    @Query("SELECT u FROM ChatUser c JOIN User u ON c.userId = u.id WHERE (c.chatId) = :chatId ")
    List<User> findInvitedUsersByChatId(@Param("chatId") long chatId);

    @Query("SELECT u FROM Chat c JOIN User u ON c.ownerId = u.id WHERE (c.id) = :chatId ")
    User findOwner(@Param("chatId") long chatId);

    //Delete chat user when chat is deleted
    @Transactional
    @Modifying //Necessaire car modification sur la table
    @Query("DELETE FROM ChatUser cu WHERE cu.chatId = :chatId")
    void deleteChatUsers(@Param("chatId") long chatId);
}
