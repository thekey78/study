package pe.kr.thekey78.messenger.transfer.transport;

import lombok.NonNull;
import pe.kr.thekey78.messenger.MessageException;
import pe.kr.thekey78.messenger.transfer.DataType;
import pe.kr.thekey78.messenger.transfer.transport.external.HostInfo;
import pe.kr.thekey78.messenger.transfer.transport.external.RoundRobinRule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class FailOverURLConnection implements FailOverTransfer {
    private boolean failOver;
    private boolean roundRobin;

    private List<HostInfo> hosts;

    private RoundRobinRule roundRobinRule;
    private DataType dataType;


    public FailOverURLConnection(@NonNull boolean failOver, @NonNull boolean roundRobin, @NonNull List<HostInfo> hosts, @NonNull RoundRobinRule roundRobinRule) {
        this.failOver = failOver;
        this.roundRobin = roundRobin;
        this.hosts = hosts;
        this.roundRobinRule = roundRobinRule;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getContentType() {
        switch (dataType) {
            case JSON:
                return "application/json";
            case XML:
                return "application/json";
            case YML:
                return "";
            default:
                return "";
        }
    }

    @Override
    public byte[] sendAndReceive(byte[] bytes) throws IOException {
        byte[] result = null;
        HostInfo hostInfo = getHostInfo();
        HttpClient httpClient = null;
        try {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(hostInfo.getConnectionTimeout()))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(hostInfo.getHost()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(hostInfo.getReadTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int statusCode = response.statusCode();
            if(statusCode != 400) {
                throw new MessageException("Http "+statusCode+" Error");
            }

            result = response.body();
        } catch (URISyntaxException | InterruptedException e) {
            throw new MessageException(e);
        }
        return result;
    }

    private HostInfo getHostInfo() {
        int index = roundRobinRule.next();
        return hosts.get(index);
    }
}
