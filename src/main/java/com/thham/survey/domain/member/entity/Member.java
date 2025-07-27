package com.thham.survey.domain.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account"),
        @UniqueConstraint(columnNames = "residentId")
}, indexes = {
        @Index(name = "idx_birth_year", columnList = "birthYear")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 4, max = 50)
    private String account;

    @Setter
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(min = 13, max = 13)
    private String residentId;

    @NotBlank
    @Size(min = 11, max = 11)
    private String phoneNumber;

    @Setter
    @NotBlank
    private String address;

    private int birthYear;
}