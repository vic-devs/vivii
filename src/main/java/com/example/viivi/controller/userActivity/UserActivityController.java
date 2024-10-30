package com.example.viivi.controller.userActivity;

import com.example.viivi.models.users.UserModel;
import com.example.viivi.models.users.UserRepository;
import com.example.viivi.service.UserActivityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

    @Controller
    @RequestMapping("/activity")
    public class UserActivityController {

        @Autowired
        private UserActivityService userActivityService;

        @Autowired
        private UserRepository userRepository;

        @PostMapping(value = "/save-activity", consumes = "text/plain")
        @ResponseBody
        public ResponseEntity<String> saveUserActivity(@RequestBody String data, Authentication authentication) {
            System.out.println("Save user activity endpoint triggered");

            Map<String, Object> jsonData = null;
            try {
                jsonData = new ObjectMapper().readValue(data, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            String activityType = jsonData.get("activityType") != null ? (String) jsonData.get("activityType") : "browse";
            Double activityDuration = jsonData.get("activityDuration") != null ? ((Number) jsonData.get("activityDuration")).doubleValue() : null;

            Map<String, Object> filterTypeMap = jsonData.get("filterType") != null ? (Map<String, Object>) jsonData.get("filterType") : null;

            Long productId = jsonData.get("productId") != null ? Long.valueOf(String.valueOf(jsonData.get("productId"))) : null;
            Long productCategoryId = jsonData.get("productCategoryId") != null ? Long.valueOf(String.valueOf(jsonData.get("productCategoryId"))) : null;
            String searchFilter = jsonData.get("searchFilter") != null ? (String) jsonData.get("searchFilter") : null;
            Long orderId = jsonData.get("orderId") != null ? Long.valueOf(String.valueOf(jsonData.get("orderId"))) : null;
            String userEmail = authentication.getName();
            UserModel loggedInUser = userRepository.findByEmail(userEmail);

            Double minPriceFilter = null;
            Double maxPriceFilter = null;
            Long categoryFilter = null;

            if (filterTypeMap != null) {
                if (filterTypeMap.get("min") != null && filterTypeMap.get("min") instanceof Number) {
                    minPriceFilter = ((Number) filterTypeMap.get("min")).doubleValue();
                }

                if (filterTypeMap.get("max") != null && filterTypeMap.get("max") instanceof Number) {
                    maxPriceFilter = ((Number) filterTypeMap.get("max")).doubleValue();
                }

                if (filterTypeMap.get("categories") instanceof List) {
                    List<?> categoryList = (List<?>) filterTypeMap.get("categories");
                    if (!categoryList.isEmpty()) {
                        categoryFilter = Long.valueOf(categoryList.get(0).toString());
                    }
                }
            }

            System.out.println("Activity Type: " + activityType);
            System.out.println("Min Price Filter: " + minPriceFilter);
            System.out.println("Max Price Filter: " + maxPriceFilter);
            System.out.println("Category Filter: " + categoryFilter);

            userActivityService.saveUserActivity(
                    loggedInUser.getId(),
                    productId,
                    activityType,
                    productCategoryId,
                    minPriceFilter,
                    maxPriceFilter,
                    categoryFilter,
                    activityDuration,
                    searchFilter,
                    orderId
            );
            System.out.println(activityType + " interaction logged for user: " + loggedInUser.getId());

            return ResponseEntity.ok("User activity logged successfully");
        }

    }