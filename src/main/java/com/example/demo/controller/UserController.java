package com.example.demo.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.domain.User;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.domain.response.user.ResUpdateUserDTO;
import com.example.demo.domain.response.user.ResUserDTO;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import com.example.demo.util.exception.InvalidException;
import com.example.demo.util.exception.StorageException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    // Dependency Inject: Constructor
    private final UserService userService;
    private final FileService fileService;

    @Value("${tomosia.upload-file.base-uri}")
    private String baseURI;

    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @PostMapping("/users")
    public ResponseEntity<ResUserDTO> createNewUser(@Valid @RequestBody User createUser) throws InvalidException {
        // check email exist in database
        boolean isEmailExist = this.userService.checkEmailExist(createUser.getEmail());
        if (isEmailExist == true) {
            throw new InvalidException("Email: " + createUser.getEmail() + " is exist");
        }

        User newUser = this.userService.handleCreateUser(createUser);
        ResUserDTO userDTO = this.userService.convertToResUserDTO(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws InvalidException {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new InvalidException("User with id = " + id + " not exist");
        }
        ResUserDTO userDTO = this.userService.convertToResUserDTO(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @GetMapping("/users")
    ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsersWithPagination(spec, pageable));
    }

    @PutMapping("/users")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@Valid @RequestBody User requestUser) throws InvalidException {
        User userUpdate = this.userService.handleUpdateUser(requestUser);
        if (userUpdate == null) {
            throw new InvalidException("User with id = " + requestUser.getId() + " not exist");
        }

        ResUpdateUserDTO userDTO = this.userService.convertToResUpdateUserDTO(userUpdate);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userDTO);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) throws InvalidException {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new InvalidException("User with id = " + id + " not exist");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("Delete User Success");
    }

    @PutMapping(value = "/users/{id}/avatar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ResUpdateUserDTO> upload(@PathVariable("id") long id,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws InvalidException, URISyntaxException, IOException, StorageException {
        // check user id exist
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new InvalidException("User with id = " + id + " not exist");
        }

        // Check file is empty
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // create a directory if not exist
        this.fileService.createDirectory(baseURI + folder);

        // store avatar
        String uploadAvatar = this.fileService.uploadFile(baseURI + folder, file);

        // save avatarUrl in database
        User updateUserAvatar = this.userService.saveAvatarInDatabase(uploadAvatar, id);

        // convert to ResUploadAvatarDTO
        ResUpdateUserDTO res = this.userService.convertToResUpdateUserDTO(updateUserAvatar);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
