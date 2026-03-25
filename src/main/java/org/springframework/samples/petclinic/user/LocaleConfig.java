package org.springframework.samples.petclinic.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

	@Bean
	public LocaleResolver localeResolver() {
		// Creates a cookie named "PREFERRED_LANGUAGE"
		CookieLocaleResolver resolver = new CookieLocaleResolver("PREFERRED_LANGUAGE");
		resolver.setDefaultLocale(Locale.ENGLISH);
		resolver.setCookieMaxAge(Duration.ofDays(365)); // Remembers them for a year!
		return resolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		// This tells Spring to watch the address bar for "?lang="
		interceptor.setParamName("lang");
		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
}
