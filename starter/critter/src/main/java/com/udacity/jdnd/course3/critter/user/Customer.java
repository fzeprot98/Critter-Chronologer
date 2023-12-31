package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.pet.Pet;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 50, nullable = false)
    private String name;
    @Column(length = 15, nullable = false)
    private String phoneNumber;
    @Column
    private String notes;
    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private List<Pet> pets = new ArrayList<>();
}
