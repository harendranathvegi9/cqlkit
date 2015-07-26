package io.tenmax.cqlkit;

import com.datastax.driver.core.*;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CQLMapper maps the Cassandra ROW to CQL statement.
 */
public class CQLMapper extends AbstractMapper {
    private ColumnDefinitions.Definition[] definitions;
    private String template;
    private Pattern pattern = null;

    public CQLMapper() {
        pattern = Pattern.compile("\\?");
    }


    @Override
    protected void prepareOptions(Options options) {
        super.prepareOptions(options);

        options.addOption( "T", "template", true, "The template of CQL statements. The format is " +
                "the same as PreparedStatement." );
    }

    @Override
    protected void printVersion() {
        System.out.println("cql2cql version " + Consts.VERSION);
        System.exit(0);
    }

    @Override
    protected  void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String cmdLineSyntax =
                "cql2cql [-c contactpoint] [-q query] [-T template] [FILE]";
        String header = "File       The file to use as CQL query. If both FILE and QUERY are \n" +
                "           omitted, query will be read from STDIN.\n\n";
        formatter.printHelp(cmdLineSyntax, header, options, null);

        System.exit(0);
    }

    @Override
    protected void head(ColumnDefinitions columnDefinitions, PrintStream out) {
        template = commandLine.getOptionValue("T");
        definitions = columnDefinitions.asList().toArray(new ColumnDefinitions.Definition[]{});
    }

    @Override
    protected String map(Row row) {

        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < definitions.length; i++) {
            Object value = row.getObject(i);
            String key = definitions[i].getName();
            DataType type = definitions[i].getType();

            matcher.find();
            matcher.appendReplacement(result, type.format(value));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}