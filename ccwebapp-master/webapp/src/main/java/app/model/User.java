package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * @author yi
 */
@Entity
public class User {
    @Id
    private String id;
    private String first_name;
    private String last_name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String email_address;
    private Date account_created;
    private Date account_updated;

    public User (String first_name,String last_name, String password, String email_address){
        id = UUID.randomUUID().toString();
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.email_address = email_address;
        this.account_created = new Date();
        this.account_updated = new Date();
    }

    public User (){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public Date getAccount_created() {
        return account_created;
    }

    public void setAccount_created(Date account_created) {
        this.account_created = account_created;
    }

    public Date getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(Date account_updated) {
        this.account_updated = account_updated;
    }
}
