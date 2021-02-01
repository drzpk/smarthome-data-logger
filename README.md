# Smart Home Data Logger

This program collects data and sends them to the Smart Home server.

## Configuration

Configuration is defined in the file `config.properties` that must exist in the same directory as the JAR.

### Common configuration

```properties
# Directory where logs will be saved
log_directory=/tmp/data-logger-logs

# Address of the PV-Stats instance
pvstats.url=https://pv-ctl.domain.com

# Connection timeout to pv-stats server (seconds)
pvstats.timeout=3
```

### Interter configuration

Properties described in this section are required for each inverter type. `Example` is the name of the source, that
is arbitrary but must be unique.

```properties
# Interver type
source.example.type=SOFAR

# Data source credentials (defined in pv-stats)
source.example.user=viewer_sofar__example
source.example.password=3ddAsoQDjkf3LjuRsaOSLC

# Data source connection timeout (seconds)
source.example.timeout=3

# Interval between current/"light" stats (such as power, voltage, in seconds)
source.example.metrics_interval=15

# Interval between "heavy" measurements (containing all historical data from certain period, in seconds)
# (only supported by SMA inverters)
source.example.measurement_interval=60
```

#### SMA inverter configuration

```properties
source.example.type=SMA

# SNA inverter url
source.example.url=https://192.168.1.100:80

# Common configuration goes here...
```

#### Sofar inverter configuration (WiFi)

This configuration allows connecting with an inverter using a wierless dongle.

```properties
source.example.type=SOFAR_WIFI

# Sofar inverter URL (port should be 8899)
source.example.url=192.168.1.100:8899
# Inverter WiFi dongle's serial number (visible on the dongle itself or its box)
source.example.sn=2952164280

# Common configuration goes here...
```

#### Sofar inverter configuration (Modbus)

This configuration allows connecting with an inverter using a Modbus protocol over RS485.
A RS485 communication device is required to be installed in the client device.

```properties
source.example.type=SOFAR_MODBUS

# RS485 device path (COM1, COM2 etc. on Windows)
source.example.devpath=/dev/ttyUSB0
# Inverter slave ID (usually 1)
source.example.slaveId=1

# Common configuration goes here...
```

If using this configuration on a linux machine, make sure that the user you start the data-logger with has
required permissions to the serial device, otherwise you will end up with *permission denied* exceptions.
Usually addding a user to the `dialout` group should solve this issue:
```
sudo usermod -a -G dialout <user running the data-logger>
```
