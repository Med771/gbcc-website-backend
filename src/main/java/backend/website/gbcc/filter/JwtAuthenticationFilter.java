package backend.website.gbcc.filter;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.helper.AuthCookieHelper;
import backend.website.gbcc.helper.JwtTokenHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenHelper jwtTokenHelper;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = readCookieValue(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenHelper.parseClaims(token);
            String subject = claims.getSubject();

            if (subject == null || subject.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            UUID accountId = UUID.fromString(subject);
            AccountEntity account = accountRepository.findById(accountId).orElse(null);

            if (account == null || Boolean.TRUE.equals(account.getIsBlocked())) {
                filterChain.doFilter(request, response);
                return;
            }

            AccountPrincipal principal = new AccountPrincipal(account.getId(), account.getRole());

            String roleAuthority = "ROLE_" + account.getRole().name();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(roleAuthority))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String readCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthCookieHelper.ACCESS_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
