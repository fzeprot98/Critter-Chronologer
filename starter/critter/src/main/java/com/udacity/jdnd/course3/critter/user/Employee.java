package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.schedule.Schedule;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 50, nullable = false)
    private String name;
    @ElementCollection(targetClass = EmployeeSkill.class)
    @Enumerated(EnumType.STRING)
    private Set<EmployeeSkill> skills;
    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysAvailable;
    @ManyToMany(mappedBy = "employees")
    private List<Schedule> schedule = new ArrayList<>();

    @PostConstruct
    private void construct() {
        this.skills = new HashSet<>();
        this.daysAvailable = new HashSet<>();
    }
}
