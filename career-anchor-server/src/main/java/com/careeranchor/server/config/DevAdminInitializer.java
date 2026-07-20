package com.careeranchor.server.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.entity.AdminAccount;
import com.careeranchor.server.mapper.AdminAccountMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevAdminInitializer implements ApplicationRunner {
    private final AdminAccountMapper mapper;
    private final PasswordEncoder encoder;
    private final AppProperties properties;

    public DevAdminInitializer(AdminAccountMapper mapper, PasswordEncoder encoder, AppProperties properties) {
        this.mapper = mapper;
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        String password = properties.admin().bootstrapPassword();
        if (password == null || password.isBlank() || mapper.selectCount(Wrappers.emptyWrapper()) > 0) return;
        AdminAccount account = new AdminAccount();
        account.setUsername("admin");
        account.setPasswordHash(encoder.encode(password));
        mapper.insert(account);
    }
}
