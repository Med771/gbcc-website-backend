package backend.website.gbcc.filter;

import backend.website.gbcc.config.property.JwtProperty;
import backend.website.gbcc.helper.JwtTokenHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.model.AccountRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import backend.website.gbcc.model.AccountPrincipal;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static backend.website.gbcc.helper.AuthCookieHelper.ACCESS_COOKIE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        JwtProperty jwtProperty = new JwtProperty();
        jwtProperty.setSecret("0123456789abcdef0123456789abcdef");
        JwtTokenHelper jwtTokenHelper = new JwtTokenHelper(jwtProperty);
        filter = new JwtAuthenticationFilter(jwtTokenHelper, accountRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinue_whenNoCookie() throws Exception {
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenValidTokenAndAccount() throws Exception {
        JwtProperty jwtProperty = new JwtProperty();
        jwtProperty.setSecret("0123456789abcdef0123456789abcdef");
        JwtTokenHelper jwtTokenHelper = new JwtTokenHelper(jwtProperty);
        filter = new JwtAuthenticationFilter(jwtTokenHelper, accountRepository);

        UUID accountId = UUID.randomUUID();
        String token = jwtTokenHelper.createAccessToken(accountId, AccountRole.CUSTOMER);

        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setRole(AccountRole.CUSTOMER);
        account.setIsBlocked(false);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(ACCESS_COOKIE, token)});
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(AccountPrincipal.class);
        assertThat(((AccountPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).accountId())
                .isEqualTo(accountId);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAccountBlocked() throws Exception {
        JwtProperty jwtProperty = new JwtProperty();
        jwtProperty.setSecret("0123456789abcdef0123456789abcdef");
        JwtTokenHelper jwtTokenHelper = new JwtTokenHelper(jwtProperty);
        filter = new JwtAuthenticationFilter(jwtTokenHelper, accountRepository);

        UUID accountId = UUID.randomUUID();
        String token = jwtTokenHelper.createAccessToken(accountId, AccountRole.CUSTOMER);

        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setIsBlocked(true);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(ACCESS_COOKIE, token)});
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
