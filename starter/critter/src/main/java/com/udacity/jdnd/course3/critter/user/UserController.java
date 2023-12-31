package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ModelMapper mapper;

    public UserController(UserService userService, ModelMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping("/customer")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO c){
        Customer customer = userService.saveCustomer(c);
        return buildCustomerResponse(customer);
    }

    @GetMapping("/customer")
    public List<CustomerDTO> getAllCustomers(){
        return userService.getAllCustomer().stream().map(this::buildCustomerResponse).collect(Collectors.toList());
    }

    @GetMapping("/customer/pet/{petId}")
    public CustomerDTO getOwnerByPet(@PathVariable long petId){
        Customer owner = userService.getOwnerByPet(petId);
        return buildCustomerResponse(owner);
    }

    @PostMapping("/employee")
    public EmployeeDTO saveEmployee(@RequestBody EmployeeDTO e) {
        Employee emp = userService.saveEmployee(e);
        return mapper.map(emp, EmployeeDTO.class);
    }

    @PostMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable long employeeId) {
        return mapper.map(userService.getEmployee(employeeId), EmployeeDTO.class);
    }

    @PutMapping("/employee/{employeeId}")
    public void setAvailability(@RequestBody Set<DayOfWeek> daysAvailable, @PathVariable long employeeId) {
        userService.setAvailability(daysAvailable, employeeId);
    }

    @GetMapping("/employee/availability")
    public List<EmployeeDTO> findEmployeesForService(@RequestBody EmployeeRequestDTO e) {
        return userService.findEmployeeByService(e).stream().map(employee -> mapper.map(employee, EmployeeDTO.class)).collect(Collectors.toList());
    }

    private CustomerDTO buildCustomerResponse(Customer owner) {
        CustomerDTO response = mapper.map(owner, CustomerDTO.class);
        List<Pet> pets = owner.getPets();
        Stream<Pet> petStream = Optional.ofNullable(pets).map(Collection::stream).orElseGet(Stream::empty);
        List<Long> petIds = petStream.map(Pet::getId).collect(Collectors.toList());
        response.setPetIds(petIds);
        return response;
    }
}
