package org.openmrs.maven.plugins;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.ho.yaml.YamlDecoder;

import java.io.EOFException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Mojo(name = "help", requiresProject = false)
public class Help extends AbstractTask {

    private static final String HELP_FILE = "help.yaml";
    private static final String DESC_MESSAGE = "Description: ";
    private static final String INFO = "OpenMRS SDK %s";
    private static final String WIKI = "https://wiki.openmrs.org/display/docs/OpenMRS+SDK";

    public void executeTask() throws MojoExecutionException, MojoFailureException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(HELP_FILE);
        YamlDecoder dec = new YamlDecoder(stream);
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(4);
        formatter.setDescPadding(8);
        try {
            Map keys = (Map) dec.readObject();
            List l = (List) keys.get("help");
            String printVersion = String.format(INFO, keys.get("version"));
            String printWikiLink = "For more info, see SDK documentation: " + WIKI;
            PrintWriter writer = new PrintWriter(System.out);
            writer.println();
            writer.println();
            writer.println(printVersion);
            writer.println();
            writer.println(printWikiLink);
            writer.println();
            writer.println();
            writer.flush();
            if (l == null) {
                throw new MojoExecutionException("Error during reading help data");
            }
            for (Object o: l) {
                Map m = (Map) o;
                Options options = new Options();
                List params = (List) m.get("options");
                String header = (m.get("desc") != null) ? m.get("desc").toString() : "None";
                header = DESC_MESSAGE.concat(header);
                if (params != null) {
                    for (Object x: params) {
                        Map option = (Map) x;
                        String name = option.get("name").toString();
                        String desc = option.get("desc").toString();
                        if ((name == null) || (desc == null)) throw new MojoExecutionException("Error in help file structure");
                        options.addOption(new Option(name, desc));
                    }
                }
                formatter.printHelp(m.get("name").toString(), header, options, "");
                writer.println();
                writer.flush();
            }
            writer.flush();
        } catch (EOFException e) {
            throw new MojoExecutionException(e.getMessage());
        } finally {
            dec.close();
        }
    }
}
