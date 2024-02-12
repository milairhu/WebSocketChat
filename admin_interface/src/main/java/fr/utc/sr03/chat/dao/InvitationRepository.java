package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.ChatUser;
import fr.utc.sr03.chat.model.InvitationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<ChatUser, InvitationId> {
    void deleteByChatIdAndUserId(@Param("chatId")long chatId, @Param("userId") long userId);
    void deleteByChatId(@Param("chatId")long chatId);
    void deleteByUserId(@Param("userId")long userId);

}
