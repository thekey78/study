package pe.kr.thekey78.transfer.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder()
public class HostInfo {
    /**
     * Host ip
     */
    private String host;

    /**
     * Host Port
     */
    private int port;

    /**
     * Read Timeout
     */
    private int readTimeout;

    /**
     * Connection Timeout
     */
    private int connectionTimeout;

    /**
     * 연결 유지 여부. 기본값 false
     */
    private boolean permanent = false;

    /**
     * 비동기 여부. 기본값 false
     */
    private boolean async = false;


    private TransgerType transgerType;
}
