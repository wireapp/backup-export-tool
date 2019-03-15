package com.wire.bots.recording;

import com.codahale.metrics.health.HealthCheck;
import com.wire.bots.sdk.tools.Logger;

import java.util.UUID;

public class UserHealthCheck extends HealthCheck {
    @Override
    protected Result check() {
        try {
            Logger.debug("Started UserHealthCheck");
            Helper.getProfile(UUID.fromString("73b4e21f-ef98-447d-80de-ae0b34e3feeb"));
            return Result.healthy();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.unhealthy(e.getMessage());
        } finally {
            Logger.debug("Finished UserHealthCheck");
        }
    }
}
