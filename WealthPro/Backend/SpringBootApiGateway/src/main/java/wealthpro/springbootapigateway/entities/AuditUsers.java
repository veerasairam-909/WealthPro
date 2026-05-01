package wealthpro.springbootapigateway.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auditlog")
public class AuditUsers {

    @Id
    private long id;
    private String username;
    private String roles;

    @Column("remoteAddress")
    private String remoteAddress;

    private String method;

    @Column("pathinfo")
    private String path;

    private String action;
    private String resource;
    private LocalDateTime timestamp;
    private String metadata;
}
