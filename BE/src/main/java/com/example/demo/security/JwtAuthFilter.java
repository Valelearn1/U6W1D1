package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.RevokedTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            if (jwtService.isValid(token) && !revokedTokenRepository.existsByJti(jwtService.extractJti(token))) {
                String email = jwtService.extractEmail(token);
                Optional<User> user = userRepository.findByEmail(email);
                // Un account disattivato non deve poter usare un token emesso in precedenza
                user.filter(User::isActive).ifPresent(u -> {
                    var authentication = new UsernamePasswordAuthenticationToken(u, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        chain.doFilter(request, response);
    }
}
