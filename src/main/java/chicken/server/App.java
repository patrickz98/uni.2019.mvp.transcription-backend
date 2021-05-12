package chicken.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main App that starts the spring boot service.
 */
@SpringBootApplication
public class App extends SpringBootServletInitializer
{
    /**
     * Starts the Application
     */
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(App.class);
    }
}
