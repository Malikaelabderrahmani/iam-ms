package pfe.mandomati.iamms.Model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rhs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RH {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "cni", length = 50, unique = true)
    private String cni;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "cnss_number", length = 50, unique = true)
    private String cnssNumber;

    @Column(name = "position", length = 100, nullable = false)
    private String position;
}
