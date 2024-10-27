package com.example.viivi.models.users;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.viivi.models.cart.CartModel;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Entity
@Table(name = "users")
public class UserModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = true)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // New field for cart association
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CartModel cart;

    // Fields for top categories
    @Column(name = "top_category1", nullable = true)
    private Long topCategory1;

    @Column(name = "top_category2", nullable = true)
    private Long topCategory2;

    @Column(name = "top_category3", nullable = true)
    private Long topCategory3;

    // Constructors
    public UserModel() {}

    public UserModel(String email, String password, String fullName, Role role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Getters and setters for the cart
    public CartModel getCart() {
        return cart;
    }

    public void setCart(CartModel cart) {
        this.cart = cart;
    }

    // Getters and setters for top categories
    public Long getTopCategory1() {
        return topCategory1;
    }

    public void setTopCategory1(Long topCategory1) {
        this.topCategory1 = topCategory1;
    }

    public Long getTopCategory2() {
        return topCategory2;
    }

    public void setTopCategory2(Long topCategory2) {
        this.topCategory2 = topCategory2;
    }

    public Long getTopCategory3() {
        return topCategory3;
    }

    public void setTopCategory3(Long topCategory3) {
        this.topCategory3 = topCategory3;
    }

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Always true as we're not tracking expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Always true as we're not tracking locked accounts
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Always true as we're not tracking credential expiration
    }

    @Override
    public boolean isEnabled() {
        return true;  // Always true unless you want to add this functionality
    }

    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(id, userModel.id) &&
               Objects.equals(email, userModel.email) &&
               Objects.equals(fullName, userModel.fullName) &&
               role == userModel.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, fullName, role);
    }

    @Override
    public String toString() {
        return "UserModel{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", fullName='" + fullName + '\'' +
               ", role=" + role +
               '}';
    }
}
