package wealthpro.springbootapigateway.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users implements Persistable<String> {

    // Auto-generated numeric surrogate key — used in JWT claims and Notification.userId.
    // NOT the JPA @Id (username is the primary key); R2DBC reads this back from DB on SELECT.
    // @Column is required: R2DBC 3.x NamingStrategy converts camelCase → snake_case by default
    // (userId → user_id), but the DB column is named userId — must be explicit.
    @Column("userId")
    private Long userId;

    @Id
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;

    // Stored as VARCHAR in DB (e.g. "RM", "ADMIN", "CLIENT").
    // Kept as String so unknown legacy role values (e.g. "ADVISOR") in the DB
    // do not cause Role.valueOf() to throw IllegalArgumentException during findAll().
    // Role enum validation is enforced at write-time (UserController.parseRole / guardRole).
    private String roles;

    @Transient
    private boolean isNew = false;

    @Override
    public String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
