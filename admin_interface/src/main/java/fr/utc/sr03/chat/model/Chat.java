package fr.utc.sr03.chat.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chats")
public class Chat{
    @Id
    @Column(name ="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    @Column(name = "title")
    @Size(max = 40)
    @NotBlank
    private String title;
    @Column(name = "description")
    @Size(max = 50)
    private String description;
    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    @Column(name = "duration")
    @Min(1)
    @NotNull
    private int duration; //in minutes
    @Column(name = "ownerid")
    @NotNull
    private long ownerId;

    public Chat(String title, String description, LocalDateTime date, int duration, long ownerId){
        this.title = title;
        this.description = description;
        this.date = date;
        this.duration = duration;
        this.ownerId = ownerId;
    }
    public Chat(long id, String title, String description, LocalDateTime date, int duration, long ownerId){
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.duration = duration;
        this.ownerId = ownerId;
    }

    public Chat() {

    }
    public Chat(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
