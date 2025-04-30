package com.ska.telescopeSimulator.configuration;

import com.ska.telescopeSimulator.service.DeviceService;
import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.sky.SimulatedStar;
import com.ska.telescopeSimulator.web.WebsocketController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MainConfiguration {


    @Bean(name = "alphaCentauri")
    SimulatedStar alphaCentauri() { return new SimulatedStar(new DeviceCoordinates(20.0, 20.0, 20.0), 20.0, 5.0);}

    @Bean(name = "proximaCentauri")
    SimulatedStar proximaCentauri() { return new SimulatedStar(new DeviceCoordinates(80.0, 80.0, 80.0), 40.0, 8.0);}

    @Bean(name = "arcturus")
    SimulatedStar arcturus() { return new SimulatedStar(new DeviceCoordinates(-20.0, -10.0, 40.0), 30.0, 10.0);}

    @Bean
    SimulatedSky simulatedSky(List<SimulatedStar> stars){ return new SimulatedSky(stars);}

    @Bean
    DeviceService deviceService(SimulatedSky simulatedSky, WebsocketController websocketController) { return new DeviceService(simulatedSky, websocketController);}


}
