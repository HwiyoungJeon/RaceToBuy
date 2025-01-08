package com.jh.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    @Autowired
    private List<RouteLocator> routeLocators;

    @EventListener(ApplicationReadyEvent.class)
    public void printRoutes() {
        routeLocators.forEach(routeLocator -> {
            routeLocator.getRoutes()
                .subscribe(route -> {
                    System.out.println("Route ID: " + route.getId());
                    System.out.println("Predicates: " + route.getPredicate());
                    System.out.println("Filters: " + route.getFilters());
                    System.out.println("URI: " + route.getUri());
                    System.out.println("--------------------");
                });
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
