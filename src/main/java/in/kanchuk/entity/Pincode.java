package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pincodes")
public class Pincode {

    @Id
    @Column(length = 6, nullable = false)
    private String pincode;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "hyperlocal", nullable = false)
    private boolean hyperlocal = false;

    @Column(name = "is_serviceable", nullable = false)
    private boolean isServiceable = true;
}
