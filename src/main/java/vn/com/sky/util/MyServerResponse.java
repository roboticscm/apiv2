package vn.com.sky.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class MyServerResponse implements ServerResponse {
    public String body;

    public MyServerResponse(String body) {
        this.body = body;
    }

    @Override
    public HttpStatus statusCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int rawStatusCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public HttpHeaders headers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultiValueMap<String, ResponseCookie> cookies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Void> writeTo(ServerWebExchange exchange, Context context) {
        // TODO Auto-generated method stub
        return null;
    }
}
