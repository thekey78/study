package pe.kr.thekey78.messenger.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME) // 컴파일 이후에도 JVM에 의해서 참조가 가능합니다.
@Target({
        ElementType.FIELD // 필 선언시
})
public @interface CharEncoding {
    String value() default "UTF-8";
}
