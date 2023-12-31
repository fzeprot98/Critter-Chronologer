package com.udacity.jdnd.course3.critter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.udacity.jdnd.course3.critter.pet.PetController;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.pet.PetType;
import com.udacity.jdnd.course3.critter.schedule.ScheduleController;
import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import com.udacity.jdnd.course3.critter.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Transactional
@SpringBootTest(classes = CritterApplication.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.ANY)
public class CritterFunctionalTest {

    @Autowired
    private UserController userController;

    @Autowired
    private PetController petController;

    @Autowired
    private ScheduleController scheduleController;

    @Test
    public void testCreateCustomer(){
        CustomerDTO c = createCustomerDTO();
        CustomerDTO nc = userController.saveCustomer(c);
        CustomerDTO retrievedCustomer = userController.getAllCustomers().get(0);
        Assertions.assertEquals(nc.getName(), c.getName());
        Assertions.assertEquals(nc.getId(), retrievedCustomer.getId());
        Assertions.assertTrue(retrievedCustomer.getId() > 0);
    }

    @Test
    public void testCreateEmployee(){
        EmployeeDTO e = createEmployeeDTO();
        EmployeeDTO ne = userController.saveEmployee(e);
        EmployeeDTO retrievedEmployee = userController.getEmployee(ne.getId());
        Assertions.assertEquals(e.getSkills(), ne.getSkills());
        Assertions.assertEquals(ne.getId(), retrievedEmployee.getId());
        Assertions.assertTrue(retrievedEmployee.getId() > 0);
    }

    @Test
    public void testAddPetsToCustomer() {
        CustomerDTO c = createCustomerDTO();
        CustomerDTO nc = userController.saveCustomer(c);

        PetDTO p = createPetDTO();
        p.setOwnerId(nc.getId());
        PetDTO np = petController.savePet(p);

        PetDTO rp = petController.getPet(np.getId());
        Assertions.assertEquals(rp.getId(), np.getId());
        Assertions.assertEquals(rp.getOwnerId(), nc.getId());

        List<PetDTO> pets = petController.getPetsByOwner(nc.getId());
        Assertions.assertEquals(np.getId(), pets.get(0).getId());
        Assertions.assertEquals(np.getName(), pets.get(0).getName());

        CustomerDTO rc = userController.getAllCustomers().get(0);
        Assertions.assertTrue(rc.getPetIds() != null && rc.getPetIds().size() > 0);
        Assertions.assertEquals(rc.getPetIds().get(0), rp.getId());
    }

    @Test
    public void testFindPetsByOwner() {
        CustomerDTO c = createCustomerDTO();
        CustomerDTO nc = userController.saveCustomer(c);

        PetDTO p = createPetDTO();
        p.setOwnerId(nc.getId());
        PetDTO newPet = petController.savePet(p);
        p.setType(PetType.DOG);
        p.setName("Cat");

        List<PetDTO> pets = petController.getPetsByOwner(nc.getId());
        Assertions.assertEquals(pets.size(), 1);
        Assertions.assertEquals(pets.get(0).getOwnerId(), nc.getId());
        Assertions.assertEquals(pets.get(0).getId(), newPet.getId());
    }

    @Test
    public void testFindOwnerByPet() {
        CustomerDTO c = createCustomerDTO();
        CustomerDTO nc = userController.saveCustomer(c);

        PetDTO p = createPetDTO();
        p.setOwnerId(nc.getId());
        PetDTO newPet = petController.savePet(p);

        CustomerDTO owner = userController.getOwnerByPet(newPet.getId());
        Assertions.assertEquals(owner.getId(), nc.getId());
        Assertions.assertEquals(owner.getPetIds().get(0), newPet.getId());
    }

