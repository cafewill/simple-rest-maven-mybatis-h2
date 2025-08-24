package com.cube.simple.init;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.cube.simple.model.Device;
import com.cube.simple.service.DeviceService;

import jakarta.annotation.PostConstruct;

@Profile("local")
@Component
public class DataInitializer {

	@Autowired
    private DataSource writeDataSource;

    @Autowired
    private DeviceService deviceService;
	
    @PostConstruct
    public void initialize() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        populator.execute(writeDataSource);

        deviceService.insert(Device.builder()
                .id("admin")
                .token("e0bonP1dTtmBiHfFPg8oHY:APA91bGQBW97r_A7OKTjV9oioPL9vK4yiQCC5b2vdq2ZS8TEbOIes0BE6_PS7TBpYU4EeEzW_TsUp6WDdErFpE4o5U57uvgwtOKL5Jpym6JaL-fwgxkcPS4")
                .build());
        deviceService.insert(Device.builder()
                .id("owner")
                .token("fT-Y7VYQRwG86djqTPwbhE:APA91bFZu6iiTYOggwXVwL-8ykHx1hsPksah3Qblrn6wUoEEB_z32T6SCI5raq29S02negwJxX-dfOdFlSKeB7Vt2n27rpncYH3uaCDMDBUIUMFx30Hr3gQ")
                .build());
        deviceService.insert(Device.builder()
                .id("user")
                .token("eCZ7SJP2TUOVz-l5aLglkj:APA91bGcVpakTwP7TtlUcJDQCatCLlTGHgjDD1YYxo8Sd4dGjOIzjdadFak-ZvKZAQWHmcveJRlswYtcqC3BUgxReWnQqXfVvRhtFB9v3deCiKAtTbdzFGw")
                .build());
    }
}
