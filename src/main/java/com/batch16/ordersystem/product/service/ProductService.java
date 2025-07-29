package com.batch16.ordersystem.product.service;

import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.repository.MemberRepository;
import com.batch16.ordersystem.product.domain.Product;
import com.batch16.ordersystem.product.dto.ProductCreateDto;
import com.batch16.ordersystem.product.dto.ProductResDto;
import com.batch16.ordersystem.product.dto.ProductSearchDto;
import com.batch16.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public Long save(ProductCreateDto productCreateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        Product product = productRepository.save(productCreateDto.toEntity(member));

        if (productCreateDto.getProductImage() != null) {
            String fileName = "product-" + product.getId() + "-productImage-" + productCreateDto.getProductImage().getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType()) //image/jpeg, video/mp4 ...
                    .build();

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (Exception e) {
                e.printStackTrace(); // 예외 원인 콘솔에 출력
                throw new IllegalArgumentException("프로필 이미지 업로드 중 오류가 발생했습니다.", e);
            }

            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);
        }
        return product.getId();
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto){
        System.out.println(productSearchDto);
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                Root : 엔티티의 속성을 접근하기 위한 객체, CriteriaBuilder : 쿼리를 생성하기 위한 객체
                List<Predicate> predicateList = new ArrayList<>();
                if(productSearchDto.getCategory() !=null){
                    predicateList.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if(productSearchDto.getName() !=null){
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%"+productSearchDto.getName()+"%"));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i=0; i<predicateList.size(); i++){
                    predicateArr[i] = predicateList.get(i);
                }
//                위의 검색 조건들을 하나(한줄)의 Predicate객체로 만들어서 return
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(p->ProductResDto.fromEntity(p));
    }

    public ProductResDto findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보없음"));
        return ProductResDto.fromEntity(product);
    }

}
