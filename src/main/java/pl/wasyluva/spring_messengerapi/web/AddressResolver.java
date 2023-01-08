package pl.wasyluva.spring_messengerapi.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class AddressResolver {
    @Bean(name = "localHostProtocol")
    public String localHostProtocol() {
        return "http";
    }

    @Bean(name = "localHostAddress")
    public String localHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Value("${server.port}")
    private int serverPort;

    @Bean(name = "localHostPort")
    public String localHostPort(){
        return serverPort + "";
    }

    @Bean(name = "localHostUrl")
    public String localHostUrl() throws UnknownHostException {
        return localHostProtocol() + "://" + localHostAddress() + ":" + localHostPort();
    }

    @Bean(name = "frontHostPort")
    public String frontHostPort() {
        return "3000";
    }
}
