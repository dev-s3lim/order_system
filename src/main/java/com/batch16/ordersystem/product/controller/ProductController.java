package com.batch16.ordersystem.product.controller;

import com.batch16.ordersystem.common.dto.CommonDto;
import com.batch16.ordersystem.product.dto.ProductCreateDto;
import com.batch16.ordersystem.product.dto.ProductResDto;
import com.batch16.ordersystem.product.dto.ProductSearchDto;
import com.batch16.ordersystem.product.dto.ProductUpdateDto;
import com.batch16.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@ModelAttribute ProductCreateDto productCreateDto){
        Long id = productService.save(productCreateDto);
        return  new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.CREATED.value())
                        .statusMessage("상품등록완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(Pageable pageable, ProductSearchDto productSearchDto){
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, productSearchDto);
        return  new ResponseEntity<>(
                CommonDto.builder()
                        .result(productResDtoList)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("상품목록조회성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        ProductResDto productResDto = productService.findById(id);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(productResDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("상품상세조회성공")
                        .build(),
                HttpStatus.OK
        );
    }

    // 이미 등록된 상품 수정을 위한 컨트롤러
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> productUpdate(@PathVariable Long id, @ModelAttribute ProductUpdateDto dto) {
        Long updatedId = productService.productUpdate(id, dto);
        return ResponseEntity.ok(
                CommonDto.builder()
                        .result(updatedId)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("상품수정완료")
                        .build()
        );
    }
}