package pe.kr.thekey78.messenger.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * VO 필드에 대한 기본값 설정. byte, short, int, long, float, double, boolean, BigInteger, BigDecimal 및 Wrapper Class에 대해 설정이 가능하다.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME) // 컴파일 이후에도 JVM에 의해서 참조가 가능합니다.
@Target({
        ElementType.FIELD // 필 선언시
})
public @interface DefaultValue {
    String value() default "";
}
