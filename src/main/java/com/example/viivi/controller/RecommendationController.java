package com.example.viivi.controller;

import com.example.viivi.models.users.UserModel;
import com.example.viivi.service.RecommendationDto;
import com.example.viivi.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/v1")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public String getRecommendations(
            @RequestParam(defaultValue = "10") int n,
            @RequestParam(defaultValue = "false") boolean isContentBased,
            @RequestParam(defaultValue = "1.2") double boostFactor,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = (UserModel) authentication.getPrincipal();  // Cast to UserModel
        Long userId = user.getId();

        // Fetch different types of recommendations
//        List<RecommendationDto> popularProducts = recommendationService.getPopularProductsForUser(userId.intValue(), n);
//        List<RecommendationDto> categoryRecommendations = recommendationService.getSimilarOrderedProductRecommendations(userId.intValue(), n);// Example category IDs
//        List<RecommendationDto> metadataRecommendations = recommendationService.getMetadataBasedRecommendations(userId.intValue(), n);
//        List<RecommendationDto> profileRecommendations = recommendationService.getProfileBasedRecommendations(userId.intValue(), n);
        List<RecommendationDto> hybridRecommendations = recommendationService.getHybridRecommendations(userId.intValue(), n, boostFactor);

        // Add data to the model
//        model.addAttribute("popularProducts", popularProducts);
//        model.addAttribute("categoryRecommendations", categoryRecommendations);
//        model.addAttribute("metadataRecommendations", metadataRecommendations);
//        model.addAttribute("profileRecommendations", profileRecommendations);
        model.addAttribute("hybridRecommendations", hybridRecommendations);
        model.addAttribute("userId", userId);

        return "recommendations";
    }
}
