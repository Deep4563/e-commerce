package com.chiragbhisikar.e_commerce.service;

import com.chiragbhisikar.e_commerce.dto.AuthenticationRequestDto;
import com.chiragbhisikar.e_commerce.dto.LoginResponseDto;
import com.chiragbhisikar.e_commerce.exception.AlreadyExistException;
import com.chiragbhisikar.e_commerce.exception.NotFoundException;
import com.chiragbhisikar.e_commerce.model.User;
import com.chiragbhisikar.e_commerce.model.RefreshToken;
import com.chiragbhisikar.e_commerce.model.auth.RoleType;
import com.chiragbhisikar.e_commerce.repository.RefreshTokenRepository;
import com.chiragbhisikar.e_commerce.repository.UserRepository;
import com.chiragbhisikar.e_commerce.security.AuthUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(AuthenticationRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        // generation access & refresh token for user authentication
        String accessToken = authUtil.generateAccessToken(user);
        String refreshToken = authUtil.generateRefreshToken(user);

        // finding refresh token for specific user
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId()).orElse(null);

        // if user it not present in refresh_token_user then inserting it in refresh_token_user table
        if (refreshTokenEntity == null) {
            refreshTokenEntity = RefreshToken.builder()
                    .user(user)
                    .token(refreshToken)
                    .expiryDate(authUtil.generateExpirationTimeFromToken(refreshToken).toInstant())
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);
        }
//        // if user is present in refresh_token_user then updating it in refresh_token_user table
        else {
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setExpiryDate(authUtil.generateExpirationTimeFromToken(refreshToken).toInstant());

            refreshTokenRepository.save(refreshTokenEntity);
        }

        // generating response
        return LoginResponseDto.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponseDto signUp(AuthenticationRequestDto signRequestDto) throws AlreadyExistException {
        User user = userRepository.findByUsername(signRequestDto.getUsername()).orElse(null);

        if (user != null)
            throw new AlreadyExistException("User already exists");

        user = User.builder()
                .username(signRequestDto.getUsername())
                .roles(Collections.singleton(RoleType.USER))
                .password(passwordEncoder.encode(signRequestDto.getPassword()))
                .build();

        userRepository.save(user);

        return login(signRequestDto);
    }

    public LoginResponseDto refreshToken(String refreshToken) throws NotFoundException, ExpiredJwtException {
        String username = authUtil.generateUsernameFromToken(refreshToken);  //refresh token is valid

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User Not Found With " + username + " Email !"));

        RefreshToken refreshTokenUser = refreshTokenRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("Refresh Token Not Found"));

        if (refreshTokenUser.getToken().equals(refreshToken)) {
            String accessToken = authUtil.generateAccessToken(user);
            return new LoginResponseDto(user.getUsername(), accessToken, refreshToken);
        }

        throw new BadCredentialsException("Invalid Refresh Token !");
    }
}
