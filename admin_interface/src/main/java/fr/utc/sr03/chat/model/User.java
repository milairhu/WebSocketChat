package fr.utc.sr03.chat.model;


import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name ="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY) // strategy=GenerationType.IDENTITY => obligatoire pour auto increment mysql
    private long id;
    @Column(name = "firstname")
    @Size(min = 2)
    @Size(max = 30)
    private String firstName;
    @Column(name = "familyname")
    @Size(min = 2)
    @Size(max = 30)
    private String familyName;
    @Size(min = 5)
    @Size(max = 50)
    private String email;
    @Size(min = 8)
    @Size(max = 50)
    private String password;
    @Column(name="isadmin")
    private boolean isAdmin;
    @Column(name="isdeactivated")
    private boolean isDeactivated;

    public User(){}

    public User(long id) {
        this.id = id;
    }
    public User(String firstName, String familyName, String email, String password, boolean isAdmin, boolean deactivated){
        this.firstName = firstName;
        this.familyName = familyName;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isDeactivated = deactivated;
    }
    public User(long id, String firstName, String familyName, String email, String password, boolean isAdmin, boolean deactivated){
        this.id = id;
        this.firstName = firstName;
        this.familyName = familyName;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isDeactivated = deactivated;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean getIsDeactivated() {
        return isDeactivated;
    }

    public void setIsDeactivated(boolean deactivated) {
        this.isDeactivated = deactivated;
    }

}
