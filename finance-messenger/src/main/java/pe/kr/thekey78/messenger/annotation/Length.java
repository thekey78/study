package pe.kr.thekey78.messenger.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.enumeration.Ascii;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME) // 컴파일 이후에도 JVM에 의해서 참조가 가능합니다.
@Target({
        ElementType.FIELD // 필 선언시
})
public @interface Length {
    /**
     * 전문길이
     * @return
     */
    int value() default 0;
    /**
     * 전문의 정렬방식
     * @return
     */
    Align align() default Align.LEFT;
    /**
     * Padding
     * @return
     */
    byte pad() default Ascii.SPACE;

    /**
     * 참조 Field
     * @return
     */
    String ref() default "";
}
