package fr.utc.sr03.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "attempts")
public class Attempt implements Serializable {
    @Column(name = "userid")
    @Id
    private long userId;

    @Column( name = "nbattempts")
    private long nbAttempts;

    public Attempt(){}

    public Attempt( long userId, long nbAttempts) {
        this.userId = userId;
        this.nbAttempts = nbAttempts;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getNbAttempts() {
        return nbAttempts;
    }

    public void setNbAttempts(long nbAttempts) { this.nbAttempts = nbAttempts;}
}
