package com.chiragbhisikar.e_commerce.controller;

import com.chiragbhisikar.e_commerce.dto.ApiResponse;
import com.chiragbhisikar.e_commerce.dto.AuthenticationRequestDto;
import com.chiragbhisikar.e_commerce.dto.LoginResponseDto;
import com.chiragbhisikar.e_commerce.exception.AlreadyExistException;
import com.chiragbhisikar.e_commerce.exception.NotFoundException;
import com.chiragbhisikar.e_commerce.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthenticationRequestDto signInRequestDto, HttpServletRequest request,
                                             HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(signInRequestDto);

        /*
        Cookie Security: By setting the refresh token as an HTTP-only cookie, you protect it from XSS attacks.
        Consider also setting the Secure flag if your application uses HTTPS
        .*/
        Cookie cookie = new Cookie("refreshToken", loginResponseDto.getRefreshToken());
        cookie.setHttpOnly(true); // Prevents client-side scripts from accessing the cookie
        response.addCookie(cookie); // Http only cookies can be passed from backend to frontend only

        return new ResponseEntity(loginResponseDto, HttpStatus.ACCEPTED);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody AuthenticationRequestDto signUpRequestDto) throws AlreadyExistException {
        return new ResponseEntity(authService.signUp(signUpRequestDto), HttpStatus.ACCEPTED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(HttpServletRequest request) throws NotFoundException, ExpiredJwtException {
        /*
        Cookie Security: By setting the refresh token as an HTTP-only cookie, you protect it from XSS attacks.
        Consider also setting the Secure flag if your application uses HTTPS
        */
        if (request.getCookies() == null || request.getCookies().length == 0) {
            throw new NotFoundException("Refresh Token Not Found!");
        }

        String refreshToken = Arrays.stream(request.getCookies()) //getCookies() method returns a array of cookie
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new NotFoundException("Refresh Token Not Found!"));

        LoginResponseDto loginResponseDto = authService.refreshToken(refreshToken);
        return new ResponseEntity(loginResponseDto, HttpStatus.ACCEPTED);
    }
}