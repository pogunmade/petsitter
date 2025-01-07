package com.example.petsitter.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_PROBLEM_JSON;

@OpenAPIDefinition(
    info = @Info(
        title = "Pet Sitter API",
        version = "0.1",
        description = """
			Demo Spring Boot implementation of the Pet Sitter API from <i>"Designing APIs with Swagger and OpenAPI"
			</i> Ponelat, J.S., Rosenstock, L.L. (2022).
			
			We assume the domain model and a slightly modified version of the OpenAPI contract described at the end of
			Part 2. The focus of this implementation is software design. In particular how to use concepts from Domain
			Driven Design to connect the domain model and implementation.
			
			Note, this application is intended for demonstration purposes. It is not a production ready application,
			nor is it an enterprise grade application. The implementation reflects this.
			
			<b>Disclaimer:</b> there is no association with the book or its authors. Any errors are entirely
			self-contained."""
    ),
    tags = {
        @Tag(name = "Users", description = "User related operations"),
        @Tag(name = "Jobs", description = "Job related operations")
    },
    security = @SecurityRequirement(name = "SessionToken")
)
@SecurityScheme(name = "SessionToken",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "Authorization"
)
@Configuration
@RequiredArgsConstructor
class OpenApiConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public OpenApiCustomizer openApiCustomizer() throws JsonProcessingException {

        var problemDetail = """
           {
             "type": "A URI reference that identifies the problem type",
             "title": "A short, human-readable summary of the problem type",
             "status": "HTTP status code",
             "detail": "A human-readable explanation specific to this occurrence of the problem",
             "instance": "A URI reference that identifies the specific occurrence of the problem",
             "timestamp": "The time at which the problem occurred",
             "objectName": "A human-readable explanation of a problem object specific to this occurrence of the problem",
             "objectName.fieldName": "A human-readable explanation of a problem object and associated field specific to this occurrence of the problem"
           }""";

        var problemDetailExample = new Example();

        problemDetailExample.setValue(objectMapper.readTree(problemDetail));

        return openApi -> {

            openApi.getComponents().addExamples("problemDetail", problemDetailExample);

            // declutter swagger ui a bit
            openApi.getPaths().values().stream()
                .map(PathItem::readOperations)
                .flatMap(List::stream)
                .map(Operation::getResponses)
                .forEach(apiResponses -> {

                    apiResponses.keySet().stream()
                        .filter(key -> !key.startsWith("2"))
                        .sorted()
                        .skip(1)
                        .map(key -> apiResponses.get(key).getContent().get(MEDIA_TYPE_APPLICATION_PROBLEM_JSON))
                        .forEach(mediaType ->
                            {
                                mediaType.getExamples().clear();
                                mediaType.setExample("see Problem Detail");
                            }
                        );
                });
        };
    }
}
