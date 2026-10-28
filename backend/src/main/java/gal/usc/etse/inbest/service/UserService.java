package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.exception.UserAlreadyExistsException;
import gal.usc.etse.inbest.model.dto.CreateUserRequest;
import gal.usc.etse.inbest.model.entity.CashAccount;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.CashAccountRepository;
import gal.usc.etse.inbest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final CashAccountRepository cashAccountRepository;

    public UserService(UserRepository repository, CashAccountRepository cashAccountRepository) {
        this.repository = repository;
        this.cashAccountRepository = cashAccountRepository;
    }

    @Transactional
    public User register(CreateUserRequest request) {
        if (repository.existsByUsername(request.username())
                || repository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException();
        }

        var user = repository.save(new User(request.username(), request.email()));
        cashAccountRepository.save(new CashAccount(user));
        return user;
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
