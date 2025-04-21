package com.example.demo.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

import org.apache.catalina.connector.Response;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.domain.Product;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.domain.response.file.ResUploadFileDTO;
import com.example.demo.service.FileService;
import com.example.demo.service.ProductService;
import com.example.demo.util.exception.InvalidException;
import com.example.demo.util.exception.StorageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    // Dependency Inject: Constructor
    private final ProductService productService;
    private final FileService fileService;

    // Inject ObjectMapper vào Controller
    private final ObjectMapper objectMapper;

    @Value("${tomosia.upload-file.base-uri}")
    private String baseURI;

    public ProductController(ProductService productService, FileService fileService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/products", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Product> createNewProduct(@RequestPart("product") String productString,
            @RequestPart(name = "file", required = false) MultipartFile file, @RequestParam("folder") String folder)
            throws StorageException, URISyntaxException, IOException {

        // Convert JSON String into Product Object
        Product createProduct = objectMapper.readValue(productString, Product.class);

        // create a directory if not exist
        this.fileService.createDirectory(baseURI + folder);

        // Upload Product Image and Get Image URL
        String uploadImageUrl = this.fileService.uploadFile(baseURI + folder, file);

        // Set URL ảnh vào Product
        createProduct.setImage(uploadImageUrl);

        // Save Product in database
        Product product = this.productService.handleCreateProduct(createProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping(value = "/products", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Product> updateProduct(@RequestPart("product") String productString,
            @RequestPart(name = "file", required = false) MultipartFile file, @RequestParam("folder") String folder)
            throws IOException, URISyntaxException, InvalidException {

        // Convert JSON String into Product Object
        Product requestProduct = objectMapper.readValue(productString, Product.class);

        Product currentProduct = this.productService.handleGetProducById(requestProduct.getId());
        if (currentProduct == null) {
            throw new InvalidException("Product with id = " + requestProduct.getId() + " not exist");
        }

        // create a directory if not exist
        this.fileService.createDirectory(baseURI + folder);

        // Upload Product Image and Get Image URL
        String uploadImageUrl = this.fileService.uploadFile(baseURI + folder, file);

        // Set URL ảnh vào Product
        currentProduct.setImage(uploadImageUrl);

        // Save Product in database
        Product product = this.productService.handleUpdateProduct(currentProduct, requestProduct);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(product);
    }

    @PutMapping(value = "/products/upload/image", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ResUploadFileDTO> upload(@RequestParam("productId") long productId,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws StorageException, URISyntaxException, IOException {

        // Check file is empty
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // create a directory if not exist
        this.fileService.createDirectory(baseURI + folder);

        // upload product image
        String uploadImageUrl = this.fileService.uploadFile(baseURI + folder, file);

        // save imageUrl in database
        Product updatedProduct = this.productService.saveProductImage(uploadImageUrl, productId);

        ResUploadFileDTO res = new ResUploadFileDTO();
        res.setFileName(updatedProduct.getImage());
        res.setUploadedAt(Instant.now());

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") long id) throws InvalidException {
        Product product = this.productService.handleGetProducById(id);
        if (product == null) {
            throw new InvalidException("Product with id = " + id + " not exist.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping("/products")
    public ResponseEntity<ResultPaginationDTO> getAllProducts(@Filter Specification<Product> spec, Pageable pageable) {
        ResultPaginationDTO result = this.productService.fetchAllProductsWithPagination(spec, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") long id) throws InvalidException {
        Product currentProduct = this.productService.handleGetProducById(id);
        if (currentProduct == null) {
            throw new InvalidException("Product with id = " + id + " not exist");
        }
        this.productService.handleDeleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body("Delete Success");
    }

}
