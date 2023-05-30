package com.naconsulta.naconsulta.services;

import com.naconsulta.naconsulta.dtos.*;
import com.naconsulta.naconsulta.entities.Role;
import com.naconsulta.naconsulta.entities.Telephone;
import com.naconsulta.naconsulta.entities.User;
import com.naconsulta.naconsulta.repositories.RoleRepository;
import com.naconsulta.naconsulta.repositories.TelephoneRepository;
import com.naconsulta.naconsulta.repositories.UserRepository;
import com.naconsulta.naconsulta.services.exceptions.DatabaseException;
import com.naconsulta.naconsulta.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id " + id + " not found");
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    @Transactional(readOnly = true)
    public List<UserMinDto> findAllOrByName(String name) {
        List<User> list = repository.searchByName(name);
        return list.stream().map(x -> new UserMinDto(x)).collect(Collectors.toList());
    }

    @Transactional
    public UserFormDto insert(UserInsertDto dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);
        return new UserFormDto(entity, entity.getPhones(), entity.getRoles());
    }

    @Transactional(readOnly = true)
    public UserMaxDto userLogged() {
        User entity = authService.authenticated();
        return new UserMaxDto(entity, entity.getPhones(), entity.getRoles(), entity.getAppointments());
    }

    @Transactional(readOnly = true)
    public UserMaxDto findById(Long id) {
        authService.validateSelfOrAdmin(id);
        Optional<User> obj = repository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return new UserMaxDto(entity, entity.getPhones(), entity.getRoles(), entity.getAppointments());
    }

    private void copyDtoToEntity(UserFormDto dto, User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setGender(dto.getGender());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDto roleDto : dto.getRoles()) {
            Role role = roleRepository.getReferenceById(roleDto.getId());
            entity.getRoles().add(role);
        }

        entity.getPhones().clear();
        for (TelephoneDto telephoneDto : dto.getPhones()) {
            Telephone telephone = telephoneRepository.getReferenceById(telephoneDto.getId());
            entity.getPhones().add(telephone);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username);
        if (user == null) {
            logger.error("Usuário não encontrado " + username);
            throw new UsernameNotFoundException("Email não encontrado");
        }
        logger.info("Usuário encontrado " + username);
        return user;
    }
}

