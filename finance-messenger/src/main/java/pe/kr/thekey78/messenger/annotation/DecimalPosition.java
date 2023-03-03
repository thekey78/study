package pe.kr.thekey78.messenger.annotation;

import pe.kr.thekey78.messenger.enumeration.Align;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME) // 컴파일 이후에도 JVM에 의해서 참조가 가능합니다.
@Target(value = {
        ElementType.FIELD // 필 선언시
})
public @interface DecimalPosition {
    /**
     * 정렬 구분. 기본값 우측정렬
     * @return
     */
    public Align type() default Align.RIGHT;

    /**
     * 소숫접이 들어갈 위치
     * @return
     */
    public int position();
}
