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

    @Column("userId")
    private Long userId;

    @Id
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;

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
