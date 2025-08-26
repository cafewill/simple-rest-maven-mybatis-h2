package com.cube.simple.init;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.cube.simple.model.Device;
import com.cube.simple.model.admin.Place;
import com.cube.simple.service.DeviceService;
import com.cube.simple.service.admin.PlaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Profile({"local", "develop"})
@Component
public class DataInitializer {

	@Autowired
    private DataSource writeDataSource;

    @Autowired
    private DeviceService deviceService;
	
    @Autowired
    private PlaceService regionService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() throws JsonMappingException, JsonProcessingException {
    	
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        populator.execute(writeDataSource);
        
        deviceService.insert(Device.builder()
        		.id("user")
        		.token("eCZ7SJP2TUOVz-l5aLglkj:APA91bGcVpakTwP7TtlUcJDQCatCLlTGHgjDD1YYxo8Sd4dGjOIzjdadFak-ZvKZAQWHmcveJRlswYtcqC3BUgxReWnQqXfVvRhtFB9v3deCiKAtTbdzFGw")
        		.build());
        deviceService.insert(Device.builder()
        		.id("owner")
        		.token("fT-Y7VYQRwG86djqTPwbhE:APA91bFZu6iiTYOggwXVwL-8ykHx1hsPksah3Qblrn6wUoEEB_z32T6SCI5raq29S02negwJxX-dfOdFlSKeB7Vt2n27rpncYH3uaCDMDBUIUMFx30Hr3gQ")
        		.build());
        deviceService.insert(Device.builder()
        		.id("admin")
        		.token("e0bonP1dTtmBiHfFPg8oHY:APA91bGQBW97r_A7OKTjV9oioPL9vK4yiQCC5b2vdq2ZS8TEbOIes0BE6_PS7TBpYU4EeEzW_TsUp6WDdErFpE4o5U57uvgwtOKL5Jpym6JaL-fwgxkcPS4")
        		.build());

        List<String> names = Arrays.asList(
                "{\"ko\": \"서울\", \"en\": \"Seoul\", \"cn\": \"首爾\"}",
                "{\"ko\": \"강릉\", \"en\": \"Gangneung\", \"cn\": \"江陵\"}",
                "{\"ko\": \"속초\", \"en\": \"Sokcho\", \"cn\": \"束草\"}",
                "{\"ko\": \"삼척\", \"en\": \"Samcheok\", \"cn\": \"三陟\"}",
                "{\"ko\": \"인천\", \"en\": \"Incheon\", \"cn\": \"仁川\"}",
                "{\"ko\": \"송도\", \"en\": \"Songdo\", \"cn\": \"松島\"}",
                "{\"ko\": \"제주\", \"en\": \"Jeju\", \"cn\": \"濟州\"}",
                "{\"ko\": \"부산\", \"en\": \"Busan\", \"cn\": \"釜山\"}",
                "{\"ko\": \"목포\", \"en\": \"Mokpo\", \"cn\": \"木浦\"}",
                "{\"ko\": \"여수\", \"en\": \"Yeosu\", \"cn\": \"麗水\"}",
                "{\"ko\": \"안동\", \"en\": \"Andong\", \"cn\": \"安東\"}",
                "{\"ko\": \"대전\", \"en\": \"Daejeon\", \"cn\": \"大田\"}",
                "{\"ko\": \"전주\", \"en\": \"Jeonju\", \"cn\": \"全州\"}",
                "{\"ko\": \"군산\", \"en\": \"Gunsan\", \"cn\": \"群山\"}",
                "{\"ko\": \"포항\", \"en\": \"Pohang\", \"cn\": \"浦項\"}"
        );
        
        List<String> descriptions = Arrays.asList(
        	    "{\"ko\":\"대한민국의 수도, 역사와 현대가 공존하는 도시.\",\"en\":\"Capital of Korea where history meets hi-tech.\",\"cn\":\"韩国首都，传统与尖端并存。\"}",
        	    "{\"ko\":\"동해안 바다와 커피거리로 유명한 도시.\",\"en\":\"East Sea beaches and a lively coffee scene.\",\"cn\":\"以东海岸海滩和咖啡文化闻名。\"}",
        	    "{\"ko\":\"설악산과 어시장이 가까운 항구도시.\",\"en\":\"Gateway to Seoraksan with a bustling fish market.\",\"cn\":\"通往雪岳山的港口城市，鱼市热闹。\"}",
        	    "{\"ko\":\"해안 동굴과 바다열차로 알려진 관광지.\",\"en\":\"Known for coastal caves and the sea train.\",\"cn\":\"以海岸洞窟和海上列车著称。\"}",
        	    "{\"ko\":\"국제공항이 있는 관문 도시, 차이나타운과 송도.\",\"en\":\"Gateway city with ICN airport, Chinatown, and Songdo.\",\"cn\":\"有国际机场的门户城市，含中华街与松岛。\"}",
        	    "{\"ko\":\"국제업무·스마트시티로 계획된 신도시.\",\"en\":\"A planned smart city and global business hub.\",\"cn\":\"规划的智慧新城与国际商务中心。\"}",
        	    "{\"ko\":\"화산섬·유네스코 자연유산, 한라산과 올레길.\",\"en\":\"Volcanic island with Hallasan and Olle trails.\",\"cn\":\"火山岛，汉拿山与偶来小路。\"}",
        	    "{\"ko\":\"대한민국 제2의 도시, 해운대와 영화제로 유명.\",\"en\":\"Korea’s second city, famed for Haeundae and BIFF.\",\"cn\":\"韩国第二大城市，海云台与电影节闻名。\"}",
        	    "{\"ko\":\"다도해 관문, 근대역사 문화거리가 매력적.\",\"en\":\"Gateway to the Dadohae with modern-history streets.\",\"cn\":\"多岛海门户，近代历史文化街迷人。\"}",
        	    "{\"ko\":\"밤바다 야경과 향일암이 유명한 도시.\",\"en\":\"Famous for night sea views and Hyangiram temple.\",\"cn\":\"以夜海景与向日庵闻名。\"}",
        	    "{\"ko\":\"하회마을과 전통문화의 고장.\",\"en\":\"Home to Hahoe Village and deep traditions.\",\"cn\":\"河回村与传统文化之乡。\"}",
        	    "{\"ko\":\"과학·연구 중심 도시, 엑스포 과학공원 보유.\",\"en\":\"Science and research hub with Expo Science Park.\",\"cn\":\"科学研究中心，拥有世博科学公园。\"}",
        	    "{\"ko\":\"한옥마을과 미식의 도시.\",\"en\":\"Hanok Village and a foodie haven.\",\"cn\":\"韩屋村与美食之城。\"}",
        	    "{\"ko\":\"근대항만 도시, 시간여행 마을이 인기.\",\"en\":\"Modern-era port with ‘time-travel’ streets.\",\"cn\":\"近代港口城市，时光旅行村受欢迎。\"}",
        	    "{\"ko\":\"포스코 제철, 호미곶과 영일대 해변으로 유명.\",\"en\":\"Steel city with Homigot and Yeongildae Beach.\",\"cn\":\"钢铁之城，虎尾岬与迎日台海滩著名。\"}"
        	);

        /*
        for (String name : names) {
            Map<String, String> nameMap = objectMapper.readValue(name, new TypeReference<>() {});
            Place region = Place.builder().name(nameMap).build();
            regionService.insert(region);
        }
        */
        for (int i = 0; i < names.size(); i++) {
            String nameJson = names.get(i);
            String descJson = descriptions.get(i);

            Map<String, String> nameMap = objectMapper.readValue(nameJson, new TypeReference<>() {});
            Map<String, String> descMap = objectMapper.readValue(descJson, new TypeReference<>() {});

            Place place = Place.builder()
                    .name(nameMap)
                    .description(descMap)
                    .build();

            regionService.insert(place);
        }        
    }
}
