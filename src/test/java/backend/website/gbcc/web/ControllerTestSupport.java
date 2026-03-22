package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.model.AccountPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    /**
     * JWT-фильтр в тестах не парсит cookie: просто передаёт запрос дальше по цепочке (как в интеграции с реальным токеном).
     */
    public static void stubJwtFilterPassThrough(JwtAuthenticationFilter jwtAuthenticationFilter) {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                try {
                    FilterChain chain = invocation.getArgument(2);
                    chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                } catch (IOException | ServletException e) {
                    throw new IllegalStateException(e);
                }
                return null;
            }
        };
        try {
            lenient().doAnswer(answer).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static org.springframework.test.web.servlet.request.RequestPostProcessor principal(AccountPrincipal principal) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
