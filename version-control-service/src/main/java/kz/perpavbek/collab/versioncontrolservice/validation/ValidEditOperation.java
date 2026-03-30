package kz.perpavbek.collab.versioncontrolservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EditOperationValidator.class)
@Documented
public @interface ValidEditOperation {

    String message() default "Invalid edit operation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
