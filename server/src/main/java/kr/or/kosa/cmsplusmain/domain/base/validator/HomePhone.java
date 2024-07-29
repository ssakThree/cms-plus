package kr.or.kosa.cmsplusmain.domain.base.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = HomePhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HomePhone {
	String message() default "잘못된 유선전화 형식입니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
