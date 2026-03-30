package kz.perpavbek.collab.versioncontrolservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;

public class EditOperationValidator
        implements ConstraintValidator<ValidEditOperation, EditOperationRequest> {

    @Override
    public boolean isValid(EditOperationRequest value,
                           ConstraintValidatorContext context) {
        if (value == null || value.getType() == null) {
            return true;
        }

        switch (value.getType()) {

            case INSERT:
                return value.getContent() != null && !value.getContent().isBlank();

            case DELETE:
                return value.getLength() != null && value.getLength() > 0;

            case REPLACE:
                return value.getContent() != null &&
                        !value.getContent().isBlank() &&
                        value.getLength() != null &&
                        value.getLength() > 0;

            default:
                return false;
        }
    }
}
