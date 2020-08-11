package com.wire.backups.exports.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.backups.exports.model.ExportConfig;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;

public abstract class CommandBase extends ConfiguredCommand<ExportConfig> {

    public CommandBase(String name, String description) {
        super(name, description);
    }

    protected Client getClient(Bootstrap<ExportConfig> bootstrap, ExportConfig configuration) {
        final Environment environment = new Environment(getName(),
                new ObjectMapper(),
                bootstrap.getValidatorFactory().getValidator(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader());

        return new JerseyClientBuilder(environment)
                .using(configuration.jerseyClient)
                .withProvider(MultiPartFeature.class)
                .withProvider(JacksonJsonProvider.class)
                .build(getName());
    }
}
