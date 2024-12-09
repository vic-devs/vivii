package com.example.viivi.service;

import com.example.viivi.models.category.CategoryModel;
import com.example.viivi.models.category.CategoryRepository;
import com.example.viivi.models.products.ProductModel;
import com.example.viivi.models.products.ProductPhotosModel;
import com.example.viivi.models.products.ProductPhotosRepository;
import com.example.viivi.models.products.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class RecommendationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProductPhotosRepository productPhotosRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String API_URL = "http://127.0.0.1:7000";

//    public List<RecommendationDto> getPopularProductsForUser(int userId, int numRecommendations) {
//        return fetchRecommendations(userId, numRecommendations, "/recommend/popular");
//    }
//
//    public List<RecommendationDto> getSimilarOrderedProductRecommendations(int userId, int numRecommendations) {
//        String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/recommend/orders")
//                .queryParam("user_id", userId)
//                .queryParam("n", numRecommendations)
//                .toUriString();
//
//        return fetchRecommendationsFromUrl(url);
//    }
//
//
//    public List<RecommendationDto> getMetadataBasedRecommendations(int userId, int numRecommendations) {
//        return fetchRecommendations(userId, numRecommendations, "/recommend/metadata");
//    }
//
//    public List<RecommendationDto> getProfileBasedRecommendations(int userId, int numRecommendations) {
//        return fetchRecommendations(userId, numRecommendations, "/recommend/profile");
//    }

    public List<RecommendationDto> getHybridRecommendations(int userId, int numRecommendations, double boostFactor) {
        // Build the URL dynamically with required query parameters
        String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/recommend/hybrid")
                .queryParam("user_id", userId)
                .queryParam("top_n", numRecommendations)
                .queryParam("boost_factor", boostFactor)
                .toUriString();

        // Fetch recommendations from the Flask API
        return fetchRecommendationsFromUrl(url);
    }

    private List<RecommendationDto> fetchRecommendations(int userId, int numRecommendations, String endpoint) {
        String url = UriComponentsBuilder.fromHttpUrl(API_URL + endpoint)
                .queryParam("user_id", userId)
                .queryParam("n", numRecommendations)
                .toUriString();

        return fetchRecommendationsFromUrl(url);
    }

    private List<RecommendationDto> fetchRecommendationsFromUrl(String url) {
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        List<RecommendationDto> recommendations = new ArrayList<>();
        Map<String, Object> recommendationData = responseEntity.getBody();

        if (recommendationData != null && recommendationData.containsKey("recommendations")) {
            List<Map<String, Object>> recommendationList = (List<Map<String, Object>>) recommendationData.get("recommendations");
            for (Map<String, Object> item : recommendationList) {
                RecommendationDto dto = new RecommendationDto();

                dto.setProductId(item.get("product_id") != null ? ((Number) item.get("product_id")).intValue() : null);
                dto.setCategoryId(item.get("category_id") != null ? ((Number) item.get("category_id")).intValue() : null);
                dto.setPredictedScore(item.get("score") != null ? ((Number) item.get("score")).doubleValue() : null);

                // Fetch product details using product ID
                if (dto.getProductId() != null) {
                    ProductModel product = productRepository.findById((long) dto.getProductId()).orElse(null);
                    if (product != null) {
                        dto.setName(product.getName());

                        // Convert BigDecimal to Double
                        if (product.getPrice() != null) {
                            dto.setPrice(product.getPrice().doubleValue());
                        }

                        // Fetch the primary photo for the product
                        ProductPhotosModel primaryPhoto = productPhotosRepository.findByProductIdAndIsPrimaryTrue((long) dto.getProductId());
                        if (primaryPhoto != null) {
                            dto.setImageUrl(primaryPhoto.getPhotoUrl());
                        } else {
                            // Fallback URL if no image is available
                            dto.setImageUrl("/images/default-image.jpg");
                        }
                    }
                }

                // Fetch category name using category ID
                if (dto.getCategoryId() != null) {
                    CategoryModel category = categoryRepository.findById((long) dto.getCategoryId()).orElse(null);
                    if (category != null) {
                        dto.setCategoryName(category.getName());
                    }
                }

                recommendations.add(dto);
            }
        }

        return recommendations;
    }
}
