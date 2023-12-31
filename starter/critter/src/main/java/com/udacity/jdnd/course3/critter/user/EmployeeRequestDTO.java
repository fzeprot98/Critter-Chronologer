package com.udacity.jdnd.course3.critter.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class EmployeeRequestDTO {
    private Set<EmployeeSkill> skills;
    private LocalDate date;
}
