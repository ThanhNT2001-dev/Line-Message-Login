package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.OrderDetail;
import com.example.demo.domain.Product;
import com.example.demo.domain.response.Meta;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
    // Dependency Inject: Constructor
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product handleGetProducById(long id) {
        Optional<Product> productOptional = this.productRepository.findById(id);
        if (productOptional.isPresent()) {
            return productOptional.get();
        }
        return null;
    }

    public Product handleCreateProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Product saveProductImage(String uploadImageUrl, long productId) {
        Product product = this.handleGetProducById(productId);
        product.setImage(uploadImageUrl);

        return this.productRepository.save(product);
    }

    public ResultPaginationDTO fetchAllProductsWithPagination(Specification<Product> spec, Pageable pageable) {
        Page<Product> pageProduct = this.productRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageProduct.getTotalPages());
        meta.setTotal(pageProduct.getTotalElements());

        result.setMeta(meta);
        result.setResult(pageProduct.getContent());

        return result;
    }

    public Product handleUpdateProduct(Product currentProduct, Product requestProduct) {
        currentProduct.setName(requestProduct.getName());
        currentProduct.setQuantity(requestProduct.getQuantity());
        currentProduct.setPrice(requestProduct.getPrice());
        currentProduct.setDetailDesc(requestProduct.getDetailDesc());
        currentProduct.setBrand(requestProduct.getBrand());

        return this.productRepository.save(currentProduct);
    }

    public void handleDeleteProduct(long id) {
        Product currentProduct = this.handleGetProducById(id);
        List<OrderDetail> orderDetails = currentProduct.getOrderDetails();
        for (OrderDetail orderDetail : orderDetails) {
            // Delete Order-detail
            this.orderDetailRepository.deleteById(orderDetail.getId());
        }

        // Delete product
        this.productRepository.deleteById(id);
    }

}
