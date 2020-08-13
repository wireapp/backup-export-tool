package com.wire.backups.exports.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import com.wire.backups.exports.model.ExportConfig;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;

public class OpenBackupCommand extends ConfiguredCommand<ExportConfig> {

    private final ExporterProducer exporterProducer;

    public OpenBackupCommand(String name, String description, ExporterProducer exporterProducer) {
        super(name, description);
        this.exporterProducer = exporterProducer;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-e", "--email")
                .dest("email")
                .type(String.class)
                .required(true)
                .help("Email address");

        subparser.addArgument("-p", "--password")
                .dest("password")
                .type(String.class)
                .required(true)
                .help("Password");

        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Backup file");

        subparser.addArgument("-out", "--output")
                .dest("out")
                .type(String.class)
                .required(false)
                .help("Output directory");
    }

    @Override
    public void run(Bootstrap<ExportConfig> bootstrap, Namespace namespace, ExportConfig configuration) throws Exception {
        final Exporter exporter = exporterProducer.build(parseExportConfiguration(namespace), getClient(bootstrap, configuration));
        exporter.run();
    }

    protected ExportConfiguration parseExportConfiguration(Namespace namespace) {
        final ExportConfiguration conf = new ExportConfiguration();
        conf.setEmail(namespace.getString("email"));
        conf.setPassword(namespace.getString("password"));
        conf.setIn(namespace.getString("in"));

        String out = namespace.getString("out");
        if (out != null && out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        conf.setOut(out);
        return conf;
    }

    private Client getClient(Bootstrap<ExportConfig> bootstrap, ExportConfig configuration) {
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
