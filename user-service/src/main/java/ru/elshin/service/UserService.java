package ru.elshin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.elshin.dto.UserResponse;
import ru.elshin.entity.User;
import ru.elshin.exception.ResourceNotFoundException;
import ru.elshin.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor // Автоматически внедрит UserRepository через конструктор
public class UserService {

    private final UserRepository userRepository;

    @CacheEvict(value = "users", key = "#result.id")
    public User createUser(User user) {
        // Простая проверка: если email занят, бросим ошибку
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + id));
        log.info("Запрос пользователя из БД для ID: {}", id);
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}