    @Test
    public void testChangeEmployeeAvailability() {
        EmployeeDTO e = createEmployeeDTO();
        EmployeeDTO ne = userController.saveEmployee(e);
        Assertions.assertNull(ne.getDaysAvailable());

        Set<DayOfWeek> availability = Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        userController.setAvailability(availability, ne.getId());

        EmployeeDTO re = userController.getEmployee(ne.getId());
        Assertions.assertEquals(availability, re.getDaysAvailable());
    }

    @Test
    public void testFindEmployeesByServiceAndTime() {
        EmployeeDTO ne = createEmployeeDTO();
        EmployeeDTO re = createEmployeeDTO();
        EmployeeDTO emp3 = createEmployeeDTO();

        ne.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        re.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        emp3.setDaysAvailable(Sets.newHashSet(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        ne.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.PETTING));
        re.setSkills(Sets.newHashSet(EmployeeSkill.PETTING, EmployeeSkill.WALKING));
        emp3.setSkills(Sets.newHashSet(EmployeeSkill.WALKING, EmployeeSkill.SHAVING));

        EmployeeDTO emp1n = userController.saveEmployee(ne);
        EmployeeDTO emp2n = userController.saveEmployee(re);
        EmployeeDTO emp3n = userController.saveEmployee(emp3);

        EmployeeRequestDTO er1 = new EmployeeRequestDTO();
        er1.setDate(LocalDate.of(2019, 12, 25));
        er1.setSkills(Sets.newHashSet(EmployeeSkill.PETTING));

        Set<Long> eIds1 = userController.findEmployeesForService(er1).stream().map(EmployeeDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds1expected = Sets.newHashSet(emp1n.getId(), emp2n.getId());
        Assertions.assertEquals(eIds1, eIds1expected);

        EmployeeRequestDTO er2 = new EmployeeRequestDTO();
        er2.setDate(LocalDate.of(2019, 12, 27));
        er2.setSkills(Sets.newHashSet(EmployeeSkill.WALKING, EmployeeSkill.SHAVING));

        Set<Long> eIds2 = userController.findEmployeesForService(er2).stream().map(EmployeeDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds2expected = Sets.newHashSet(emp3n.getId());
        Assertions.assertEquals(eIds2, eIds2expected);
    }

    @Test
    public void testSchedulePetsForServiceWithEmployee() {
        EmployeeDTO employeeTemp = createEmployeeDTO();
        employeeTemp.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        EmployeeDTO e = userController.saveEmployee(employeeTemp);
        CustomerDTO c = userController.saveCustomer(createCustomerDTO());
        PetDTO petTemp = createPetDTO();
        petTemp.setOwnerId(c.getId());
        PetDTO p = petController.savePet(petTemp);

        LocalDate date = LocalDate.of(2023, 12, 28);
        List<Long> petList = Lists.newArrayList(p.getId());
        List<Long> employeeList = Lists.newArrayList(e.getId());
        Set<EmployeeSkill> skillSet =  Sets.newHashSet(EmployeeSkill.PETTING);

        scheduleController.createSchedule(createScheduleDTO(petList, employeeList, date, skillSet));
        ScheduleDTO scheduleDTO = scheduleController.getAllSchedules().get(0);

        Assertions.assertEquals(scheduleDTO.getActivities(), skillSet);
        Assertions.assertEquals(scheduleDTO.getDate(), date);
        Assertions.assertEquals(scheduleDTO.getEmployeeIds(), employeeList);
        Assertions.assertEquals(scheduleDTO.getPetIds(), petList);
    }

    @Test
    public void testFindScheduleByEntities() {
        ScheduleDTO s1 = populateSchedule(1, 2, LocalDate.of(2022, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        ScheduleDTO s2 = populateSchedule(3, 1, LocalDate.of(2022, 12, 26), Sets.newHashSet(EmployeeSkill.PETTING));

        ScheduleDTO s3 = new ScheduleDTO();
        s3.setEmployeeIds(s1.getEmployeeIds());
        s3.setPetIds(s2.getPetIds());
        s3.setActivities(Sets.newHashSet(EmployeeSkill.SHAVING, EmployeeSkill.PETTING));
        s3.setDate(LocalDate.of(2023, 3, 23));
        scheduleController.createSchedule(s3);
        List<ScheduleDTO> o1 = scheduleController.getScheduleForEmployee(s1.getEmployeeIds().get(0));
        compareSchedules(s1, o1.get(0));
        compareSchedules(s3, o1.get(1));

        List<ScheduleDTO> o2 = scheduleController.getScheduleForEmployee(s2.getEmployeeIds().get(0));
        compareSchedules(s2, o2.get(0));

        List<ScheduleDTO> p1 = scheduleController.getScheduleForPet(s1.getPetIds().get(0));
        compareSchedules(s1, p1.get(0));

        List<ScheduleDTO> p2 = scheduleController.getScheduleForPet(s2.getPetIds().get(0));
        compareSchedules(s2, p2.get(0));
        compareSchedules(s3, p2.get(1));

        List<ScheduleDTO> c1 = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(s1.getPetIds().get(0)).getId());
        compareSchedules(s1, c1.get(0));

        List<ScheduleDTO> c2 = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(s2.getPetIds().get(0)).getId());
        compareSchedules(s2, c2.get(0));
        compareSchedules(s3, c2.get(1));
    }


    private static EmployeeDTO createEmployeeDTO() {
        EmployeeDTO e = new EmployeeDTO();
        e.setName("Orez");
        e.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.PETTING));
        return e;
    }
    private static CustomerDTO createCustomerDTO() {
        CustomerDTO c = new CustomerDTO();
        c.setName("Orez");
        c.setPhoneNumber("222-333-444");
        return c;
    }

