package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }
}
