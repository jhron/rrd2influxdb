# rrd2influxdb
Tool for converting Openhab rrd4j data file to [InfluxDB line protocol](https://docs.influxdata.com/influxdb/v1.4/write_protocols/line_protocol_reference/) file so it can be easily imported by using [InfluxDB HTTP API](https://docs.influxdata.com/influxdb/v1.4/guides/writing_data/).

### Latest version
[Download](https://github.com/jhron/rrd2influxdb/releases/)

### Usage

```java -jar rrd2influxdb.jar -i input_rrd_file -o output_influxdb_line_protocol_file```

In case that output file already exists, program will not continue (no files will be overwriten).

### All supported parameters

```
 -i,--input <arg>         Input rrd4j file
 -m,--measurement <arg>   InfluxDB measurement name. When not set, input
                          file name without extension will be used.
 -o,--output <arg>        Output InfluxDB line protocol file
```
 
Parameter ```measurement``` is optional and when it is not specified, measurement name is taken from input rrd4j file without rrd extenstion.
 
### Example
 
```java -jar rrd2influxdb.jar -i Temperature_LR.rrd -o TempLivingRoom.txt```
 
It creates TempLivingRoom.txt with following content:
 
```
Temperature_LR value=22.950377508444447 1480550400
Temperature_LR value=22.321925532618533 1481155200
Temperature_LR value=23.084880952380615 1481760000
Temperature_LR value=22.709831349205146 1482364800
...
...
```
 
Now you can import it into InfluxDB using HTTP API using following parameter (modified according to yours actual settings)
 
```
curl -i -XPOST "http://influxdb_hostname:port/write?db=dbName&precision=s" -u username:password --data-binary @TempLivingRoom.txt
```
 
Please don't forget to use ```precision=s```.

### Which data is taken from rrd files

Program will take all available data from file with non ```NaN``` value. ```NaN``` values are ignored.



 