    private static PetDTO createPetDTO() {
        PetDTO p = new PetDTO();
        p.setName("cat");
        p.setType(PetType.CAT);
        return p;
    }

    private static EmployeeRequestDTO createEmployeeRequestDTO() {
        EmployeeRequestDTO employeeRequestDTO = new EmployeeRequestDTO();
        employeeRequestDTO.setDate(LocalDate.of(2019, 12, 25));
        employeeRequestDTO.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        return employeeRequestDTO;
    }

    private static ScheduleDTO createScheduleDTO(List<Long> petIds, List<Long> employeeIds, LocalDate date, Set<EmployeeSkill> activities) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setPetIds(petIds);
        scheduleDTO.setEmployeeIds(employeeIds);
        scheduleDTO.setDate(date);
        scheduleDTO.setActivities(activities);
        return scheduleDTO;
    }

    private ScheduleDTO populateSchedule(int numEmployees, int numPets, LocalDate date, Set<EmployeeSkill> activities) {
        List<Long> employeeIds = IntStream.range(0, numEmployees)
                .mapToObj(i -> createEmployeeDTO())
                .map(e -> {
                    e.setSkills(activities);
                    e.setDaysAvailable(Sets.newHashSet(date.getDayOfWeek()));
                    return userController.saveEmployee(e).getId();
                }).collect(Collectors.toList());
        CustomerDTO cust = userController.saveCustomer(createCustomerDTO());
        List<Long> petIds = IntStream.range(0, numPets)
                .mapToObj(i -> createPetDTO())
                .map(p -> {
                    p.setOwnerId(cust.getId());
                    return petController.savePet(p).getId();
                }).collect(Collectors.toList());
        return scheduleController.createSchedule(createScheduleDTO(petIds, employeeIds, date, activities));
    }

    private static void compareSchedules(ScheduleDTO s1, ScheduleDTO s2) {
        Assertions.assertEquals(s1.getPetIds(), s2.getPetIds());
        Assertions.assertEquals(s1.getActivities(), s2.getActivities());
        Assertions.assertEquals(s1.getEmployeeIds(), s2.getEmployeeIds());
        Assertions.assertEquals(s1.getDate(), s2.getDate());
    }

}
