package com.refanzzzz.tokonyadia.service.impl;

import com.refanzzzz.tokonyadia.constant.Constant;
import com.refanzzzz.tokonyadia.constant.UserRole;
import com.refanzzzz.tokonyadia.dto.request.CustomerRequest;
import com.refanzzzz.tokonyadia.dto.response.CustomerResponse;
import com.refanzzzz.tokonyadia.entity.Customer;
import com.refanzzzz.tokonyadia.entity.UserAccount;
import com.refanzzzz.tokonyadia.repository.CustomerRepository;
import com.refanzzzz.tokonyadia.service.CustomerService;
import com.refanzzzz.tokonyadia.service.UserAccountService;
import com.refanzzzz.tokonyadia.specification.CustomerSpecification;
import com.refanzzzz.tokonyadia.util.SortUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;
    private UserAccountService userAccountService;

    @Override
    public Page<CustomerResponse> getAll(CustomerRequest request) {

        Sort sortBy = SortUtil.parseSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sortBy);
        Specification<Customer> customerSpecification = CustomerSpecification.getCustomerSpecification(request);

        Page<Customer> customerResponsePage = customerRepository.findAll(customerSpecification, pageable);

        return customerResponsePage.map(this::toCustomerResponse);
    }

    @Override
    public CustomerResponse getById(String id) {
        Customer customer = getOne(id);

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CustomerResponse create(CustomerRequest request) {
        UserAccount userAccount = UserAccount.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        userAccountService.create(userAccount);

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .userAccount(userAccount)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        customerRepository.saveAndFlush(customer);
        return toCustomerResponse(customer);
    }

    @Override
    public void remove(String id) {
        Customer customer = getOne(id);
        customerRepository.delete(customer);
    }

    @Override
    public CustomerResponse update(String id, CustomerRequest request) {
        Customer customer = getOne(id);

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setPhoneNumber(request.getPhoneNumber());

        Customer savedCustomer = customerRepository.save(customer);
        return toCustomerResponse(savedCustomer);
    }

    @Override
    public Customer getOne(String id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constant.ERROR_GET_CUSTOMER));
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .userid(customer.getUserAccount().getId())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .build();
    }
}
