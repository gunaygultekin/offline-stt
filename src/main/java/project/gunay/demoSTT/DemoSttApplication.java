package project.gunay.demoSTT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class DemoSttApplication extends SpringBootServletInitializer {

    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder){
        return  applicationBuilder.sources(DemoSttApplication.class);
    }

    public static void main(String[] args) {
		SpringApplication.run(DemoSttApplication.class, args);
	}
}
