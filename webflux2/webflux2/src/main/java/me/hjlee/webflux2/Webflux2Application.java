package me.hjlee.webflux2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@Slf4j
public class Webflux2Application {

	@GetMapping("/")
	Mono<String> hello(){
		return Mono.just("Hello WebFlux").log();
	}

	public static void main(String[] args) {
		SpringApplication.run(Webflux2Application.class, args);
	}

}
