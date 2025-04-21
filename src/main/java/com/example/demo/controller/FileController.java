package com.example.demo.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.User;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import com.example.demo.util.exception.StorageException;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${tomosia.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;
    private final UserService userService;

    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping("/download/files")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws StorageException, URISyntaxException, FileNotFoundException {

        // check file or folder exist
        if (fileName == null) {
            throw new StorageException("Missing request param: (fileName or folder not exist)");
        }

        // check file exist (and not a directory) and get file length
        long fileLength = this.fileService.getFileLength(fileName, baseURI, folder);
        if (fileLength == 0) {
            throw new StorageException("File with name = " + fileName + " not found");
        }

        // download single file
        // Lấy nội dung file dưới dạng InputStreamResource và truyền file từ server tới client
        InputStreamResource resource = this.fileService.getResource(fileName, baseURI, folder);  

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/users/export/files")
    public ResponseEntity<byte[]> exportUsersToExcel() throws IOException {
        // Step 1: Get All User from database
        List<User> users = this.userService.getAllUsers();

        // Step 2: Convert List Users into file Excel as byte[]
        byte[] excelData = this.fileService.exportExcelFile(users);

        String fileName = "users_" + LocalDate.now() + ".xlsx"; // Xác định tên và loại tệp tải xuống

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)   // attachment: Yêu cầu trình duyệt tải file
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Định dạng nội dung phản hồi là 1 file nhị phân
                .body(excelData);
      
    }
}
