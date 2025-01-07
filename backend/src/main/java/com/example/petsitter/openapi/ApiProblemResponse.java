package com.example.petsitter.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.ProblemDetail;

import java.lang.annotation.*;

import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_PROBLEM_JSON;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse
@Repeatable(ApiProblemResponses.class)
public @interface ApiProblemResponse {

    @AliasFor(
        annotation = ApiResponse.class
    )
    String responseCode() default "";

    @AliasFor(
        annotation = ApiResponse.class
    )
    String description() default "";

    @AliasFor(
        annotation = ApiResponse.class
    )
    Content[] content() default {

        @Content(mediaType = MEDIA_TYPE_APPLICATION_PROBLEM_JSON,
            schema = @Schema(implementation = ProblemDetail.class), examples = {
            @ExampleObject(name = "Problem Detail", ref = "#/components/examples/problemDetail")
    })};
}
