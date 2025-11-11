package edu.ucsal.fiadopay.domain.merchant;

import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.user.User;
import edu.ucsal.fiadopay.domain.merchant.dto.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merchant_table")
@Setter
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class Merchant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false, unique=true)
    private String clientId;

    @Column(nullable=false, unique=true)
    private String clientSecret;

    @Column(nullable=false, unique=true)
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(nullable=false)
    private  Double interest;

    @OneToMany(mappedBy = "merchant")
    private Set<Payment> payments = new HashSet();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

     public boolean isEnable(){
        return  getStatus() == Status.ACTIVE ;
    }

}
