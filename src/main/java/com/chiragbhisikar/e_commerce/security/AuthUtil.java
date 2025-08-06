package com.chiragbhisikar.e_commerce.security;


import com.chiragbhisikar.e_commerce.model.User;
import com.chiragbhisikar.e_commerce.model.auth.AuthProviderType;
import com.chiragbhisikar.e_commerce.model.auth.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Component
public class AuthUtil {
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /* Generate Token */
    public String generateToken(User user, String tokenType) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(
                        tokenType == "AccessToken"
                                ? new Date(System.currentTimeMillis() + 1000 * 60 * 10) // For
                                : new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30 * 6) // For 6 months
                )
                .signWith(getSecretKey())
                .compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, "AccessToken");
    }

    public String generateRefreshToken(User user) {    //create refresh token
        return generateToken(user, "RefreshToken");
    }
    /* Generate Token End */

    /* Generate User Information From Token */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long generateUserIdFromToken(String token) {
        return Long.valueOf(extractAllClaims(token).getSubject());
    }

    public String generateUsernameFromToken(String token) {
        return extractAllClaims(token).get("username").toString();
    }

    public Set<RoleType> generateUserRolesFromToken(String token) {
        return (Set<RoleType>) extractAllClaims(token).get("roles");
    }

    public Date generateExpirationTimeFromToken(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        return generateExpirationTimeFromToken(token).before(new Date());
    }
    /* Generate User Information From End */


    public AuthProviderType getProviderTypeFromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            case "facebook" -> AuthProviderType.FACEBOOK;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();

            default -> {
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };

        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Unable to determine providerId for OAuth2 login");
        }
        return providerId;
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }
}
