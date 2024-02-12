package fr.utc.sr03.chat.model;

import java.io.Serializable;
import java.util.Objects;

//Classe créée pour satisfaire la clé passée à InvitationRepository
public class InvitationId implements Serializable {
    private long chatId;
    private long userId;

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvitationId that = (InvitationId) o;
        return chatId == that.chatId &&
                userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, userId);
    }
}