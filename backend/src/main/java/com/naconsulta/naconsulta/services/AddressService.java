package com.naconsulta.naconsulta.services;

import com.naconsulta.naconsulta.dtos.AddressMinDto;
import com.naconsulta.naconsulta.entities.Address;
import com.naconsulta.naconsulta.repositories.AddressRepository;
import com.naconsulta.naconsulta.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    AddressRepository repository;

    @Transactional(readOnly = true)
    public AddressMinDto findbyId(Long id) {
        Address entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado"));
        return new AddressMinDto(entity, entity.getDoctors());
    }

    @Transactional(readOnly = true)
    public List<AddressMinDto> findByNeighborhood(String name) {
        List<Address> result = repository.searchByNeighborhood(name);
        return result.stream().map(x -> new AddressMinDto(x, x.getDoctors())).collect(Collectors.toList());
    }

}
