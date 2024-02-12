package fr.utc.sr03.chat.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "chatuser")
@IdClass(InvitationId.class)
public class ChatUser implements Serializable {
    @Column(name = "chatid")
    @Id
    private long chatId;

    @Column(name = "userid")
    @Id
    private long userId;

    public ChatUser(){}

    public ChatUser(long chatId, long userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

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
}


