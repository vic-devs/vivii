package com.example.viivi.service;

public class RecommendationDto {

    private Integer productId;
    private String name;
    private Double price;
    private Integer categoryId;
    private String categoryName;
    private Double predictedScore;
    private String imageUrl;  // New field for product image URL
    private double interactionValue;

    // Getters and setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    // Getters and setters for categoryName
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(Double predictedScore) {
        this.predictedScore = predictedScore;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getInteractionValue() {
        return interactionValue;
    }

    // Setter for interactionValue
    public void setInteractionValue(double interactionValue) {
        this.interactionValue = interactionValue;
    }
}
