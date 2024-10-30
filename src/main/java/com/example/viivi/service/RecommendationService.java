package com.example.viivi.service;

import com.example.viivi.models.category.CategoryModel;
import com.example.viivi.models.category.CategoryRepository;
import com.example.viivi.models.products.ProductPhotosModel;
import com.example.viivi.models.products.ProductPhotosRepository;
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
    private CategoryRepository categoryRepository;

    private static final String API_URL = "http://127.0.0.1:7000";

    public List<RecommendationDto> getPopularProductsForUser(int userId, int numRecommendations) {
        return fetchRecommendations(userId, numRecommendations, "/recommend/popular");
    }

    public List<RecommendationDto> getSimilarOrderedProductRecommendations(int userId, int numRecommendations) {
        String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/recommend/orders")
                .queryParam("user_id", userId)
                .queryParam("n", numRecommendations)
                .toUriString();

        return fetchRecommendationsFromUrl(url);
    }


    public List<RecommendationDto> getMetadataBasedRecommendations(int userId, int numRecommendations) {
        return fetchRecommendations(userId, numRecommendations, "/recommend/metadata");
    }

    public List<RecommendationDto> getProfileBasedRecommendations(int userId, int numRecommendations) {
        return fetchRecommendations(userId, numRecommendations, "/recommend/profile");
    }

    public List<RecommendationDto> getHybridRecommendations(int userId, int numRecommendations, double knnWeight, double svdWeight) {
        String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/recommend/hybrid")
                .queryParam("user_id", userId)
                .queryParam("n", numRecommendations)
                .queryParam("weights", knnWeight + "," + svdWeight)
                .toUriString();

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
        ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<RecommendationDto> recommendations = new ArrayList<>();
        List<Map<String, Object>> recommendationData = responseEntity.getBody();

        if (recommendationData != null) {
            for (Map<String, Object> item : recommendationData) {
                RecommendationDto dto = new RecommendationDto();

                // Safely set each field with null checks
                dto.setProductId(item.get("product_id") != null ? ((Number) item.get("product_id")).intValue() : null);
                dto.setName((String) item.getOrDefault("name", "Unknown Product"));
                dto.setPrice(item.get("price") != null ? ((Number) item.get("price")).doubleValue() : null);
                dto.setCategoryId(item.get("category_id") != null ? ((Number) item.get("category_id")).intValue() : null);
                dto.setPredictedScore(item.get("predicted_score") != null ? ((Number) item.get("predicted_score")).doubleValue() : null);

                double interactionValue = item.get("interaction_value") != null ? ((Number) item.get("interaction_value")).doubleValue() : 0.0;
                if (interactionValue > 1) {
                    interactionValue = interactionValue / 100;
                }
                dto.setInteractionValue(interactionValue);



                // Fetch primary photo URL using the product ID, if available
                if (dto.getProductId() != null) {
                    ProductPhotosModel primaryPhoto = productPhotosRepository.findByProductIdAndIsPrimaryTrue((long) dto.getProductId());
                    if (primaryPhoto != null) {
                        dto.setImageUrl(primaryPhoto.getPhotoUrl());
                    }
                }

                // Fetch category name using category ID, if available
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
