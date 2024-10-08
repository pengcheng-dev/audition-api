package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebServiceConfiguration implements WebMvcConfigurer {

    private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceConfiguration.class);
    @Autowired
    private transient AuditionLogger logger;

    @Autowired
    private transient ResponseHeaderInjector responseHeaderInjector;

    @Autowired
    private transient ResponseMetricsInjector responseMetricsInjector;

    @Autowired
    private transient RequestLoggingInjector requestLoggingInjector;

    /**
     * Add three injectors for a request, including:
     * 1. response header injector to return tracing info to client
     * 2. request logging injector to log request and response info
     * 3. metrics to record application key metrics
     *
     * @param registry metrics registry
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(responseHeaderInjector);
        registry.addInterceptor(requestLoggingInjector);
        registry.addInterceptor(responseMetricsInjector);

    }

    /**
     * 1. allows for date format as yyyy-MM-dd
     * 2. Does not fail on unknown properties
     * 3. maps to camelCase
     * 4. Does not include null values or empty values
     * 5. does not write datas as timestamps.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.setDateFormat(new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN, Locale.getDefault()));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    /**
     * Create a logging interceptor that logs request/response for rest template calls.
     *
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(createClientFactory()));

        final MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper());
        restTemplate.getMessageConverters().add(0, messageConverter);

        restTemplate.getInterceptors().add(loggingInterceptor());

        return restTemplate;
    }

    /**
     * Add request and response info to and from a rest template call.
     *
     * @return ClientHttpRequestInterceptor
     */
    @Bean
    @SuppressWarnings("PMD.GuardLogStatement")
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {

            final StringBuilder logMessage = new StringBuilder(1024);

            logMessage.append("Request URI: ").append(request.getURI())
                .append("\nRequest Method: ").append(request.getMethod())
                .append("\nRequest Body: ").append(new String(body, Charset.defaultCharset())).append('\n');

            logger.info(LOG, logMessage.toString());

            final var response = execution.execute(request, body);

            logMessage.setLength(0); // Clear the StringBuilder for reuse
            logMessage.append("Response Status Code: ").append(response.getStatusCode())
                .append("\nResponse Body: ").append(new String(response.getBody().readAllBytes(), Charset.defaultCharset())).append('\n');

            logger.info(LOG, logMessage.toString());

            return response;
        };
    }

    private SimpleClientHttpRequestFactory createClientFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }

}
