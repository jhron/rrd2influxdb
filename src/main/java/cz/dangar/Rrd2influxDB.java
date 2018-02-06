package cz.dangar;

import org.apache.commons.cli.*;
import org.rrd4j.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Rrd2influxDB {

    private static String inputFilePath;
    private static String outputFilePath;
    private static String measurementName;

    public static void main( String[] args )
    {
        System.setProperty("java.awt.headless","true");
        if ( !parseParameters( args ) ) {
            System.exit(1);
            return;
        }

        try {
            RrdDb rrdDb = new RrdDb( inputFilePath );
            HashMap<Long, Double> measurements = new HashMap<Long, Double>();
            for ( int i=0; i<rrdDb.getDsCount(); i++ ) {
                Datasource ds = rrdDb.getDatasource(i);
                for ( int j = 0; j < rrdDb.getArcCount(); j++ ) {
                    Archive a = rrdDb.getArchive(j);
                    Long st = a.getStartTime();
                    Robin r = a.getRobin(i);
                    for ( int k = 0; k < r.getSize(); k++ ) {
                        double val = r.getValue(k);
                        if (!Double.isNaN(val)) {
                            measurements.put(st, val);
                        }
                        st += 60 * a.getSteps();
                    }
                }
            }
            if ( measurements.size() > 0 ) {
                File f = new File(outputFilePath);
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                TreeMap<Long, Double> sortedMeasurement = new TreeMap<Long, Double>(measurements);
                for (Map.Entry<Long, Double> entry : sortedMeasurement.entrySet()) {
                    writer.write(measurementName + " value=" + entry.getValue() + " " + entry.getKey() + "\n");
                }
                writer.close();
            }
            else {
                System.out.println("Input file contains no data.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean parseParameters(String[] args) {
        Options options = new Options();

        Option inputF = new Option( "i", "input", true, "Input rrd4j file" );
        inputF.setRequired( true );
        options.addOption( inputF );

        Option outputF = new Option( "o", "output", true, "Output InfluxDB line protocol file");
        outputF.setRequired( true );
        options.addOption( outputF );

        Option mName = new Option( "m", "measurement", true, "InfluxDB measurement name. When not set, input file name without extension will be used.");
        mName.setRequired( false );
        options.addOption( mName );

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse( options, args );
        } catch ( ParseException e ) {
            System.out.println( e.getMessage() );
            formatter.printHelp( "rrd2influxdb", options );
            return false;
        }

        File inputFile = new File(cmd.getOptionValue("input"));
        if ( !inputFile.isFile() || !inputFile.exists() ) {
            System.out.println( String.format( "Input file '%s' does't exist.", inputFile.getPath() ) );
            System.exit(1);
            return false;
        }
        else {
            inputFilePath = inputFile.getPath();
        }
        measurementName = cmd.getOptionValue("measurement", inputFile.getName().replace(".rrd", "") );
        outputFilePath = cmd.getOptionValue("output");
        File outputFile = new File(outputFilePath);
        if ( outputFile.exists() ) {
            System.out.println( String.format( "Output file '%s' already exists. Can't continue.", outputFile.getPath() ) );
            System.exit(1);
            return false;
        }
        return true;
    }
}
