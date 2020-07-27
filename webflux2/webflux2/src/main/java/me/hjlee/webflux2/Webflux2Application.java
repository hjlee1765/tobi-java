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
		log.info("pops1");
		String msg = generateHello();
		Mono<String> m = Mono.just(msg).doOnNext(c->log.info(c)).log();
		m.block();
		return m;
	}
	private String generateHello() {
		log.info("method generateHello()");
		return "Helo Mono";
	}

	public static void main(String[] args) {
		SpringApplication.run(Webflux2Application.class, args);
	}

}
