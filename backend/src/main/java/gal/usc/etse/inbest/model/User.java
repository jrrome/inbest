package gal.usc.etse.inbest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Entity
@Table (name = "users")
public class User {

    //Persistent fields

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    @Pattern(regexp = "^[^\\p{Lu}\\p{Lt}\\s]+$", message = "must be lowercase without whitespace")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    //Constructors

    protected User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    //Persistence lifecycle

    @PrePersist
    private void onCreate(){
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    //Accessors

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
