package com.traazu.auth_service.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, StaffRepository staffRepository, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final TokenInfos tokenInfos;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);

            tokenInfos = jwtUtil.getTokenInfos(jwt);
            
            if (!tokenInfos.isValid()) {
                throw new IllegalArgumentException("invalid token: missing claims!");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                BaseUser baseUser = findUser(tokenInfos);
                if (baseUser == null) {
                    throw new IllegalAccessError("user not found!");
                }
                CustomUserDetails customUserDetails = new CustomUserDetails(baseUser);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + tokenInfos.role()));
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                                        customUserDetails,
                                                                        null,
                                                                        authorities
                                                                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));                                                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch(MalformedJwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Malformed token!");
            logger.error("Malformed JWT: {}", e);
            return;
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired!");
            return;
            
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token signature!");
            logger.error("Invalid JWT signature: {}", e);
            return;
            
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("invalid token!");
            logger.error("Cannot set user authentication from JWT: {}", e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private BaseUser findUser(TokenInfos tokenInfos) {
        BaseUser baseUser = null;
        String role = tokenInfos.role();
        if (role.contains(UserRole.USER.name())) {
            return userRepository.findById(tokenInfos.id()).orElse(null);
        }
        if (role.contains(UserRole.SUPPORT.name()) || role.contains(UserRole.ADMIN.name())) {
            return staffRepository.findById(tokenInfos.id()).orElse(null);
        }
        return baseUser;
    }
    
}
