package com.careertrack.controller;

import com.careertrack.model.User;
import com.careertrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/mark-featured")
    public ResponseEntity<?> markFeatured(@RequestBody Map<String, Object> data) {
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            var opt = userRepository.findById(userId);
            if (opt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            User user = opt.get();
            user.setFeatured(true);
            user.setFeaturedExpiresAt(LocalDateTime.now().plusDays(30));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Vous êtes maintenant en suggestion pour 30 jours"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
