package pfe.mandomati.iamms.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pfe.mandomati.iamms.Model.Enums.RequestType;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    @ToString.Exclude
    private User user;

    private String endpoint;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    private int statusCode;
}
