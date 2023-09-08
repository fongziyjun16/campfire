package proj.fzy.campfire.service.common.utils;

import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;
import proj.fzy.campfire.service.common.config.properties.MachineIdProperties;

@Component
public class DbIdUtils {

    private final MachineIdProperties machineIdProperties;

    public DbIdUtils(MachineIdProperties machineIdProperties) {
        this.machineIdProperties = machineIdProperties;
    }

    public Long getNextId() {
        return IdUtil.getSnowflake(machineIdProperties.getWorkerId(), machineIdProperties.getDatacenterId()).nextId();
    }
}
