package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Order;
import com.example.demo.domain.User;
import com.example.demo.domain.response.Meta;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.domain.response.user.ResUpdateUserDTO;
import com.example.demo.domain.response.user.ResUserDTO;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    // Dependency Inject: Constructor
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public UserService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public User handleCreateUser(User createUser) {
        return this.userRepository.save(createUser);
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setGender(user.getGender());
        res.setEmail(user.getEmail());
        res.setAddress(user.getAddress());
        res.setAvatar(user.getAvatar());
        res.setCreateAt(user.getCreateAt());

        return res;
    }

    public boolean checkEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User handleGetUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public User handleUpdateUser(User requestUser) {
        User currentUser = this.handleGetUserById(requestUser.getId());
        if (currentUser != null) {
            currentUser.setName(requestUser.getName());
            currentUser.setPhone(requestUser.getPhone());
            currentUser.setGender(requestUser.getGender());
            currentUser.setAddress(requestUser.getAddress());

            // update user in database
            currentUser = this.userRepository.save(currentUser);
        }

        return currentUser;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setGender(user.getGender());
        res.setEmail(user.getEmail());
        res.setAddress(user.getAddress());
        res.setAvatar(user.getAvatar());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }

    public ResultPaginationDTO fetchAllUsersWithPagination(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        result.setMeta(meta);

        // convert User -> UserDTO
        List<ResUserDTO> listUsers = pageUser.getContent().stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        result.setResult(listUsers);

        return result;
    }

    public void handleDeleteUser(long id) {
        User user = this.handleGetUserById(id);
        if (user != null) {
            // Fetch all order belong to this user
            List<Order> listOrders = this.orderRepository.findByUser(user);

            // Delete all order
            this.orderRepository.deleteAll(listOrders);
        }
        // Delete user by id
        this.userRepository.deleteById(id);
    }

    public User saveAvatarInDatabase(String uploadAvatar, long id) {
        User currentUser = this.handleGetUserById(id);
        if (currentUser != null) {
            currentUser.setAvatar(uploadAvatar);
        }
        return this.userRepository.save(currentUser);
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }
    
}
