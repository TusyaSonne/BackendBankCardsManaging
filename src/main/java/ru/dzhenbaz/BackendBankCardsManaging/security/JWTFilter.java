package ru.dzhenbaz.BackendBankCardsManaging.security;


import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.dzhenbaz.BackendBankCardsManaging.service.ClientDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

/**
 * Фильтр для проверки JWT токенов в запросах.
 * Отвечает за аутентификацию пользователей на основе переданных токенов.
 */
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ClientDetailsService clientDetailsService;

    @Autowired
    public JWTFilter(JwtUtil jwtUtil, ClientDetailsService clientDetailsService) {
        this.jwtUtil = jwtUtil;
        this.clientDetailsService = clientDetailsService;
    }

    /**
     * Проверяет наличие и валидность JWT токена в каждом HTTP-запросе.
     * Если токен валидный, устанавливает аутентификацию в {@link SecurityContextHolder}.
     *
     * @param request     входящий HTTP-запрос
     * @param response    исходящий HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException в случае ошибки обработки запроса
     * @throws IOException      в случае ошибки ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // В случае, если аутентификация еще не прошла - не проверяем токен
        String path = request.getServletPath();
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (jwt.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid JWT Token in Bearer Header");
            } else {
                try {
                    String username = jwtUtil.validateTokenAndRetrieveClaim(jwt);
                    UserDetails userDetails = clientDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                            userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (JWTVerificationException exception) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid JWT Token");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
