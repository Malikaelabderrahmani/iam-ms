package pfe.mandomati.iamms.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import pfe.mandomati.iamms.model.Enums.*;


@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_type", length = 50)
    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Column(name = "request_date")
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String endpoint;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    private int statusCode;
}
