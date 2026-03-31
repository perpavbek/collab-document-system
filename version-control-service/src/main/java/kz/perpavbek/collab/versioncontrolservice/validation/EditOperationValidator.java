package kz.perpavbek.collab.versioncontrolservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;

public class EditOperationValidator
        implements ConstraintValidator<ValidEditOperation, EditOperationRequest> {

    @Override
    public boolean isValid(EditOperationRequest value,
                           ConstraintValidatorContext context) {

        if (value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Request cannot be null")
                    .addConstraintViolation();
            return false;
        }

        if (value.getType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Type must be specified")
                    .addConstraintViolation();
            return false;
        }

        switch (value.getType()) {
            case INSERT:
                if (value.getContent() == null || value.getContent().isBlank()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("INSERT operation must have non-empty content")
                            .addConstraintViolation();
                    return false;
                }
                return true;

            case DELETE:
                if (value.getLength() == null || value.getLength() <= 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("DELETE operation must have length > 0")
                            .addConstraintViolation();
                    return false;
                }
                return true;

            case REPLACE:
                if (value.getContent() == null || value.getContent().isBlank()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("REPLACE operation must have non-empty content")
                            .addConstraintViolation();
                    return false;
                }
                if (value.getLength() == null || value.getLength() <= 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("REPLACE operation must have length > 0")
                            .addConstraintViolation();
                    return false;
                }
                return true;

            default:
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Unknown operation type")
                        .addConstraintViolation();
                return false;
        }
    }
}
