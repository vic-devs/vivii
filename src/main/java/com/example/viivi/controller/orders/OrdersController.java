package com.example.viivi.controller.orders;

import com.example.viivi.models.orders.OrdersModel;
import com.example.viivi.models.cart.CartItemModel;
import com.example.viivi.models.cart.CartModel;
import com.example.viivi.models.cart.CartRepository;
import com.example.viivi.models.orders.OrderItemsModel;
import com.example.viivi.models.users.UserModel;
import com.example.viivi.models.orders.OrdersRepository;
import com.example.viivi.models.orders.OrderItemsRepository;
import com.example.viivi.models.products.ProductRepository;
import com.example.viivi.models.products.ProductModel;
import com.example.viivi.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ProductRepository productRepository;

    // Show the order summary (cart items)
    @GetMapping("/create")
    public String orderSummary(@AuthenticationPrincipal UserModel user, Model model) {
        Optional<CartModel> cartOptional = cartRepository.findByUserIdWithItems(user.getId());

        if (cartOptional.isPresent()) {
            CartModel cart = cartOptional.get();
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                model.addAttribute("cart", cart);
                model.addAttribute("totalPrice", cart.getTotalPrice());
            } else {
                model.addAttribute("message", "Your cart is empty.");
                return "redirect:/cart"; // Redirect to cart if empty
            }
        } else {
            model.addAttribute("message", "Your cart is empty.");
            return "redirect:/cart";
        }

        return "create-order"; // Display order summary (cart items)
    }

    // Process the checkout and create a new order
    @PostMapping("/checkout")
    public String checkoutOrder(@AuthenticationPrincipal UserModel user) {
        Optional<CartModel> cartOptional = cartRepository.findByUserIdWithItems(user.getId());

        if (cartOptional.isPresent()) {
            CartModel cart = cartOptional.get();
            if (!cart.getItems().isEmpty()) {
                OrdersModel order = new OrdersModel(user, BigDecimal.valueOf(cart.getTotalPrice()), "pending", new java.sql.Timestamp(System.currentTimeMillis()));
                ordersRepository.save(order);

                for (CartItemModel cartItem : cart.getItems()) {
                    ProductModel product = cartItem.getProduct();
                    if (product.getStockQuantity() >= cartItem.getQuantity()) {
                        product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                        productRepository.save(product);

                        OrderItemsModel orderItem = new OrderItemsModel(order, product, cartItem.getQuantity(), cartItem.getPrice());
                        orderItemsRepository.save(orderItem);
                    } else {
                        return "redirect:/cart?error=insufficient_stock";
                    }
                }

                // Clear the user's cart after placing the order
                cart.getItems().clear();
                cartRepository.save(cart);

                // Log user activity for checkout
                userActivityService.saveUserActivity(
                        user.getId(),
                        null, // No specific product ID for the entire checkout action
                        "completed_checkout", // Activity type for checkout
                        null, // No specific product category ID
                        null, // No min price filter
                        null, // No max price filter
                        null, // No category filter
                        null, // Activity duration is not relevant for checkout
                        null, // No search filter
                        order.getId() // Set the order ID for the activity log
                );

                return "redirect:/orders";
            }
        }

        return "redirect:/cart";
    }




    // View all orders for the logged-in user
    @GetMapping
    public String listOrders(@AuthenticationPrincipal UserModel user, Model model) {
        List<OrdersModel> orders = ordersRepository.findByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "orders-list";
    }

    // View a specific order
    @GetMapping("/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model) {
    Optional<OrdersModel> order = ordersRepository.findById(orderId);

    if (order.isPresent()) {
        List<OrderItemsModel> orderItems = orderItemsRepository.findByOrderId(orderId);
        model.addAttribute("order", order.get());
        model.addAttribute("orderItems", orderItems);
    } else {
        // Handle error case where order is not found
        model.addAttribute("error", "Order not found.");
        return "error";  // Ensure you return an appropriate error page template
    }

    return "order-details";  // Make sure this matches the name of your HTML file
}

}
