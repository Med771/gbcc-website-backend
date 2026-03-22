package backend.website.gbcc.web;

import backend.website.gbcc.config.SecurityConfig;
import backend.website.gbcc.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Срез MockMvc: {@link SecurityConfig}, {@link GlobalExceptionHandler}, JWT-фильтр через {@link ControllerTestSupport},
 * авто-конфигурация servlet security (bean {@code HttpSecurity}). Для неаутентифицированного доступа к защищённым путям
 * Spring Security в тестах обычно отдаёт 403 (а не 401).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WebMvcTest
@ImportAutoConfiguration(classes = {ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public @interface GbccWebMvcTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
