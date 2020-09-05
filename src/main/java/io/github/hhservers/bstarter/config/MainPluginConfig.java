package io.github.hhservers.bstarter.config;

import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable @Data
public class MainPluginConfig {
    @Setting(value = "nodeOneString")
    private String nodeOneString = "nodeOneString";

    @Setting(value = "nodeTwo")
    private MainPluginConfig.NodeTwo nodeTwo = new NodeTwo();

    @ConfigSerializable @Data
    public static class NodeTwo {
        @Setting(value = "nodeTwoString")
        private String nodeTwoString = "nodeTwoString";
        @Setting(value = "nodeTwoInt", comment = "nodeTwoInt")
        private Integer nodeTwoInt = 2;
        @Setting(value = "nodeTwoList",comment = "nodeTwoList")
        private List<String> nodeTwoList = Arrays.asList("nodeTwoList1", "nodeTwoList2");
    }

}
