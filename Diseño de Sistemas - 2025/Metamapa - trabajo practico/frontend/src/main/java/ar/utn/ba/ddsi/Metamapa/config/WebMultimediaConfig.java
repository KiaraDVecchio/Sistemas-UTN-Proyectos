package ar.utn.ba.ddsi.Metamapa.config;

/*import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// package ar.utn.ba.ddsi.Metamapa.config;
@Configuration
public class WebMultimediaConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
    registry.addResourceHandler("/media/**")
        .addResourceLocations("file:" + location)
        .setCachePeriod(3600);
  }
}
*/