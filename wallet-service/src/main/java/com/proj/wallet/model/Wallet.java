package com.proj.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
   
   @Id
   @Column(name="wallet_id")
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID walletId;

   @Column(name = "account_no", nullable = false, unique = true)
   private String accountNo;
   
   @Column(name="user_id", nullable = false, unique = true)
   private UUID userId;

   @Column(nullable = false, precision = 19, scale = 2)
   private BigDecimal balance;

   @Column(nullable = false)
   private String currency;

   @Column(nullable = false)
   private String status;

   @Column(name="updated_at", nullable = false)
   private LocalDateTime updatedAt;

}
